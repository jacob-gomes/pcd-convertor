package com.pdfcore.main.model;

import java.io.InputStream;

public class BinaryStream {
 private InputStream input;
 private int length;
 
 
public BinaryStream(InputStream input, int length) {
	super();
	this.input = input;
	this.length = length;
}
public void setInput(InputStream input) {
	this.input = input;
}
public InputStream getInput() {
	return input;
}
public void setLength(int length) {
	this.length = length;
}
public int getLength() {
	return length;
}
 
}
