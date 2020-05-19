package com.pdfcore.main.processor.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.pdfcore.main.constants.Constants;
import com.pdfcore.main.exceptions.AccroFieldExtractorException;
import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.DataReaderException;
import com.pdfcore.main.exceptions.DataWriterException;
import com.pdfcore.main.exceptions.FormMappingException;
import com.pdfcore.main.interfaces.DataProvider;
import com.pdfcore.main.interfaces.FieldValueConstructor;
import com.pdfcore.main.interfaces.FormMappingProvider;
import com.pdfcore.main.interfaces.WriterProvider;
import com.pdfcore.main.model.BinaryStream;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.model.GenResult;
import com.pdfcore.main.model.JobResult;
import com.pdfcore.main.processor.AbstractMergePCDProcessor;
import com.pdfcore.main.processor.enums.CompleteFlagEnum;
import com.pdfcore.main.processor.enums.MergePdfConfig;
import com.pdfcore.main.processor.enums.OutputDestination;
import com.pdfcore.main.processor.enums.OutputLevel;
import com.pdfcore.main.processor.enums.PageSize;
import com.pdfcore.main.processor.enums.ReadFromEnum;
import com.pdfcore.main.processor.enums.WriteToEnum;
import com.pdfcore.main.providers.data.CSVDataProvider;
import com.pdfcore.main.providers.data.DBDataProvider;
import com.pdfcore.main.providers.data.PDFDataProvider;
import com.pdfcore.main.providers.formmapping.FormMappingProviderFactory;
import com.pdfcore.main.providers.writer.CsvWriterProvider;
import com.pdfcore.main.providers.writer.DBWriterProvider;
import com.pdfcore.main.providers.writer.PdfTemplateWriterProvider;
import com.pdfcore.main.service.impl.PCDConvertorServiceImpl;
import com.pdfcore.main.service.util.DbUtil;
import com.pdfcore.main.service.util.PCDConvertorUtil;
import com.pdfcore.main.service.util.PropertyUtils;

@Component
public class JobBatch extends AbstractMergePCDProcessor{
	
	private static final Logger logger = Logger
			.getLogger(JobBatch.class);
    
	private PcdJobConf pcdJobConf;
    private String job;

    private DbUtil dbUtil;

    /**
     * Default constructor.
     */
    public JobBatch() {}
    
    public JobBatch(PcdJobConf cf) {
        pcdJobConf = cf;
        setJob(cf.getJobFileName());
        //dbUtil = DbUtil.getInstance(cf);
    }


    private DbUtil getDbUtil() throws ConfigInitException {
        if (null == dbUtil) {
            dbUtil = DbUtil.getInstance(pcdJobConf);
        }
        return dbUtil;
    }
    
    @Override
    protected void executeMergeProcess() throws Exception {
    	pcdJobConf = PCDConvertorUtil.generatePcdJobConf(super.getProcessingInstructionModel());
    	setJob(pcdJobConf.getJobFileName());
    	try {
    		this.runJob();
    	} catch (ConfigInitException | DataReaderException | DataWriterException | SQLException e) {
    		logger.error("Exeception occured during executing batch Job", e);
    		throw new Exception(e);
    	}
    }
    	
    /**
     * Build the the configuration object from configuration file
     *
     * @param configFile
     * @return
     * @throws com.pdfcore.main.exceptions.ConfigInitException
     *
     */
    public BatchConfigFromFile constructBatchConfigFromProperties(String configFile) throws ConfigInitException {
        BatchConfigFromFile runBatchConfig = BatchConfigFromFile.getInstance();
        // get the properties file for this Job Batch
        Properties configRun = PropertyUtils.getProperties(configFile);

        // Find generation source and destination
        if (configRun.containsKey("generate.from")) {
            String generateFrom = configRun.getProperty("generate.from").trim();
            if (generateFrom.equalsIgnoreCase("csv")) {
                runBatchConfig.setReadFrom(ReadFromEnum.CSV);
            } else {
                if (generateFrom.equalsIgnoreCase("db")) {
                    runBatchConfig.setReadFrom(ReadFromEnum.DB);
                } else {
                    if (generateFrom.equalsIgnoreCase("pdf")) {
                        runBatchConfig.setReadFrom(ReadFromEnum.PDF);
                    } else {
                        throw new ConfigInitException(
                                "Invalid generate.from was found. Valid values are csv or db or pdf");
                    }
                }
            }
        } else {
            throw new ConfigInitException("No generate.from key was found.");
        }
        if (configRun.containsKey("generate.to")) {
            String generateTo = configRun.getProperty("generate.to").trim();
            if (generateTo.equalsIgnoreCase("csv")) {
                runBatchConfig.setWriteTo(WriteToEnum.CSV);
            } else {
                if (generateTo.equalsIgnoreCase("db")) {
                    runBatchConfig.setWriteTo(WriteToEnum.DB);
                } else {
                    if (generateTo.equalsIgnoreCase("pdf")) {
                        runBatchConfig.setWriteTo(WriteToEnum.PDF);
                    } else {
                        throw new ConfigInitException("Invalid generate.to was found. Valid values are csv,db or pdf");
                    }

                }

            }
        } else {
            throw new ConfigInitException("No generate.to key was found.");
        }

        // merge generate.mergepdf = combine
        if (configRun.containsKey("generate.mergepdf")) {
            //
            String mergeValue = configRun.getProperty("generate.mergepdf").trim();
            if (mergeValue.equalsIgnoreCase("separate")) {
                runBatchConfig.setMerge(MergePdfConfig.SEPARATE);
            } else {
                if (mergeValue.equalsIgnoreCase("combine")) {
                    runBatchConfig.setMerge(MergePdfConfig.COMBINE);
                } else {
                    if (mergeValue.equalsIgnoreCase("both")) {
                        runBatchConfig.setMerge(MergePdfConfig.BOTH);
                    } else {
                        throw new ConfigInitException(
                                "Invalid generate.mergepdf was found. Valid values separate,combine or both");
                    }
                }
            }
        } else {
            throw new ConfigInitException("No generate.mergepdf key was found.");
        }

        if (null != pcdJobConf.getMergedPdfFileName() && !"".equals(pcdJobConf.getMergedPdfFileName())) {
            runBatchConfig.setMergedFileName(pcdJobConf.getMergedPdfFileName());
        } else {
            if (configRun.containsKey("generate.merged.pdf.name")) {
                runBatchConfig.setMergedFileName(configRun.getProperty("generate.merged.pdf.name"));
                pcdJobConf.setMergedPdfFileName(configRun.getProperty("generate.merged.pdf.name"));
            }
        }

        // output generate.outputdestination = file
        if (configRun.containsKey("generate.outputdestination")) {
            //
            String outputValue = configRun.getProperty("generate.outputdestination").trim();
            if (outputValue.equalsIgnoreCase("file")) {
                runBatchConfig.setOutput(OutputDestination.FILE);
            } else {
                if (outputValue.equalsIgnoreCase("db")) {
                    runBatchConfig.setOutput(OutputDestination.DB);
                } else {

                    throw new ConfigInitException(
                            "Invalid generate.outputdestination was found. Valid values file or db");

                }
            }
        } else {
            throw new ConfigInitException("No generate.outputdestination key was found.");
        }
        // generate.pk.reader = trace_no
        if (configRun.containsKey("generate.pk.reader")) {
            //
            runBatchConfig.setPkReader(configRun.getProperty("generate.pk.reader").trim());

        } else {
            throw new ConfigInitException("No generate.pk.reader key was found.");
        }



        // generate.pk.writer = trace_no
        if (configRun.containsKey("generate.pk.writer")) {
            //
            runBatchConfig.setPkWriter(configRun.getProperty("generate.pk.writer").trim());

        } else {
            throw new ConfigInitException("No generate.pk.writer key was found.");
        }
        // generate.level = transaction
        //
        if (configRun.containsKey("generate.level")) {
            String outputValue = configRun.getProperty("generate.level").trim();
            if (outputValue.equalsIgnoreCase("order")) {
                runBatchConfig.setLevel(OutputLevel.ORDER);
            } else {
                if (outputValue.equalsIgnoreCase("transaction")) {
                    runBatchConfig.setLevel(OutputLevel.TRANSACTION);
                } else {

                    throw new ConfigInitException("Invalid generate.level was found. Valid values order or transaction");

                }
            }
        } else {
            throw new ConfigInitException("No generate.level key was found.");
        }
        // Find and construct the forms you want to generate
        String property = null;
        if (configRun.containsKey("generate.formnames")) {
            property = configRun.getProperty("generate.formnames");
        } else {
            throw new ConfigInitException(
                    "The properties file you provided for this batch doesn't contain generate.formnames key");
        }
        if (property == null || property.trim().length() == 0) {
            throw new ConfigInitException(
                    "The properties file you provided for this batch doesn't contain forms to generate");

        }
        if (configRun.containsKey("application.overwriteentries")) {
            String overwrite = configRun.getProperty("application.overwriteentries");
            if (overwrite == null || overwrite.trim().length() == 0) {
                throw new ConfigInitException(
                        "The properties file you provided for this batch contains bad value for application.overwriteentries. Valid values are Yes or No");
            }
            overwrite = overwrite.trim();
            if (overwrite.equalsIgnoreCase("yes")) {
                runBatchConfig.setShouldOverwrite(true);
            } else {
                if (overwrite.equalsIgnoreCase("no")) {
                    runBatchConfig.setShouldOverwrite(false);
                } else {
                    throw new ConfigInitException(
                            "The properties file you provided for this batch contains bad value for application.overwriteentries. Valid values are Yes or No");
                }
            }

        }
        if (configRun.containsKey("application.idsToWrite")) {
            String ids = configRun.getProperty("application.idsToWrite");
            if (ids == null || ids.trim().length() == 0) {
                throw new ConfigInitException(
                        "The properties file you provided for this batch contains bad value for application.idsToWrite . If you want write for all ids in data source comment the application.idsToWrite key");
            }
            String[] idsA = ids.split(" ");
            if (idsA == null || idsA.length == 0) {
                throw new ConfigInitException(
                        "The properties file you provided for this batch contains bad value for application.idsToWrite . If you want write for all ids in data source comment the application.idsToWrite key");
            }
            for (int i = 0; i < idsA.length; i++) {
                String id = idsA[i].trim();
                if (id.length() > 0) {
                    runBatchConfig.addId(id);
                }
            }
        }

        String[] forms = property.split(" ");
        for (int i = 0; i < forms.length; i++) {

            LogHandler.logInfo("Found form " + forms[i] + ". Creating config for generation");
            ConfigFormData confData = new ConfigFormData(forms[i]);

            // Get the FormMapping

            String formConfs = "generate." + forms[i] + ".";

            // Configure formMapping from file
            if (configRun.containsKey(formConfs + "readerfromname")) {
                confData.setReaderFromName(configRun.getProperty(formConfs + "readerfromname"));
            }
            if (configRun.containsKey(formConfs + "writertoname")) {
                confData.setWriterToName(configRun.getProperty(formConfs + "writertoname"));
            }


            //genearte.pdf.numword.capital=all
            if (configRun.containsKey(formConfs + "numword.capital")) {
                confData.setNumWordCapital(configRun.getProperty(formConfs + "numword.capital"));
            }

            if (configRun.containsKey(formConfs + "output.folder.pattern")) {
                confData.setOutputFolderPattern(configRun.getProperty(formConfs + "output.folder.pattern"));
            }
            if (configRun.containsKey(formConfs + "output.filename.column")) {
                confData.setOutputFileNameColumn(configRun.getProperty(formConfs + "output.filename.column"));
            }


            // Verify if there exists a field value constructor for this form
            // If so get an instance

            if (configRun.containsKey(formConfs + "fieldconstructor")) {
                String fieldConstructor = configRun.getProperty(formConfs + "fieldconstructor");

                // try to get an instance of it by reflection
                try {
                    Class<?> fieldConstructorClass = Class.forName(fieldConstructor);
                    // get an instance
                    Object newInstance = fieldConstructorClass.newInstance();
                    // verify than it implements the FieldValueConstructor
                    // interface
                    if (newInstance instanceof FieldValueConstructor) {
                        confData.setValueConstrucor((FieldValueConstructor) newInstance);
                    } else {
                        LogHandler
                                .logError(new ConfigInitException(
                                        "The field constructor class you provided doesn't implement the FieldValueConstructor"));
                    }
                }
                catch (Exception e) {
                    LogHandler.logError(new ConfigInitException(
                            "The field constructor class you provided could not be instantiated.", e));
                    LogHandler.logInfo("Generation for form " + forms[i] + "was stopped");
                    continue;
                }

            }

            if (configRun.containsKey(formConfs + "fileType")) {
                confData.setFileMappingType(((configRun.getProperty(formConfs + "fileType"))));
            }

            if (configRun.containsKey(formConfs + "autoIncreaseFieldIndex")) {
                confData.setAutoIncreaseFieldIndex(((configRun.getProperty(formConfs + "autoIncreaseFieldIndex"))));
            }

            if (configRun.containsKey(formConfs + "autoIncreasePageIndex")) {
                confData.setAutoIncreasePageIndex(((configRun.getProperty(formConfs + "autoIncreasePageIndex"))));
            }

            if (configRun.containsKey(formConfs + "norpp")) {
                confData.setNorpp(Integer.parseInt((configRun.getProperty(formConfs + "norpp"))));
            }

            if (configRun.containsKey(formConfs + "numberOfPage")) {
                confData.setNumberOfPage(Integer.parseInt((configRun.getProperty(formConfs + "numberOfPage"))));
            }

            if (configRun.containsKey(formConfs + "numberOfReplicateRecord")) {
                confData.setNumberOfReplicateRecord(Integer.parseInt((configRun.getProperty(formConfs
                        + "numberOfReplicateRecord"))));
            }

            if (configRun.containsKey(formConfs + "pagesize")) {
                String pagesize = configRun.getProperty(formConfs + "pagesize");
                if (pagesize.equalsIgnoreCase("letter")) {
                    confData.setPageSize(PageSize.LETTER);
                } else {
                    if (pagesize.equalsIgnoreCase("A4")) {
                        confData.setPageSize(PageSize.A4);
                    } else {
                        if (pagesize.equalsIgnoreCase("LEGAL")) {
                            confData.setPageSize(PageSize.LEGAL);
                        } else {
                            LogHandler.logError(new ConfigInitException("Unknown pagesize provided."));
                            LogHandler.logInfo("Generation for form " + forms[i] + "was stopped");
                            continue;
                        }
                    }
                }
            }

            for(Object key: configRun.keySet()) {
                String wcKey = formConfs + "WHERE_COLUMN_";
                if(((String)key).startsWith(formConfs + "WHERE_COLUMN_")
                        && !((String)key).startsWith(formConfs + "WHERE_COLUMN_VALUE")) {
                    String ex = ((String) key).substring(wcKey.length());
                    String valKey = formConfs + "WHERE_COLUMN_VALUE_" + ex;
                    System.out.println("key=" + key);
                    System.out.println("valKey=" + valKey);
                    confData.addWhereCondition((configRun.getProperty((String)key)), (configRun.getProperty(valKey)));
                }
            }

            runBatchConfig.addFormToGenerate(confData);
            LogHandler.logInfo("Form succesful added to generation:" + forms[i]);

        }
        return runBatchConfig;

    }

    /**
     * Do the conversion from the configuration file
     *
     * @param pcdJobConf
     * @param runConfig
     * @throws com.pdfcore.main.exceptions.ConfigInitException
     *
     * @throws com.pdfcore.main.exceptions.DataWriterException
     *
     * @throws com.pdfcore.main.exceptions.DataReaderException
     * @throws SQLException 
     *
     */
    public List<JobResult> executeBatch(BatchConfigFromFile runConfig)
            throws ConfigInitException, DataWriterException, DataReaderException, SQLException {

        List<JobResult> jobResultList = new ArrayList<JobResult>();
        LogHandler.logInfo("New batch config recived. Starting generation");
        if (runConfig == null) {
            LogHandler.logFatal(new ConfigInitException("The Batch Config you provided is null"));
            return jobResultList;
        }
        // validate config
        LogHandler.logInfo("Starting validation of the config");
        if (runConfig.getReadFrom() == null) {
            LogHandler.logFatal(new ConfigInitException("You didn't provide a Read From value. Generation will stop"));
            return jobResultList;
        } else {
            LogHandler.logInfo("All forms will be generate from: " + runConfig.getReadFrom() + " datasource");
        }

        if (runConfig.getWriteTo() == null) {
            LogHandler.logFatal(new ConfigInitException("You didn't provide a Write To value. Generation will stop"));
            return jobResultList;
        } else {
            LogHandler.logInfo("All forms will be generate to: " + runConfig.getWriteTo() + " destination");
        }

        // Start forms generation
        LogHandler.logInfo("Starting form generations");
        ArrayList<ConfigFormData> formsToGenerate = runConfig.getFormsToGenerate();

        for (int i = 0; i < formsToGenerate.size(); i++) {
            ConfigFormData form = formsToGenerate.get(i);
            if (form == null) {
                continue;
            }
            if (form.getFormName() == null) {
                LogHandler.logError(new ConfigInitException("Form at position " + i + " has no name. Skipped"));
                continue;
            }
            FormMappingProvider propertiesFormMappingProvider;
            try {
                propertiesFormMappingProvider = createFormMappingProvider(form.getFileMappingType());
            }
            catch (FormMappingException e1) {
                LogHandler.logError(new ConfigInitException("Form at position " + i + " has incorrect type. Skipped"));
                continue;
            }

            LogHandler.logInfo("Starting generation for form : " + form.getFormName());
            FormMapping formMapping;
            try {
                formMapping = propertiesFormMappingProvider.getFormMapping(pcdJobConf, form); //RICH:TODO
            }
            catch (FormMappingException e) {
                LogHandler.logFatal(e);
                LogHandler.logInfo("Generation for form: " + form.getFormName() + " was stoped");
                continue;
            }
            DataProvider dataProvider = null;
            try {
                dataProvider = createDataProvider(pcdJobConf, runConfig, form, formMapping);

                WriterProvider writerProvider = createWriterProvider(pcdJobConf, runConfig, form);

                ArrayList<GenResult> result = writerProvider.write(dataProvider, formMapping);
                JobResult jobResult = new JobResult(result);
                //System.out.println("aaa-->" + jobResult.getOutputAbsolutePath());
                //System.out.println("aaa-->" + jobResult.getOutputFileNames());
                jobResultList.add(jobResult);

                LogHandler.logInfo("Generation of current form succesful.");
                // If output was pdf do more processing
                if (WriteToEnum.PDF.equals(runConfig.getWriteTo())) {
                    processGeneratedPdf(runConfig, form, formMapping, result, jobResult);
                }

                LogHandler.logInfo("Generation Done.");
            }
            finally {
            }

        }
        return jobResultList;

    }

    /**
     * Create different kind of writer provider. Override this method to add
     * more writer providers
     *
     * @param runConfig
     * @param form
     * @return
     * @throws com.pdfcore.main.exceptions.ConfigInitException
     *
     */
    protected WriterProvider createWriterProvider(PcdJobConf pcdJobConf, BatchConfigFromFile runConfig,
                                                  ConfigFormData form)
            throws ConfigInitException {
        WriterProvider writerProvider = null;
        switch (runConfig.getWriteTo()) {
            case DB:
                writerProvider = new DBWriterProvider(pcdJobConf, runConfig.getPkWriter(), runConfig.isShouldOverwrite());
                break;
            case CSV:
                writerProvider = new CsvWriterProvider(pcdJobConf, runConfig.getPkWriter());
                break;
            case PDF:
                writerProvider = new PdfTemplateWriterProvider(pcdJobConf, runConfig.getPkWriter(), form.getNorpp(),
                        form.getNumberOfPage(), form.getNumberOfReplicateRecord());

                break;
            default:
                throw new ConfigInitException("Invalid Writer to was provided");
        }
        return writerProvider;
    }

    /**
     * Create different kind of data provider. Override this method to add more
     * data providers
     *
     * @param runConfig
     * @param form
     * @param formMapping
     * @return
     * @throws com.pdfcore.main.exceptions.DataReaderException
     *
     * @throws com.pdfcore.main.exceptions.ConfigInitException
     * @throws SQLException 
     *
     */
    protected DataProvider createDataProvider(PcdJobConf pcdJobConf, BatchConfigFromFile runConfig,
                                              ConfigFormData form,
                                              FormMapping formMapping)
            throws DataReaderException, ConfigInitException, SQLException {
        DataProvider dataProvider = null;
        switch (runConfig.getReadFrom()) {
            case CSV:
                dataProvider = new CSVDataProvider(pcdJobConf, formMapping, form.getValueConstrucor(), runConfig.getIds(),
                        runConfig.getPkReader());
                break;
            case PDF:
                dataProvider = new PDFDataProvider(pcdJobConf, formMapping, form.getValueConstrucor(), runConfig.getIds(),
                        runConfig.getPkReader(), form.getNorpp(), form.isAutoIncreaseFieldIndex(), form.getNumberOfPage(),
                        form.isAutoIncreasePageIndex());
                break;
            case DB:

                dataProvider = new DBDataProvider(pcdJobConf, formMapping, form, runConfig.getIds(),
                        runConfig.getPkReader());
                break;

            default:
                throw new ConfigInitException("Invalid Read from was provided");

        }
        return dataProvider;
    }

    /**
     * Create different kind of form mapping providers according to form mapping
     * type such as properties, csv (row, col)...
     *
     * @param form
     * @return
     * @throws com.pdfcore.main.exceptions.FormMappingException
     *
     */
    protected FormMappingProvider createFormMappingProvider(
            String formMappingType) throws FormMappingException {
        FormMappingProvider formMappingProvider;
        formMappingProvider = createFormMappingProviderFactory().getFormMapping(formMappingType);
        return formMappingProvider;
    }

    /**
     * Actually the FormMappingProviderFactory supports to create 3 types
     * mapping files, this api allows to create a subclass of
     * FormMappingProviderFactory to add more kind of mapping type
     *
     * @return
     */
    protected FormMappingProviderFactory createFormMappingProviderFactory() {
        return FormMappingProviderFactory.getInstance();
    }

    /**
     * Merging PDF if output is pdf.
     *
     * @param runConfig
     * @param form
     * @param formMapping
     * @param result
     * @throws com.pdfcore.main.exceptions.ConfigInitException
     * @throws SQLException 
     *
     */
    protected void processGeneratedPdf(BatchConfigFromFile runConfig, ConfigFormData form,
                                       FormMapping formMapping, ArrayList<GenResult> result,
                                       JobResult jobResult) throws ConfigInitException, SQLException {
        switch (runConfig.getOutput()) {
            case FILE:
                switch (runConfig.getMerge()) {
                    case SEPARATE:
                        break;
                    case BOTH:
                        ArrayList<File> files = new ArrayList<File>();
                        for (int j = 0; j < result.size(); j++) {
                            files.add(new File(result.get(j).getGenerateForm()));
                        }

                        removeDuplicate(files);
                        System.out.println("\n\nboth");
                        String filePath = pcdJobConf.getConfPath() + File.separator + pcdJobConf.getOutputFolder() + File.separator
                                + pcdJobConf.getPdfFolder() + File.separator;
                        String fileName = formMapping.getWriteToName() + "_FINAL_"
                                + System.currentTimeMillis() + pcdJobConf.getPdfExtension();
                        jobResult.setMergedFileName(fileName);
                        File finalOutput = new File(filePath + fileName);
                        try {
                            MergePDF.concatPDFs(files, new FileOutputStream(finalOutput), true, com.lowagie.text.PageSize.LEGAL);
                            LogHandler.logInfo("Starting Merge of pdfs.");
                        }
                        catch (FileNotFoundException e) {
                            LogHandler.logError(new ConfigInitException("Merge of pdf failure", e));
                        }
                        LogHandler.logInfo("Merge of pdfs succesfull.");
                        break;
                    case COMBINE:

                        files = new ArrayList<File>();
                        for (int j = 0; j < result.size(); j++) {
                            files.add(new File(result.get(j).getGenerateForm()));
                        }

                        removeDuplicate(files);
                        System.out.println("\n\ncombine");
                        String pdfFileName = formMapping.getWriteToName() + "_FINAL_"
                                + System.currentTimeMillis();
                        System.out.println("\n\nfilename is : " + pdfFileName);
                        if (null != runConfig.getMergedFileName()) {
                            pdfFileName = runConfig.getMergedFileName();
                        }
                        finalOutput = PropertyUtils.getFile(pcdJobConf, pcdJobConf.getOutputFolder() + File.separator
                                + pcdJobConf.getPdfFolder() + File.separator + pdfFileName + pcdJobConf.getPdfExtension());
                        try {
                            MergePDF.concatPDFs(files, new FileOutputStream(finalOutput), true, com.lowagie.text.PageSize.LEGAL);
                            LogHandler.logInfo("Starting Merge of pdfs.");
                            for (int j = 0; j < files.size(); j++) {
                                files.get(j).deleteOnExit();
                            }
                        }
                        catch (FileNotFoundException e) {
                            LogHandler.logError(new ConfigInitException("Merge of pdf failure", e));
                        }
                        LogHandler.logInfo("Merge of pdfs succesfull.");
                        break;

                }
                break;
            case DB:
                LogHandler.logInfo("Updating generation result in db");
                switch (runConfig.getLevel()) {
                    case TRANSACTION:

                        for (int j = 0; j < result.size(); j++) {
                            String sqlQuery = Constants.SQL_UPDATE + " " + runConfig.getPkWriter() + "=?";
                            GenResult genResult = result.get(j);
                            BinaryStream file = null;
                            if (genResult.getGenerateForm() != null && !genResult.getGenerateForm().trim().equalsIgnoreCase("")) {
                                File fis = new File(genResult.getGenerateForm());
                                try {
                                    file = new BinaryStream(new FileInputStream(fis), (int) fis.length());
                                }
                                catch (FileNotFoundException e) {
                                    LogHandler.logError(new ConfigInitException("Could not get file: "
                                            + genResult.getGenerateForm()));
                                }

                            }

                            Object[] data = new Object[]{genResult.getCompletFlag().getId(), file == null ? null : file,
                                    genResult.getError() == null ? null : genResult.getError().getMessage(), genResult.getId()};
                            getDbUtil().executeUpdate(sqlQuery, formMapping.getWriteToName(), data);
                            LogHandler.logDebug("Updated for record with pk = " + genResult.getId());
                        }
                        break;
                    case ORDER:
                        System.out.println("\n\norder");
                        HashMap<String, ArrayList<File>> orderFiles = new HashMap<String, ArrayList<File>>();
                        HashMap<String, GenResult> failed = new HashMap<String, GenResult>();
                        for (int j = 0; j < result.size(); j++) {
                            GenResult genResult = result.get(j);
                            if (genResult == null) {
                                continue;
                            }
                            if (genResult.getCompletFlag().equals(CompleteFlagEnum.COMPLETED)) {
                                if (failed.containsKey(genResult)) {
                                    continue;
                                }
                                if (!orderFiles.containsKey(genResult.getId())) {
                                    ArrayList<File> filesForPk = new ArrayList<File>();
                                    filesForPk.add(new File(genResult.getGenerateForm()));
                                    orderFiles.put(genResult.getId(), filesForPk);
                                } else {
                                    orderFiles.get(genResult.getId()).add(new File(genResult.getGenerateForm()));
                                }
                            } else {
                                if (orderFiles.containsKey(genResult.getId())) {
                                    orderFiles.remove(genResult.getId());
                                }
                                failed.put(genResult.getId(), genResult);

                            }

                        }
                        String sqlQuery = Constants.SQL_UPDATE + " " + runConfig.getPkWriter() + "=?";
                        Iterator<Entry<String, GenResult>> iterator = failed.entrySet().iterator();
                        while (iterator.hasNext()) {
                            GenResult genResult = iterator.next().getValue();
                            if (genResult != null) {
                                Object[] data = new Object[]{genResult.getCompletFlag().getId(), null,
                                        genResult.getError().getMessage(), genResult.getId()};
                                getDbUtil().executeUpdate(sqlQuery, formMapping.getWriteToName(), data);
                                LogHandler.logDebug("Updated for record with pk = " + genResult.getId());
                            }

                        }
                        Iterator<Entry<String, ArrayList<File>>> iterator2 = orderFiles.entrySet().iterator();
                        while (iterator2.hasNext()) {
                            Entry<String, ArrayList<File>> next = iterator2.next();


                            File finalOutput = PropertyUtils.getFile(pcdJobConf, pcdJobConf.getOutputFolder() + File.separator
                                    + formMapping.getWriteToName() + "_FINAL_" + System.currentTimeMillis()
                                    + pcdJobConf.getPdfExtension());


                            try {
                                MergePDF.concatPDFs(next.getValue(), new FileOutputStream(finalOutput), true, form
                                        .getPageSize().getSize());
                            }
                            catch (FileNotFoundException e) {
                                LogHandler.logError(new ConfigInitException("Unable to update for key: " + next.getKey(), e));
                                continue;
                            }
                            BinaryStream file = null;
                            try {
                                file = new BinaryStream(new FileInputStream(finalOutput), (int) finalOutput.length());
                            }
                            catch (FileNotFoundException e) {
                                LogHandler.logError(new ConfigInitException("Unable to update for key: " + next.getKey(), e));
                                continue;
                            }
                            Object[] data = new Object[]{CompleteFlagEnum.COMPLETED.getId(), file, null, next.getKey()};
                            getDbUtil().executeUpdate(sqlQuery, formMapping.getWriteToName(), data);
                            LogHandler.logDebug("Updated for record with pk = " + next.getKey());

                        }

                    default:
                        break;
                }
                LogHandler.logInfo("Updating generation result in db ended");
                break;
            default:
                break;
        }
    }

    /**
     * Remove duplicate item in list
     *
     * @param files
     */
    private static void removeDuplicate(ArrayList<File> files) {
        Set<File> s = new LinkedHashSet<File>(files);
        files.clear();
        files.addAll(s);
    }

    /**
     * Extract accrofields from pdf form.
     *
     * @param inputFileName  : full path to input file name
     * @param outputFileName : full path to output file name
     * @throws com.pdfcore.main.exceptions.AccroFieldExtractorException
     *
     */
    public void extractAccrofield(String inputFileName,
                                  String outputFileName) throws AccroFieldExtractorException {
        File input = new File(inputFileName);
        File output = new File(outputFileName);
        extractAccrofield(input, output);
    }

    /**
     * Extract accrofields from pdf form.
     *
     * @param inputFile
     * @param outputFile
     * @throws com.pdfcore.main.exceptions.AccroFieldExtractorException
     *
     */
    public void extractAccrofield(File inputFile, File outputFile) throws AccroFieldExtractorException {
        if (!inputFile.exists() || inputFile.isDirectory()) {
            throw new AccroFieldExtractorException("Input file is not existed");
        }

        AccroFieldExtractor.extract(inputFile, outputFile);
    }


    public List<JobResult> runJob() throws ConfigInitException, DataReaderException, DataWriterException, SQLException {
        //String filePath = cf.getAbsoluteFilePath();
    	
    	String jobFile;
    	
    	if (pcdJobConf.getConfPath().equalsIgnoreCase("")) {
    		jobFile = Constants.CONF_FOLDER + File.separator + this.job + ".properties";
    	} else {
    		jobFile = pcdJobConf.getConfPath() + File.separator + Constants.CONF_FOLDER + File.separator + this.job + ".properties";
    	}
        
        List<JobResult> jobResultList = this.executeBatch(constructBatchConfigFromProperties(jobFile));
        return jobResultList;
        //System.out.println(System.getProperties());
    }


	public String getJob() {
		return job;
	}


	public void setJob(String job) {
		this.job = job;
	}

	
}
