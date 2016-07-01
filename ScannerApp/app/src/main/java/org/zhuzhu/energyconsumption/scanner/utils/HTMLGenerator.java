/**
 * Copyright (C) 2016 Chenfeng ZHU
 */
package org.zhuzhu.energyconsumption.scanner.utils;

import java.util.Map;

/**
 * This is to generate the HTML contents according to the data received.
 *
 * @author Chenfeng Zhu
 */
public final class HTMLGenerator {

    private final static int G_WIDTH = 270;
    private final static int G_HEIGHT = 150;
    private final static String[] LINE_COLORS = {"#ff0066", "#00cc00", "#0066cc", "#6600ff"};
    private final static int COLOR_NUMBER = LINE_COLORS.length;
    private static int token = 0;
    private final static String LINE_COLOR = "#f52714";
    private final static String TEXT_COLOR = "#f52714";
    private final static int LINE_WIDTH = 3;
    private final static String BACKGROUND_COLOR = "transparent";

    /**
     * Get the HTML content according to the data.
     *
     * @param data dataset
     * @return the HTML content
     */
    public static String getHTMLContent(Map<String, Double> data) {
        return getHTMLContent(data, G_WIDTH, G_HEIGHT);
    }

    /**
     * Get the HTML content according to the data.
     *
     * @param data   dataset
     * @param width  width of graph
     * @param height height of graph
     * @return the HTML content
     */
    public static String getHTMLContent(Map<String, Double> data, int width, int height) {
        // TODO: improve
        String lineColor = LINE_COLORS[token%COLOR_NUMBER];
        StringBuffer sb = new StringBuffer();
        String content1 = "<html>"
                + "  <head>"
                + "    <script type='text/javascript' src='js/jsapi.js'></script>"
                + "    <script type='text/javascript'>"
                + "      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});"
                + "      google.setOnLoadCallback(drawChart);"
                + "      function drawChart() {"
                + "        var data = google.visualization.arrayToDataTable(["
                + "          ['Time', 'Energy'],";
        sb.append(content1);
        StringBuffer sbData = new StringBuffer();
        if (data != null && data.size() > 0) {
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                sbData.append("['" + entry.getKey() + "',  " + entry.getValue() + "],");
            }
            sb.append(sbData.substring(0, sbData.length() - 1));
        } else {
            return "<html><body>No data found.</body></html>";
            // testing data.
//            String tmp = "          ['14:00',  103],"
//                    + "          ['18:00',  100],"
//                    + "          ['19:00',  117],"
//                    + "          ['20:00',  66],"
//                    + "          ['21:00',  103]";
//            sb.append(tmp);
        }
        String content2 = "        ]);"
                + "        var options = {"
//                + "          title: 'Energy Consumption',"
                + "          width: " + width + ","
                + "          height: " + height + ","
                + "          lineWidth: " + LINE_WIDTH + ","
                + "          colors: ['" + lineColor + "'],"
                + "          backgroundColor: '" + BACKGROUND_COLOR + "',"
                + "          hAxis: { textPosition: 'none' },"
                + "          vAxis: { textStyle: { color: '" + TEXT_COLOR + "' } },"
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
        sb.append(content2);
        token++;
        return sb.toString();
    }

}
