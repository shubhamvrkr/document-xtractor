package com.infosys.docxtract.parsers;

import com.infosys.docxtract.handler.*;
import org.slf4j.*;
import java.awt.image.*;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.rendering.*;
import org.apache.pdfbox.pdmodel.encryption.*;
import java.io.*;

public class PdfParser
{
    public static final int DPI = 300;
    Logger logger;
    
    public PdfParser() {
        this.logger = LoggerFactory.getLogger((Class)RequestHandler.class);
    }
    
    public List<BufferedImage> pdfToPng(final File pdfFile) {
        final List<BufferedImage> pngs = new ArrayList<BufferedImage>();
        try {
            BufferedImage pageImage;
            final PDDocument document = PDDocument.load(pdfFile);
            final PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                pageImage = pdfRenderer.renderImageWithDPI(page, DPI, ImageType.RGB);
                this.logger.info("Pdf Page Image: " + pageImage.getData().getDataBuffer().getSize());
                pngs.add(pageImage);
            }
            document.close();
        }
        catch (InvalidPasswordException e) {
            this.logger.error("PDF to PNG covnersion Failed: " + e.getMessage());
        }
        catch (IOException e2) {
            this.logger.error("PDF to PNG covnersion Failed: " + e2.getMessage());
        }
        return pngs;
    }
}