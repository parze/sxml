/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.parze.sxml;

/**
 *
 * @author past01
 */
@SuppressWarnings("rawtypes")
public class Namespace implements Comparable {

    private String prefix;

    private String namespace;

    public Namespace(String prefix, String namespace) {
        this.prefix = prefix;
        this.namespace = namespace;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
	public int compareTo(Object o) {
        return (prefix + namespace).compareTo(((Namespace)o).getPrefix()+((Namespace)o).getNamespace());
    }

}
