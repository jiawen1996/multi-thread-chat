/**
 * Contenir des threads utilisé par la classe Client
 */
package thread;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import message.TextMessage;

/**
 * Un Thread pour envoyer le message de client
 * 
 * @author Linh Nguyen - Jiawen Lyu
 */
public class SendMessage extends Thread {
	// La variable pour récupérer l'entre sur la console
	private Scanner scn = new Scanner(System.in);
	// Le stream de sortie
	private final ObjectOutputStream outputStream;
	// Le signal permettant savoir si le socket est fermé
	private Boolean closed = false;

	public SendMessage(ObjectOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void sendObject(Object obj) throws IOException {
		this.outputStream.writeObject(obj);
		this.outputStream.flush();
	}

	public void terminerSocket() {
		// Attendre que le serveur ferme la connexion
		try {
			this.sleep(20);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			this.outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Préparer le message et l'envoyer au serveur
	 */

	public void run() {
		// Démarrer le heartbeatAgent dans le thread de sendMessage
		HeartbeatAgent heartbeatAgent = new HeartbeatAgent(outputStream, this.closed);
		heartbeatAgent.setPriority(Thread.MIN_PRIORITY);
		heartbeatAgent.start();

		while (!this.closed) {
			synchronized (this) {

				// Récupérer le contenu du message
				String sendMsg = scn.nextLine();
				if (sendMsg != null) {
					try {
						this.sendObject(new TextMessage(sendMsg));

						// Quitter la boucle après avoir envoyer le message de fermeture du socket
						if (sendMsg.equals("exit")) {
							// Fermer le flux de SendMessage
							this.closed = true;
							// Fermer le flux de heartbeatAgent
							heartbeatAgent.closeHeartbeatAgent(true);
							break;
						}
					} catch (IOException ex) {
						// TODO: handle exception
						Logger.getLogger(SendMessage.class.getName()).log(Level.SEVERE, null, ex);
						System.out.println("Échoué à envoyer le message. ");
						break;
					}
				}
			}

		}

		// Fermer le flux
		if (this.closed) {
			terminerSocket();
		}

	}
}
