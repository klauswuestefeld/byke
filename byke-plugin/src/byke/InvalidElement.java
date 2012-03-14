package byke;


public class InvalidElement extends Exception {

	public InvalidElement(String message) {
		super(message);
	}

	public InvalidElement(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 1L;

}
