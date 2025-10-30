package ma.emsi.radia.tp1_jakartaee.llm; 

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitaire pour gérer la construction du corps JSON des requêtes 
 * à l'API de Gemini et l'extraction des réponses. Gère l'historique de conversation.
 */
@Dependent
public class JsonUtilPourGemini implements Serializable {
    
    // Classe de données simple pour transporter les résultats de l'interaction LLM
    public static class LlmInteraction implements Serializable {
        public final String texteRequeteJson;
        public final String texteReponseJson;
        public final String reponseTexte;

        public LlmInteraction(String texteRequeteJson, String texteReponseJson, String reponseTexte) {
            this.texteRequeteJson = texteRequeteJson;
            this.texteReponseJson = texteReponseJson;
            this.reponseTexte = reponseTexte;
        }
    }
    
    private String systemRole; 
    // Pointer utilisé pour ajouter de nouveaux messages à la fin du tableau "contents"
    private final JsonPointer pointerContents = Json.createPointer("/contents/-"); 
    
    // Contient l'objet JSON complet de la conversation (incluant l'historique)
    private JsonObject requeteJson;
    
    // Garde le dernier JSON de la requête (formaté) pour le débogage
    private String texteRequeteJson; 

    @Inject 
    private LlmClientPourGemini geminiClient; 

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
    }
    
    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }
    
    /**
     * Pour envoyer une requête à l'API de Gemini.
     */
    public LlmInteraction envoyerRequete(String question) throws RequeteException {
        String requestBody;
        
        // CORRECTION : On construit this.requeteJson ou on le met à jour.
        if (this.requeteJson == null) {
            requeteJson = creerRequeteJson(this.systemRole, question);
        } else {
            requeteJson = ajouteQuestionDansJsonRequete(question);
        }
        
        requestBody = requeteJson.toString();

        Entity<String> entity = Entity.entity(requestBody, MediaType.APPLICATION_JSON_TYPE);
        
        // Mise en forme pour l'affichage dans le JSF AVANT l'envoi
        this.texteRequeteJson = prettyPrinting(this.requeteJson); 
        
        // Envoi de la requête
        try (Response response = geminiClient.envoyerRequete(entity)) {
            String texteReponseJson = response.readEntity(String.class);
            
            if (response.getStatus() == 200) {
                // Le texteRequeteJson formaté est stocké dans LlmInteraction
                return new LlmInteraction(
                        this.texteRequeteJson, 
                        texteReponseJson, 
                        extractReponse(texteReponseJson) // Stocke aussi la réponse dans requeteJson pour l'historique
                );
            } else {
                // Gestion des erreurs
                int status = response.getStatus();
                String errorMessage = status + " : " + response.getStatusInfo();
                
                // 🛠️ AJOUT DE LA GESTION SPÉCIFIQUE DU 503
                if (status == 503) {
                     throw new RequeteException("Service temporairement indisponible (503). Le modèle est surchargé. Veuillez réessayer dans quelques secondes.", this.texteRequeteJson);
                }

                try (JsonReader errorReader = Json.createReader(new StringReader(texteReponseJson))) {
                    JsonObject errorObject = errorReader.readObject();
                    if (errorObject.containsKey("error") && errorObject.getJsonObject("error").containsKey("message")) {
                         errorMessage += " - Détails: " + errorObject.getJsonObject("error").getString("message");
                    }
                } catch (JsonException ignored) {
                    // Si le corps n'est pas un JSON, on utilise le statut par défaut
                }

                throw new RequeteException("Erreur de l'API LLM: " + errorMessage, this.texteRequeteJson);
            }
        }
    }
    
    /**
     * Réinitialise l'état interne de la conversation JSON (historique).
     */
    public void resetConversation() {
        this.requeteJson = null;       // Réinitialise l'objet JSON qui stocke l'historique
        this.texteRequeteJson = null;  // Réinitialise le dernier JSON envoyé
        this.systemRole = null;        // Réinitialise le rôle système
    }

    /**
     * Crée le corps JSON initial avec le rôle système et la première question.
     * @return Le JsonObject racine de la requête.
     */
    private JsonObject creerRequeteJson(String systemRole, String question) {
        // Construction de l'objet de la question
        JsonObject userContent = Json.createObjectBuilder()
                .add("role", "user")
                .add("parts", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("text", question))
                        .build())
                .build();

        JsonArray contents = Json.createArrayBuilder()
                .add(userContent)
                .build();
        
        // Construction de l'objet racine avec l'outil de recherche (GROUNDING)
        JsonObjectBuilder rootBuilder = Json.createObjectBuilder()
                .add("contents", contents)
                // AJOUT DE L'OUTIL GOOGLE SEARCH POUR LA RECHERCHE D'INFOS ACTUELLES
                .add("tools", Json.createArrayBuilder()
                    .add(Json.createObjectBuilder()
                        .add("google_search", Json.createObjectBuilder().build())
                    )
                .build());
                
        // Le rôle système n'est ajouté que si spécifié et UNIQUEMENT à la première requête.
        if (systemRole != null && !systemRole.isBlank()) {
             JsonObject systemInstruction = Json.createObjectBuilder()
                .add("parts", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("text", systemRole)))
                .build();
             rootBuilder.add("system_instruction", systemInstruction);
        }

        return rootBuilder.build();
    }

    /**
     * Ajoute un nouveau message utilisateur au JSON de requête existant pour l'historique.
     * @return Le nouveau JsonObject mis à jour.
     */
    private JsonObject ajouteQuestionDansJsonRequete(String nouvelleQuestion) {
        // Crée l'objet du nouveau message
        JsonObject nouveauMessageUser = Json.createObjectBuilder()
                .add("role", "user")
                .add("parts", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("text", nouvelleQuestion))
                        .build())
                .build();
        
        // Utilise le pointer pour ajouter le nouveau message à la fin du tableau "contents"
        return this.pointerContents.add(this.requeteJson, nouveauMessageUser);
    }

    /**
     * Met en forme un JsonObject pour un affichage lisible.
     */
    private String prettyPrinting(JsonObject jsonObject) {
        Map<String, Boolean> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
            jsonWriter.write(jsonObject);
        }
        return stringWriter.toString();
    }

    /**
     * Extrait la réponse textuelle du JSON de l'API et met à jour l'historique.
     */
    private String extractReponse(String json) throws RequeteException {
        try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
            JsonObject jsonObject = jsonReader.readObject();
            
            // Vérification de la présence des candidats
            if (!jsonObject.containsKey("candidates") || jsonObject.getJsonArray("candidates").isEmpty()) {
                throw new RequeteException("Réponse de l'API sans 'candidates'.", this.texteRequeteJson);
            }
            
            JsonObject messageReponse = jsonObject
                    .getJsonArray("candidates")
                    .getJsonObject(0)
                    .getJsonObject("content");
            
            // Vérification de la structure de la réponse
            if (messageReponse == null || !messageReponse.containsKey("parts") || messageReponse.getJsonArray("parts").isEmpty()) {
                 // Si la réponse est vide (e.g., bloquée par le filtre de sécurité)
                 if (jsonObject.containsKey("promptFeedback") && jsonObject.getJsonObject("promptFeedback").containsKey("blockReason")) {
                     String reason = jsonObject.getJsonObject("promptFeedback").getString("blockReason");
                     throw new RequeteException("Requête bloquée par les filtres de sécurité de l'API: " + reason, this.texteRequeteJson);
                 }
                 throw new RequeteException("Réponse de l'API vide ou mal formée.", this.texteRequeteJson);
            }

            // Ajoute l'objet JSON de la réponse de l'API au JSON de la prochaine requête
            this.requeteJson = this.pointerContents.add(this.requeteJson, messageReponse);
            
            // Extrait seulement le texte de la réponse
            return messageReponse.getJsonArray("parts").getJsonObject(0).getString("text");
            
        } catch (JsonException e) {
             throw new RequeteException("Impossible de lire la réponse JSON de l'API.", this.texteRequeteJson);
        }
    }
}
