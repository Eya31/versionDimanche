package tn.SGII_Ville.exception;

public class RessourceIndisponibleException extends Exception {
    
    public RessourceIndisponibleException(String message) {
        super(message);
    }
    
    public RessourceIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}