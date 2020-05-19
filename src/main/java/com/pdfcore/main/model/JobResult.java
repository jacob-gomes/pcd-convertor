package com.pdfcore.main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Rick Chen
 * Date: 2/16/13
 * Time: 4:14 PM
 */
public class JobResult {

    private ArrayList<GenResult> allResults = new ArrayList<GenResult>();
    private String mergedFileName;


    public JobResult(ArrayList<GenResult> result) {
        if (null != result) {
            allResults.addAll(result);
        }
    }

    public String getOutputAbsolutePath() {
        if(allResults.size() == 0) {
            return null;
        }
        return allResults.get(0).getOutputAbsoluteFolder();
    }

    public List<String> getOutputFileNames() {
        List<String> names = new ArrayList<String>();
        if(allResults.size() == 0 ) {
            return names;
        }

        for(GenResult gr: allResults) {
            if (null != gr.getOutputFile()) {
                names.add(gr.getOutputFile());
            }
        }
        return names;
    }

    public void setMergedFileName(String mergedFileName) {
        this.mergedFileName = mergedFileName;
    }

    public String getMergedFileName() {
        return mergedFileName;
    }
}
