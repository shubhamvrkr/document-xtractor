package com.infosys.docxtract.handler;

import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import com.infosys.docxtract.config.*;
import com.infosys.docxtract.parsers.*;
import com.infosys.docxtract.ocr.*;
import org.slf4j.*;

import javax.annotation.*;

import org.springframework.web.reactive.function.BodyExtractors;
import reactor.core.publisher.*;
import org.springframework.web.reactive.function.server.*;
import org.springframework.http.*;

import java.awt.image.*;

import com.infosys.docxtract.helper.*;

import java.util.*;

import com.infosys.docxtract.models.*;

import java.io.*;

import org.apache.commons.io.*;
import org.springframework.http.codec.multipart.*;
import org.opencv.core.*;

@Component
public class RequestHandler {
    Logger logger;
    @Autowired
    OcrManager ocrManager;
    List<DocumentReference> documentReferencesList;
    @Autowired
    private ServerConfiguration serverConfiguration;
    private PdfParser pdfParser;
    private ImageParser imageParser;
    private AbstarctOcrReader abstarctOcrReader;

    public RequestHandler() {
        this.logger = LoggerFactory.getLogger((Class) RequestHandler.class);
        this.pdfParser = new PdfParser();
        this.imageParser = new ImageParser();
    }

    @PostConstruct
    public void init() {
        this.abstarctOcrReader = this.ocrManager.getAbstarctOcrReader();
        this.documentReferencesList = (List<DocumentReference>) this.serverConfiguration.getTemplates();
    }

    public Mono<ServerResponse> createTemplate(ServerRequest serverRequest) {
        this.logger.info("------------------CREATE TEMPLATE API CALLED------------------");
        boolean crop = Boolean.parseBoolean(serverRequest.pathVariable("crop"));
        this.logger.info("Crop Enabled: " + crop);
        return serverRequest.body(BodyExtractors.toMultipartData()).flatMap((parts) -> {
            Map<String, Part> map = parts.toSingleValueMap();
            FilePart filePart = (FilePart) map.get("file");
            this.logger.info("File Name: " + filePart.filename());
            this.logger.info("File Extension: " + FilenameUtils.getExtension(filePart.filename()));
            try {
                File pdfFile = File.createTempFile(filePart.filename(), FilenameUtils.getExtension(filePart.filename()));
                filePart.transferTo(pdfFile);
                List<BufferedImage> imageList = this.pdfParser.pdfToPng(pdfFile);
                if (crop) {
                    imageList = this.imageParser.cropImages(imageList);
                }
                BarcodeWithRect barcodeWithRect = this.imageParser.findBarcodeFromImage((BufferedImage) imageList.get(0));
                String barCode = "";
                if (barcodeWithRect != null) {
                    barCode = String.valueOf(abstarctOcrReader.readTextFromImage(Util.bufferedImageToBase64(barcodeWithRect.getImage(),Util.PNG_IMAGE_TYPE),"TEXT_DETECTION"));
                    //barCode = "13456477474373";
                }
                this.logger.info("Barcode value: " + barCode);
                barCode = Util.removeNonAlphaNumeric(barCode);
                Document document = this.buildDocument(imageList, barcodeWithRect, barCode);
                return ServerResponse.status(HttpStatus.OK).body(Mono.just(document), Document.class);

            } catch (IOException var10) {
                this.logger.error("Failed to create temporary file: " + var10.getMessage());
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).syncBody("Could not create temporary file!!");
            }
        });
    }

    public Mono<ServerResponse> saveTemplate(final ServerRequest serverRequest) {
        this.logger.info("------------------SAVE TEMPLATE API CALLED------------------");
        final Mono<Document> documentMono = serverRequest.bodyToMono(Document.class);
        return documentMono.flatMap(document -> {
            File file = this.serverConfiguration.saveTemplate(document);
            this.documentReferencesList.add(new DocumentReference(document.getDocCode(), file.getAbsolutePath()));
            return ServerResponse.status(HttpStatus.OK).syncBody((Object) "Successfully saved template");
        });
    }

    public Mono<ServerResponse> processTemplate(ServerRequest serverRequest) {
        this.logger.info("------------------PROCESS TEMPLATE API CALLED------------------");
        boolean crop = Boolean.parseBoolean(serverRequest.pathVariable("crop"));
        this.logger.info("Crop Enabled: " + crop);
        return serverRequest.body(BodyExtractors.toMultipartData()).flatMap((parts) -> {
            Map<String, Part> map = parts.toSingleValueMap();
            FilePart filePart = (FilePart) map.get("file");
            this.logger.info("File Name: " + filePart.filename());
            this.logger.info("File Extension: " + FilenameUtils.getExtension(filePart.filename()));
            try {
                File pdfFile = File.createTempFile(filePart.filename(), FilenameUtils.getExtension(filePart.filename()));
                filePart.transferTo(pdfFile);
                List<BufferedImage> imageList = this.pdfParser.pdfToPng(pdfFile);
                if (crop) {
                    imageList = this.imageParser.cropImages(imageList);
                }
                boolean isDocFound = false;
                Document template = new Document();
                //performance issue
                for (DocumentReference documentReference : documentReferencesList) {

                    template = this.serverConfiguration.getTemplateById(documentReference.getTemplatePath());
                    this.logger.info("Trying document: " + template.getDocCode());
                    String barcodePageId = template.getBarcode().getPageId();
                    Rect barcodeBoundary = template.getBarcode().getBoundary();
                    Page barcodePage = template.getPages().get(barcodePageId);
                    if (barcodePage == null) {
                        this.logger.info("Barcode page not found");
                    } else {
                        int width = barcodePage.getWidth();
                        int height = barcodePage.getHeight();
                        BufferedImage firstPage = imageList.get(0);
                        BufferedImage firstPageResize = this.imageParser.reSizeImage(firstPage, width, height);
                        BufferedImage barcodeImageFromScanned = this.imageParser.findBarcodeFromImage(firstPageResize, barcodeBoundary);
                        //test to write barcode crop
                        Util.writeBufferedImageToFile(barcodeImageFromScanned, template.getDocCode() + "." + Util.PNG_IMAGE_TYPE, Util.PNG_IMAGE_TYPE);
                        String barCodeTextFromScanned = String.valueOf(this.abstarctOcrReader.readTextFromImage(Util.bufferedImageToBase64(barcodeImageFromScanned, Util.PNG_IMAGE_TYPE),"TEXT_DETECTION"));
                        barCodeTextFromScanned = Util.removeAlphaCharaters(barCodeTextFromScanned);
                        this.logger.info("Barcode text from ocr: " + barCodeTextFromScanned);
                        if (barCodeTextFromScanned.compareToIgnoreCase(template.getDocCode()) == 0) {
                            isDocFound = true;
                            break;
                        }
                    }
                }
                if (!isDocFound) {
                    return ServerResponse.status(HttpStatus.NOT_FOUND).syncBody("Failed to identify document type. Make sure the barcode on the document is clear enough");
                } else {
                    Map<String, Page> pages = template.getPages();
                    imageList = this.imageParser.reSizeImage(imageList, template);
                    int pageIndex = 1;

                    for (BufferedImage scannedPage : imageList) {
                        Page page = (Page) pages.get("page-" + pageIndex);
                        if (page.getFields() != null) {
                            for (Field field : page.getFields()) {
                                BufferedImage croppedField = this.imageParser.cropImageToBufferedImage(scannedPage, field.getBoundary());
                                //save cropped field image for testing
                                Util.writeBufferedImageToFile(croppedField, template.getDocCode() + pageIndex + field.getName() + "." + Util.PNG_IMAGE_TYPE, Util.PNG_IMAGE_TYPE);
                                String text = this.getDataFromField(field, croppedField);
                                this.logger.info("Text: " + text);
                                field.setValue(text);
                            }
                        }
                        this.logger.info("Scanned image size for page " + page.getId() + " : " + scannedPage.getData().getDataBuffer().getSize());
                        String base64Img = Util.bufferedImageToBase64(scannedPage, Util.PNG_IMAGE_TYPE);
                        this.logger.info("Scanned image size(base64) for page " + page.getId() + " : " + base64Img.getBytes().length);
                        page.setContent(base64Img);
                        pages.put(page.getId(), page);
                        pageIndex++;
                    }
                    template.setPages(pages);
                    return ServerResponse.status(HttpStatus.OK).body(Mono.just(template), Document.class);
                }
            } catch (IOException var27) {
                this.logger.error("Failed to create temporary file: " + var27.getMessage());
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).syncBody("Could not create temporary file!!");
            }
        });
    }

    private Document buildDocument(final List<BufferedImage> imageList, final BarcodeWithRect barcodeWithRect, final String barCode) {
        int pageCount = 1;
        final Map<String, Page> pages = new HashMap<String, Page>();
        for (final BufferedImage image : imageList) {
            final Page page = new Page();
            page.setId("page-" + pageCount);
            page.setWidth(image.getWidth());
            page.setHeight(image.getHeight());
            try {
                this.logger.info("Input image size for page " + page.getId() + " : " + image.getData().getDataBuffer().getSize());
                final String content = Util.bufferedImageToBase64(image, Util.PNG_IMAGE_TYPE);
                this.logger.info("Input image size(base64) for page " + page.getId() + " : " + content.getBytes().length);
                page.setContent(content);
            } catch (IOException e) {
                this.logger.error("Failed converting image to base64: " + e.getMessage());
                page.setContent((String) null);
            }
            pages.put(page.getId(), page);
            ++pageCount;
        }
        final Barcode barcode = new Barcode();
        barcode.setPageId("page-1");
        if (barcodeWithRect != null) {
            barcode.setBoundary(barcodeWithRect.getRect());
        }
        final Document document = new Document();
        document.setDocCode(barCode);
        document.setPageCount(imageList.size());
        document.setBarcode(barcode);
        document.setPages((Map) pages);
        return document;
    }

    private TreeMap<String, Page> sortMap(final Map<String, Page> pages) {
        final TreeMap<String, Page> sorted = new TreeMap<String, Page>();
        sorted.putAll(pages);
        return sorted;
    }

    private String getDataFromField(final Field f, final BufferedImage image) {
        try {
            String s = String.valueOf(this.abstarctOcrReader.readTextFromImage(Util.bufferedImageToBase64(image, Util.PNG_IMAGE_TYPE),"TEXT_DETECTION"));
            this.logger.info("Adapter returned text: " + s);
            if (f.getType() == FieldType.TEXT) {
                s = Util.removeNonAlphaNumeric(s);
                return s;
            }
            if (f.getType() == FieldType.NUMBER) {
                s = Util.removeAlphaCharaters(s);
                return s;
            }
            if (f.getType() == FieldType.DATE) {
                final StringBuilder stringBuilder = new StringBuilder();
                //s = Util.removeAlphaCharaters(s);
                final String format = f.getFormat();
                if (format.contains("/")) {
                    final String[] h = format.split("/");
                    final int first = h[0].length();
                    final int second = h[1].length();
                    for (int i = 0; i < s.length(); ++i) {
                        if (i == first || i == first + second) {
                            stringBuilder.append("-");
                        }
                        stringBuilder.append(s.charAt(i));
                    }
                } else if (format.contains("-")) {
                    final String[] h = format.split("-");
                    final int first = h[0].length();
                    final int second = h[1].length();
                    for (int i = 0; i < s.length(); ++i) {
                        if (i == first + 1 || i == first + second + 1) {
                            stringBuilder.append("-");
                        }
                        stringBuilder.append(s.charAt(i));
                    }
                }
                return stringBuilder.toString();
            }
            s = Util.removeNonAlphaNumeric(s);
            return s;
        } catch (IOException e) {
            this.logger.info("Error getting text for field: " + e.getMessage());
            return "";
        }
    }

    private void writeStringToFile(final String s, final String name) {
        try {
            final File file = new File("./templates/barcode-test/" + name + ".txt");
            final FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(s);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            this.logger.error(e.getMessage());
        }
    }
}