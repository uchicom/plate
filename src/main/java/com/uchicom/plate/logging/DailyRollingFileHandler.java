// (C) 2025 uchicom
package com.uchicom.plate.logging;

import com.uchicom.plate.Constants;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class DailyRollingFileHandler extends StreamHandler {

  private final String fileNameFormat;
  private Path logDirPath;
  private LocalDate logDate;

  public DailyRollingFileHandler(String fileNameFormat) throws IOException {
    this.fileNameFormat = fileNameFormat;
    init();
  }

  void init() {
    logDate = LocalDate.now(ZoneId.of("Asia/Tokyo"));
    logDirPath = Paths.get(Constants.LOG_DIR);
    if (!logDirPath.toFile().exists()) {
      logDirPath.toFile().mkdirs();
    }
    setFormatter(
        new Formatter() {
          @Override
          public String format(LogRecord record) {
            ZonedDateTime zdt =
                ZonedDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault());
            String source;
            if (record.getSourceClassName() != null) {
              source = record.getSourceClassName();
              if (record.getSourceMethodName() != null) {
                source += " " + record.getSourceMethodName();
              }
            } else {
              source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
              StringWriter sw = new StringWriter();
              PrintWriter pw = new PrintWriter(sw);
              pw.println();
              record.getThrown().printStackTrace(pw);
              pw.close();
              throwable = sw.toString();
            }
            return String.format(
                Constants.LOG_FORMAT,
                zdt,
                source,
                record.getLoggerName(),
                record.getLevel().getLocalizedName(),
                message,
                throwable);
          }
        });
    open(logDate);
  }

  @Override
  public synchronized void publish(LogRecord record) {
    var recordDate = LocalDate.ofInstant(record.getInstant(), ZoneId.systemDefault());

    // Change the log file
    if (!logDate.equals(recordDate)) {
      open(recordDate);
      logDate = recordDate;
      try {
        Thread.sleep(50);
      } catch (InterruptedException ex) {
        reportError(null, ex, ErrorManager.GENERIC_FAILURE);
      }
    }

    super.publish(record);
    flush();
  }

  void open(LocalDate fileDate) {
    try {
      Path path = logDirPath.resolve(generateFilename(fileDate));
      setOutputStream(
          Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
    } catch (IOException ex) {
      reportError(null, ex, ErrorManager.GENERIC_FAILURE);
    }
  }

  String generateFilename(LocalDate date) {
    return fileNameFormat.replaceAll("%d", DateTimeFormatter.ISO_DATE.format(date));
  }
}
