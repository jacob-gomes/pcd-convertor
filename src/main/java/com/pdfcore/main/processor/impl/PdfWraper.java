package com.pdfcore.main.processor.impl;

import java.io.IOException;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.pdfcore.main.exceptions.ConfigInitException;

public class PdfWraper {
	private AcroFields acroFields;
	private int currentRecordIndex;
	private int numberOfRecordPerPage;
	private int currentPageIndex;
	private int numberOfPage;
	private boolean autoIncreaseRecordIndex;
	private boolean autoIncreasePageIndex;

	public PdfWraper(PdfReader reader, int numberOfRecordPerPage, boolean autoIncreaseRecordIndex, int numberOfPage, boolean autoIncreasePageIndex)
			throws ConfigInitException, IOException {
		acroFields = reader.getAcroFields();
		this.numberOfRecordPerPage = numberOfRecordPerPage;
		this.autoIncreaseRecordIndex = autoIncreaseRecordIndex;
		this.numberOfPage = numberOfPage;
		this.autoIncreasePageIndex = autoIncreasePageIndex;
		currentRecordIndex = 0;
		currentPageIndex = 0;
		acroFields = reader.getAcroFields();
	}

	public boolean next() {
		if (currentPageIndex < numberOfPage ) {
			if (currentRecordIndex < numberOfRecordPerPage) {
				currentRecordIndex++;
				return true;
			} else {
				currentPageIndex++;
				currentRecordIndex = 0;
				return next();
			}
			
		}
		return false;
	}

	public String getValueForColumn(String columnName) {
		columnName = autoIncreasePageIndex ? columnName.concat("_").concat(String.valueOf(currentPageIndex)) : columnName;
		columnName = autoIncreaseRecordIndex ? columnName.concat("_").concat(String.valueOf(currentRecordIndex)) : columnName;
		return acroFields.getField(columnName);
	}

}
