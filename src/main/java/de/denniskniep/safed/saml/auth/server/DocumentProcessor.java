package de.denniskniep.safed.saml.auth.server;

import org.w3c.dom.Document;

public interface DocumentProcessor {
    Document process(Document document);
}
