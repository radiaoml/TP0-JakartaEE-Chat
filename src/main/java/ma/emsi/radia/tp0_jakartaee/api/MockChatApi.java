import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MockChatApi implements ChatApi {
    @Override
    public String sendMessage(String role, String message) {
        // This is where your current processing logic belongs!
        String processedText = message.toUpperCase();
        return "||Rôle: " + role + " | " + processedText + "||";
    }
}