package Server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import Common.Constants;


/**
 * This class runs represents an entry point of the server that the clients can
 * connect to. It runs in a separate thread from the main server's thread and 
 * accepts client's connections. For each such connection, a new thread with
 * PubSubAgent class is started to process client's request.
 */
public class PubSubGate implements Runnable{
	
	/** The back reference to the event manager of this server. it used to 
	 * handle any information from the client (subscribe, publish, etc.)*/
	EventManager manager;
	
	/**
	 * Instantiates a new pub sub gate with the back reference to its parent
	 * event manager (main thread of the server).
	 *
	 * @param em the em
	 */
	public PubSubGate(EventManager em){
		manager = em;
	}
	
	/** Wait for connection and spawn a new thread to process each such 
	 * connection. 
	 */
	@Override
	public void run() {
		try {
			int portNum = Constants.SERVER_PORT;
			ServerSocket serverSocket = new ServerSocket(portNum);
			System.out.println("Waiting for the clients to connect on port " +
					serverSocket.getLocalPort() + "...");
			while(true) {
		    	Socket requestSocket = serverSocket.accept();
		        Thread serverThread = new Thread(new PubSubAgent(requestSocket, manager));
		        serverThread.start();
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
