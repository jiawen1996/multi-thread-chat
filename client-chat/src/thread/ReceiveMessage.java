package thread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Contenir des threads utilisé par la classe Client
 */
import message.HBMessage;
import message.TextMessage;

/**
 * Un Thread qui traite des messages reçus de client
 * 
 * @author Linh Nguyen
 * @author Jiawen Lyu
 *
 */
public class ReceiveMessage extends Thread {

	// Le stream d'entré
	private final ObjectInputStream inputStream;

	// Le socket communication de client

	private Socket client;
	// Le contenu de message reçu
	private String receivedMsg;

	private Boolean closed = false;

	private static Queue<String> hbMsgList = new LinkedList<>();

	public ReceiveMessage(Socket client, ObjectInputStream inputStream) {
		this.inputStream = inputStream;
		this.client = client;
	}

	/**
	 * La méthode de traitement des messages reçus
	 * 
	 * @throws ClassNotFoundException
	 * 
	 * @throws IOException
	 * 
	 */
	public void interpreterMessage() throws ClassNotFoundException, IOException {
		Object obj = this.inputStream.readObject();
		if (obj instanceof HBMessage) {
			hbMsgList.add("ACK");

		} else {
			TextMessage receivedObj = (TextMessage) obj;
			this.receivedMsg = receivedObj.getMsg();
		}

	}

	public void terminerSocket() {
		// Attendre que le serveur ferme la connexion
		try {
			this.sleep(20);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Fermer la connexion
		try {
			this.inputStream.close();
			this.client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {

		try {

			// Démarrer hbListener
			HeartbeatListener hbListener = new HeartbeatListener(hbMsgList, this.closed);
			hbListener.setPriority(Thread.MIN_PRIORITY);
			hbListener.start();

			// Récupérer les messages jusqu'à quand il reçoit le message de fin
			while (!this.closed) {

				try {
					interpreterMessage();
					// Recevoir le message entrant
					if (this.receivedMsg != null) {

						synchronized (this) {

							// Quitter la boucle si le socket a fermé, sinon afficher le message sur la
							// console
							if (this.receivedMsg.equals("Vous avez quitté la conversation")) {
								System.out.println("Bye!");
								this.closed = true;
								break;
							} else {
								System.out.println(this.receivedMsg);
								this.receivedMsg = null;
							}
						}
					}

				} catch (IOException ex) {
					terminerSocket();
					System.out.println(" Message reçu est erroné ");
					break;
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (this.closed) {
				hbListener.closeHBListener(true);
				terminerSocket();
			}
		} catch (Exception e) {
			System.out.println("server die");
			// TODO: handle exception
		}
	}
}
