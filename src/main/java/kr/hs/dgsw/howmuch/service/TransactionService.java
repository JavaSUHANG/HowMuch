package kr.hs.dgsw.howmuch.service;

import kr.hs.dgsw.howmuch.entity.Transaction;

import java.util.List;

public interface TransactionService {
    List<Transaction> findAll();
    Transaction findById(Long id);
}
