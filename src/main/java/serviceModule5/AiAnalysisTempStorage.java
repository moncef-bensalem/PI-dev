package serviceModule5;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import modelModule5.ai.AnalysisResult;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AiAnalysisTempStorage {

    private static final String TEMP_FILE_NAME = "ai-evaluation.json";
    private static final Gson GSON = new GsonBuilder().create();
    private static final Logger LOGGER = Logger.getLogger(AiAnalysisTempStorage.class.getName());

    private Path getTempFilePath() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return new File(tmpDir, TEMP_FILE_NAME).toPath();
    }

    public void save(AnalysisResult result) {
        if (result == null) {
            return;
        }

        Path tempFilePath = getTempFilePath();

        try (Writer writer = Files.newBufferedWriter(tempFilePath, StandardCharsets.UTF_8)) {
            GSON.toJson(result, writer);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Impossible d'écrire l'analyse IA temporaire: " + tempFilePath, e);
        }
    }

    public Optional<AnalysisResult> loadAndClear() {
        Path tempFilePath = getTempFilePath();

        if (!Files.exists(tempFilePath)) {
            return Optional.empty();
        }

        try (Reader reader = Files.newBufferedReader(tempFilePath, StandardCharsets.UTF_8)) {
            AnalysisResult result = GSON.fromJson(reader, AnalysisResult.class);

            try {
                Files.deleteIfExists(tempFilePath);
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Impossible de supprimer le fichier temporaire d'analyse IA: " + tempFilePath, e);
            }

            return Optional.ofNullable(result);
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.WARNING, "Impossible de lire / parser le fichier temporaire d'analyse IA: " + tempFilePath, e);

            try {
                Files.deleteIfExists(tempFilePath);
            } catch (IOException deleteException) {
                LOGGER.log(Level.FINE, "Impossible de supprimer le fichier temporaire d'analyse IA après erreur: " + tempFilePath, deleteException);
            }

            return Optional.empty();
        }
    }
}

