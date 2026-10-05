package de.denniskniep.safed.output;

import de.denniskniep.safed.common.report.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResultOutputWriter {

    private static final Logger LOG = LoggerFactory.getLogger(ResultOutputWriter.class);

    private static final String SCAN_STATUS_FILENAME = "safed-scan-status.txt";

    public void writeScanStatusToFile(List<Report> reports) {
        if (reports == null || reports.isEmpty()) {
            LOG.warn("No reports given to write scan status to file.");
            return;
        }

        String scanStatusList = reports.stream().map(resport -> resport.getStatus().name()).collect(Collectors.joining(", "));
        String tempDir = System.getProperty("java.io.tmpdir");

        String filePath = tempDir + File.separator + SCAN_STATUS_FILENAME;
        try {
            Files.writeString(Paths.get(filePath), scanStatusList);
            LOG.info("Scan status written to file: {}", filePath);
        } catch (Exception e) {
            LOG.error("Failed to write scan status to file: {}", filePath, e);
        }
    }
}
