package service;

import com.google.gson.*;
import model.ai.AnalysisResult;
import model.ai.ScoreSuggestion;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AiVideoAnalysisService {
    private static final Logger LOGGER = Logger.getLogger(AiVideoAnalysisService.class.getName());
    private final HttpClient httpClient;
    private final String apiKey;

    public static class AiVideoAnalysisException extends Exception {
        public AiVideoAnalysisException(String message) { super(message); }
        public AiVideoAnalysisException(String message, Throwable cause) { super(message, cause); }
    }

    public AiVideoAnalysisService() throws AiVideoAnalysisException {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.apiKey = getenvTrimmed("AI_VIDEO_ANALYSIS_API_KEY");
        if (this.apiKey == null) {
            throw new AiVideoAnalysisException("Clé API Gemini manquante (AI_VIDEO_ANALYSIS_API_KEY).");
        }
    }

    public AnalysisResult analyseInterview(File videoFile) throws AiVideoAnalysisException {
        try {
            // 1. Upload vers Google Files API (Resumable)
            String fileUri = uploadVideo(videoFile);
            String fileName = fileUri.substring(fileUri.lastIndexOf("/") + 1);

            // 2. Attendre que Google traite la vidéo (important pour les vidéos longues)
            waitForVideoProcessing(fileName);

            // 3. Demander l'analyse à Gemini
            return callGemini(fileUri);

        } catch (AiVideoAnalysisException e) {
            // On laisse passer les messages déjà adaptés à l'utilisateur (ex: quota dépassé)
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Échec de l'analyse Gemini", e);
            throw new AiVideoAnalysisException("Erreur lors de l'analyse : " + e.getMessage(), e);
        }
    }

    private String uploadVideo(File videoFile) throws IOException, InterruptedException {
        long fileSize = videoFile.length();
        String mimeType = "video/mp4";

        // Initialisation de l'upload résumable
        HttpRequest initRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/upload/v1beta/files?key=" + apiKey))
                .header("X-Goog-Upload-Protocol", "resumable")
                .header("X-Goog-Upload-Command", "start")
                .header("X-Goog-Upload-Header-Content-Length", String.valueOf(fileSize))
                .header("X-Goog-Upload-Header-Content-Type", mimeType)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> initRes = httpClient.send(initRequest, HttpResponse.BodyHandlers.ofString());
        String uploadUrl = initRes.headers().firstValue("x-goog-upload-url").orElseThrow();

        // Envoi du fichier
        HttpRequest uploadRequest = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("X-Goog-Upload-Offset", "0")
                .header("X-Goog-Upload-Command", "upload, finalize")
                .POST(HttpRequest.BodyPublishers.ofFile(videoFile.toPath()))
                .build();

        HttpResponse<String> uploadRes = httpClient.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(uploadRes.body()).getAsJsonObject();
        return json.getAsJsonObject("file").get("uri").getAsString();
    }

    private void waitForVideoProcessing(String fileName) throws Exception {
        int attempts = 0;
        while (attempts < 20) { // Max 10 minutes d'attente
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/files/" + fileName + "?key=" + apiKey))
                    .GET().build();

            HttpResponse<String> res = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String state = JsonParser.parseString(res.body()).getAsJsonObject().get("state").getAsString();

            if ("ACTIVE".equals(state)) return;
            if ("FAILED".equals(state)) throw new Exception("Le traitement de la vidéo par Google a échoué.");

            Thread.sleep(30000); // Attendre 30 sec entre chaque vérification
            attempts++;
        }
    }

    private AnalysisResult callGemini(String fileUri) throws Exception {
        try {
            // Tentative avec le modèle récent gemini-1.5-flash
            return callGeminiWithModel(fileUri, "gemini-1.5-flash");
        } catch (AiVideoAnalysisException e) {
            String msg = e.getMessage();
            // Si le modèle 1.5 n'est pas disponible pour cette clé / version d'API,
            // on bascule automatiquement vers un modèle plus ancien mais largement disponible.
            if (msg != null &&
                    msg.contains("is not found for API version v1beta, or is not supported for generateContent")) {
                LOGGER.log(Level.WARNING,
                        "Modèle gemini-1.5-flash indisponible pour cette clé. Bascule vers gemini-1.0-pro-vision.",
                        e);
                return callGeminiWithModel(fileUri, "gemini-1.0-pro-vision");
            }
            throw e;
        }
    }

    private AnalysisResult callGeminiWithModel(String fileUri, String modelName) throws Exception {
        // On demande spécifiquement un format JSON à Gemini pour qu'il remplisse AnalysisResult
        String prompt = "Analyse le discours de cette vidéo d'entretien. " +
                "Ignore le visuel, concentre-toi sur la parole. " +
                "Réponds UNIQUEMENT au format JSON suivant : " +
                "{\"globalComment\": \"ton commentaire\", \"scoreSuggestions\": [{\"nomCritere\": \"Nom\", \"noteAttribuee\": 15.0, \"appreciationSpecifique\": \"détail\"}]}";

        JsonObject payload = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject part1 = new JsonObject();
        JsonObject fileData = new JsonObject();
        fileData.addProperty("mime_type", "video/mp4");
        fileData.addProperty("file_uri", fileUri);
        part1.add("file_data", fileData);

        JsonObject part2 = new JsonObject();
        part2.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(part1);
        parts.add(part2);

        JsonObject contentObj = new JsonObject();
        contentObj.add("parts", parts);
        contents.add(contentObj);
        payload.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseGeminiOutput(response.body());
    }

    private AnalysisResult parseGeminiOutput(String responseBody) throws AiVideoAnalysisException {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

            // Gestion explicite des erreurs retournées par l'API Gemini
            if (root.has("error")) {
                JsonObject error = root.getAsJsonObject("error");
                int code = error.has("code") ? error.get("code").getAsInt() : 0;
                String status = error.has("status") ? error.get("status").getAsString() : "";
                String message = error.has("message") ? error.get("message").getAsString() : "Erreur inconnue de l'API Gemini.";

                LOGGER.log(Level.SEVERE, "Erreur retournée par l'API Gemini : code={0}, status={1}, message={2}",
                        new Object[]{code, status, message});

                if (code == 429 || "RESOURCE_EXHAUSTED".equals(status)) {
                    throw new AiVideoAnalysisException(
                            "Le quota d'utilisation de l'API Gemini est dépassé. Détail : " + message
                    );
                }

                throw new AiVideoAnalysisException(
                        "Erreur retournée par l'API Gemini : " + message
                );
            }

            // Extraction du texte JSON imbriqué dans la réponse Gemini
            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) {
                throw new AiVideoAnalysisException(
                        "Réponse inattendue de l'API Gemini : le tableau 'candidates' est vide ou absent."
                );
            }

            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            JsonObject content = firstCandidate.getAsJsonObject("content");
            if (content == null) {
                throw new AiVideoAnalysisException(
                        "Réponse inattendue de l'API Gemini : l'objet 'content' est manquant."
                );
            }

            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null || parts.size() == 0) {
                throw new AiVideoAnalysisException(
                        "Réponse inattendue de l'API Gemini : le tableau 'parts' est vide ou absent."
                );
            }

            JsonObject firstPart = parts.get(0).getAsJsonObject();
            if (!firstPart.has("text")) {
                throw new AiVideoAnalysisException(
                        "Réponse inattendue de l'API Gemini : le champ 'text' est manquant dans 'parts[0]'."
                );
            }

            String rawText = firstPart.get("text").getAsString();

            // Nettoyage si Gemini met des balises ```json
            String cleanJson = rawText.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonObject data = JsonParser.parseString(cleanJson).getAsJsonObject();
            AnalysisResult result = new AnalysisResult();
            if (!data.has("globalComment")) {
                throw new AiVideoAnalysisException(
                        "Le JSON retourné par Gemini ne contient pas le champ 'globalComment'."
                );
            }
            result.setGlobalComment(data.get("globalComment").getAsString());

            List<ScoreSuggestion> suggestions = new ArrayList<>();
            JsonArray scores = data.getAsJsonArray("scoreSuggestions");
            if (scores == null) {
                throw new AiVideoAnalysisException(
                        "Le JSON retourné par Gemini ne contient pas le tableau 'scoreSuggestions'."
                );
            }

            for (JsonElement el : scores) {
                JsonObject s = el.getAsJsonObject();
                suggestions.add(new ScoreSuggestion(
                        s.get("nomCritere").getAsString(),
                        s.get("noteAttribuee").getAsDouble(),
                        s.get("appreciationSpecifique").getAsString()
                ));
            }
            result.setScoreSuggestions(suggestions);
            return result;
        } catch (JsonParseException | IllegalStateException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Échec du parsing de la réponse Gemini : " + responseBody, e);
            throw new AiVideoAnalysisException(
                    "La réponse de l'API Gemini est dans un format inattendu et n'a pas pu être analysée.", e
            );
        }
    }

    private static String getenvTrimmed(String key) {
        String val = System.getenv(key);
        return (val != null) ? val.trim() : null;
    }
}