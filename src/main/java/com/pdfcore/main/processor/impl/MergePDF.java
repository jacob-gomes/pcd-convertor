package com.pdfcore.main.processor.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * @author Dilshan
 *
 */
public class MergePDF {
	
	/**
	 * Concatenate all pdf files
	 * @param streamOfPDFFiles pdf files to merge
	 * @param outputStream output stream to write
	 * @param true if the pagination is needed
	 * @param pageSize page size to print
	 * */
	public static void  concatPDFs(List<InputStream> streamOfPDFFiles,	OutputStream outputStream, boolean paginate, Rectangle pageSize) {

		Document document = new Document();
		
		try {
			List<InputStream> pdfs = streamOfPDFFiles;
			List<PdfReader> readers = new ArrayList<PdfReader>();
			int totalPages = 0;
			Iterator<InputStream> iteratorPDFs = pdfs.iterator();
	
			// Create Readers for the pdfs.
			while (iteratorPDFs.hasNext()) {
				InputStream pdf = iteratorPDFs.next();
				PdfReader pdfReader = new PdfReader(pdf);
				readers.add(pdfReader);
				totalPages += pdfReader.getNumberOfPages();
			}
			// Create a writer for the outputstream
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.setPageSize(pageSize);
			document.open();
			BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN,
		    BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
	        PdfContentByte pdfContentByte = writer.getDirectContent(); // Holds the PDF
		
	        PdfImportedPage page;
	        int currentPageNumber = 0;
	        int pageOfCurrentReaderPDF = 0;
	        Iterator<PdfReader> iteratorPDFReader = readers.iterator();
	 
	    	// Loop through the PDF files and add to the output.
	        while (iteratorPDFReader.hasNext()) {
	            PdfReader pdfReader = iteratorPDFReader.next();
		 
	            // Create a new page in the target for each source page.
	            while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
	                document.newPage();
	                pageOfCurrentReaderPDF++;
	                currentPageNumber++;
	                page = writer.getImportedPage(pdfReader,
	                    pageOfCurrentReaderPDF);
	                pdfContentByte.addTemplate(page, 0, 0);
	 
	                // Code for pagination.
	                if (paginate) {
	                	pdfContentByte.beginText();
	                	pdfContentByte.setFontAndSize(bf, 9);
	                	pdfContentByte.showTextAligned(PdfContentByte.ALIGN_CENTER, ""
	                    		+ currentPageNumber + " of " + totalPages, 520,
	                                5, 0);
	                	pdfContentByte.endText();
	                }
	            }
	            pageOfCurrentReaderPDF = 0;
	        }
	            
	        outputStream.flush();
	        document.close();
	        outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen())
                document.close();
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        for (int i = 0 ; i <streamOfPDFFiles.size() ; i++)
        {
        	try {
				streamOfPDFFiles.get(i).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        
	}
	
	
	
	public static void  concatPDF(List<String> streamOfPDFFiles,	OutputStream outputStream, boolean paginate, Rectangle pageSize) {

		Document document = new Document();
		
		try {
			int totalPages = 0;

			// Create Readers for the pdfs.
			for (String data : streamOfPDFFiles) {
                PdfReader reader = new PdfReader(data);
				totalPages += reader.getNumberOfPages();
				reader.close();
			}

			// Create a writer for the outputstream
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			document.setPageSize(pageSize);
			document.open();
			BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN,
		    BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
	        PdfContentByte pdfContentByte = writer.getDirectContent(); // Holds the PDF
		

	        int currentPageNumber = 0;
	        int pageOfCurrentReaderPDF = 0;

            for (String data : streamOfPDFFiles) {
                PdfReader pdfReader = new PdfReader(data);

                // Create a new page in the target for each source page.
                while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
                    document.newPage();
                    pageOfCurrentReaderPDF++;
                    currentPageNumber++;

                    PdfImportedPage page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
                    pdfContentByte.addTemplate(page, 0, 0);

                    // Code for pagination.
                    if (paginate) {
                        pdfContentByte.beginText();
                        pdfContentByte.setFontAndSize(bf, 9);
                        pdfContentByte.showTextAligned(PdfContentByte.ALIGN_CENTER, ""
                                + currentPageNumber + " of " + totalPages, 520,
                                5, 0);
                        pdfContentByte.endText();
                    }
                }
                pageOfCurrentReaderPDF = 0;
                pdfReader.close();
                writer.freeReader(pdfReader);
            }

            outputStream.flush();
	        document.close();
	        outputStream.close();
	        writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
            if (document.isOpen())
                document.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
   }

    public static void concatPDFs(ArrayList<File> files, FileOutputStream outputStream, boolean paginate, Rectangle pageSize) {
        List<String> filePaths = new ArrayList<String>();
        for (int i = 0; i < files.size(); i++) {
            filePaths.add(files.get(i).getAbsolutePath());
        }
        concatPDF(filePaths, outputStream, paginate, pageSize);

    }

    public static void main(String[] args) throws FileNotFoundException {
		List<InputStream> files = new ArrayList<InputStream>();
		files.add(new FileInputStream("D:\\Louis\\Projects\\pdf\\svn\\TaxPrinting\\src\\pdf\\CheckPrinting.pdf"));
		files.add(new FileInputStream("D:\\Louis\\Projects\\pdf\\svn\\TaxPrinting\\src\\pdf\\CheckPrinting.pdf"));
		OutputStream outputStream = new FileOutputStream(new File("D:\\Louis\\Projects\\pdf\\svn\\TaxPrinting\\src\\pdf\\CheckPrinting1.pdf"));
		boolean paginate = true;
		Rectangle pageSize = PageSize.A4;
		concatPDFs(files, outputStream, paginate, pageSize);
	}
}
