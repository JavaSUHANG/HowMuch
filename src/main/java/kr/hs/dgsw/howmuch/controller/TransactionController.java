package kr.hs.dgsw.howmuch.controller;

import kr.hs.dgsw.howmuch.dto.ApiResponse;
import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.service.TransactionExportService;
import kr.hs.dgsw.howmuch.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionExportService transactionExportService;

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
