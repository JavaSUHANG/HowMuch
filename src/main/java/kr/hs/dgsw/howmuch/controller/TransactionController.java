package kr.hs.dgsw.howmuch.controller;

import kr.hs.dgsw.howmuch.dto.ApiResponse;
import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success(transactionService.findAll()));
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
