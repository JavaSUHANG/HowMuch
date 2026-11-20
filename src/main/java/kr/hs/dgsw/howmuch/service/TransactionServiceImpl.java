package kr.hs.dgsw.howmuch.service;

import kr.hs.dgsw.howmuch.dto.TransactionCreateRequest;
import kr.hs.dgsw.howmuch.dto.TransactionSummaryResponseDTO;
import kr.hs.dgsw.howmuch.dto.TransactionUpdateRequestDto;
import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.entity.TransactionType;
import kr.hs.dgsw.howmuch.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    @Override
    public Transaction create(TransactionCreateRequest request) {
        Transaction transaction = new Transaction(
                request.amount(),
                request.transactionType(),
                request.category(),
                request.description(),
                request.transactionDate()
        );
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public List<Transaction> importTransactionsFromCsv(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV 파일이 비어 있습니다.");
        }

        List<String> headers = List.of(
                "amount", "transactionType", "category", "description", "transactionDate"
        );
        List<Transaction> transactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                file.getInputStream(), StandardCharsets.UTF_8.newDecoder()
        ))) {
            // Excel에서 저장한 UTF-8 CSV의 첫 글자에 붙을 수 있는 BOM을 제거합니다.
            reader.mark(1);
            if (reader.read() != '\uFEFF') {
                reader.reset();
            }

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setAllowMissingColumnNames(true)
                    .setIgnoreEmptyLines(true)
                    .get();

            try (CSVParser parser = format.parse(reader)) {
                if (!parser.getHeaderNames().equals(headers)) {
                    throw new IllegalArgumentException(
                            "CSV 헤더는 " + String.join(",", headers) + " 순서여야 합니다."
                    );
                }

                List<CSVRecord> records;
                try {
                    records = parser.getRecords();
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                }

                int rowNumber = 1;
                for (CSVRecord record : records) {
                    transactions.add(parseTransaction(record, rowNumber++));
                }
            }
        } catch (CSVException e) {
            throw new IllegalArgumentException("CSV 형식이 올바르지 않습니다.");
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException("CSV 파일은 UTF-8 인코딩이어야 합니다.");
        }

        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("CSV 파일에 등록할 데이터가 없습니다.");
        }
        return transactionRepository.saveAll(transactions);
    }

    private Transaction parseTransaction(CSVRecord record, int rowNumber) {
        String errorPrefix = rowNumber + "번째 데이터의 ";
        if (record.size() != 5) {
            throw new IllegalArgumentException(errorPrefix + "컬럼은 5개여야 합니다.");
        }

        int amount;
        try {
            amount = Integer.parseInt(record.get("amount").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(errorPrefix + "금액은 정수여야 합니다.");
        }

        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(record.get("transactionType").trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(errorPrefix + "입출금 유형은 INCOME 또는 EXPENSE여야 합니다.");
        }

        String category = record.get("category");
        if (category.isBlank()) {
            category = null;
        }
        if (category != null && category.length() > 50) {
            throw new IllegalArgumentException(errorPrefix + "카테고리는 50자 이하여야 합니다.");
        }

        String description = record.get("description");
        if (description.isBlank()) {
            description = null;
        }
        if (description != null && description.length() > 100) {
            throw new IllegalArgumentException(errorPrefix + "설명은 100자 이하여야 합니다.");
        }

        LocalDate transactionDate = null;
        String date = record.get("transactionDate").trim();
        if (!date.isEmpty()) {
            try {
                transactionDate = LocalDate.parse(date);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(errorPrefix + "날짜는 올바른 yyyy-MM-dd 형식이어야 합니다.");
            }
        }

        return new Transaction(amount, transactionType, category, description, transactionDate);
    }

    @Override
    public Transaction update(Long id, TransactionUpdateRequestDto request) {
        Transaction transaction = findById(id);
        if (transaction == null) {
            return null;
        }

        transaction.update(
                request.amount(),
                request.transactionType(),
                request.category(),
                request.description(),
                request.transactionDate()
        );
        return transactionRepository.save(transaction);
    }

    @Override
    public boolean delete(Long id) {
        Transaction transaction = findById(id);
        if (transaction == null) {
            return false;
        }

        transactionRepository.delete(transaction);
        return true;
    }

    @Override
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Override
    public List<Transaction> findByYearAndMonth(int year, int month) {
        YearMonth targetMonth = YearMonth.of(year, month);
        return transactionRepository.findByTransactionDateBetween(
                targetMonth.atDay(1),
                targetMonth.atEndOfMonth()
        );
    }

    @Override
    public Transaction findById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }

    @Override
    public TransactionSummaryResponseDTO getSummary() {
        long totalIncome = 0;
        long totalExpense = 0;
        for (Transaction transaction : transactionRepository.findAll()) {
            if (transaction.getTransactionType() == TransactionType.INCOME) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += transaction.getAmount();
            }
        }
        return new TransactionSummaryResponseDTO(totalIncome, totalExpense, totalIncome - totalExpense);
    }

    @Override
    public List<Transaction> findByPeriod(LocalDate start, LocalDate end) {
        return transactionRepository.findByTransactionDateBetween(start, end);
    }
}
