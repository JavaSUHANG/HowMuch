package kr.hs.dgsw.howmuch.service;

import kr.hs.dgsw.howmuch.dto.TransactionExportDto;
import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.entity.TransactionType;
import kr.hs.dgsw.howmuch.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionExportServiceImpl implements TransactionExportService {
    private final TransactionRepository transactionRepository;

    @Override
    public byte[] exportTransactionsToExcel(String order) throws IOException {
        List<Transaction> transactions;
        if ("desc".equals(order)) {
            transactions = transactionRepository.findAllByOrderByTransactionDateDescIdDesc();
        } else {
            transactions = transactionRepository.findAllByOrderByTransactionDateAscIdAsc();
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("입출금 내역");
            String[] headers = {"번호", "날짜", "구분", "카테고리", "금액", "설명"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(
                    workbook.createDataFormat().getFormat("yyyy-mm-dd")
            );

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(
                    workbook.createDataFormat().getFormat("#,##0")
            );

            int rowNumber = 1;
            for (Transaction transaction : transactions) {
                TransactionExportDto dto = TransactionExportDto.from(transaction);
                Row row = sheet.createRow(rowNumber++);

                // 식별자는 문자열로 저장해서 큰 번호의 정밀도 손실을 방지합니다.
                row.createCell(0).setCellValue(dto.getId().toString());

                Cell dateCell = row.createCell(1);
                if (dto.getTransactionDate() != null) {
                    dateCell.setCellValue(dto.getTransactionDate().atStartOfDay());
                    dateCell.setCellStyle(dateStyle);
                }

                row.createCell(2).setCellValue(
                        dto.getTransactionType() == TransactionType.INCOME
                                ? "입금" : "출금"
                );
                row.createCell(3).setCellValue(
                        dto.getCategory() == null ? "" : dto.getCategory()
                );

                Cell amountCell = row.createCell(4);
                amountCell.setCellValue(dto.getAmount());
                amountCell.setCellStyle(amountStyle);

                row.createCell(5).setCellValue(
                        dto.getDescription() == null ? "" : dto.getDescription()
                );
            }

            sheet.createFreezePane(0, 1);
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 18 * 256);
            }
            sheet.setColumnWidth(5, 40 * 256);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
