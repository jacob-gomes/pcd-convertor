package com.pdfcore.main.providers.writer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Jpeg;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.DataReaderException;
import com.pdfcore.main.exceptions.DataWriterException;
import com.pdfcore.main.exceptions.FormMappingException;
import com.pdfcore.main.interfaces.DataProvider;
import com.pdfcore.main.interfaces.WriterProvider;
import com.pdfcore.main.model.ColIndexMapping;
import com.pdfcore.main.model.ConstructedMapping;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.model.GenResult;
import com.pdfcore.main.model.ImageMapping;
import com.pdfcore.main.model.Mapping;
import com.pdfcore.main.model.NormalMapping;
import com.pdfcore.main.model.PreSufMapping;
import com.pdfcore.main.processor.enums.CompleteFlagEnum;
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;
import com.pdfcore.main.service.util.PropertyUtils;

public class PdfTemplateWriterProvider extends WriterProvider {
	private static final Logger log = Logger
			.getLogger(PdfTemplateWriterProvider.class);

	private final int numberOfRecordsPerPage;
	private final int numberOfPage;
	private final int numberOfReplicateRecord;
	private String writeTo;
	private File file;
	private final PcdJobConf pcdJobConf;
	private int outputFileIndex = 0;
	private Date beginTime;
	private DataWriterException dataWriteException;
	private static final int threadCount = 8;
	private Semaphore semaphore = new Semaphore(threadCount);
	byte[] contentOfTemplateFile;

	// private File template = null;

	public PdfTemplateWriterProvider(PcdJobConf pcdJobConf, String pk) {
		this(pcdJobConf, pk, 1, 1, 1);
	}

	public PdfTemplateWriterProvider(PcdJobConf pcdJobConf, String pk,
			int norpp, int numberOfPage, int numberOfReplicateRecord) {
		super(pk);
		this.pcdJobConf = pcdJobConf;
		numberOfRecordsPerPage = norpp;
		this.numberOfPage = numberOfPage;
		this.numberOfReplicateRecord = numberOfReplicateRecord;
	}

	@Override
	public ArrayList<GenResult> write(DataProvider dp, FormMapping fm)
			throws DataWriterException, DataReaderException {
		System.out.println("PdfWriter - write - enter");

		beginTime = new Date();
		checkParams(dp, fm);

		if (pcdJobConf.isLoadTemplateFileIntoMemory()) {
			try {
				contentOfTemplateFile = FileUtils.readFileToByteArray(file);
			} catch (IOException e1) {
				throw new DataWriterException(
						"Read template file into memeory failed: "
								+ e1.getMessage());
			}
		}

		ArrayList<GenResult> result = new ArrayList<GenResult>();

		int page = numberOfPage;
		int records = numberOfRecordsPerPage;

		// Create a thread pool to run the stamper tasks.
		ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);

		// Create stamper tasks, each task will create and fill a stamper.
		// List<StamperTask> stamperTasks = new LinkedList<StamperTask>();
		StamperTask stamperTask = null;
		while (dp.hasNext()) {
			// Create a new stamper if needed.
			GenResult recordGenResult = new GenResult();
			if (records == this.numberOfRecordsPerPage && numberOfPage == 1) {
				// Put a ready stamper task to thread pool to run.
				if (stamperTask != null) {
					try {
						semaphore.acquire();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					threadPool.execute(stamperTask);
					stamperTask = null;
				}

				// Create a new stamper task.
				stamperTask = createNewStamper(dp, fm, recordGenResult);
				stamperTask.setFieldCount(fm.getMappingsNumber());
				stamperTask.setPageNo(page);
			}

			// Get PK of the record.
			LogHandler
					.logDebug("Data source has more records. Getting primary key for next record");
			String pkValue;
			try {
				pkValue = dp.getValueForPk();
			} catch (DataReaderException e) {
				LogHandler
						.logError("Unable to get primary key for record. Errors bellow. Continue at next record");
				LogHandler.logError(e);
				continue;
			}
			recordGenResult.setId(pkValue);

			// Prepare field values for insert.
			int mappingsNumber = fm.getMappingsNumber();
			Object[][] fieldValues = new Object[mappingsNumber][2];
			boolean hasError = prepareFieldValues(dp, fm, result,
					recordGenResult, mappingsNumber, fieldValues);
			if (hasError) {
				continue;
			}
			stamperTask.addRecord(fieldValues, recordGenResult);

			result.add(recordGenResult);
			records--;
			if (records == 0) {
				records = numberOfRecordsPerPage;
				page--;
			}
		}

		// Put the last stamper task to thread pool to run. (the other tasks
		// have already been put into thread pool)
		if (stamperTask != null) {
			try {
				semaphore.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			threadPool.execute(stamperTask);
		}

		// Shut down the thread pool and wait until all tasks were executed.
		threadPool.shutdown();
		while (!threadPool.isTerminated()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Check if there is any exception generated by stamper tasks.
		if (dataWriteException != null) {
			throw dataWriteException;
		}

		return result;
	}

	private void checkParams(DataProvider dp, FormMapping fm)
			throws DataWriterException {
		if (dp == null) {
			throw new DataWriterException("Null Data provider provided.");
		}
		if (fm == null) {
			throw new DataWriterException("Null Form mapping provided.");
		}
		writeTo = fm.getWriteToName();
		ArrayList<String> fmErrors = fm.getErrors();
		if (fmErrors != null) {
			LogHandler
					.logError("Form Mapping provided is invalid. It failed validation with these erorr(s):");
			for (int i = 0; i < fmErrors.size(); i++) {
				LogHandler.logError(fmErrors.get(i));
			}
			throw new DataWriterException(
					"Form Mapping failed validation. Stoped write operation");
		}
		String fileName = pcdJobConf.getInputFolder() + File.separator
				+ pcdJobConf.getPdfTemplateFolder() + File.separator + writeTo
				+ pcdJobConf.getPdfExtension();
		LogHandler.logDebug("Using the following pdf template: " + fileName);

		try {
			file = PropertyUtils.getFile(pcdJobConf, fileName);
		} catch (ConfigInitException e) {
			throw new DataWriterException("Unable to get pdf template: "
					+ fileName + ". See cause for details.", e);
		}
		try {
			// just to check
			new PdfReader(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new DataWriterException("Unable to get pdf template: "
					+ fileName + ". See cause for details.", e);
		} catch (IOException e) {
			throw new DataWriterException("Unable to get pdf template: "
					+ fileName + ". See cause for details.", e);
		}

		LogHandler.logDebug("Pdf template read succesfull");
	}

	private boolean prepareFieldValues(DataProvider dp, FormMapping fm,
			ArrayList<GenResult> result, GenResult genResult,
			int mappingsNumber, Object[][] objectValues) {
		boolean errors = false;
		for (int i = 0; i < mappingsNumber && errors == false; i++) {
			Mapping mapping = fm.getMapping(i);
			try {
				objectValues[i][0] = mapping.getWriterField();
				objectValues[i][1] = getValueForMapping(dp, mapping);
			} catch (DataReaderException e) {
				DataWriterException error = new DataWriterException(
						"Unable to get mapping value for :"
								+ mapping.getWriterField()
								+ ". Skipping to next record", e);
				LogHandler.logError(error);
				genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
				genResult.setError(error);
				result.add(genResult);
				errors = true;
			} catch (FormMappingException e) {
				DataWriterException error = new DataWriterException(
						"Unable to get mapping value for :"
								+ mapping.getWriterField()
								+ ". Skipping to next record", e);
				LogHandler.logError(error);
				genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
				genResult.setError(error);
				result.add(genResult);
				errors = true;
			}
		}
		return errors;
	}

	private void processAField(PdfStamper stamper, int page, int records,
			String fieldName, Object fieldValue) {
		if (numberOfPage > 1) {
			fieldName = fieldName + "_" + (numberOfPage - page + 1);
		}

		if (numberOfRecordsPerPage > 1) {
			fieldName = fieldName + "_"
					+ (numberOfRecordsPerPage - records + 1);
		}
		if (fieldValue instanceof File) {
			File image = (File) fieldValue;
			float[] fieldPositions = stamper.getAcroFields().getFieldPositions(
					fieldName);

			Jpeg jpeg;
			try {
				jpeg = new Jpeg(image.toURI().toURL());
			} catch (BadElementException e) {
				LogHandler.logError(new DataWriterException(
						"Unable to get Image: " + image.getAbsolutePath()
								+ "Skipping it.", e));
				return;
			} catch (MalformedURLException e) {
				LogHandler.logError(new DataWriterException(
						"Unable to get Image: " + image.getAbsolutePath()
								+ "Skipping it.", e));
				return;
			} catch (IOException e) {
				LogHandler.logError(new DataWriterException(
						"Unable to get Image: " + image.getAbsolutePath()
								+ "Skipping it.", e));
				return;
			}
			try {
				stamper.getOverContent((int) fieldPositions[0]).addImage(jpeg,
						jpeg.getWidth(), 0, 0, jpeg.getHeight(),
						fieldPositions[1], fieldPositions[2], true);
			} catch (DocumentException e) {
				LogHandler.logError(new DataWriterException(
						"Unable to insert Image: " + image.getAbsolutePath()
								+ "into pdf. Skipping it.", e));
				return;
			}

		} else {
			String fieldValueStr = (String) fieldValue;
			try {
				if (numberOfReplicateRecord > 1) {

					for (int j = 1; j <= numberOfReplicateRecord; j++) {
						stamper.getAcroFields().setField(fieldName + "_" + j,
								fieldValueStr);
						LogHandler.logDebug("write to field" + fieldName + "_"
								+ j + ", value = " + fieldValueStr);
					}
				} else {
					stamper.getAcroFields().setField(fieldName, fieldValueStr);
					LogHandler.logDebug("write to field" + fieldName
							+ ", value = " + fieldValueStr);
				}

			} catch (IOException e) {
				LogHandler.logError(new DataWriterException(
						"Unable to insert value: " + fieldValueStr
								+ "into field " + fieldName + " .", e));
				return;
			} catch (DocumentException e) {
				LogHandler.logError(new DataWriterException(
						"Unable to insert value: " + fieldValueStr
								+ "into field " + fieldName + " .", e));
				return;
			}
		}
	}

	private StamperTask createNewStamper(DataProvider dp, FormMapping fm,
			GenResult genResult) throws DataWriterException {

		// create new pdf file
		StamperTask stamperTask;
		try {
			stamperTask = newStamper(dp, fm, genResult, pcdJobConf.nextIndex()); // RICH:
																					// TODO
		} catch (FileNotFoundException e) {
			throw new DataWriterException(
					"Unable to create new pdf stamper. See cause for details.",
					e);
		} catch (ConfigInitException e) {
			throw new DataWriterException(
					"Unable to create new pdf stamper. See cause for details.",
					e);
		} catch (IOException e) {
			throw new DataWriterException(
					"Unable to create new pdf stamper. See cause for details.",
					e);
		} catch (DocumentException e) {
			throw new DataWriterException(
					"Unable to create new pdf stamper. See cause for details.",
					e);
		}
		return stamperTask;
	}

	public void removeWatermark(PdfReader reader, PdfStamper stamper) {

		try {

			PdfDictionary root = reader.getCatalog();
			root.remove(PdfName.OCPROPERTIES);
			PdfDictionary page;
			PdfArray contentarray;
			PRStream stream;
			String content;
			PdfDictionary resources;
			PdfDictionary xobjects;
			for (int i = 0; i < reader.getNumberOfPages(); i++) {
				page = reader.getPageN(1);
				contentarray = page.getAsArray(PdfName.CONTENTS);
				if (contentarray != null) {
					for (int j = 0; j < contentarray.size(); j++) {
						stream = (PRStream) contentarray.getAsStream(j);
						content = new String(PdfReader.getStreamBytes(stream));
						if (content.indexOf("/OC") > 0) {
							stream.put(PdfName.LENGTH, new PdfNumber(0));
							stream.setData(new byte[0]);
						}
					}
				}
				resources = page.getAsDict(PdfName.RESOURCES);
				xobjects = resources.getAsDict(PdfName.XOBJECT);
				for (Object name : xobjects.getKeys()) {
					stream = (PRStream) xobjects.getAsStream((PdfName) name);
					if (stream.get(PdfName.OC) == null) {
						continue;
					}
					stream.put(PdfName.LENGTH, new PdfNumber(0));
					stream.setData(new byte[0]);
				}
			}

			stamper.setViewerPreferences(PdfWriter.PageModeUseNone);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void closeStamper(PdfStamper stamper) throws DocumentException,
			IOException {

		if (stamper != null) {

			stamper.setFormFlattening(true);
			stamper.close();
			stamper = null;
		}
	}

	private StamperTask newStamper(DataProvider dp, FormMapping fm,
			GenResult genResult, int idx) throws ConfigInitException,
			FileNotFoundException, IOException, DocumentException {

		String path, fileName;
		path = fileName = pcdJobConf.getConfPath() + File.separator
				+ pcdJobConf.getOutputFolder() + File.separator
				+ pcdJobConf.getPdfFolder();

		if (StringUtils.isNotEmpty(fm.getOutputFolderPattern())) {
			String outputPattern = fm.getOutputFolderPattern();
			String[] pp = outputPattern.split("/");
			for (String p : pp) {
				NormalMapping nm = new NormalMapping(p, p);
				try {
					String folder = dp.getValueForMapping(nm);
					if (StringUtils.isNotEmpty(folder)) {
						fileName = fileName + File.separator + folder;
						File ff = new File(fileName);
						if (!ff.exists()) {
							ff.mkdir();
						}
					}
				} catch (DataReaderException e) {
					e.printStackTrace();
				}
			}

		}

		if (StringUtils.isNotEmpty(fm.getOutputFileNameColumn())) {
			// else {
			String column = fm.getOutputFileNameColumn();

			NormalMapping nm = new NormalMapping(column, column);
			try {
				String columnValue = dp.getValueForMapping(nm);
				fileName = fileName + File.separator + columnValue + "_"
						+ System.currentTimeMillis() + "_"
						+ (++outputFileIndex) + pcdJobConf.getPdfExtension();

			} catch (DataReaderException e) {
				e.printStackTrace();
			}

		} else if (null != pcdJobConf.getMergedPdfFileName()
				&& !"".equals(pcdJobConf.getMergedPdfFileName())) {
			fileName = fileName + File.separator
					+ pcdJobConf.getMergedPdfFileName() + "_" + idx
					+ pcdJobConf.getPdfExtension();
		} else {
			fileName = fileName + File.separator + writeTo
					+ System.currentTimeMillis() + "_" + (++outputFileIndex)
					+ pcdJobConf.getPdfExtension();
		}

		String ffName = fileName.replace(path + File.separator, "");

		genResult.setOutputFile(ffName);
		genResult.setOutputAbsoluteFolder(path);

		long usedTime = (new Date().getTime() - beginTime.getTime());
		usedTime = usedTime == 0 ? 1 : usedTime;
		System.out.println(String.format(
				"Rich file[%s] Files per seconds[%.3f]", ffName,
				outputFileIndex * 1000.0 / usedTime));

		// }

		// File outputFile = PropertyUtils.getFile(pcdJobConf, fileName);
		File outputFile = new File(fileName);

		StamperTask stamperTask = new StamperTask(outputFile);
		// removeWatermark(template, stamper);
		return stamperTask;

	}

	private static Object getValueForMapping(DataProvider dp, Mapping mapping)
			throws DataReaderException, FormMappingException {
		switch (mapping.getType()) {

		case Image:
			return dp.getValueForMapping((ImageMapping) mapping);
		case ColumnIndex:
			return dp.getValueForMapping((ColIndexMapping) mapping);
		case Constructed:
			return dp.getValueForMapping((ConstructedMapping) mapping);
		case Normal:
			return dp.getValueForMapping((NormalMapping) mapping);
		case PreSufMapping:
			return dp.getValueForMapping((PreSufMapping) mapping);
		default:
			throw new FormMappingException("Unknown mapping provided");
		}
	}

	private class StamperTask implements Runnable {
		private int fieldCount;
		private File outputFile;
		private int pageIndex;

		public void setPageNo(int pageNo) {
			this.pageIndex = pageNo;
		}

		public void setFieldCount(int fieldCount) {
			this.fieldCount = fieldCount;
		}

		private List<Object[][]> records = new LinkedList<Object[][]>();
		private List<GenResult> genResultsOfRecords = new LinkedList<GenResult>();

		public void addRecord(Object[][] record, GenResult recordGenResult) {
			records.add(record);
			genResultsOfRecords.add(recordGenResult);
		}

		public StamperTask(File outputFile) {
			this.outputFile = outputFile;
		}

		@Override
		public void run() {
			semaphore.release();

			// Create a new stamper.
			PdfStamper stamper;
			PdfReader templateReader;
			try {
				// Create reader for template file.
				long start1 = System.currentTimeMillis();
				if (pcdJobConf.isLoadTemplateFileIntoMemory()) {
					templateReader = new PdfReader(new ByteArrayInputStream(
							contentOfTemplateFile));
				} else {
					templateReader = new PdfReader(new FileInputStream(file));
				}
				long end1 = System.currentTimeMillis();

				long start2 = System.currentTimeMillis();
				FileOutputStream fileOutputStream = new FileOutputStream(
						outputFile);
				long end2 = System.currentTimeMillis();
				log.debug(String
						.format("thread[%d]: create PDF reader use [%d] ms, create file use [%d] ms, maxMem[%d] totalMem[%d] freeMem[%d]",
								Thread.currentThread().getId(), end1 - start1,
								end2 - start2,
								Runtime.getRuntime().maxMemory(), Runtime
										.getRuntime().totalMemory(), Runtime
										.getRuntime().freeMemory()));
				stamper = new PdfStamper(templateReader, fileOutputStream);
			} catch (Exception e) {
				dataWriteException = new DataWriterException(
						"Unable to create pdf stamper. See cause for details.",
						e);
				return;
			}

			// Insert records to stamper.
			int recordIndex = numberOfRecordsPerPage;
			for (int i = 0; i < records.size(); i++) {
				// Insert the record(field values) to PDF.
				Object[][] fieldValues = records.get(i);
				for (int j = 0; j < fieldCount; j++) {
					String fieldName = (String) fieldValues[j][0];
					Object fieldValue = fieldValues[j][1];
					processAField(stamper, pageIndex, recordIndex, fieldName,
							fieldValue);
				}

				GenResult recordGenResult = genResultsOfRecords.get(i);
				recordGenResult.setCompletFlag(CompleteFlagEnum.COMPLETED);
				recordGenResult.setGenerateForm(outputFile.getAbsolutePath());

				recordIndex--;
			}

			try {
				templateReader.close();
				closeStamper(stamper);

			} catch (Exception e) {
				dataWriteException = new DataWriterException(
						"Unable to close pdf stamper. See cause for details.",
						e);
				return;
			}

		}
	}

}
