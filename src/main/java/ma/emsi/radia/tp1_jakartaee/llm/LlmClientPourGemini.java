package ma.emsi.radia.tp1_jakartaee.llm;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;

/**
 * Client JAX-RS (Jakarta RESTful Web Services) pour interagir avec l'API de Gemini.
 * Gère la configuration de l'URL et de la clé API.
 */
@ApplicationScoped
public class LlmClientPourGemini implements Serializable {

    // ⚠️ ATTENTION : COLLER VOTRE CLÉ D'API COMPLÈTE ICI.
    // Clé fournie par l'utilisateur: AIzaSyBemU-lRoBCCZAOh9M2Tnp-Bddv3NYrNnc
    // Assurez-vous d'utiliser la clé complète si celle-ci était tronquée.
    private static final String API_KEY = "AIzaSyBemU-lRoBCCZAOh9M2Tnp-Bddv3NYrNnc"; 
    
    // URL du modèle que nous allons utiliser pour le chat (gemini-2.5-flash)
    private static final String BASE_URL = 
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"; 

    private transient Client client;
    private WebTarget webTarget;

    /**
     * Initialisation du client JAX-RS après la construction de l'objet CDI.
     */
    @PostConstruct
    public void init() {
        this.client = ClientBuilder.newClient();
        // Le WebTarget inclut l'URL de base et la clé d'API comme paramètre de requête
        this.webTarget = client.target(BASE_URL).queryParam("key", API_KEY);
    }

    /**
     * Envoie la requête JSON (body) à l'API de Gemini et retourne la réponse HTTP brute.
     * @param entity L'entité (corps JSON) de la requête.
     * @return La réponse HTTP brute (doit être fermée par l'appelant, ici JsonUtilPourGemini).
     */
    public Response envoyerRequete(Entity<String> entity) {
        // La méthode init() garantit que webTarget n'est pas null
        return webTarget.request().post(entity);
    }
}
