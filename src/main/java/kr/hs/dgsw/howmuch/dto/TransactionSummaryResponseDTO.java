package kr.hs.dgsw.howmuch.dto;

public record TransactionSummaryResponseDTO(
        long totalIncome,
        long totalExpense,
        long balance
) {
}
