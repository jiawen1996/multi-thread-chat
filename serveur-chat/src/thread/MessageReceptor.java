/**
 * Contenir le thread utilisé par la classe Server
 */
package thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

import message.HBMessage;
import message.TextMessage;

/**
 * Un Thread qui traite des messages provenance d'un client de serveur
 * 
 * @author Linh Nguyen - Jiawen Lyu
 *
 */
public class MessageReceptor extends Thread {
	// La liste de clients actifs
	private static Hashtable<MessageReceptor, String> listClient = new Hashtable<MessageReceptor, String>();
	private static Queue<String> hbMsgList = new LinkedList<>();
	// Le Socket de communication d'un client
	private Socket client;
	// Le pseudonyme du client
	private String clientName;
	// Le stream d'entrée
	private ObjectInputStream inputStream = null;
	// Le stream de sortie
	private ObjectOutputStream outputStream = null;
	private Boolean closed = false;

	public MessageReceptor(Socket client, Hashtable<MessageReceptor, String> listClient) {
		this.client = client;
		this.listClient = listClient;
		this.clientName = "";
	}

	/**
	 * Envoyer l'acqiuttement de Heartbeat au client
	 * 
	 * @throws IOException
	 */
	public void hbMsgHandler() throws IOException {
		sendObject(this, new HBMessage());
		hbMsgList.add(this.clientName);
	}

	/**
	 * Envoyer l'objet de message à la destination
	 * 
	 * @param destination
	 * @param obj
	 * @throws IOException
	 */
	public void sendObject(MessageReceptor destination, Object obj) throws IOException {
		destination.outputStream.writeObject(obj);
		destination.outputStream.flush();
	}

	/**
	 * Annoncer la déconnexion aux autres clients et terminer les I/O flux et le
	 * socket
	 * 
	 */
	public void terminerSocket() {
		try {
			System.out.println(this.clientName + " se déconnecte.");
			listClient.remove(this);

			synchronized (this) {
				if (!listClient.isEmpty()) {
					for (MessageReceptor client : listClient.keySet()) {
						if (client != null && client != this && client.clientName != null) {
							try {
								this.sendObject(client,
										new TextMessage("*** " + this.clientName + " a quitté la conversation ***"));
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
			// Fermer la connexion
			this.inputStream.close();
			this.outputStream.close();
			this.client.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Diffuser le message aux autres clients
	 * 
	 * @param msg
	 * @param clientName
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void broadcast(String msg, String clientName) throws IOException, ClassNotFoundException {
		synchronized (this) {
			for (MessageReceptor client : listClient.keySet()) {
				if (client != null && client.clientName != null && client.clientName != this.clientName) {
					this.sendObject(client, new TextMessage(clientName + " a dit : " + msg));
				}
			}

			System.out.println("Broadcast message a été envoyé par " + this.clientName);
		}
	}

	/**
	 * Communiquer entre le serveur et un client Demander le pseudonyme du client
	 */

	@Override
	public void run() {

		try {

			// Récupérer input and out streams de ce socket
			inputStream = new ObjectInputStream(client.getInputStream());
			outputStream = new ObjectOutputStream(client.getOutputStream());

			// Récupérer le pseudonyme d'un client
			String clientName;

			// Assurer qu'il n'y a qu'un seul thread qui utilise cet objet
			synchronized (this) {
				// Demander le pseudonyme
				this.sendObject(this, new TextMessage("Entrer votre pseudonyme :"));
				InputStream in = this.client.getInputStream();
				boolean isClientNameInitialized = false;
				while (!isClientNameInitialized) {
					if (in.available() > 0) {
						Object obj = inputStream.readObject();
						//
						if (obj instanceof HBMessage) {
							hbMsgHandler();
						} else {

							TextMessage receivedObj = (TextMessage) obj;
							clientName = receivedObj.getMsg();
							if ((clientName.indexOf('@') == -1) || (clientName.indexOf('!') == -1)
									|| this.listClient.containsValue(clientName)) {

								// Si pseudonyme unique, il va remplacer la valeur dans listClient
								if (this.listClient.containsValue(clientName)) {
									this.sendObject(this,
											new TextMessage("Votre pseudo a été utilisé. Nouveau pseudonyme : "));
									continue;
								} else {
									this.listClient.put(this, clientName);
									this.clientName = clientName;
									isClientNameInitialized = true;
								}
							} else {
								this.outputStream.write(("Le pseudo ne devrait pas contenir '@' ou '!'.").getBytes());
								this.outputStream.flush();
							}
						}
					} else {
						Thread.sleep(10);
					}

				}
			}
			// Démarrer hbListener
			HeartbeatListener hbListener = new HeartbeatListener(hbMsgList, this.closed, this.clientName);
			hbListener.setPriority(Thread.MIN_PRIORITY);
			hbListener.start();

			System.out.println("Pseudo de nouveau client : " + this.clientName);
			this.sendObject(this, new TextMessage(
					this.clientName + " a rejoint la conversation. Tapez 'exit' pour se déconnecter \n"));
			this.sendObject(this,
					new TextMessage("------------------------------------------------------------------------------"));

			// Annoncer aux autres clients
			synchronized (this) {
				for (MessageReceptor client : listClient.keySet()) {
					if (client != null && client != this) {
						this.sendObject(client, new TextMessage(this.clientName + " a  rejoint la conversation"));
					}
				}
			}

			// Commencer la conversation
			// Quit la conversation lorsque le serveur reçoit un message "exit"
			while (true) {
				Object obj = inputStream.readObject();
				if (obj instanceof HBMessage) {
					hbMsgHandler();
				} else {

					TextMessage receivedObj = (TextMessage) obj;
					String msg = receivedObj.getMsg();
					// Quitter la conversation
					if (msg.startsWith("exit")) {
						this.closed = true;
						hbListener.closeHBListener(true);
						break;
					} else {

						// Diffuser le message aux autres clients
						broadcast(msg, this.clientName);
					}
				}
			}

			// Terminer la session
			if (this.closed) {
				this.sendObject(this, new TextMessage("Vous avez quitté la conversation"));
				terminerSocket();
			}

		} catch (IOException e) {
			terminerSocket();
			System.out.println("La session de " + this.clientName + " a terminé.");

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
