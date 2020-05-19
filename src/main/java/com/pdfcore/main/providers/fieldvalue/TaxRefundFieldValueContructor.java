package com.pdfcore.main.providers.fieldvalue;

import com.pdfcore.main.interfaces.FieldValueConstructor;
import com.pdfcore.main.processor.impl.NumWord;

//crw: you have a typo in class name
public class TaxRefundFieldValueContructor implements FieldValueConstructor
{

	@Override
	public String getValueForField(String writeToName, String string, String numWordCapital)
	{
        //crw: extract "amount_words" to a constant, you also use it in CheckFieldValueConstructor
		if (writeToName.equalsIgnoreCase("amount_words"))
			return new NumWord().convertAmountToText(string, numWordCapital).trim();
		return "";
	}

}
