package com.lxzh123.libsag.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JdomTools {

    /**
     * find document
     *
     * @param path
     * @return
     * @throws Exception
     */
    public static Document getDocument(String path) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(path);
    }


    /**
     * delete node
     *
     * @param oldChild
     * @param document
     * @throws Exception
     */
    public static void deleteNode(Node document, Node oldChild) throws Exception {
        document.removeChild(oldChild);
    }

    /**
     * replace node value
     *
     * @param newChild
     * @param oldChild
     */
    public static void replaceNodeText(Node newChild, Node oldChild) {
        oldChild.getParentNode().replaceChild(newChild, oldChild);
    }

    public static void replaceNodeText(Node node, String value) {
        node.setNodeValue(value);
    }

    /**
     * write a new node
     *
     * @param document
     * @param path
     * @throws Exception
     */
    public static void write2Xml(Document document, String path) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yse");
        transformer.transform(new DOMSource(document), new StreamResult(path));
    }
}