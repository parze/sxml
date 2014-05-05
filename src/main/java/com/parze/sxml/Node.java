package com.parze.sxml;

import java.util.*;

public class Node {

    private String fileName;
    private int row = -1;
    private String name;
    private Map<String, String> attributes;
	private String prefix = null;
    private Node parent = null;
    private String value = null;
    private List<Node> children = null;
    private Namespace namespace = null;
    
    public Node(String name) {
        int i = name.indexOf(":");
        if (i > 0) {
            prefix = name.substring(0, i);
            this.name = name.substring(i+1);
        } else {
            this.name = name;
        }
    }

    // used when decoding from XML
    public Node(String name, String fileName, int row) {
        this(name);
        this.fileName = fileName;
        this.row = row;
    }

    // Used when encoding out to XML
    public Node(String name, Namespace namespace) {
        this(name);
        this.namespace = namespace;
    }

    public String getFileName() {
        return fileName;
    }

    public int getRow() {
        return row;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public List<Node> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public Node getChild(String name) {
        if (children != null) {
            for (Node child : children) {
                if (child.getName().equalsIgnoreCase(name)) {
                    return child;
                }
            }
        }
        throw new SxmlException("Failed to find child "+name);
    }
    
    // integer
	public void addAttributeInteger(String name, int value) {
		addAttributeString(name, new Integer(value).toString());
	}
	public void addAttributeInteger(String name, int value, int defaultValue) {
		if (value != defaultValue) {
			addAttributeInteger(name, value);
		}
	}	
	public int getAttributeInteger(String name) {
		return Integer.parseInt(getAttributeString(name));
	}
	public int getAttributeInteger(String name, int defaultInt) {
		String value = getAttributeString(name);
		if (value == null) {
			return defaultInt;
		}
		return Integer.parseInt(value);
	}
	
	// double
	public void addAttributeDouble(String name, double value) {
		addAttributeString(name, new Double(value).toString());
	}
	public double getAttributeDouble(String name) {
		return Double.parseDouble(getAttributeString(name));
	}
	public double getAttributeDouble(String name, double defaultValue) {
		if (hasAttribute(name)) {
			return getAttributeDouble(name);
		}
		return defaultValue;
	}	
	
	
	// boolean
	public void addAttributeBoolean(String name, boolean value) {
		addAttributeString(name, new Boolean(value).toString());
	}
	public boolean getAttributeBoolean(String name) {
		return Boolean.parseBoolean(getAttributeString(name));
	}	
	public boolean getAttributeBoolean(String name, boolean defaultValue) {
		if (hasAttribute(name)) {
			return getAttributeBoolean(name);
		}
		return defaultValue;
	}
	
	// string
    public String getAttributeString(String attributeName) {    	
        if (hasAttribute(attributeName)) {
            String value = attributes.get(attributeName);
            return replace(value, "{inch}", "\"");
        }
        return null;
    }
    public void addAttributeString(String name, String value) {
        value = replace(value, "\"", "{inch}");
        getAttributes().put(name, value);
    }		
    public String getAttributeString(String attributeName, String defaultValue) {
    	String value = getAttributeString(attributeName);
    	if (value == null) {
    		return defaultValue;
    	}
    	return value;
    }
    private String replace(String value, String replace, String with) {
    	if (value == null) {
    		return null;
    	}
    	int p = value.indexOf(replace);
    	while (p >= 0) {
    		value = value.substring(0, p) + with + value.substring(p+replace.length());
    		p = value.indexOf(replace, p+with.length());
    	}
    	return value;
    }

    // other
    public String getAttributeAsString() {
    	StringBuffer sb = new StringBuffer();
    	if (this.attributes != null && this.attributes.size() > 0) {
    		for (String key : this.attributes.keySet()) {
    			sb.append(" "+key+"=\""+this.attributes.get(key)+"\"");
    		}
    	}
    	return sb.toString();
    }
    
    public Map<String, String> getAttributes() {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        return this.attributes;
    }
    
	public boolean hasAttributes() {
		return this.attributes != null && this.attributes.size() > 0;
	}
    
	public boolean hasAttribute(String name) {
		if (this.attributes == null) {
			return false;
		}
		return this.attributes.containsKey(name);
	}
	
    public Set<String> getAttributeKeys() {
    	if (this.attributes == null) {
    		return null;
    	}
    	return this.attributes.keySet();
    }
            
    public void addChild(Node child) {
        if (children == null) {
            children = new ArrayList<Node>();
        }
        children.add(child);
        child.setParent(this);
    }

    public void setTextValue(String value) {
        this.value = value;
    }

    public String getTextValue() {
        return value;
    }

    public Node getParent() {
        return parent;
    }

    public Node findChild(String name) {
        if (hasChildren()) {
            for (Node child : getChildren()) {
                if (child.getName().equalsIgnoreCase(name)) {
                    return child;
                }
            }
        }
        return null;
    }

    public String getChildTextValue(String childName) {
        Node t = findChild(childName);
        if (t == null) {
            return null;
        }
        return t.getTextValue();
    }

    public boolean hasChildren() {
        return children !=null && children.size()>0;
    }

    public boolean hasTextValue() {
        return value != null;
    }

    public String getStartXmlName() {
        return "<"+ namespace.getPrefix() + ":" + getName() + getAttributeAsString()+">";
    }
    
    public String getStartXmlNameAndNamespacesDifinitions() {
        StringBuilder xmlName = new StringBuilder();
        xmlName.append("<").append(namespace.getPrefix()).append(":").append(getName()).append(getAttributeAsString());
        for (Namespace ns : collectNS(new TreeSet<Namespace>(), this)) {
            xmlName.append(" xmlns:").append(ns.getPrefix()).append("=\"")
                    .append(ns.getNamespace()).append("\"");
        }
        xmlName.append(">");
        return xmlName.toString();
    }
    private Set<Namespace> collectNS(Set<Namespace> ns, Node node) {
        ns.add(node.getNamespace());
        if (node.hasChildren()) {
            for (Node child : node.getChildren()) {
                collectNS(ns, child);
            }
        }
        return ns;
    }

    public String getEndXmlName() {
        return "</"+ namespace.getPrefix() + ":" + getName()+">";
    }

    public String getEmptyXmlName() {
        return "<"+ namespace.getPrefix() + ":" + getName() + getAttributeAsString()+"/>";
    }

    public String getXmlRef() {
        return (getParent().getName()+"."+getName()).toLowerCase();
    }

    private void setParent(Node parent) {
        this.parent = parent;
    }


}
