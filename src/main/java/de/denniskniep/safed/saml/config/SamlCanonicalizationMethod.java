package de.denniskniep.safed.saml.config;

public enum SamlCanonicalizationMethod {
    EXCLUSIVE("http://www.w3.org/2001/10/xml-exc-c14n#"),
    EXCLUSIVE_WITH_COMMENTS("http://www.w3.org/2001/10/xml-exc-c14n#WithComments"),
    INCLUSIVE("http://www.w3.org/TR/2001/REC-xml-c14n-20010315"),
    INCLUSIVE_WITH_COMMENTS("http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments");

    private final String method;

    SamlCanonicalizationMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return method;
    }
}
