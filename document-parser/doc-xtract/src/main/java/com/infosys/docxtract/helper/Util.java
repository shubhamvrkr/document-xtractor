package com.infosys.docxtract.helper;

import javax.imageio.*;
import java.awt.image.*;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.*;

import java.io.*;

import org.slf4j.*;

public class Util {
    public static String PNG_IMAGE_TYPE;
    static Logger logger;

    static {
        Util.logger = LoggerFactory.getLogger((Class) Util.class);
        Util.PNG_IMAGE_TYPE = "png";
    }

    public static String bufferedImageToBase64(final BufferedImage bufferedImage, final String imageType) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, imageType, bos);
        final byte[] imageBytes = bos.toByteArray();
        final String imageString = Base64.getEncoder().encodeToString(imageBytes);
        bos.close();
        return imageString;
    }

    public static BufferedImage base64ToBufferedImage(final String imageString) {
        BufferedImage image = null;
        try {
            final byte[] imageByte = Base64.getDecoder().decode(imageString);
            final ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public static Mat bufferedImageToMat(final BufferedImage image) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, Util.PNG_IMAGE_TYPE, byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode((Mat) new MatOfByte(byteArrayOutputStream.toByteArray()), -1);
    }

    public static String removeNonAlphaNumeric(String s) {
        s = s.replaceAll("[^a-zA-Z0-9]", "");
        return s.trim();
    }

    public static String removeAlphaCharaters(String s) {
        s = s.replaceAll("[^0-9]", "");
        return s.trim();
    }

    public static void writeBufferedImageToFile(final BufferedImage bufferedImage, final String name, final String type) {
        try {
            final File outputfile = new File("./templates/barcode_test/" + name);
            ImageIO.write(bufferedImage, type, outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}