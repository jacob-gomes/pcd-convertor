package com.pdfcore.main.processor.enums;

import com.lowagie.text.Rectangle;

public enum PageSize {
	LETTER(com.lowagie.text.PageSize.LETTER), LEGAL(com.lowagie.text.PageSize.LEGAL),A4(com.lowagie.text.PageSize.A4);
	private final Rectangle size;
	PageSize(Rectangle size)
	{
		this.size = size;
	}
	public Rectangle getSize()
	{
		return size;
	}
}
