package de.denniskniep.safed.saml.config;

import de.denniskniep.safed.common.config.FederationAppConfig;
import de.denniskniep.safed.common.utils.Serialization;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.util.XmlKeyInfoKeyNameTransformer;

public class SamlAppConfig extends FederationAppConfig {

    private int assertionLifespanInMinutes = 1;

    private int sessionLifespanInMinutes = 120;

    private boolean enableAuthnStatement = true;

    private boolean enableOneTimeUse = true;

    private String nameIdFormat = JBossSAMLURIConstants.NAMEID_FORMAT_UNSPECIFIED.getUri().toString();

    private String nameId = "jonny.tester";

    private SamlCanonicalizationMethod canonicalizationMethod = SamlCanonicalizationMethod.EXCLUSIVE;



    private XmlKeyInfoKeyNameTransformer keyNameTransformer = XmlKeyInfoKeyNameTransformer.NONE;

    private boolean signDocument = true;

    private boolean signAssertion = false;

    private boolean encryptAssertion = false;

    public boolean isSignDocument() {
        return signDocument;
    }

    public boolean isSignAssertion() {
        return signAssertion;
    }

    public int getAssertionLifespanInMinutes() {
        return assertionLifespanInMinutes;
    }

    public void setAssertionLifespanInMinutes(int assertionLifespanInMinutes) {
        this.assertionLifespanInMinutes = assertionLifespanInMinutes;
    }

    public boolean isEnableAuthnStatement() {
        return enableAuthnStatement;
    }

    public void setEnableAuthnStatement(boolean enableAuthnStatement) {
        this.enableAuthnStatement = enableAuthnStatement;
    }

    public boolean isEnableOneTimeUse() {
        return enableOneTimeUse;
    }

    public void setEnableOneTimeUse(boolean enableOneTimeUse) {
        this.enableOneTimeUse = enableOneTimeUse;
    }

    public String getNameIdFormat() {
        return nameIdFormat;
    }

    public void setNameIdFormat(String nameIdFormat) {
        this.nameIdFormat = nameIdFormat;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public int getSessionLifespanInMinutes() {
        return sessionLifespanInMinutes;
    }

    public void setSessionLifespanInMinutes(int sessionLifespanInMinutes) {
        this.sessionLifespanInMinutes = sessionLifespanInMinutes;
    }

    public SamlCanonicalizationMethod getCanonicalizationMethod() {
        return canonicalizationMethod;
    }

    public void setCanonicalizationMethod(SamlCanonicalizationMethod canonicalizationMethod) {
        this.canonicalizationMethod = canonicalizationMethod;
    }

    public boolean requireSignDocument() {
        return signDocument;
    }

    public void setSignDocument(boolean signDocument) {
        this.signDocument = signDocument;
    }

    public boolean requireSignAssertion() {
        return signAssertion;
    }

    public void setSignAssertion(boolean signAssertion) {
        this.signAssertion = signAssertion;
    }

    public boolean requireEncryptAssertion() {
        return encryptAssertion;
    }

    public void setEncryptAssertion(boolean encryptAssertion) {
        this.encryptAssertion = encryptAssertion;
    }

    public XmlKeyInfoKeyNameTransformer getKeyNameTransformer() {
        return keyNameTransformer;
    }

    public void setKeyNameTransformer(XmlKeyInfoKeyNameTransformer keyNameTransformer) {
        this.keyNameTransformer = keyNameTransformer;
    }

    public SamlAppConfig deepCopy(){
        return Serialization.DeepCopy(this, SamlAppConfig.class);
    }
}
