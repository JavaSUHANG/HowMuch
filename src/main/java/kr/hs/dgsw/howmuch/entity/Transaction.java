package kr.hs.dgsw.howmuch.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 금액 */
    @Column(nullable = false)
    private int amount;

    /** INCOME(입력) | EXPENSE(출금) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    /** 카테고리 구분 */
    @Column(length = 50)
    private String category;

    /** 설명 */
    @Column(length = 100)
    private String description;

    /** 입출금 발생 날짜 */
    private LocalDate transactionDate;

    @PrePersist
    public void prePersist() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDate.now();
        }
    }

    /** 필수 입력 생성자 */
    public Transaction(int amount, TransactionType transactionType) {
        this.amount = amount;
        this.transactionType = transactionType;
    }

    /** 선택 입력 포함 생성자 ( 없는 정보는 null 저장 ) */
    public Transaction(int amount, TransactionType transactionType, String category, String description) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.category = category;
        this.description = description;
    }

    /** 선택 입력과 날짜를 포함한 생성자 */
    public Transaction(int amount, TransactionType transactionType, String category, String description,
                       LocalDate transactionDate) {
        this(amount, transactionType, category, description);
        this.transactionDate = transactionDate;
    }

    public void update(int amount, TransactionType transactionType, String category, String description,
                       LocalDate transactionDate) {
        this.amount = amount;
        this.transactionType = transactionType;
        this.category = category;
        this.description = description;
        if (transactionDate != null) {
            this.transactionDate = transactionDate;
        }
    }
}
