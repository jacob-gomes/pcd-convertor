package com.pdfcore.main.providers.fieldvalue;

import java.text.SimpleDateFormat;
import java.util.Date;


import com.pdfcore.main.interfaces.FieldValueConstructor;
import com.pdfcore.main.processor.impl.NumWord;

public class CheckFieldValueConstructor implements FieldValueConstructor
{

	@Override
    public String getValueForField(String writeToName, String string, String numWordCapital) {
        //crw: extract "amount_words" to a constant
        if (writeToName.equals("amount_words")) {
            return new NumWord().convertAmountToText(string, numWordCapital).trim();
        }
        //crw: extract "today_date" to a constant
        if (writeToName.equals("today_date")) {
            return new SimpleDateFormat("MMM dd, yyyy").format(new Date());
        }
        return "";
	}

}
