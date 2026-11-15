package kr.hs.dgsw.howmuch;

import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.entity.TransactionType;
import kr.hs.dgsw.howmuch.repository.TransactionRepository;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionExportTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void clearTransactions() {
        transactionRepository.deleteAll();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"asc", "desc"})
    void exportsHeaderOnlyWorkbookWhenThereAreNoTransactions(String order) throws Exception {
        byte[] file = downloadExcel(order);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            Sheet sheet = workbook.getSheet("입출금 내역");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);

            String[] headers = {"번호", "날짜", "구분", "카테고리", "금액", "설명"};
            for (int i = 0; i < headers.length; i++) {
                assertThat(sheet.getRow(0).getCell(i).getStringCellValue())
                        .isEqualTo(headers[i]);
            }
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"asc"})
    void exportsSortedTransactionsWithNumericAmountsAndDates(String order) throws Exception {
        LocalDate earlierDate = LocalDate.of(2026, 11, 1);
        LocalDate laterDate = LocalDate.of(2026, 11, 2);

        Transaction later = saveTransaction(
                3000, TransactionType.EXPENSE, "교통", "버스", laterDate
        );
        Transaction expense = saveTransaction(
                5000, TransactionType.EXPENSE, null, null, earlierDate
        );
        Transaction income = saveTransaction(
                10000, TransactionType.INCOME, "용돈", "=1+1", earlierDate
        );

        byte[] file = downloadExcel(order);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(4);

            Row expenseRow = sheet.getRow(1);
            assertThat(expenseRow.getCell(0).getStringCellValue())
                    .isEqualTo(expense.getId().toString());
            assertThat(expenseRow.getCell(1).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(DateUtil.isCellDateFormatted(expenseRow.getCell(1))).isTrue();
            assertThat(expenseRow.getCell(1).getLocalDateTimeCellValue().toLocalDate())
                    .isEqualTo(earlierDate);
            assertThat(expenseRow.getCell(2).getStringCellValue()).isEqualTo("출금");
            assertThat(expenseRow.getCell(3).getStringCellValue()).isEmpty();
            assertThat(expenseRow.getCell(4).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(expenseRow.getCell(4).getNumericCellValue()).isEqualTo(5000);
            assertThat(expenseRow.getCell(5).getStringCellValue()).isEmpty();

            Row incomeRow = sheet.getRow(2);
            assertThat(incomeRow.getCell(0).getStringCellValue())
                    .isEqualTo(income.getId().toString());
            assertThat(incomeRow.getCell(2).getStringCellValue()).isEqualTo("입금");
            assertThat(incomeRow.getCell(3).getStringCellValue()).isEqualTo("용돈");
            assertThat(incomeRow.getCell(4).getNumericCellValue()).isEqualTo(10000);
            assertThat(incomeRow.getCell(5).getCellType()).isEqualTo(CellType.STRING);
            assertThat(incomeRow.getCell(5).getStringCellValue()).isEqualTo("=1+1");

            Row laterRow = sheet.getRow(3);
            assertThat(laterRow.getCell(0).getStringCellValue())
                    .isEqualTo(later.getId().toString());
            assertThat(laterRow.getCell(1).getLocalDateTimeCellValue().toLocalDate())
                    .isEqualTo(laterDate);
            assertThat(laterRow.getCell(4).getNumericCellValue()).isEqualTo(3000);
            assertThat(laterRow.getCell(5).getStringCellValue()).isEqualTo("버스");
        }
    }

    @Test
    void exportsTransactionsByDateAndIdDescending() throws Exception {
        LocalDate earlierDate = LocalDate.of(2026, 11, 1);
        LocalDate laterDate = LocalDate.of(2026, 11, 2);

        Transaction laterFirst = saveTransaction(
                3000, TransactionType.EXPENSE, "교통", "버스", laterDate
        );
        Transaction earlier = saveTransaction(
                5000, TransactionType.EXPENSE, "식비", "점심", earlierDate
        );
        Transaction laterSecond = saveTransaction(
                10000, TransactionType.INCOME, "용돈", "테스트", laterDate
        );

        byte[] file = downloadExcel("desc");

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(4);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue())
                    .isEqualTo(laterSecond.getId().toString());
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue())
                    .isEqualTo(laterFirst.getId().toString());
            assertThat(sheet.getRow(3).getCell(0).getStringCellValue())
                    .isEqualTo(earlier.getId().toString());
            assertThat(sheet.getRow(1).getCell(1).getLocalDateTimeCellValue().toLocalDate())
                    .isEqualTo(laterDate);
            assertThat(sheet.getRow(3).getCell(1).getLocalDateTimeCellValue().toLocalDate())
                    .isEqualTo(earlierDate);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "ASC", "DESC", "", " ", "asc,desc", " desc"})
    void rejectsInvalidOrder(String order) throws Exception {
        mockMvc.perform(get("/api/transactions/export").param("order", order))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.res").value("fail"))
                .andExpect(jsonPath("$.message").value("정렬 순서는 asc 또는 desc만 가능합니다."))
                .andExpect(header().doesNotExist(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    void existingTransactionQueriesStillReturnJson() throws Exception {
        Transaction transaction = saveTransaction(
                1000, TransactionType.INCOME, "용돈", "테스트", LocalDate.of(2026, 11, 1)
        );

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value("success"))
                .andExpect(jsonPath("$.data.length()").value(1));

        mockMvc.perform(get("/api/transactions/{id}", transaction.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(transaction.getId().intValue()));

        mockMvc.perform(get("/api/transactions/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.res").value("fail"));
    }

    private byte[] downloadExcel(String order) throws Exception {
        MockHttpServletRequestBuilder request = get("/api/transactions/export");
        if (order != null) {
            request.param("order", order);
        }
        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"transactions.xlsx\""
                ))
                .andReturn().getResponse().getContentAsByteArray();
    }

    private Transaction saveTransaction(int amount, TransactionType type,
                                        String category, String description,
                                        LocalDate date) {
        Transaction transaction = new Transaction(amount, type, category, description);
        ReflectionTestUtils.setField(transaction, "transactionDate", date);
        return transactionRepository.saveAndFlush(transaction);
    }
}
