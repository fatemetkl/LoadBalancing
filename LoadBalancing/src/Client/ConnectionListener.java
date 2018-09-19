package Client;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Thread for accepting connections from the server
 * @author Sviatoslav Sivov
 */
public class ConnectionListener extends Thread {

	/** ServerSocket, on which server is going to request connections */
	private final ServerSocket serverSocket;
	
	/** Client representation object */
	private final Client client;
	
	/**
	 * Create a new thread for accepting connections on the given socket
	 * for a given client
	 * @param ss - opened server socket
	 * @param _client - governing client
	 */
	public ConnectionListener(ServerSocket ss, Client _client) { 
		super();
		serverSocket = ss;
		client = _client;
	}
	
	/**
	 * Accept server connetions and carry them out in a separate thread, until client terminates
	 */
	public void run() {		
		while (! serverSocket.isClosed()) {
			try {
				Socket connectionSocket = serverSocket.accept();
				Connection newConnection = new Connection(connectionSocket, client);
				newConnection.start();
			} catch (IOException e) {
				System.out.println("Server socket was closed. Terminating ConnectionListener.");
				return;
			}
		}
	}
}
