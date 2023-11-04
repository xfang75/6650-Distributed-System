package client;

import java.io.FileWriter;
import java.io.IOException;

public class CSVWritter {
  FileWriter fileWriter;
  public CSVWritter(FileWriter fileWriter) {
    this.fileWriter = fileWriter;
  }

  public void writeToCSV(String startTime, String requestType, long latency, int responseCode)
      throws IOException {
    fileWriter.append(startTime);
    fileWriter.append(",");
    fileWriter.append(requestType);
    fileWriter.append(",");
    fileWriter.append(String.valueOf(latency));
    fileWriter.append(",");
    fileWriter.append(String.valueOf(responseCode));
    fileWriter.append("\n");
  }
}
