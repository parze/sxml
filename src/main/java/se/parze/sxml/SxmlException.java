package com.parze.sxml;

public class SxmlException extends RuntimeException {

    public SxmlException(String s, Exception e) {
        super(s, e);
    }

    public SxmlException(String s) {
        super(s);
    }
}
