package ma.emsi.radia.tp1_jakartaee; 

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.radia.tp1_jakartaee.api.ChatApi; 

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Named("bb") 
@ViewScoped
public class ChatBean implements Serializable {

    // ... (Propriétés existantes) ...
    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    // Propriétés pour le débogage (JSON)
    private String texteRequeteJson = ""; 
    private String texteReponseJson = "";
    
    // NOUVELLE PROPRIÉTÉ REQUISE
    private boolean debug = true; 

    @Inject
    private FacesContext facesContext;

    
    @Inject // INJECTION DU SERVICE DE CHAT
    private ChatApi chatService;

    // ********** GETTERS AND SETTERS **********

    public String getTexteRequeteJson() { return texteRequeteJson; }
    public void setTexteRequeteJson(String texteRequeteJson) { this.texteRequeteJson = texteRequeteJson; }

    public String getTexteReponseJson() { return texteReponseJson; }
    public void setTexteReponseJson(String texteReponseJson) { this.texteReponseJson = texteReponseJson; }
    
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }

    // NOUVELLE MÉTHODE REQUISE : toggleDebug()
    /**
     * Inverse l'état du mode débogage. Utilisée par un bouton dans l'XHTML.
     */
    public void toggleDebug() {
      this.setDebug(!isDebug());
    }

    public ChatBean() {
        // Laisser vide
    }


    public String getRoleSysteme() { 
        if (roleSysteme == null) {
            getRolesSysteme(); 
            if (listeRolesSysteme != null && !listeRolesSysteme.isEmpty()) {
                roleSysteme = (String) listeRolesSysteme.get(0).getValue();
            }
        }
        return roleSysteme; 
    }

    public void setRoleSysteme(String roleSysteme) {
        this.roleSysteme = roleSysteme;
    }

    public boolean isRoleSystemeChangeable() { return roleSystemeChangeable; }

    public String getQuestion() { return question; }

    public void setQuestion(String question) { this.question = question; }

    public String getReponse() { return reponse; }

    
    public void setReponse(String reponse) { this.reponse = reponse; }

    public String getConversation() { return conversation.toString(); }

    public void setConversation(String conversation) { this.conversation = new StringBuilder(conversation); }
    
    // ********** ACTION METHODS **********

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            
            // Étape 1 : Envoyer le message à l'API LLM pour obtenir la réponse.
            // C'est cet appel qui initialise le rôle système, construit le JSON, envoie la requête
            // et stocke les JSON de débogage.
            this.reponse = chatService.sendMessage(this.roleSysteme, this.question); 
            
            // Étape 2 : Récupérer les JSON de la requête et de la réponse pour le débogage
            // Ces valeurs sont maintenant disponibles car elles ont été stockées par sendMessage
            this.texteRequeteJson = chatService.getLatestSentJson();
            this.texteReponseJson = chatService.getLatestReceivedJson(); 

            if (this.conversation.isEmpty()) {
                this.roleSystemeChangeable = false;
            }

            afficherConversation();
            this.question = null;

        } catch (Exception e) {
            // Gérer les erreurs d'appel API
            this.reponse = "ERREUR: Impossible de contacter l'API de chat: " + e.getMessage();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erreur API", this.reponse);
            facesContext.addMessage(null, message);
        }

        return null;
    }

    public String nouveauChat() {
        // Réinitialise l'état du service CDI (historique JSON)
        this.chatService.resetConversation(); 
        // Réinitialise l'état du bean CDI (puisqu'il est @ViewScoped)
        return "index?faces-redirect=true";
    }

    private void afficherConversation() {
        if (this.conversation.isEmpty()) {
             // Ajouter le rôle de l'API au début de la conversation initiale
             this.conversation.append("Rôle de l'API : ").append(this.roleSysteme).append("\n");
        }
        
        // Ajouter la question et la réponse
        this.conversation
            .append("== User:\n").append(question).append("\n") 
            .append("== Serveur:\n").append(reponse).append("\n");
    }

    
    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();
            
            // Rôle par défaut (Assistant)
            String roleAssistant = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            this.listeRolesSysteme.add(new SelectItem(roleAssistant, "Assistant")); // C'est le premier, donc le rôle par défaut.

            String roleTraducteur = """
                    You are an interpreter. You translate from English to French and from French to English.
                    If the user type a French text, you translate it into English.
                    If the user type an English text, you translate it into French.
                    If the text contains only one to three words, give some examples of usage of these words in English.
                    """;
            this.listeRolesSysteme.add(new SelectItem(roleTraducteur, "Traducteur Anglais-Français"));

            String roleGuide = """
                    Your are a travel guide. If the user type the name of a country or of a town,
                    you tell them what are the main places to visit in the country or the town
                    are you tell them the average price of a meal.
                    """;
            this.listeRolesSysteme.add(new SelectItem(roleGuide, "Guide touristique"));
            
            String roleKatseye = """
                    You are an expert on the K-Pop girl group KATSEYE (formed through Dream Academy). 
                    Your mission is to provide comprehensive, accurate, and enthusiastic answers to all questions 
                    regarding the members (Manon, Sophia, Daniela, Lara, Megan, Yoonchae), their discography (songs, albums), 
                    their personal details, and their journey from Dream Academy to their debut. 
                    Always maintain an energetic and positive tone appropriate for a dedicated fan or media outlet.
                    """;
            this.listeRolesSysteme.add(new SelectItem(roleKatseye, "Expert Katseye"));
            
            // Initialisation de roleSysteme si ce n'est pas déjà fait
            if (this.roleSysteme == null) {
                 this.roleSysteme = roleAssistant;
            }
            }
        return this.listeRolesSysteme;
    }
}
