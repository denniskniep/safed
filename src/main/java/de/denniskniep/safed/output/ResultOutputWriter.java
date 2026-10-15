package de.denniskniep.safed.output;

import de.denniskniep.safed.common.report.Report;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResultOutputWriter {

    private static final Logger LOG = LoggerFactory.getLogger(ResultOutputWriter.class);

    private final OutputConfig outputConfig;

    public ResultOutputWriter(OutputConfig outputConfig) {
        this.outputConfig = outputConfig;
    }

    public void writeScanStatusToFile(List<Report> reports) {
        String filePath = outputConfig.getScanStatusFile();
        if (StringUtils.isBlank(filePath)) {
            LOG.debug("No scan status file configured, skip writing scan status to file.");
            return;
        }

        if (reports == null || reports.isEmpty()) {
            LOG.warn("No reports given to write scan status to file.");
            return;
        }

        String scanStatusList = reports.stream().map(report -> report.getStatus().name()).collect(Collectors.joining(", "));

        try {
            Files.writeString(Paths.get(filePath), scanStatusList);
            LOG.info("Scan status written to file: {}", filePath);
        } catch (Exception e) {
            LOG.error("Failed to write scan status to file: {}", filePath, e);
        }
    }
}
