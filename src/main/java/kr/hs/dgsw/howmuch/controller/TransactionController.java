package kr.hs.dgsw.howmuch.controller;

import kr.hs.dgsw.howmuch.dto.ApiResponse;
import kr.hs.dgsw.howmuch.dto.TransactionCreateRequest;
import kr.hs.dgsw.howmuch.dto.TransactionUpdateRequest;
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
            @RequestBody TransactionUpdateRequest request
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
