package kr.hs.dgsw.howmuch.service;

import java.io.IOException;

public interface TransactionExportService {
    byte[] exportTransactionsToExcel(String order) throws IOException;
}
