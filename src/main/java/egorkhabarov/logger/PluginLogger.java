package egorkhabarov.logger;

import egorkhabarov.Gems;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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
        this.setupLogger();
    }

    public void setupLogger() {
        try {
            File logDir = new File(plugin.getDataFolder(), "logs");
            if (!logDir.exists() && !logDir.mkdirs()) {
                this.plugin.getLogger().warning("Не удалось создать папку logs!");
                return;
            }
            for (var handler : PluginLogger.logger.getHandlers()) {
                handler.close();
                logger.removeHandler(handler);
            }

            LocalDate currentDate = LocalDate.now();
            FileHandler fileHandler = this.getFileHandler(logDir, currentDate);
            PluginLogger.logger.addHandler(fileHandler);
            PluginLogger.logger.setUseParentHandlers(false);
            PluginLogger.logger.setLevel(Level.INFO);
            this.archiveOldLogs(logDir, currentDate);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось настроить логирование: " + e.getMessage());
        }
    }

    private @NotNull FileHandler getFileHandler(File logDir, LocalDate date) throws IOException {
        String fileName = logDir + File.separator + date + ".log";
        FileHandler handler = new FileHandler(fileName, true);

        handler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                Date logDate = new Date(record.getMillis());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getDefault());
                String timestamp = sdf.format(logDate);
                return String.format("[%s] %s%n", timestamp, record.getMessage());
            }
        });

        handler.setLevel(Level.ALL);
        return handler;
    }

    private void archiveOldLogs(File logDir, LocalDate currentDate) {
        File[] logFiles = logDir.listFiles((dir, name) ->
            name.endsWith(".log") && !name.equals(currentDate + ".log"));

        if (logFiles == null) {
            return;
        }

        for (File logFile : logFiles) {
            File gzFile = new File(logFile.getAbsolutePath() + ".gz");

            try (
                FileInputStream fis = new FileInputStream(logFile);
                GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(gzFile))
            ) {
                fis.transferTo(gzos);
            } catch (IOException e) {
                plugin.getLogger().severe("Ошибка при архивировании " + logFile.getName() + ": " + e.getMessage());
                continue;
            }

            try {
                Files.delete(logFile.toPath());
                plugin.getLogger().info("Архивирован лог: " + gzFile.getName());
            } catch (IOException e) {
                plugin.getLogger().warning("Не удалось удалить старый лог " + logFile.getName());
            }
        }
    }

    public void log(String playerName, String operationDetails) {
        PluginLogger.logger.info(String.format("[%s]: %s", playerName, operationDetails));
    }
}
