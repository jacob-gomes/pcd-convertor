package com.pdfcore.main.processor.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.pdfcore.main.exceptions.AccroFieldExtractorException;
import com.pdfcore.main.processor.AbstractExtractPCDProcessor;

@Component
public class AccroFieldExtractor extends AbstractExtractPCDProcessor{

	private static Logger LOG = Logger.getLogger(AccroFieldExtractor.class.getName());

	@Override
	protected void executeExtractProcess(File inputFile, File outputFile) throws Exception {
		extract(inputFile, outputFile);
	}
	
	/**
	 * Extract accrofields from pdf form.
	 * @param input: pdf file
	 * @param output: file with csv format.
	 * @throws AccroFieldExtractorException
	 */
	public static void extract(File input, File output) throws AccroFieldExtractorException {
		if (input == null || output == null) {
			throw new AccroFieldExtractorException("Input and output can not be null");
		}

		if (!input.exists()) {
			throw new AccroFieldExtractorException("Input does not exist");
		}

		LOG.info("Begin extraction");
		FileWriter out = null;
		try {
			out = new FileWriter(output);
			PdfReader reader = new PdfReader(new FileInputStream(input));
			AcroFields acroFields = reader.getAcroFields();
			Integer size = acroFields.getFields().size();
			for (Integer j = 0; j < size - 1; ++j) {
				LOG.debug(("field " + (j + 1)).concat(acroFields.getFields().keySet().toArray()[j].toString()));
				// out.write("'" + acroFields.getFields().keySet().toArray()[j] + "',");
				out.write(acroFields.getFields().keySet().toArray()[j] + ",");
			}
			out.write(acroFields.getFields().keySet().toArray()[size - 1].toString());
			out.flush();
			out.close();
		} catch (IOException e) {
			LOG.error(e.getMessage());
			throw new AccroFieldExtractorException("Can not write output file");
		}
		LOG.info("End extraction");

	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

		try {

			Integer inputDocumentsCount = args.length;
			String mappingFile;

			for (Integer i = 0; i < inputDocumentsCount; ++i) {

				mappingFile = "accroFields." + args[i] + ".csv";
				PdfReader reader = new PdfReader("input/" + args[i]);

				BufferedWriter out = new BufferedWriter(new FileWriter(mappingFile));

				AcroFields acroFields = reader.getAcroFields();
				Integer size = acroFields.getFields().size();
				if (size < 1)
					continue;

				for (Integer j = 0; j < size - 1; ++j) {

					out.write("'" + acroFields.getFields().keySet().toArray()[j] + "',");
				}

				out.write("'" + acroFields.getFields().keySet().toArray()[size - 1] + "'");

				out.close();
			}

		} catch (Exception e) {
			System.out.println(e.toString());
			LOG.info(e.toString());
			return;
		}
		LOG.info("Successful converstions.");
	}

	

}
