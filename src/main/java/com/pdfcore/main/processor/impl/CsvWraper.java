package com.pdfcore.main.processor.impl;

import java.io.IOException;

import com.pdfcore.main.exceptions.ConfigInitException;

import au.com.bytecode.opencsv.CSVReader;

public class CsvWraper {
	private CSVReader reader;
	private String[] header; 
	private String[] currentValues;
	
	public CsvWraper(CSVReader reader) throws ConfigInitException
	{
		this.reader = reader;
		try 
		{
			header = reader.readNext();
		} 
		catch (IOException e) 
		{
			throw new ConfigInitException(e.getMessage(),e);
		}
	}
	public boolean next()
	{
		try {
			currentValues = reader.readNext();
		} 
		catch (IOException e) {
			return false;
		}
		if (currentValues == null)
			return false;
		return true;
	}
	public String getValueForColumn(String columnName)
	{
		int index = getIndexForColumn(columnName);
		if (index == -1)
			return null;
		return currentValues[index];
		
	}

	public int getIndexForColumn(String headerName)
	{
		if (headerName == null)
			return -1;
		for (int i = 0 ; header != null && i < header.length ; i++)
		{
			if (headerName.equals(header[i]))
				return i;
		}
		return -1;
	}
	
}
