package kr.hs.dgsw.howmuch.service;

import kr.hs.dgsw.howmuch.dto.TransactionCreateRequest;
import kr.hs.dgsw.howmuch.entity.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction create(TransactionCreateRequest request);
    List<Transaction> findAll();
    Transaction findById(Long id);
}
