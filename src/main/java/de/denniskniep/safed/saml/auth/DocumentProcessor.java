package de.denniskniep.safed.saml.auth;

import org.w3c.dom.Document;

public interface DocumentProcessor {
    Document process(Document document);
}
