package com.infosys.docxtract.parsers;

import com.infosys.docxtract.models.BarcodeWithRect;
import com.infosys.docxtract.models.Document;
import com.infosys.docxtract.models.Page;
import org.opencv.core.Point;
import org.slf4j.*;
import com.infosys.docxtract.helper.*;
import org.opencv.imgproc.*;
import java.util.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.*;
import javax.imageio.*;
import java.io.*;
import java.awt.image.*;
import java.awt.*;
import java.util.List;

public class ImageParser {

    private static final int BARCODE_PADDING_X = 10;
    private static final int BARCODE_PADDING_Y = 10;
    private static final int BARCODE_PADDING_WIDTH = 10;
    private static final int BARCODE_PADDING_HEIGHT = 10;
    private static final int BARCODE_DILATION_COUNT = 70;
    private static final int BARCODE_EROSION_COUNT = 10;

    Logger logger;
    
    public ImageParser() {
        this.logger = LoggerFactory.getLogger(ImageParser.class);
    }
    
    public List<BufferedImage> cropImages(final List<BufferedImage> images) {
        final List<BufferedImage> croppedImages = new ArrayList<BufferedImage>();
        for (final BufferedImage image : images) {
            try {
                final Mat source = Util.bufferedImageToMat(image);
                final Rect rect = this.findFormBoundary(source);
                Mat sourceCopy = source.clone();
                Imgproc.rectangle(sourceCopy,rect,new Scalar(0,0,255),10);
                Imgcodecs.imwrite("./templates/barcode_test/crop-"+new Random().nextInt()+"."+Util.PNG_IMAGE_TYPE,sourceCopy);
                final Mat croppedImage = this.cropImage(source, rect);
                final BufferedImage cp = this.matToBufferedImage(croppedImage);
                croppedImages.add(cp);
            }
            catch (IOException e) {
                this.logger.info("Failed converting buffered image to mat: " + e.getMessage());
            }
        }
        return croppedImages;
    }
    
    public BufferedImage reSizeImage(final BufferedImage image, final int width, final int height) {
        try {
            final Mat croppedImage = Util.bufferedImageToMat(image);
            final Size sz = new Size((double)width, (double)height);
            Imgproc.resize(croppedImage, croppedImage, sz);
            final BufferedImage cropImage = this.matToBufferedImage(croppedImage);
            return cropImage;
        }
        catch (IOException e) {
            this.logger.info("Failed getting mat from buffered image: " + e.getMessage());
            return null;
        }
    }
    
    public List<BufferedImage> reSizeImage(final List<BufferedImage> images, final Document document) {
        final List<BufferedImage> output = new ArrayList<BufferedImage>();
        final int pageIndex = 1;
        final Map<String, Page> pages = (Map<String, Page>)document.getPages();
        for (final BufferedImage image : images) {
            final Page inputPage = pages.get("page-" + pageIndex);
            final BufferedImage ci = this.reSizeImage(image, inputPage.getWidth(), inputPage.getHeight());
            this.logger.info("Image Parser Page Image: " + ci.getData().getDataBuffer().getSize());
            output.add(ci);
        }
        return output;
    }
    
    public BarcodeWithRect findBarcodeFromImage(final BufferedImage page) {
        try {
            final Mat source = Util.bufferedImageToMat(page);
            final Rect rect = this.findBarcodeBoundary(source);
            //Mat sourceCopy = source.clone();
            //Imgproc.rectangle(sourceCopy,rect,new Scalar(255,0,0),10);
            //Imgcodecs.imwrite("./templates/barcode_test/barcode-"+new Random().nextInt()+"."+Util.PNG_IMAGE_TYPE,sourceCopy);
            if (rect != null) {
                final Mat croppedImage = this.cropImage(source, rect);
                final BufferedImage cp = this.matToBufferedImage(croppedImage);

                return new BarcodeWithRect(cp, rect);
            }
            return null;
        }
        catch (IOException e) {
            this.logger.info("Failed converting buffered image to mat: " + e.getMessage());
            return new BarcodeWithRect();
        }
    }
    
    public BufferedImage findBarcodeFromImage(final BufferedImage page, final Rect rect) {
        try {
            final Mat source = Util.bufferedImageToMat(page);
            final Mat croppedImage = this.cropImage(source, rect);
            final BufferedImage cp = this.matToBufferedImage(croppedImage);
            return cp;
        }
        catch (IOException e) {
            this.logger.info("Failed converting buffered image to mat: " + e.getMessage());
            return null;
        }
    }
    
    private Rect findFormBoundary(final Mat src) {
        final Mat grayscale = new Mat();
        final Mat gaussianblur = new Mat();
        final Mat threshold = new Mat();
        final Mat dilation = new Mat();
        final Mat hierarchy = new Mat();
        final List<MatOfPoint> counters = new ArrayList<MatOfPoint>();
        Imgproc.cvtColor(src, grayscale, 6);
        Imgproc.GaussianBlur(grayscale, gaussianblur, new Size(5.0, 5.0), 0.0);
        Imgproc.threshold(gaussianblur, threshold, 220.0, 235.0, 1);
        final Mat element1 = Imgproc.getStructuringElement(0, new Size(11.0, 11.0));
        Imgproc.dilate(threshold, dilation, element1);
        Imgproc.findContours(dilation, (List)counters, hierarchy, 0, 2);
        MatOfPoint largest = null;
        double largestArea = -1.0;
        for (final MatOfPoint point : counters) {
            final double temp = Imgproc.contourArea((Mat)point);
            if (temp > largestArea) {
                largestArea = temp;
                largest = point;
            }
        }
        final MatOfPoint2f approx = new MatOfPoint2f();
        final MatOfPoint2f curve = new MatOfPoint2f(largest.toArray());
        final double peri = Imgproc.arcLength(curve, true);
        Imgproc.approxPolyDP(curve, approx, 0.05 * peri, true);
        final Rect rect = Imgproc.boundingRect((Mat)approx);
        return rect;
    }
    
    private Rect findBarcodeBoundary(final Mat source) {
        final Mat gray = new Mat();
        final Mat gradX = new Mat();
        final Mat gradY = new Mat();
        final Mat gradient = new Mat();
        final Mat blurred = new Mat();
        final Mat thresh = new Mat();
        final Mat closed = new Mat();
        final Mat hierarchy = new Mat();
        final List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.cvtColor(source, gray, 6);
        final int ddepth = 6;
        Imgproc.Sobel(gray, gradX, ddepth, 1, 0, -1);
        Imgproc.Sobel(gray, gradY, ddepth, 0, 1, -1);
        Core.subtract(gradX, gradY, gradient);
        Core.convertScaleAbs(gradient, gradient);
        //Imgcodecs.imwrite("./templates/barcode_test/gradient-"+new Random().nextInt()+"."+Util.PNG_IMAGE_TYPE,gradient);
        Imgproc.blur(gradient, blurred, new Size(9.0, 9.0));
        Imgproc.threshold(blurred, thresh, 225.0, 255.0, 0);
        final Mat kernel = Imgproc.getStructuringElement(0, new Size(21.0, 7.0));
        Imgproc.morphologyEx(thresh, closed, 3, kernel);
        Imgproc.erode(closed, closed, new Mat(), new Point(), BARCODE_EROSION_COUNT);
        //Imgcodecs.imwrite("./templates/barcode_test/erosion-"+new Random().nextInt()+"."+Util.PNG_IMAGE_TYPE,closed);
        Imgproc.dilate(closed, closed, new Mat(), new Point(), BARCODE_DILATION_COUNT);
        //Imgcodecs.imwrite("./templates/barcode_test/dialate-"+new Random().nextInt()+"."+Util.PNG_IMAGE_TYPE,closed);
        Imgproc.findContours(closed.clone(), (List)contours, hierarchy, 0, 2);
        Collections.sort(contours,new Comparator());
        Collections.reverse(contours);
        if (contours.size() > 0) {
            final MatOfPoint2f approx = new MatOfPoint2f();
            final MatOfPoint2f curve = new MatOfPoint2f(contours.get(0).toArray());
            final double peri = Imgproc.arcLength(curve, true);
            Imgproc.approxPolyDP(curve, approx, 0.01 * peri, true);
            Rect rect = Imgproc.boundingRect((Mat)approx);
            rect = new Rect(rect.x - BARCODE_PADDING_X, rect.y - BARCODE_PADDING_Y, rect.width + BARCODE_PADDING_WIDTH, rect.height + BARCODE_PADDING_HEIGHT);
            return rect;
        }
        return null;
    }
    
    public BufferedImage cropImageToBufferedImage(final BufferedImage source, final Rect boundary) {
        try {
            final Mat src = Util.bufferedImageToMat(source);
            final Mat cropImage = new Mat(src, boundary);
            final BufferedImage cpage = this.matToBufferedImage(cropImage);
            return cpage;
        }
        catch (IOException e) {
            this.logger.error("Error converting buffered image to mat: " + e.getMessage());
            return null;
        }
    }
    
    private Mat cropImage(final Mat source, final Rect boundary) {
        final Mat cropImage = new Mat(source, boundary);
        return cropImage;
    }
    
    private Mat cropImage(final Mat source, final Rect boundary, final double width, final double height) {
        final Mat cropImage = new Mat(source, boundary);
        final Size sz = new Size(width, height);
        Imgproc.resize(cropImage, cropImage, sz);
        return cropImage;
    }
    
    private BufferedImage matToBufferedImage(final Mat mat) {
        try {
            final MatOfByte bytemat = new MatOfByte();
            Imgcodecs.imencode("." + Util.PNG_IMAGE_TYPE, mat, bytemat);
            final byte[] bytes = bytemat.toArray();
            final InputStream in = new ByteArrayInputStream(bytes);
            final BufferedImage img = ImageIO.read(in);
            return img;
        }
        catch (IOException e) {
            this.logger.error("Error converting mat to buffered image: " + e.getMessage());
            return null;
        }
    }
    
    private BufferedImage resizeBufferedImage(final BufferedImage img, final int width, final int height) {
        final Image tmp = img.getScaledInstance(width, height, 4);
        final BufferedImage dimg = new BufferedImage(width, height, 2);
        final Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    public class Comparator implements java.util.Comparator<MatOfPoint>{
        @Override
        public int compare(final MatOfPoint o1, final MatOfPoint o2) {
            final double A1 = Imgproc.contourArea((Mat)o1);
            final double A2 = Imgproc.contourArea((Mat)o2);
            final int result = Double.compare(A1, A2);
            return result;
        }
    }
}