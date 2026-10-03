package com.wolfe.catalog;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class PdfImportAfterCommitContractTest {
    @Test
    void pdfImportMustNotStartWorkerBeforeTransactionCommit() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/wolfe/catalog/pdf/PdfImportService.java"));
        assertTrue(source.contains("registerSynchronization"), "PDF worker submission must be registered after transaction commit");
        assertTrue(source.contains("afterCommit"), "PDF worker must start from an afterCommit callback");
    }
}
