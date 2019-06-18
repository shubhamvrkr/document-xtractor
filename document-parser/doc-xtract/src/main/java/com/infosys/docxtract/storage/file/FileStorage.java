package com.infosys.docxtract.storage.file;

import com.infosys.docxtract.storage.*;
import org.slf4j.*;
import java.util.*;
import com.google.gson.*;
import org.apache.commons.io.*;
import com.infosys.docxtract.models.*;
import java.io.*;

public class FileStorage extends AbstractStorage
{
    private static final String TEMPLATE_SUFFIX = "-template.json";
    Logger logger;
    
    public FileStorage() {
        this.logger = LoggerFactory.getLogger((Class)FileStorage.class);
    }
    
    @Override
    public void createTemplateStorage(final String location) {
        final File file = new File(location);
        if (file.exists()) {
            return;
        }
        file.mkdirs();
    }
    
    @Override
    public List<DocumentReference> getAllTemplates(final String location) {
        final List<DocumentReference> templates = new ArrayList<DocumentReference>();
        final Gson gson = new Gson();
        final File dir = new File(location);
        final File[] listFiles;
        final File[] directoryListing = listFiles = dir.listFiles();
        for (final File file : listFiles) {
            if (file.isFile() && FilenameUtils.getExtension(file.getName()).compareToIgnoreCase("json") == 0) {
                try {
                    final BufferedReader br = new BufferedReader(new FileReader(file));
                    final Document document = (Document)gson.fromJson((Reader)br, (Class)Document.class);
                    templates.add(new DocumentReference(document.getDocCode(), file.getAbsolutePath()));
                    br.close();
                }
                catch (IOException e) {
                    this.logger.error("Failed reading templates : " + e.getMessage());
                }
            }
        }
        return templates;
    }
    
    @Override
    public File saveTemplate(final Document template, final String location) {
        try {
            final Gson gson = new Gson();
            final String jsonString = gson.toJson((Object)template);
            final File file = new File(location + "/" + template.getDocCode() + "-template.json");
            final FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(jsonString);
            fileWriter.flush();
            fileWriter.close();
            return new File(location + "/" + template.getDocCode() + "-template.json");
        }
        catch (IOException e) {
            this.logger.error("Failed saving template " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Document getTemplateById(final String filePath) {
        try {
            final Gson gson = new Gson();
            final BufferedReader br = new BufferedReader(new FileReader(filePath));
            final Document document = (Document)gson.fromJson((Reader)br, (Class)Document.class);
            br.close();
            return document;
        }
        catch (FileNotFoundException e) {
            this.logger.info("Document path location not found: " + e.getMessage());
        }
        catch (IOException e2) {
            this.logger.info("Failed closing buffered reader: " + e2.getMessage());
        }
        return null;
    }
}