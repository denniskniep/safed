package de.denniskniep.safed.saml.scans;

import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.List;

public class SamlResponseDocument {

    public static final String XPATH_NAME_ID = "//saml:NameID";
    public static final String XPATH_ISSUE_INSTANT = "//samlp:Response/@IssueInstant";
    public static final String XPATH_ASSERTION_ISSUE_INSTANT = "//saml:Assertion/@IssueInstant";
    public static final String XPATH_CONDITIONS_NOT_ON_OR_AFTER = "//saml:Conditions/@NotOnOrAfter";
    public static final String XPATH_CONDITIONS_NOT_BEFORE = "//saml:Conditions/@NotBefore";
    public static final String XPATH_STATUS_CODE = "//samlp:StatusCode/@Value";

    private final Document document;

    public SamlResponseDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public List<Node> selectNodesByXpath(String xPathValue) {
        try {
            List<Node> nodes = new ArrayList<>();
            XPath xpath = XPathFactory.newInstance().newXPath();
            SimpleNamespaceContext simpleNamespaceContext = new SimpleNamespaceContext();
            simpleNamespaceContext.bindNamespaceUri("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
            simpleNamespaceContext.bindNamespaceUri("samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
            xpath.setNamespaceContext(simpleNamespaceContext);
            XPathExpression expr = xpath.compile(xPathValue);
            var nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                nodes.add(nodeList.item(i));
            }
            return nodes;
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    public NodeList getSignatures() {
        return document.getElementsByTagNameNS("*", "Signature");
    }

    public void removeNodesByXpath(String xPathValue) {
        List<Node> nodes = selectNodesByXpath(xPathValue);
        removeNodes(nodes);
    }

    public void removeNodes(List<Node> nodes) {
        for (Node node : nodes) {
            Node parent = node.getParentNode();
            parent.removeChild(node);
        }
        document.normalize();
    }
}
