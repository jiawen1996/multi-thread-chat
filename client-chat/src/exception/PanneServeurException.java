package exception;

/**
 * Une exception d'erreur afin de générer une message d’erreur personnalisé qui
 * avertit au client que le serveur a échoué.
 * 
 * @author Linh Nguyen - Jiawen Lyu
 *
 */
public class PanneServeurException extends Exception {
	private final String message;

	public PanneServeurException(String message) {
		this.message = message;
	}

	/**
	 * Interface pour récupérer le message d'erreur
	 */
	public String getMessage() {
		return this.message;
	}
}
