package kr.hs.dgsw.howmuch.service;

import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.entity.TransactionType;
import kr.hs.dgsw.howmuch.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TransactionServiceTest {
    @Autowired
    private TransactionService service;

    @Autowired
    private TransactionRepository repository;

    @Test
    void findsOnlyLeapFebruaryTransactions() {
        LocalDate first = LocalDate.of(2024, 2, 1);
        LocalDate last = LocalDate.of(2024, 2, 29);
        for (LocalDate date : List.of(first.minusDays(1), first, last, last.plusDays(1))) {
            repository.save(new Transaction(1000, TransactionType.INCOME, null, null, date));
        }

        assertThat(service.findByYearAndMonth(2024, 2))
                .extracting(Transaction::getTransactionDate)
                .containsExactlyInAnyOrder(first, last);
    }
}
