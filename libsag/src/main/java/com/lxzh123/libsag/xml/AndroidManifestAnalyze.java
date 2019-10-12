package com.lxzh123.libsag.xml;

import com.lxzh123.libsag.log.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AndroidManifestAnalyze {
    private final static String TAG = "AndroidManifestAnalyze";

    private String appPackage;
    private List<String> permissions = new ArrayList<>();
    private List<String> activities = new ArrayList<>();

    public String getAppPackage() {
        return appPackage;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public List<String> getActivities() {
        return activities;
    }

    /**
     * parse package name
     *
     * @param doc
     * @return
     */
    public String findPackage(Document doc) {
        Node node = doc.getFirstChild();
        NamedNodeMap attrs = node.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.item(i).getNodeName().equals("package")) {
                return attrs.item(i).getNodeValue();
            }
        }
        return null;
    }

    /**
     * update manifest package name
     *
     * @param manifestName
     * @param pkgName
     */
    public void updatePkgName(String manifestName, String pkgName) {
        Document document = null;
        try {
            document = JdomTools.getDocument(manifestName);
        } catch (Exception ex) {

        }
        if (document == null) {
            return;
        }
        Node node = document.getFirstChild();
        NamedNodeMap attrs = node.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.item(i).getNodeName().equals("package")) {
                attrs.item(i).setNodeValue(pkgName);
            }
        }
        try {
            JdomTools.write2Xml(document, manifestName);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.get().printStacktrace(TAG, e);
        }
    }

    /**
     * parse launcher activity
     *
     * @param doc
     * @return
     */
    public String findLaucherActivity(Document doc) {
        Node activity = null;
        String sTem = "";
        NodeList categoryList = doc.getElementsByTagName("category");
        for (int i = 0; i < categoryList.getLength(); i++) {
            Node category = categoryList.item(i);
            NamedNodeMap attrs = category.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                if (attrs.item(j).getNodeName() == "android:name") {
                    if (attrs.item(j).getNodeValue().equals("android.intent.category.LAUNCHER")) {
                        activity = category.getParentNode().getParentNode();
                        break;
                    }
                }
            }
        }
        if (activity != null) {
            NamedNodeMap attrs = activity.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                if (attrs.item(j).getNodeName() == "android:name") {
                    sTem = attrs.item(j).getNodeValue();
                }
            }
        }
        return sTem;
    }

    /**
     * only parse package name
     *
     * @param filePath
     * @return
     */
    public void parsePackage(String filePath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            //load xml
            Document document = db.parse(filePath);

            //get package name
            appPackage = findPackage(document);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * parser entrance
     *
     * @param filePath
     */
    public void xmlHandle(String filePath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // create DocumentBuilder object
            DocumentBuilder db = dbf.newDocumentBuilder();

            //load xml
            Document document = db.parse(filePath);
            NodeList permissionList = document.getElementsByTagName("uses-permission");
            NodeList activityAll = document.getElementsByTagName("activity");

            //get permission list
            for (int i = 0; i < permissionList.getLength(); i++) {
                Node permission = permissionList.item(i);
                permissions.add((permission.getAttributes()).item(0).getNodeValue());
            }

            //get activity list
            appPackage = findPackage(document);
            for (int i = 0; i < activityAll.getLength(); i++) {
                Node activity = activityAll.item(i);
                NamedNodeMap attrs = activity.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    if (attrs.item(j).getNodeName() == "android:name") {
                        String sTem = attrs.item(j).getNodeValue();
                        if (sTem.startsWith(".")) {
                            sTem = appPackage + sTem;
                        }
                        activities.add(sTem);
                    }
                }
            }
            String s = findLaucherActivity(document);
            if (s.startsWith(".")) {
                s = appPackage + s;
            }
            //move entrance to head
            activities.remove(s);
            activities.add(0, s);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testOutput(AndroidManifestAnalyze a) {
        System.out.println("packageName:" + a.appPackage);
        System.out.println("permissions(" + a.permissions.size() + "):");
        for (int i = 0; i < a.permissions.size(); i++) {
            System.out.println(a.permissions.get(i));
        }

        System.out.println("activities(" + a.activities.size() + "):");
        for (int i = 0; i < a.activities.size(); i++) {
            System.out.println(a.activities.get(i));
        }
    }

    public static void main(String[] args) {
        AndroidManifestAnalyze a = new AndroidManifestAnalyze();
        a.xmlHandle("D:\\Android\\Code\\reinforce-java-android\\sdk\\sdk\\AndroidManifest.xml");
        testOutput(a);
    }
}