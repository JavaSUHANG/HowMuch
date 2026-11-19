package kr.hs.dgsw.howmuch.dto;

import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TransactionExportDto {
    private Long id;
    private LocalDate transactionDate;
    private TransactionType transactionType;
    private String category;
    private int amount;
    private String description;

    public static TransactionExportDto from(Transaction transaction) {
        return new TransactionExportDto(
                transaction.getId(),
                transaction.getTransactionDate(),
                transaction.getTransactionType(),
                transaction.getCategory(),
                transaction.getAmount(),
                transaction.getDescription()
        );
    }
}
