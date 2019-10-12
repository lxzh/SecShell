package com.lxzh123.libsag.xml;

import com.lxzh123.libsag.log.Logger;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XMLUtil {
    private final static String TAG = "XMLUtil";

    /**
     * prepare xml document object
     *
     * @param fileName
     * @return
     */
    public static Document getDocument(final String fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new File(fileName));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            Logger.get().printStacktrace(TAG, e);
        }
        //document.setXmlStandalone(true);
        return document;
    }

    /**
     * update node value in xml
     *
     * @param inputName
     * @param outputName
     * @param nodeName
     * @param nodeValue
     */
    public static void updateNode(String inputName, String outputName, String nodeName, String nodeValue) {
        Document document = getDocument(inputName);
        NodeList tagName = document.getElementsByTagName(nodeName);
        Node item = tagName.item(0);
        item.setNodeValue(nodeValue);
        try {
            JdomTools.write2Xml(document, outputName);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.get().printStacktrace(TAG, e);
        }
    }

    public static String readXML(String xmlName, String nodeName) {
        try {
            Document document = JdomTools.getDocument(xmlName);
            NodeList tagName = document.getElementsByTagName(nodeName);
            Node item = tagName.item(0);
            return item.getTextContent();
        } catch (Exception e) {
            e.printStackTrace();
            Logger.get().printStacktrace(TAG, e);
        }
        return null;
    }

    public static void writeXML(String xmlName, String outputName, String nodeName, String nodeValue) {
        try {
            Document document = JdomTools.getDocument(xmlName);
            NodeList nodeList = document.getElementsByTagName(nodeName);
            Node node = nodeList.item(0);
            node.setNodeValue(nodeValue);
            JdomTools.write2Xml(document, outputName);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.get().printStacktrace(TAG, e);
        }
    }

    public static void removeNode(String fileName, String parentName, String nodeName) {
        try {
            Document document = JdomTools.getDocument(fileName);

            JdomTools.deleteNode(document.getElementsByTagName(parentName).item(0),
                    document.getElementsByTagName(nodeName).item(0));
            JdomTools.write2Xml(document, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.get().printStacktrace(TAG, e);
        }
    }

    public static void replaceNode(String fileName, String oldNodeName, String newNodeName, String newNodeValue) {
        try {
            Document document = JdomTools.getDocument(fileName);
            Element element = document.createElement(newNodeName);
            element.setTextContent(newNodeValue);
            JdomTools.replaceNodeText(element, document.getElementsByTagName(oldNodeName).item(0));
            JdomTools.write2Xml(document, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.get().printStacktrace(TAG, e);
        }
    }
}