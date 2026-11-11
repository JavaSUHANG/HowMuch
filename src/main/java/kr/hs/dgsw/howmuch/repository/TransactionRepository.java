package kr.hs.dgsw.howmuch.repository;

import kr.hs.dgsw.howmuch.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
