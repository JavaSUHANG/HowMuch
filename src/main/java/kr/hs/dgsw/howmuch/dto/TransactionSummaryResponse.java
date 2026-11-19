package kr.hs.dgsw.howmuch.dto;

public record TransactionSummaryResponse(
        long totalIncome,
        long totalExpense,
        long balance
) {
}
