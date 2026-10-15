package de.denniskniep.safed.output;

import de.denniskniep.safed.common.report.Report;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class ResultOutputWriterTest {

    private static final String SCAN_STATUS_FILENAME = "safed-scan-status.txt";

    @TempDir
    private Path tempDir;

    private OutputConfig outputConfig;

    private ResultOutputWriter writer;

    @BeforeEach
    void setUp() {
        outputConfig = new OutputConfig();
        outputConfig.setScanStatusFile(statusFile().toString());
        writer = new ResultOutputWriter(outputConfig);
    }

    @Test
    void writeScanStatusToFile_writesSingleStatus() {
        writer.writeScanStatusToFile(reports(ScanResultStatus.OK));

        assertThat(statusFile()).hasContent("OK");
    }

    @Test
    void writeScanStatusToFile_writesMultipleStatusesCommaSeparatedInOrder() {
        writer.writeScanStatusToFile(reports(ScanResultStatus.OK, ScanResultStatus.VULNERABLE, ScanResultStatus.FAILED));

        assertThat(statusFile()).hasContent("OK, VULNERABLE, FAILED");
    }

    @Test
    void writeScanStatusToFile_overwritesExistingFile() throws IOException {
        Files.writeString(statusFile(), "VULNERABLE, VULNERABLE, VULNERABLE");

        writer.writeScanStatusToFile(reports(ScanResultStatus.OK));

        assertThat(statusFile()).hasContent("OK");
    }

    @Test
    void writeScanStatusToFile_doesNotWriteFile_onNullReports() {
        writer.writeScanStatusToFile(null);

        assertThat(statusFile()).doesNotExist();
    }

    @Test
    void writeScanStatusToFile_doesNotWriteFile_onEmptyReports() {
        writer.writeScanStatusToFile(List.of());

        assertThat(statusFile()).doesNotExist();
    }

    @Test
    void writeScanStatusToFile_doesNotWriteFile_whenNotConfigured() {
        outputConfig.setScanStatusFile(null);

        writer.writeScanStatusToFile(reports(ScanResultStatus.OK));

        assertThat(tempDir).isEmptyDirectory();
    }

    @Test
    void writeScanStatusToFile_doesNotWriteFile_whenConfiguredBlank() {
        outputConfig.setScanStatusFile("  ");

        writer.writeScanStatusToFile(reports(ScanResultStatus.OK));

        assertThat(tempDir).isEmptyDirectory();
    }

    @Test
    void writeScanStatusToFile_doesNotThrow_whenFileCannotBeWritten() {
        outputConfig.setScanStatusFile(tempDir.resolve("does-not-exist").resolve(SCAN_STATUS_FILENAME).toString());

        assertThatCode(() -> writer.writeScanStatusToFile(reports(ScanResultStatus.OK))).doesNotThrowAnyException();
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
