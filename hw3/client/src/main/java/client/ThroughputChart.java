package client;

import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYDataset;

import java.io.File;
import java.io.IOException;

public class ThroughputChart {

//  public static void main(String[] args) {
//    JFreeChart chart = createChart(createDataset());
//
//    // Set chart dimensions (width x height) in pixels
//    int chartWidth = 800;
//    int chartHeight = 600;
//
//    // Create a file to save the chart (you can change the file path)
//    File chartFile = new File("throughput_chart.png");
//
//    try {
//      // Save the chart as a PNG image
//      ChartUtils.saveChartAsPNG(chartFile, chart, chartWidth, chartHeight);
//      System.out.println("Chart saved as " + chartFile.getAbsolutePath());
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//  }

  static DefaultXYDataset createDataset(ArrayList<Integer> throughputList) {
    DefaultXYDataset dataset = new DefaultXYDataset();

    int totalTime = throughputList.size(); // Total time in seconds
    double[][] data = new double[2][totalTime];
    data[0] = new double[totalTime]; // X-axis (seconds)
    data[1] = new double[totalTime]; // Y-axis (throughput/second)
    for (int i = 0; i < totalTime; i++) {
      data[0][i] = i; // Time in seconds
      data[1][i] = throughputList.get(i); // Throughput for each second
    }
    dataset.addSeries("Throughput", data);
    return dataset;
  }

  static JFreeChart createChart(DefaultXYDataset dataset) {
    JFreeChart chart = ChartFactory.createXYLineChart(
        "Average Throughput Over Time", // Chart title
        "Time (seconds)", // X-axis label
        "Throughput/second", // Y-axis label
        dataset, // Dataset
        PlotOrientation.VERTICAL, // Plot orientation
        false, // Show legend
        false, // Use tooltips
        false // Configure chart to generate URLs?
    );

    // Customize chart appearance
    chart.setTitle(new TextTitle("Average Throughput Over Time"));
    XYPlot plot = (XYPlot) chart.getPlot();
    XYSplineRenderer renderer = new XYSplineRenderer();
    renderer.setSeriesShapesVisible(0, false);
    plot.setRenderer(renderer);

    return chart;
  }
}
