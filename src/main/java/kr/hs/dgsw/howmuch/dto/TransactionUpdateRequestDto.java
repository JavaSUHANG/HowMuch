package kr.hs.dgsw.howmuch.dto;

import kr.hs.dgsw.howmuch.entity.TransactionType;

import java.time.LocalDate;

public record TransactionUpdateRequestDto(
        Integer amount,
        TransactionType transactionType,
        String category,
        String description,
        LocalDate transactionDate
) {
}
