package thread;
import java.util.LinkedList;
import java.util.Queue;

import exception.PanneClientException;
/**
 * Un thread qui collecte tous des HBMessages et évaluer l’état du client.
 * 
 * @author Linh Nguyen - Jiawen Lyu
 *
 */
public class HeartbeatListener extends Thread {
	//une queue afin de stocker des objets de HBMessage
	private static Queue<String> hbMsgList = new LinkedList<>();
	
	// Flag pour contrôler l'exécution de heartbeatListe
	private Boolean closed = false;
	
	//un intervalle de temps pour vérifier que la queue est vraiement vide
	private long DEFAULT_CHECK_PERIOD = 8 * 1000;
	
	//un intervalle de temps pour lire un objet de HBMessage
	private long DEFAULT_READ_PERIOD = 6 * 1000;
	
	final private String clientName;

	public HeartbeatListener(Queue<String> hbMsgList, Boolean closed, String clientName) {
		this.hbMsgList = hbMsgList;
		this.closed = closed;
		this.clientName = clientName;
	}

	
	/**
	 * Fermer le thread HBListener
	 * 
	 * @param doClose Fermer le thread lorsque doClose = true
	 */
	public void closeHBListener(Boolean doClose) {
		this.closed = doClose;
	}

	
	/**
	 * Vérifier que le client est vivant
	 * 
	 * @throws PanneClientException afficher le client qui quitte imprévu.
	 */
	public void checkClientAlive() throws PanneClientException {
		try {
			//Si la queue de HBMsg n'est pas vide : Le client est active
			if (hbMsgList.peek() != null) {
				System.out.println("HBListener:\t" + hbMsgList.remove() + " is alive.");
				Thread.sleep(DEFAULT_READ_PERIOD);

			} else {
				// Sinon : nous pouvons attendre un intervalle de temps(DEFAULT_CHECK_PERIOD) 
				// 		    pour confirmer que le thread ne recevoir plus de nouveaux messages.
				// 			 et retourner une erreur à travers d’une exception personnalisée et aussi terminer le programme.
				Thread.sleep(DEFAULT_CHECK_PERIOD);
				hbMsgList.element();
			}

		} catch (Exception e) {
			throw new PanneClientException("Le client \"" + clientName + "\" est en panne.");
		}
		
	}
	
	
	
	/**
	 * Une boucle infinie jusqu'à ce qu'une situation anormale soit détectée chez un client.
	 * 
	 */
	public void run() {
		while (!this.closed) {
			try {
				checkClientAlive();
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
				this.closeHBListener(true);
			}
		}
	}
}
