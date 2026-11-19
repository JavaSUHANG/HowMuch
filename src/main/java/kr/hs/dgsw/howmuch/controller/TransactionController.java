package kr.hs.dgsw.howmuch.controller;

import kr.hs.dgsw.howmuch.dto.ApiResponse;
import kr.hs.dgsw.howmuch.dto.TransactionCreateRequest;
import kr.hs.dgsw.howmuch.dto.TransactionSummaryResponse;
import kr.hs.dgsw.howmuch.dto.TransactionUpdateRequestDto;
import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.service.TransactionExportService;
import kr.hs.dgsw.howmuch.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionExportService transactionExportService;

    @PostMapping
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(
            @RequestBody TransactionCreateRequest request
    ) {
        if (request.amount() == null || request.transactionType() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("금액과 입출금 유형은 필수 입력 항목입니다."));
        }

        Transaction transaction = transactionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("입출금 내역 등록에 성공하였습니다.", transaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> updateTransaction(
            @PathVariable("id") Long id,
            @RequestBody TransactionUpdateRequestDto request
    ) {
        if (request.amount() == null || request.transactionType() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("금액과 입출금 유형은 필수 입력 항목입니다."));
        }

        Transaction transaction = transactionService.update(id, request);
        if (transaction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("존재하지 않는 입출금 내역입니다. id : " + id));
        }
        return ResponseEntity.ok(ApiResponse.success("입출금 내역 수정에 성공하였습니다.", transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable("id") Long id) {
        if (!transactionService.delete(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("존재하지 않는 입출금 내역입니다. id : " + id));
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success(transactionService.findAll()));
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportTransactionsToExcel(
            @RequestParam(value = "order", required = false) String order
    ) throws IOException {
        if (order == null) {
            order = "asc";
        }
        if (!"asc".equals(order) && !"desc".equals(order)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("정렬 순서는 asc 또는 desc만 가능합니다."));
        }

        byte[] excelFile = transactionExportService.exportTransactionsToExcel(order);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("transactions.xlsx")
                .build());

        return ResponseEntity.ok().headers(headers).body(excelFile);
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<Transaction>>> getMonthlyTransactions(
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        if (year < 1 || year > 999_999_999 || month < 1 || month > 12) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("연도와 월을 올바르게 입력해 주세요."));
        }
        return ResponseEntity.ok(ApiResponse.success(
                "월별 입출금 내역 조회에 성공하였습니다.",
                transactionService.findByYearAndMonth(year, month)
        ));
    }

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<List<Transaction>>> getPeriodTransactions(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        if (start.isAfter(end)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("시작 날짜는 종료 날짜보다 이후일 수 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success(
                "기간별 입출금 내역 조회에 성공하였습니다.",
                transactionService.findByPeriod(start, end)
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getTransactionSummary() {
        return ResponseEntity.ok(ApiResponse.success(
                "입출금 통계 조회에 성공하였습니다.",
                transactionService.getSummary()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Transaction>> getTransactionById(@PathVariable("id") Long id) {
        Transaction transaction = transactionService.findById(id);
        if (transaction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail("존재하지 않는 입출금 내역입니다. id : " + id));
        }
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }
}
