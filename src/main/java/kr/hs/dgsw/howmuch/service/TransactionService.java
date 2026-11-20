package kr.hs.dgsw.howmuch.service;

import kr.hs.dgsw.howmuch.dto.TransactionCreateRequest;
import kr.hs.dgsw.howmuch.dto.TransactionSummaryResponseDTO;
import kr.hs.dgsw.howmuch.dto.TransactionUpdateRequestDto;
import kr.hs.dgsw.howmuch.entity.Transaction;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    Transaction create(TransactionCreateRequest request);
    List<Transaction> importTransactionsFromCsv(MultipartFile file) throws IOException;
    Transaction update(Long id, TransactionUpdateRequestDto request);
    boolean delete(Long id);
    List<Transaction> findAll();
    List<Transaction> findByYearAndMonth(int year, int month);
    Transaction findById(Long id);
    List<Transaction> findByPeriod(LocalDate start, LocalDate end);
    TransactionSummaryResponseDTO getSummary();
}
