package egorkhabarov.logger;

import egorkhabarov.Gems;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

public class PluginLogger {
    private static final Logger logger = Logger.getLogger("GemsLogger");
    private final Gems plugin;

    public PluginLogger(Gems plugin) {
        this.plugin = plugin;
    }

    public void setupLogger() {
        try {
            // Получаем текущую дату
            LocalDate currentDate = LocalDate.now();
            FileHandler fileHandler = this.getFileHandler(currentDate);
            PluginLogger.logger.addHandler(fileHandler);
            PluginLogger.logger.setUseParentHandlers(false);
            // Архивируем старые логи, если они есть
            this.archiveOldLogs();
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось настроить логирование: " + e.getMessage());
        }
    }

    private @NotNull FileHandler getFileHandler(LocalDate currentDate) throws IOException {
        File logDir = new File(plugin.getDataFolder(), "logs");

        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        String logFileName = logDir + File.separator + currentDate + ".log"; // Путь к текущему лог-файлу
        File logFile = new File(logFileName);

        // Настройка логгера
        FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath(), true); // Дневной файл логов
        fileHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                // return super.format(record); // Форматируем время в UTC
                Date logDate = new Date(record.getMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String timestamp = sdf.format(logDate);

                return String.format("[%s] %s%n", timestamp, record.getMessage());
            }
        });
        fileHandler.setLevel(Level.ALL);
        return fileHandler;
    }

    private void archiveOldLogs() {
        try {
            // Получаем текущую дату для текущего лог-файла
            LocalDate currentDate = LocalDate.now();
            String currentLogFileName = currentDate + ".log"; // Имя текущего лог-файла

            // Получаем файлы в папке logs
            File logDir = new File(plugin.getDataFolder(), "logs");
            File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log") && !name.equals(currentLogFileName));

            // Архивируем старые файлы, если они есть
            if (logFiles != null) {
                for (File logFile : logFiles) {
                    // Архивируем файл в .gz
                    // logFile.renameTo(logFile+".gz");
                    String gzFileName = logFile.getAbsolutePath() + ".gz";
                    try (FileInputStream fis = new FileInputStream(logFile);
                         GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(gzFileName))) {

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            gzos.write(buffer, 0, length);
                        }
                    } catch (IOException e) {
                        plugin.getLogger().severe("Ошибка при архивировании лога: " + e.getMessage());
                    }
                    // try {
                    //     // Удаляем оригинальный лог-файл после архивирования
                    //     Files.delete(Paths.get(logFile.getAbsolutePath()));
                    //     getLogger().info("Лог файл архивирован: " + gzFileName);
                    //
                    // } catch (IOException e) {
                    //     getLogger().severe("Ошибка при удалении старого лога");
                    // }
                }
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось архивировать старые логи: " + e.getMessage());
        }
    }

    public void log(String playerName, String operationDetails) {
        PluginLogger.logger.info("["+playerName+"]" + ": " + operationDetails);
    }
}
