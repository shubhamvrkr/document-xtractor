package com.infosys.docxtract.storage;

import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import org.slf4j.*;
import com.infosys.docxtract.storage.file.*;
import javax.annotation.*;

@Component
public class StorageManager
{
    Logger logger;
    @Value("${server.storage.type}")
    private String storageType;
    AbstractStorage abstractStorage;
    
    public StorageManager() {
        this.logger = LoggerFactory.getLogger((Class)StorageManager.class);
    }
    
    @PostConstruct
    public void init() {
        this.logger.info("=====================Storage Configuration=====================");
        this.logger.info("Storage Type: " + this.storageType);
        switch (storageType) {
            case "FILE": {
                this.abstractStorage = new FileStorage();
                break;
            }
        }
    }
    
    public AbstractStorage getAbstractStorage() {
        return this.abstractStorage;
    }
    
    public void setAbstractStorage(final AbstractStorage abstractStorage) {
        this.abstractStorage = abstractStorage;
    }
}