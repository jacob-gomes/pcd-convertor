package com.pdfcore.main.processor.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

public class NumWord {

	
	public String convertAmountToText(String amount, String capital){
		String xx =  NumberToWords.convertAmountToText(amount);

        if (StringUtils.isBlank(capital) || "all".equalsIgnoreCase(capital)) {
            xx = xx.toUpperCase();
        }
        else if ("Camel".equalsIgnoreCase(capital)) {
            String[] splitted = xx.split(" ");

            String[] newStrArr = new String[splitted.length];
            int i = 0;
            for (String ss : splitted) {
                newStrArr[i++] = WordUtils.capitalize(ss);
            }

            xx = StringUtils.join(newStrArr, " ");
        }
        else {
            xx = xx.toLowerCase();
        }
//
//        String[] splitted = xx.split("-");
//
//        String[] newStrArr = new String[splitted.length];
//        int i=0;
//        for(String ss: splitted) {
//            newStrArr[i++] = WordUtils.capitalize(ss);
//        }
//
//        xx = StringUtils.join(newStrArr, "-");
//        //xx = xx.toLowerCase();

        return xx;
	}




    public static void main(String[] args) {
        System.out.println("865.00, " + (new NumWord()).convertAmountToText("865.00", "all"));
        System.out.println("9345865.10, " + (new NumWord()).convertAmountToText("9345865.00", "none"));
        System.out.println("9345865.10, " + (new NumWord()).convertAmountToText("9345865.00", "camel"));
        System.out.println("10.00, " + (new NumWord()).convertAmountToText("10.00", "camel"));
        System.out.println("50, " + (new NumWord()).convertAmountToText("50", null));
    }
} 
