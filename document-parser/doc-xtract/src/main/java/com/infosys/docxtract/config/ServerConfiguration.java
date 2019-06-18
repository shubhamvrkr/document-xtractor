package com.infosys.docxtract.config;

import org.springframework.stereotype.*;
import com.infosys.docxtract.storage.*;
import org.springframework.beans.factory.annotation.*;
import java.util.*;
import org.slf4j.*;
import org.opencv.core.*;
import javax.annotation.*;
import com.infosys.docxtract.models.*;
import java.io.*;

@Component
public class ServerConfiguration
{
    Logger logger;
    @Autowired
    StorageManager storageManager;
    AbstractStorage abstractStorage;
    private String templatesDir;
    @Value("${server.config.dir}")
    private String configDir;
    @Value("${server.opencv.dir}")
    private String opencvLib;
    private List<DocumentReference> templates;
    
    public ServerConfiguration() {
        this.logger = LoggerFactory.getLogger((Class)ServerConfiguration.class);
        this.templatesDir = "templates";
    }
    
    @PostConstruct
    private void init() {
        this.logger.info("=====================Server Configuration=====================");
        this.logger.info("Config Dir: " + this.configDir);
        this.logger.info("Opencv Dir: " + this.opencvLib);
        this.logger.info("Booting Process Started");
        System.setProperty("java.library.path", this.opencvLib);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        (this.abstractStorage = this.storageManager.getAbstractStorage()).createTemplateStorage(this.configDir + "/" + this.templatesDir);
        this.templates = (List<DocumentReference>)this.abstractStorage.getAllTemplates(this.configDir + "/" + this.templatesDir);
        this.logger.info("Loaded templates: ");
        this.templates.forEach(document -> this.logger.info("Template: " + document));
        this.logger.info("Booting Process Finished");
    }
    
    public File saveTemplate(final Document document) {
        return this.abstractStorage.saveTemplate(document, this.configDir + "/" + this.templatesDir);
    }
    
    public List<DocumentReference> getTemplates() {
        return this.templates;
    }
    
    public void setTemplates(final List<DocumentReference> templates) {
        this.templates = templates;
    }
    
    public Document getTemplateById(final String id) {
        return this.abstractStorage.getTemplateById(id);
    }
}