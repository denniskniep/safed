package de.denniskniep.safed.common.verifications;

import de.denniskniep.safed.common.scans.AuthResult;
import de.denniskniep.safed.common.scans.ScanResultStatus;
import de.denniskniep.safed.saml.SamlAuthResult;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.processing.web.util.PostBindingUtil;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class SamlProtocolInfo implements ScanResultVerificationStrategy {

    private static final String DSIG_NS = "http://www.w3.org/2000/09/xmldsig#";

    @Override
    public List<Evidence> extractInfos(AuthResult scanAuthResult) {
        if(scanAuthResult instanceof SamlAuthResult samlAuthResult){
            return List.of(
                new Evidence(EvidenceStatus.INFO, "SamlRequest", samlAuthResult.getSamlInitializationResult().getSamlRequestAsBase64()),
                new Evidence(EvidenceStatus.INFO, "SamlResponse", redactSignature(samlAuthResult.getSamlResponseResult().getSamlResponse()))
            );
        }
        return List.of();
    }

    private static String redactSignature(String samlResponseAsBase64) {
        if(samlResponseAsBase64 == null){
            return null;
        }
        try {
            byte[] decoded = PostBindingUtil.base64Decode(samlResponseAsBase64);
            Document document = DocumentUtil.getDocument(new ByteArrayInputStream(decoded));

            NodeList signatureValues = document.getElementsByTagNameNS(DSIG_NS, "SignatureValue");
            if(signatureValues.getLength() == 0){
                return samlResponseAsBase64;
            }
            for(int i = 0; i < signatureValues.getLength(); i++){
                signatureValues.item(i).setTextContent("redacted");
            }

            String redactedXml = DocumentUtil.getDocumentAsString(document);
            return Base64.getEncoder().encodeToString(redactedXml.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public VerificationResult evaluateScanResult(AuthResult firstPositiveAuthResult, AuthResult secondPositiveAuthResult, AuthResult scanAuthResult) {
        return new VerificationResult(ScanResultStatus.OK, new ArrayList<>());
    }
}