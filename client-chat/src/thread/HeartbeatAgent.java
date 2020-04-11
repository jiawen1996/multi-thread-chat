/**
 * Contenir le thread utilisé par la classe Client
 */
package thread;

import java.io.IOException;
import java.io.ObjectOutputStream;

import message.HBMessage;

/**
 * Un thread qui permet d’envoyer de HBMessage au serveur toutes les
 * DEFAULT_SAMPLING_PERIOD secondes.
 * 
 * @author Linh Nguyen - Jiawen Lyu
 *
 */

public class HeartbeatAgent extends Thread {

	// Durée du cycle (seconde) d'envoi un HBMessage
	private final int DEFAULT_SAMPLING_PERIOD = 6;

	// Nom de thread HeartbeatAgent par défault pour afficher l'état de ce thread
	// dans le console
	private String DEFAULT_NAME = "HeartbeatAgent";

	// Flag pour contrôler l'exécution de HearbeatAgent
	private Boolean closed = false;

	// Le stream de sortie
	final ObjectOutputStream oos;

	public HeartbeatAgent(ObjectOutputStream oos, Boolean closed) {
		this.oos = oos;
		this.closed = closed;
	}

	/**
	 * Envoyer un objet de HBMessage
	 * 
	 * @throws IOException
	 */
	public void sendObject(Object obj) throws IOException {
		this.oos.writeObject(obj);
		this.oos.flush();
	}

	/**
	 * Fermer le thread HeartbeatAgent
	 * 
	 * @param doClose Fermer le thread lorsque doClose = true
	 */
	public void closeHeartbeatAgent(Boolean doClose) {
		this.closed = doClose;
	}

	/**
	 * Une boucle infinite du thread
	 * Quitter la boucle lorsque closeHeartbeatAgent (true) est exécuté.
	 */
	public void run() {
		System.out.println("Running " + DEFAULT_NAME);
		try {
			while (!this.closed) {
				this.sendObject(new HBMessage());
				// Laissez le thread dormir pendant DEFAULT_SAMPLING_PERIOD secondes.
				Thread.sleep(DEFAULT_SAMPLING_PERIOD * 1000);
			}
			this.oos.close();
		} catch (InterruptedException e) {
			System.out.println("Thread " + DEFAULT_NAME + " interrupted.");
		} catch (IOException e) {
		}

	}

}
