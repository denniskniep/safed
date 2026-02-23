package de.denniskniep.examplemtls.security.x509;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "mtls")
public class MtlsConfigurationProperties {

    private String caCertificatePath;
    private String expectedSubjectCn;

    public String getCaCertificatePath() {
        return caCertificatePath;
    }

    public void setCaCertificatePath(String caCertificatePath) {
        this.caCertificatePath = caCertificatePath;
    }

    public String getExpectedSubjectCn() {
        return expectedSubjectCn;
    }

    public void setExpectedSubjectCn(String expectedSubjectCn) {
        this.expectedSubjectCn = expectedSubjectCn;
    }
}
