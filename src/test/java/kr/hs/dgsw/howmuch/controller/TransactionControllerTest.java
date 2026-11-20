package kr.hs.dgsw.howmuch.controller;

import kr.hs.dgsw.howmuch.entity.Transaction;
import kr.hs.dgsw.howmuch.entity.TransactionType;
import kr.hs.dgsw.howmuch.service.TransactionExportService;
import kr.hs.dgsw.howmuch.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@MockitoBean(types = TransactionExportService.class)
class TransactionControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TransactionService service;

    @Test
    void returnsMonthlyTransactions() throws Exception {
        Transaction expense = new Transaction(4500, TransactionType.EXPENSE);
        when(service.findByYearAndMonth(2024, 2)).thenReturn(List.of(expense));

        mvc.perform(get("/api/transactions/monthly").param("year", "2024").param("month", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.res").value("success"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].amount").value(4500))
                .andExpect(jsonPath("$.data[0].transactionType").value("EXPENSE"));
    }

    @Test
    void rejectsMonthThirteen() throws Exception {
        mvc.perform(get("/api/transactions/monthly").param("year", "2024").param("month", "13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.res").value("fail"))
                .andExpect(jsonPath("$.message").value("연도와 월을 올바르게 입력해 주세요."));

        verifyNoInteractions(service);
    }
}
