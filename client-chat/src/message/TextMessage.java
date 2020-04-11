package message;

import java.io.Serializable;

/**
 * Un objet de message qui contient des vrais messages envoyés par les clients
 * 
 * @author Linh Nguyen - Jiawen Lyu
 */

public class TextMessage implements Serializable{
	private String msg = "";

	public TextMessage(String msg) {
		this.msg = msg;
	}
	
	/**
	 * interface pour récupérer le message dans l'objet TextMessage
	 */
	public String getMsg() {
		return this.msg;
	}
}
