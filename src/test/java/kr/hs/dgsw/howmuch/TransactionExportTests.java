package kr.hs.dgsw.howmuch;

import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.entity.TransactionType;
import kr.hs.dgsw.howmuch.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionExportTests {

    @Autowired
    private TransactionRepository transactionRepository;

    private Transaction later;
    private Transaction earlierFirst;
    private Transaction earlierSecond;

    @BeforeEach
    void setUp() {
        later = transactionRepository.save(new Transaction(
                3000, TransactionType.EXPENSE, "교통", "버스", LocalDate.of(2026, 11, 2)
        ));
        earlierFirst = transactionRepository.save(new Transaction(
                5000, TransactionType.EXPENSE, "식비", "점심", LocalDate.of(2026, 11, 1)
        ));
        earlierSecond = transactionRepository.save(new Transaction(
                10000, TransactionType.INCOME, "용돈", "이번 주 용돈", LocalDate.of(2026, 11, 1)
        ));
    }

    @Test
    @DisplayName("날짜가 빠른 순서로 조회하고, 같은 날짜는 ID가 작은 순서로 조회")
    void findAllByOrderByTransactionDateAscIdAsc() {
        // when
        List<Transaction> result = transactionRepository.findAllByOrderByTransactionDateAscIdAsc();

        // then
        assertThat(result)
                .extracting(Transaction::getId)
                .containsExactly(earlierFirst.getId(), earlierSecond.getId(), later.getId());
    }

    @Test
    @DisplayName("날짜가 늦은 순서로 조회하고, 같은 날짜는 ID가 큰 순서로 조회")
    void findAllByOrderByTransactionDateDescIdDesc() {
        // when
        List<Transaction> result = transactionRepository.findAllByOrderByTransactionDateDescIdDesc();

        // then
        assertThat(result)
                .extracting(Transaction::getId)
                .containsExactly(later.getId(), earlierSecond.getId(), earlierFirst.getId());
    }
}
