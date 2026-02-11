package de.denniskniep.safed.saml.auth;

import de.denniskniep.safed.common.auth.HttpRequest;
import org.keycloak.saml.BaseSAML2BindingBuilder;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.HashMap;

public class CustomSamlBindingBuilder extends BaseSAML2BindingBuilder<CustomSamlBindingBuilder> {

    private final DocumentProcessor afterSigning;
    private final EncodingProcessor afterEncoding;

    public CustomSamlBindingBuilder(DocumentProcessor afterSigning, EncodingProcessor afterEncoding) {
        this.afterSigning = afterSigning;
        this.afterEncoding = afterEncoding;
    }

    @Override
    public CustomSamlBindingBuilder.PostBindingBuilder postBinding(Document document) throws ProcessingException {
        return new CustomSamlBindingBuilder.PostBindingBuilder(this, document);
    }

    public class PostBindingBuilder extends BasePostBindingBuilder {
        public PostBindingBuilder(CustomSamlBindingBuilder builder, Document document) throws ProcessingException {
            super(builder, document);
            super.document = afterSigning.process(document);
        }

        public SamlResponseResult createWebRequest(URL actionUrl) {
            String encoded;
            try {
                encoded = this.encoded();
            } catch (Exception e) {
                throw new RuntimeException("Can not encode SamlResponse", e);
            }

            encoded = afterEncoding.process(encoded);

            HashMap<String, String> requestParameters = new HashMap<>();
            requestParameters.put("SAMLResponse", encoded);
            requestParameters.put("RelayState", relayState);

            var request = new HttpRequest("POST", actionUrl.toString(), requestParameters);
            return new SamlResponseResult(request, encoded, relayState);
        }
    }
}
