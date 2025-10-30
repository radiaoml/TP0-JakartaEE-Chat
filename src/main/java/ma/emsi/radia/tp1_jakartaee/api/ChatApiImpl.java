package ma.emsi.radia.tp1_jakartaee.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import ma.emsi.radia.tp1_jakartaee.llm.JsonUtilPourGemini;
import ma.emsi.radia.tp1_jakartaee.llm.RequeteException;

@RequestScoped
public class ChatApiImpl implements ChatApi { 

    @Inject
    private JsonUtilPourGemini jsonUtil; 
    
    private String lastSentJson = "";
    private String lastReceivedJson = "";

    @Override
    public String sendMessage(String roleSysteme, String question) throws Exception {
        // ... (Logique non modifiée de sendMessage) ...
        jsonUtil.setSystemRole(roleSysteme);
        
        try {
            JsonUtilPourGemini.LlmInteraction interaction = jsonUtil.envoyerRequete(question);
            
            this.lastSentJson = interaction.texteRequeteJson;
            this.lastReceivedJson = interaction.texteReponseJson;
            
            return interaction.reponseTexte;

        } catch (RequeteException e) {
            this.lastSentJson = e.getRequeteJson();
            this.lastReceivedJson = "{}";
            throw new Exception("Erreur de l'API LLM: " + e.getMessage());
        }
    }
    
    // NOUVELLE MÉTHODE REQUISE : Implémentation de la méthode abstraite de l'interface ChatApi
    @Override
    public void resetConversation() {
        // Si le scope de ChatApiImpl est @RequestScoped, la réinitialisation n'est pas strictement 
        // nécessaire car chaque requête obtient une nouvelle instance. 
        // Cependant, comme JsonUtilPourGemini gère l'historique JSON, 
        // c'est lui qui doit être explicitement réinitialisé.
        
        this.jsonUtil.resetConversation(); 
        
        // Réinitialiser les champs de débogage
        this.lastSentJson = "";
        this.lastReceivedJson = "";
    }

    @Override
    public String getLatestSentJson() {
        return this.lastSentJson;
    }
    
    @Override
    public String getLatestReceivedJson() {
        return this.lastReceivedJson;
    }
}