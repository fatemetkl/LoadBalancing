package Worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Thread for accepting connections from the server
 * @author Sviatoslav Sivov
 */
public class Receiver extends Thread {

	/** ServerSocket, on which server is going to request connections */
	private final ServerSocket serverSocket;
	
	/** Client representation object */
	private final Worker worker;
	
	/**
	 * Create a new thread for accepting connections on the given socket
	 * for a given client
	 * @param ss - opened server socket
	 * @param worker 
	 * @param _client - governing client
	 */
	public Receiver(ServerSocket ss, Worker _worker) { 
		super();
		serverSocket = ss;
		worker = _worker;
	}
	
	/**
	 * Accept server connetions and carry them out in a separate thread, until client terminates
	 */
	public void run() {	
		System.out.println("Connection receiver started.");
		while (! serverSocket.isClosed()) {
			try {
				Socket connectionSocket = serverSocket.accept();
				Connection newConnection = new Connection(connectionSocket, worker);
				newConnection.start();
			} catch (IOException e) {
				System.out.println("Server socket was closed. Terminating Receiver.");
				return;
			}
		}
		System.out.println("Connection receiver finished!");
	}
}
