package com.pdfcore.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.pdfcore.main.service.PCDConvertorService;


@SpringBootApplication
public class MainAppEntryPoint  implements ApplicationRunner {
	
	@Autowired
	private PCDConvertorService pcdConvertorService;
	
	/**
	 * @param args
	 *            Legal arguments are : 1)csvtodbdataload - loads data from
	 *            DATA_FEED.csv to db 2)bulkgenerate - generates pdf's from
	 *            CHECK_PRINTING.CSV 3)webbulkgenerate - generates pdf's from
	 *            web If no arguments are provided that consoleline invocation
	 *            is assumed.
	 */
	public static void main( String[] args ){
		SpringApplication.run(MainAppEntryPoint.class, args);
    }
	
	@Override
	public void run(ApplicationArguments args) {
		pcdConvertorService.executePCDConvertor(args.getSourceArgs());
	}
	
	
}
