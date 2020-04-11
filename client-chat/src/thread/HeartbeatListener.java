package thread;

import java.util.LinkedList;
import java.util.Queue;

import exception.PanneServeurException;

/**
 * Un thread qui collecte tous des HBMessages et évaluer l’état du serveur.
 * 
 * @author Linh Nguyen - Jiawen Lyu
 *
 */
public class HeartbeatListener extends Thread {

	// une queue afin de stocker des objets de HBMessage
	private static Queue<String> hbMsgList = new LinkedList<>();

	// Flag pour contrôler l'exécution de heartbeatListe
	private Boolean closed = false;

	// un intervalle de temps pour vérifier que la queue est vraiement vide
	private long DEFAULT_CHECK_PERIOD = 10 * 1000;

	// un intervalle de temps pour lire un objet de HBMessage
	private long DEFAULT_READ_PERIOD = 6 * 1000;

	public HeartbeatListener(Queue<String> hbMsgList, Boolean closed) {
		this.hbMsgList = hbMsgList;
		this.closed = closed;
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
	 * Vérifier que le serveur est vivant
	 * 
	 * @throws PanneServeurException afficher le serveur qui quitte imprévu.
	 */
	public void checkServeurAlive() throws PanneServeurException {
		try {
			// Si la queue de HBMsg n'est pas vide : Le serveur est active
			if (hbMsgList.peek() != null) {
				hbMsgList.poll();
				Thread.sleep(DEFAULT_READ_PERIOD);
			} else {
				// Sinon : nous pouvons attendre un intervalle de temps(DEFAULT_CHECK_PERIOD)
				// pour confirmer que le thread ne recevoir plus de nouveaux messages.
				// et retourner une erreur à travers d’une exception personnalisée et aussi
				// terminer le programme.
				Thread.sleep(DEFAULT_CHECK_PERIOD);
				hbMsgList.element();
			}

		} catch (Exception e) {
			throw new PanneServeurException("Le serveur est en panne.");
		}

	}

	/**
	 * Une boucle infinie jusqu'à ce qu'une situation anormale soit détectée chez serveur.
	 * 
	 */
	public void run() {
		while (!this.closed) {
			try {
				checkServeurAlive();
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
				System.exit(0);
			}
		}
	}
}
