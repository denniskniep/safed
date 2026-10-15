package de.denniskniep.safed;

import de.denniskniep.safed.output.OutputConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(OutputConfig.class)
@SpringBootApplication
public class SafedApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafedApplication.class, args);
    }
}
