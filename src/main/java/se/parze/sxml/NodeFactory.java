package com.parze.sxml;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public class NodeFactory {

    public static void encodeToStream(Node node, OutputStream stream) {
    	String out = encodeToString(node, true);
    	try {
			stream.write(out.getBytes("UTF-8"));
		} catch (Exception e) {
			throw new SxmlException("Failed to encode node to stream.", e);
		}
    }

    public static String encodeToString(Node node, boolean readable) {
    	StringBuffer before = null;
    	if (readable) {
    		before = new StringBuffer();
    	}
        return encodeToStringRec(new StringBuffer(), before, node).toString();
    }

    private static StringBuffer encodeToStringRec(StringBuffer out, StringBuffer before, Node cNode) {
        if (before != null) out.append(before);
        if (cNode.hasChildren()) {
            if (cNode.getParent() == null) {
                out.append(cNode.getStartXmlNameAndNamespacesDifinitions());
            } else {
                out.append(cNode.getStartXmlName());
            }
            if (before != null) {
                out.append("\n");
                before.append("   ");
            }
            for (Node iNode : cNode.getChildren()) {
                encodeToStringRec(out, before, iNode);
            }
            if (before != null) {
                int len = before.length();
                before.delete(len-3, len);
            }
            if (before != null) out.append(before);
            out.append(cNode.getEndXmlName());
            if (before != null) out.append("\n");
        } else {
            if (cNode.hasTextValue()) {
                out.append(cNode.getStartXmlName());
                out.append(cNode.getTextValue());
                out.append(cNode.getEndXmlName());
                if (before != null) out.append("\n");
            } else {
                out.append(cNode.getEmptyXmlName());
                if (before != null) out.append("\n");
            }
        }
        return out;
    }

    public static Node decodeStream(String inputXml) {
        return decodeStream(inputXml, "Unknown");
    }

    public static Node decodeStream(String inputXml, String fileName) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(inputXml.getBytes("UTF-8"));
            return decodeStream(inputStream, fileName);
        } catch (UnsupportedEncodingException e) {
            throw new SxmlException("Failed to create input stream.", e);
        }

    }

    public static Node decodeStream(InputStream inputStream) {
        return decodeStream(inputStream, "Unknown");
    }

    public static Node decodeStream(InputStream inputStream, String fileName) {
        if (inputStream == null) {
            throw new SxmlException("Input stream to decoder may not be null.");
        }
        Node rootNode = null;
        FileDefaultHandler handler = null;
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            handler = new FileDefaultHandler(fileName);
            parser.parse(inputStream, handler);
            rootNode = handler.getRootNode();
        } catch (Exception e) {
            int lineNumber = -1;
            if (e instanceof SAXParseException) {
                lineNumber = ((SAXParseException) e).getLineNumber();
            }
            if (lineNumber < 0 && handler.getLocator() != null) {
                lineNumber = handler.getLocator().getLineNumber();
            }
            throw new SxmlException("Parse error in XML document. Line: " + lineNumber+" in file "+fileName, e);
        }
        return rootNode;
    }

    public static class FileDefaultHandler extends DefaultHandler {

        private Locator locator = null;
        private Node rootNode = null;
        private Node currentNode = null;
        private String fileName;

        public FileDefaultHandler(String fileName) {
            this.fileName = fileName;
        }

        public Locator getLocator() {
            return locator;
        }

        public Node getRootNode() {
            return rootNode;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        public int getLastLine() {
            return locator == null ? -1 : locator.getLineNumber();
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes att) {
            Node newNode = new Node(qName, fileName, locator.getLineNumber());
            if (att.getLength() > 0) {
                for (int i = 0; i < att.getLength(); i++) {
                	newNode.getAttributes().put(att.getQName(i), att.getValue(i));
                    //newNode.addAttribute(att.getQName(i), att.getValue(i));
                }
            }
            if (currentNode != null) {
                currentNode.addChild(newNode);
            } else {
                rootNode = newNode;
            }
            currentNode = newNode;
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (currentNode.getTextValue() != null) {
                currentNode.setTextValue(currentNode.getTextValue() + new String(ch, start, length));
            } else {
                currentNode.setTextValue(new String(ch, start, length));
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) {
            currentNode = currentNode.getParent();
        }
    }
}
