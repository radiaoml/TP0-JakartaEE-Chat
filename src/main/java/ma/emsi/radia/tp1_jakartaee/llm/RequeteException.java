package ma.emsi.radia.tp1_jakartaee.llm;

/**
 * Exception personnalisée levée lorsqu'une requête LLM échoue (par exemple, code de réponse HTTP non 200).
 * Contient le corps JSON de la requête qui a échoué à des fins de débogage.
 */
public class RequeteException extends Exception {
    private final String requeteJson;

    public RequeteException(String message, String requeteJson) {
        super(message);
        this.requeteJson = requeteJson;
    }

    public String getRequeteJson() {
        return requeteJson;
    }
}

