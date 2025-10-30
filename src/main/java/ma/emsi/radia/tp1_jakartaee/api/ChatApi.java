package ma.emsi.radia.tp1_jakartaee.api; // Le package que votre ChatBean utilise pour injecter chatService

// Interface générique de chat pour le LLM
public interface ChatApi {

    /**
     * Envoie un message à l'API LLM en utilisant le rôle système et l'historique de conversation.
     * @param roleSysteme Le rôle actuel du système (utile pour les premières requêtes).
     * @param question La nouvelle question de l'utilisateur.
     * @return La réponse texte extraite de l'API.
     */
    String sendMessage(String roleSysteme, String question) throws Exception; 

    /**
     * Réinitialise l'état de la conversation (historique JSON, etc.).
     */
    void resetConversation();
    
    /**
     * Retourne le dernier JSON de requête envoyé (pour l'affichage de débogage).
     */
    String getLatestSentJson(); 
    
    /**
     * Retourne le dernier JSON de réponse reçu (pour l'affichage de débogage).
     */
    String getLatestReceivedJson();
}