package de.denniskniep.safed.saml.auth;

import de.denniskniep.safed.common.config.IssuerConfig;
import de.denniskniep.safed.common.keys.KeyProvider;
import de.denniskniep.safed.saml.config.SamlClientConfig;
import de.denniskniep.safed.saml.data.SamlAuthData;
import de.denniskniep.safed.saml.data.SamlRequestData;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.dom.saml.v2.assertion.AudienceRestrictionType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.saml.SAML2LoginResponseBuilder;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.w3c.dom.Document;

import java.net.URI;
import java.security.*;

public class SamlResponseBuilder {

    private final DocumentProcessor beforeSigning;
    private final DocumentProcessor afterSigning;
    private final EncodingProcessor afterEncoding;

    public SamlResponseBuilder(DocumentProcessor beforeSigning, DocumentProcessor afterSigning, EncodingProcessor afterEncoding) {
        this.beforeSigning = beforeSigning;
        this.afterSigning = afterSigning;
        this.afterEncoding = afterEncoding;
    }

    private AttributeType createAttribute(String name, String value){
        AttributeType attribute = new AttributeType(name);
        attribute.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_BASIC.get());
        attribute.addAttributeValue(value);
        return attribute;
    }

    public SamlResponseResult create(IssuerConfig issuerConfig, SamlClientConfig clientConfig, SamlRequestData request, SamlAuthData samlAuthData) {

        // References:
        // SamlProtocol.authenticated
        // SamlService.handleSamlRequest

        // Manipulate standard properties
        SAML2LoginResponseBuilder builder = new SAML2LoginResponseBuilder()
                .requestID(request.getId()) // InResponseTo
                .destination(clientConfig.getRedirectUrl().toExternalForm())
                .issuer(issuerConfig.getId().toExternalForm())
                .assertionExpiration(clientConfig.getAssertionLifespanInMinutes())
                .subjectExpiration(clientConfig.getAssertionLifespanInMinutes())
                .sessionExpiration(clientConfig.getSessionLifespanInMinutes())
                .requestIssuer(clientConfig.getClientId())
                .authMethod(samlAuthData.getAuthMethod())
                .sessionIndex(samlAuthData.getSessionIndex())
                .disableAuthnStatement(!clientConfig.isEnableAuthnStatement())
                .includeOneTimeUseCondition(clientConfig.isEnableOneTimeUse())
                .nameIdentifier(clientConfig.getNameIdFormat(), clientConfig.getNameId());
        //.addExtension(new KeycloakKeySamlExtensionGenerator(keyName))

        ResponseType samlModel;
        try {
            samlModel = builder.buildModel();
        } catch (Exception e) {
            throw new RuntimeException("Can not build SAMLResponse", e);
        }
        AttributeStatementType attributeStatement = new AttributeStatementType();
        for (var claim : clientConfig.getClaims()) {
            for (var value : claim.getValues()) {
                var attr = createAttribute(claim.getName(),value);
                attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(attr));
            }
        }

        // SAML Spec 2.7.3 AttributeStatement must contain one or more Attribute or EncryptedAttribute
        if (!attributeStatement.getAttributes().isEmpty()) {
            AssertionType assertion = samlModel.getAssertions().getFirst().getAssertion();
            assertion.addStatement(attributeStatement);
        }

        AudienceRestrictionType audElement = locateAudienceRestriction(samlModel);
        if(audElement == null){
            throw new RuntimeException("Can not locate AudienceRestriction");
        }

        for (var a : audElement.getAudience().stream().toList()){
            audElement.removeAudience(a);
        }

        for (var a : samlAuthData.getAudiences()) {
            try {
                audElement.addAudience(URI.create(a));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid URI syntax for audience: " + a, e);
            }
        }

        CustomSamlBindingBuilder bindingBuilder = new CustomSamlBindingBuilder(afterSigning, afterEncoding);
        bindingBuilder.relayState(request.getRelayState());

        if (clientConfig.requireSignDocument() || clientConfig.requireSignAssertion()) {
            KeyWrapper keyPair = KeyProvider.loadSigningKey(clientConfig, issuerConfig);
            String keyName = clientConfig.getKeyNameTransformer().getKeyName(keyPair.getKid(), keyPair.getCertificate());
            bindingBuilder.canonicalizationMethod(clientConfig.getCanonicalizationMethod().toString());
            bindingBuilder.signatureAlgorithm(clientConfig.getSignatureAlgorithm());
            bindingBuilder.signWith(keyName,(PrivateKey) keyPair.getPrivateKey(), (PublicKey) keyPair.getPublicKey(), keyPair.getCertificate());
        }

        if (clientConfig.requireSignDocument()) {
            bindingBuilder.signDocument();
        }
        if (clientConfig.requireSignAssertion()) {
            bindingBuilder.signAssertions();
        }

        if (clientConfig.requireEncryptAssertion()) {
            // Todo: implement encryption (bindingBuilder.encrypt(publicKey);)
        }

        Document samlDocument;
        try {
            samlDocument = builder.buildDocument(samlModel);
        } catch (Exception e) {
            throw new RuntimeException("Can not build SAMLDocument", e);
        }

        if(beforeSigning != null){
            samlDocument = beforeSigning.process(samlDocument);
        }

        try {
            var postBindingBuilder = bindingBuilder.postBinding(samlDocument);

            //todo: Apply here XML Changes after signature!
            return postBindingBuilder.createWebRequest(clientConfig.getRedirectUrl());
        } catch (Exception e) {
            throw new RuntimeException("Can not build SAMLResponse", e);
        }
    }

    private AudienceRestrictionType locateAudienceRestriction(ResponseType samlModel) {
        try {
            return samlModel.getAssertions().getFirst().getAssertion().getConditions().getConditions()
                    .stream()
                    .filter(AudienceRestrictionType.class::isInstance)
                    .map(AudienceRestrictionType.class::cast)
                    .findFirst().orElse(null);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            return null;
        }
    }
}
