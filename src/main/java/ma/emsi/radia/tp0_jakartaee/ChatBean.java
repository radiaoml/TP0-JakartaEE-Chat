package ma.emsi.radia.tp0_jakartaee;

import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import ma.emsi.radia.tp0_jakartaee.api.ChatApi;
import jakarta.inject.Inject; // 👈 1. ADD THIS REQUIRED IMPORT for @Inject
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Named("bb")
@ViewScoped
public class ChatBean implements Serializable {

    // Properties mapped to the XHTML fields
    private String roleSysteme = "Traducteur Anglais-Français";
    private boolean roleSystemeChangeable = true;
    private String question;
    private String reponse;
    private String conversation = "";

    @Inject // 👈 2. INJECT THE SERVICE PROPERTY HERE
    private ChatApi chatService;

    // Map to hold the list of roles for p:selectOneMenu
    private final Map<String, String> rolesSysteme;

    public ChatBean() {
        // Initialize the map with the required roles
        rolesSysteme = new LinkedHashMap<>();
        rolesSysteme.put("Traducteur Anglais-Français", "Traducteur Anglais-Français");
        rolesSysteme.put("Correcteur de texte", "Correcteur de texte");
        rolesSysteme.put("Assistant de programmation", "Assistant de programmation");
        rolesSysteme.put("helpful assistant", "helpful assistant");
    }

    // ********** ACTION METHODS **********

    /**
     * Handles the 'Envoyer la question' button click, delegating processing to ChatApi.
     * @return null to stay on the same view and preserve the @ViewScoped bean instance.
     */
    public String envoyer() {
        if (question == null || question.trim().isEmpty()) {
            reponse = "Veuillez saisir une question.";
            return null;
        }

        // 1. Disable role selection after the first question is sent
        roleSystemeChangeable = false;

        // 2. DELEGATE PROCESSING TO THE SERVICE
        reponse = chatService.sendMessage(roleSysteme, question); // 👈 3. CALL SERVICE METHOD

        // 3. Update conversation history with clear line breaks
        if (!conversation.isEmpty()) {
            conversation += "\n";
        }
        
        conversation += "* Rôle de l'API: " + roleSysteme + "\n"
                      + "* Utilisateur:\n" + question + "\n"
                      + "* Serveur:\n" + reponse + "\n";
        
        // 4. Clear the question field
        question = null;

        return null;
    }

    /**
     * Handles the 'Effacer question et réponse' action (linked to h:commandButton).
     * Clears the current question and response properties.
     * @return null to stay on the same view.
     */
    public String effacer() {
        this.question = null;
        this.reponse = null;
        return null;
    }
    
    /**
     * Handles the 'Nouveau chat' button click.
     * @return "index.xhtml" to force a view change, destroying the current @ViewScoped instance
     * and creating a new one (resetting the entire application state/conversation).
     */
    public String nouveauChat() {
        return "index.xhtml";
    }

    // ********** GETTERS AND SETTERS **********

    public String getRoleSysteme() { return roleSysteme; }
    public void setRoleSysteme(String roleSysteme) { this.roleSysteme = roleSysteme; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }

    public String getConversation() { return conversation; }
    
    public boolean isRoleSystemeChangeable() { return roleSystemeChangeable; }
    
    public Map<String, String> getRolesSysteme() { return rolesSysteme; }
}