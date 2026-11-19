package kr.hs.dgsw.howmuch.service;

import kr.hs.dgsw.howmuch.dto.TransactionCreateRequest;
import kr.hs.dgsw.howmuch.dto.TransactionSummaryResponse;
import kr.hs.dgsw.howmuch.dto.TransactionUpdateRequestDto;
import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.entity.TransactionType;
import kr.hs.dgsw.howmuch.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
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
    public TransactionSummaryResponse getSummary() {
        long totalIncome = 0;
        long totalExpense = 0;
        for (Transaction transaction : transactionRepository.findAll()) {
            if (transaction.getTransactionType() == TransactionType.INCOME) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += transaction.getAmount();
            }
        }
        return new TransactionSummaryResponse(totalIncome, totalExpense, totalIncome - totalExpense);
    }
}
