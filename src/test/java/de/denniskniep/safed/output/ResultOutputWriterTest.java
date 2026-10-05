package de.denniskniep.safed.output;

import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ResultOutputWriterTest {

    private static final String SCAN_STATUS_FILENAME = "safed-scan-status.txt";

    @TempDir
    Path tempDir;

    private String originalTmpDir;
    private ResultOutputWriter writer;

    @BeforeEach
    void setUp() {
        originalTmpDir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", tempDir.toString());
        writer = new ResultOutputWriter();
    }

    @AfterEach
    void tearDown() {
        System.setProperty("java.io.tmpdir", originalTmpDir);
    }

    @Test
    void writeScanStatusToFile_writesSingleStatus() throws IOException {
        writer.writeScanStatusToFile(reports(ScanResultStatus.OK));

        assertEquals("OK", Files.readString(statusFile()));
    }

    @Test
    void writeScanStatusToFile_writesMultipleStatusesCommaSeparatedInOrder() throws IOException {
        writer.writeScanStatusToFile(reports(ScanResultStatus.OK, ScanResultStatus.VULNERABLE, ScanResultStatus.FAILED));

        assertEquals("OK, VULNERABLE, FAILED", Files.readString(statusFile()));
    }

    @Test
    void writeScanStatusToFile_overwritesExistingFile() throws IOException {
        Files.writeString(statusFile(), "VULNERABLE, VULNERABLE, VULNERABLE");

        writer.writeScanStatusToFile(reports(ScanResultStatus.OK));

        assertEquals("OK", Files.readString(statusFile()));
    }

    @Test
    void writeScanStatusToFile_doesNotWriteFile_onNullReports() {
        writer.writeScanStatusToFile(null);

        assertFalse(Files.exists(statusFile()));
    }

    @Test
    void writeScanStatusToFile_doesNotWriteFile_onEmptyReports() {
        writer.writeScanStatusToFile(List.of());

        assertFalse(Files.exists(statusFile()));
    }

    @Test
    void writeScanStatusToFile_doesNotThrow_whenFileCannotBeWritten() {
        System.setProperty("java.io.tmpdir", tempDir.resolve("does-not-exist").toString());

        assertDoesNotThrow(() -> writer.writeScanStatusToFile(reports(ScanResultStatus.OK)));
    }

    private Path statusFile() {
        return tempDir.resolve(SCAN_STATUS_FILENAME);
    }

    private static List<Report> reports(ScanResultStatus... statuses) {
        return Arrays.stream(statuses).map(status -> {
            Report report = new Report();
            report.setStatus(status);
            return report;
        }).toList();
    }
}
