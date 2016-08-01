/*
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zhuzhu.energyconsumption.utils.CommonUtils;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * Servlet implementation class QRGeneratorServlet
 *
 * @author Chenfeng Zhu
 */
@WebServlet(description = "QR Code Generator", urlPatterns = { "/qrgenerator" })
public class QRGeneratorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public QRGeneratorServlet() {
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // text in QR code
        String strText = request.getParameter("qrt");
        if (strText == null) {
            return;
        }

        // size of image
        String strSize = request.getParameter("qrs");
        int size = CommonUtils.DEFAULT_SIZE;
        try {
            size = (strSize == null) ? size : Integer.parseInt(strSize);
        } catch(Exception e) {
            size = CommonUtils.DEFAULT_SIZE;
        }

        // error correction level
        String strLevel = request.getParameter("qrl");
        ErrorCorrectionLevel level = ErrorCorrectionLevel.L;
        if ("M".equalsIgnoreCase(strLevel)) {
            level = ErrorCorrectionLevel.M;
        } else if ("Q".equalsIgnoreCase(strLevel)) {
            level = ErrorCorrectionLevel.Q;
        } else if ("H".equalsIgnoreCase(strLevel)) {
            level = ErrorCorrectionLevel.H;
        }

        // generate the image of QR code
        BufferedImage image = CommonUtils.generateImage(strText, size, level);
        if (image == null) {
            response.getWriter().print("Error: " + strText);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        // respond with the bytes of images.
        response.setContentType("image/png");
        OutputStream outStream = response.getOutputStream();
        response.setContentLength(imageInByte.length);
        outStream.write(imageInByte);
        outStream.flush();
        outStream.close();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // text in QR code
        String strText = request.getParameter("contentText");
        if (strText == null) {
            return;
        }

        // size of image
        String strSize = request.getParameter("qrcodeSize");
        int size = CommonUtils.DEFAULT_SIZE;
        try {
            size = (strSize == null) ? size : Integer.parseInt(strSize);
        } catch(Exception e) {
            size = CommonUtils.DEFAULT_SIZE;
        }

        // error correction level
        String strLevel = request.getParameter("qrcodeError");
        ErrorCorrectionLevel level = ErrorCorrectionLevel.L;
        if ("M".equalsIgnoreCase(strLevel)) {
            level = ErrorCorrectionLevel.M;
        }

        // generate the image of QR code
        BufferedImage image = CommonUtils.generateImage(strText, size, level);
        if (image == null) {
            response.getWriter().print("Error: " + strText);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        // respond with the bytes of images.
        response.setContentType("image/png");
        OutputStream outStream = response.getOutputStream();
        response.setContentLength(imageInByte.length);
        outStream.write(imageInByte);
        outStream.flush();
        outStream.close();
    }

}
