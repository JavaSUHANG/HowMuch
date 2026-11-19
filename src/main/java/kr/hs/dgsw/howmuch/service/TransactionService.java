package kr.hs.dgsw.howmuch.service;

import kr.hs.dgsw.howmuch.dto.TransactionCreateRequest;
import kr.hs.dgsw.howmuch.dto.TransactionUpdateRequestDto;
import kr.hs.dgsw.howmuch.entity.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction create(TransactionCreateRequest request);
    Transaction update(Long id, TransactionUpdateRequestDto request);
    boolean delete(Long id);
    List<Transaction> findAll();
    List<Transaction> findByYearAndMonth(int year, int month);
    Transaction findById(Long id);
}
