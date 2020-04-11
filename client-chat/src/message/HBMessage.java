package message;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Un objet de message pour le mécanisme de heartbeat qui contient le temps de génération.
 * 
 * @author Linh Nguyen - Jiawen Lyu
 *
 */
public class HBMessage implements Serializable {
	
	/**
	 *Convertir le temps de génération en string
	 */
	public String toString() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
}
