/**
 * Energy Consumption ( https://github.com/sampig/EnergyConsumption ) - This file is part of Energy Consumption.
 * Copyright (C) 2016 - Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner.utils;

import org.zhuzhu.energyconsumption.scanner.model.SettingsModel;

import java.util.List;

/**
 * This is to generate the HTML contents according to the data received.
 *
 * @author Chenfeng Zhu
 */
public final class HTMLGenerator {

    private final static int G_DEFAULT_WIDTH = 270;
    private final static int G_DEFAULT_HEIGHT = 150;
    private final static String[] LINE_COLORS = {"#ff0066", "#00cc00", "#0066cc", "#6600ff"};
    private final static int COLOR_NUMBER = LINE_COLORS.length;
    private static int token = 0;
    private final static String TEXT_COLOR = "#f52714";
    private final static int LINE_WIDTH = 3;
    private final static String BACKGROUND_COLOR = "transparent";
    // private final static int V_DEFAULT_MAX = 350;
    // private final static int V_DEFAULT_MIN = 0;

    /**
     * Get the HTML content according to the data.
     *
     * @param data dataset
     * @return the HTML content
     */
    public static String getHTMLContent(List<Double> data) {
        return getHTMLContent(data, G_DEFAULT_WIDTH, G_DEFAULT_HEIGHT);
    }

    /**
     * Get the HTML content according to the data.
     *
     * @param listData a list of data in time order
     * @param width    width of graph
     * @param height   height of graph
     * @return the HTML content
     */
    public static String getHTMLContent(List<Double> listData, int width, int height) {
        // TODO: improve
        String lineColor = LINE_COLORS[token % COLOR_NUMBER];
        StringBuilder sb = new StringBuilder();

        // the start part
        String contentStart = "<html>"
                + "  <head>"
                + "    <script type='text/javascript' src='js/jsapi.js'></script>"
                + "    <script type='text/javascript'>"
                + "      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
                + "      google.setOnLoadCallback(drawChart);"
                + "      function drawChart() {"
                + "        var data = google.visualization.arrayToDataTable(["
                + "          ['Time', 'Energy'],";
        sb.append(contentStart);

        // the data part
        StringBuilder sbData = new StringBuilder();
        if (listData != null && listData.size() > 0) {
            for (int i = 0; i < listData.size(); i++) {
                sbData.append("['").append(i).append("',  ").append(listData.get(i)).append("],");
            }
            sb.append(sbData.substring(0, sbData.length() - 1));
        } else {
            return "<html><body><img src='images/NoData782.png' width='" + width + "' height='" + height + "' /></body></html>";
        }

        // the end part
        String contentEnd = "        ]);"
                + "        var options = {"
//                + "          title: 'Energy Consumption',"
                + "          width: " + width + ","
                + "          height: " + height + ","
                + "          lineWidth: " + LINE_WIDTH + ","
                + "          colors: ['" + lineColor + "'],"
                + "          backgroundColor: '" + BACKGROUND_COLOR + "',"
                + "          hAxis: { textPosition: 'none' },"
                + "          vAxis: { textStyle: { color: '" + TEXT_COLOR + "' } },"
                //vAxis: { maxValue: 150, minValue: 0, textStyle: { color: '#f52714' } },
                + "          chartArea: {left:'15%', top:'10%', width: '80%', height: '80%'},"
                + "          legend: { position: 'none' }"
                + "        };"
                + "        var chart = new google.visualization.LineChart(document.getElementById('div_chart'));"
                + "        chart.draw(data, options);"
                + "      }"
                + "    </script>"
                + "  </head>"
                + "  <body style='margin:0; padding:0; background-color: transparent;'>"
                + "    <div id='div_chart'></div>"
                + "  </body>"
                + "</html>";
        sb.append(contentEnd);

        token++;
        return sb.toString();
    }

    /**
     * Generate the error HTML page.
     *
     * @param width
     * @param height
     * @return the HTML content
     */
    public static String getErrorPage(int width, int height) {
        return "<html><body>" +
                "<img src='images/Sad512.png' width='" + width + "' height='" + height + "' />" +
                "</body></html>";
    }

    /**
     * Generate the Settings display HTML page.
     *
     * @param settingsModel the settings model
     * @return the HTML content
     */
    public static String getSettingsPage(SettingsModel settingsModel) {
        return "<html><body>" +
                "<table><tr>" +
                "<td><img src='images/Server96.png' height='32' width='32' /></td>" +
                "<td>WebServer</td>" +
                "<td><b>" + settingsModel.webserver + "</b></td>" +
                "</tr><tr>" +
                "<td><img src='images/LineChart96.png' height='32' width='32' /></td>" +
                "<td>Quantity</td>" +
                "<td><b>" + settingsModel.quantity + "</b> points per hour</td>" +
                "</tr></table></body></html>";
    }

}
