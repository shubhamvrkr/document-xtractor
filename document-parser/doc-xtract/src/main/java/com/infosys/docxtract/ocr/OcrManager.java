package com.infosys.docxtract.ocr;

import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import org.slf4j.*;
import com.infosys.docxtract.ocr.googlevision.*;
import javax.annotation.*;

@Component
public class OcrManager
{
    Logger logger;
    @Value("${server.ocr.type}")
    private String ocrType;
    @Value("${server.ocr.url}")
    private String ocrUrl;
    @Value("${server.ocr.token}")
    private String ocrToken;
    AbstarctOcrReader abstarctOcrReader;
    
    public OcrManager() {
        this.logger = LoggerFactory.getLogger((Class)OcrManager.class);
    }
    
    @PostConstruct
    public void init() {
        this.logger.info("=====================OCR Configuration=====================");
        this.logger.info("OCR Type: " + this.ocrType);
        this.logger.info("OCR Url: " + this.ocrUrl);
        final String ocrType = this.ocrType;
        switch (ocrType) {
            case "GOOGLE_VISION": {
                this.abstarctOcrReader = (AbstarctOcrReader)new GoogleVisonOcrReader(this.ocrUrl, this.ocrToken);
                break;
            }
        }
    }
    
    public AbstarctOcrReader getAbstarctOcrReader() {
        return this.abstarctOcrReader;
    }
    
    public void setAbstarctOcrReader(final AbstarctOcrReader abstarctOcrReader) {
        this.abstarctOcrReader = abstarctOcrReader;
    }
}