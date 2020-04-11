/**
 * Contenir la classe Server
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import thread.MessageReceptor;

/**
 * 
 * Créer un socket de connexion au port défini. Créer un socket de communication
 * pour chaque client. Passer le socket client dans un thread MessageReceptor
 * pour intercepter les messages d'un client.
 * 
 * @author Linh Nguyen - Jiawen Lyu
 *
 */
public class Server {

	// Le vector listClient pour stocker des sockets de clients actifs
	private static Hashtable<MessageReceptor, String> listClient = new Hashtable<MessageReceptor, String>();
	// Le socket de connexion
	private static ServerSocket serverSocket;
	// Socket de communication avec le client
	private static Socket client;
	// Le numéro du port
	private static final int PORT_NUMBER = 1234;

	public static void main(String[] args) throws IOException {
		// Le socket de connexion au port 1234
		try {
			serverSocket = new ServerSocket(PORT_NUMBER);
			System.out.println("Le socket de connexion a été créé au port " + PORT_NUMBER);
		} catch (Exception e) {
			System.out.println("Échoué à créer un socket de connexion.");
		}

		/*
		 * Une boucle infinie pour continuer à accepter les demandes entrantes Créer un
		 * socket de communication pour chaque client Passer le socket dans un thread
		 */
		int numeroClient = 0;
		while (true) {
			client = serverSocket.accept();

			// Créer un thread MessageReceptor pour le nouveau client
			MessageReceptor newMessageReceptor = new MessageReceptor(client, listClient);
			listClient.put(newMessageReceptor, "");
			newMessageReceptor.start();

			System.out.println("Client " + numeroClient + " se connecte !");

			numeroClient++;

		}
	}
}
