/*
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * This class provides some utilities.
 *
 * @author Chenfeng Zhu
 *
 */
public abstract class CommonUtils {

    /**
     * Default URL format.
     */
    public final static String DEFAULT_URL_FORMAT = "{requestURL}/{deviceID}/{quantity}";

    /**
     * The number of data in 1 second.
     */
    public final static int FREQUENCY = 1;

    /**
     * Default character set for QR code.
     */
    public final static String DEFAULT_CHARACTER_SET = "UTF-8";

    /**
     * Default size of the image.
     */
    public final static int DEFAULT_SIZE = 120;

    /**
     *
     * @param len
     * @return
     */
    public static String getRandomHex(int len) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < len) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, len).toUpperCase();
    }

    /**
     * Get a random double number from 0 to 10.
     *
     * @return
     */
    public static double getRandomValue() {
        return getRandomValue(0, 10);
    }

    /**
     * Get a random double number from min to max.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @return
     */
    public static double getRandomValue(double min, double max) {
        Random r = new Random();
        return ((int) (10000 * ((r.nextDouble() * (max - min)) + min))) / 10000.0;
    }

    /**
     * Generate the image of QR code according to the text, size and error correction level.
     *
     * @param text
     *            the context
     * @param size
     *            the size(pixel) of image
     * @param level
     *            the error correction level
     * @return
     */
    public static BufferedImage generateImage(String text, int size, ErrorCorrectionLevel level) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(
                EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, DEFAULT_CHARACTER_SET);
        // Now with zxing version 3.2.1 you could change border size (white border size to just 1)
        hintMap.put(EncodeHintType.MARGIN, 1); /* default = 4 */
        hintMap.put(EncodeHintType.ERROR_CORRECTION, level);
        try {
            BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size,
                    hintMap);
            int CrunchifyWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(CrunchifyWidth, CrunchifyWidth,
                    BufferedImage.TYPE_BYTE_GRAY);
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, CrunchifyWidth, CrunchifyWidth);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < CrunchifyWidth; i++) {
                for (int j = 0; j < CrunchifyWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            return image;
        } catch (WriterException e) {
        }
        return null;
    }

}
