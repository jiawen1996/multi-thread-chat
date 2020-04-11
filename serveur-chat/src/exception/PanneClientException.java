package exception;

public class PanneClientException extends Exception {
	private String message;

	public PanneClientException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
}
