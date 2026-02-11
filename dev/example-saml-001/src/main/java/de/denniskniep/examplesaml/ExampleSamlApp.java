package de.denniskniep.examplesaml;

import org.mockito.internal.configuration.plugins.Plugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExampleSamlApp {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleSamlApp.class);


    public static void main(final String[] args) {
        SpringApplication.run(ExampleSamlApp.class);
        verifyMockMaker();
    }

    private static void verifyMockMaker(){
        var mockMaker = Plugins.getMockMaker();
        LOG.info("MockMaker:{}", mockMaker.getClass().getSimpleName());

        try {
            mockMaker.clearAllCaches();
        }catch (Exception e){
            LOG.error("Error when using MockMaker", e);
            throw e;
        }
    }
}
