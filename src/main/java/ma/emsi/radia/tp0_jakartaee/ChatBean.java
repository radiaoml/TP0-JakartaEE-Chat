package ma.emsi.radia.tp0_jakartaee; 

import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped; 
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Named("bb") 
@ViewScoped 
public class ChatBean implements Serializable {

    // Properties mapped to the XHTML fields
    private String roleSysteme = "Traducteur Anglais-Français";
    private boolean roleSystemeChangeable = true; // Used for disabled attribute on p:selectOneMenu
    private String question;
    private String reponse;
    private String conversation = "";

    // Map to hold the list of roles for p:selectOneMenu
    private final Map<String, String> rolesSysteme;

    public ChatBean() {
        // Initialize the map with the required roles
        rolesSysteme = new LinkedHashMap<>();
        rolesSysteme.put("Traducteur Anglais-Français", "Traducteur Anglais-Français");
        rolesSysteme.put("Correcteur de texte", "Correcteur de texte");
        rolesSysteme.put("Assistant de programmation", "Assistant de programmation");
        rolesSysteme.put("helpful assistant", "helpful assistant"); // Added as seen in the UI image
    }

    // ********** ACTION METHODS **********

    /**
     * Handles the 'Envoyer la question' button click.
     * Implements the required simple processing: adds role, changes case, adds ||.
     * @return null to stay on the same view and preserve the @ViewScoped bean instance.
     */
    public String envoyer() {
        // ... (previous checks and processing code remains the same) ...

        // 1. Disable role selection after the first question is sent
        roleSystemeChangeable = false;

        // 2. Perform the simple required processing
        String processedText = question.toUpperCase();
        reponse = "||Rôle: " + roleSysteme + " | " + processedText + "||";

        // 3. Update conversation history with clear line breaks
        // Ensure a newline before the new entry starts if the conversation is not empty
        if (!conversation.isEmpty()) {
            conversation += "\n"; 
        }
        
        // This structure closely mimics the desired output:
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
        // The conversation history is intentionally kept here.
        return null;
    }
    
    /**
     * Handles the 'Nouveau chat' button click.
     * @return "index.xhtml" to force a view change, destroying the current @ViewScoped instance
     * and creating a new one (resetting the entire application state/conversation).
     */
    public String nouveauChat() {
        // Note: The return value must match the resource name used in web.xml or navigation rules.
        // Assuming your page is index.xhtml:
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
    // No setter for conversation, as it's only updated by the server logic

    public boolean isRoleSystemeChangeable() { return roleSystemeChangeable; }
    // No setter for roleSystemeChangeable, as it's updated only in the bean

    public Map<String, String> getRolesSysteme() { return rolesSysteme; }
}