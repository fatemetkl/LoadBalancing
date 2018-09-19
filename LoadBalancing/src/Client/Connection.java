package Client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;

import Common.Constants;
import Common.Message;
import Common.ExternalSocketCloser;

/**
 * This is a listening thread, which receives and
 * interprets messages from the server and makes 
 * client invoke corresponding methods
 * @author Sviatoslav Sivov
 */
public class Connection extends Thread {

	/** Socket for connection with the server */
	private final Socket socket;
	
	/** Governing client object */
	private final Client client;
	
	/** Read messages from this stream for communication with the server */
	private ObjectInputStream oiStream;
	
	ObjectOutputStream ooStream;
	
	/**
	 * Create a new connection using given socket and a 'manager' client
	 * @param _socket - connection socket
	 * @param _client - client state object
	 */
	public Connection(Socket _socket, Client _client) {
		super();
		socket = _socket;
		client = _client;
		try {
			//oiStream = new ObjectInputStream(socket.getInputStream());
			oiStream = new ObjectInputStream(socket.getInputStream());
			ooStream = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			oiStream = null;
		}
	}
	
	/**
	 * Receive and interpret messages until connection is terminated
	 */
	public void run() {
		if (oiStream == null) {
			return;
		}
		while (! socket.isClosed()) {
			Message message = null;
			try {
				message = (Message) oiStream.readObject();
				interpretMessage(message);
			} catch (ClassNotFoundException | IOException e) {
				try {
					socket.close();
				} catch (IOException e1) {}
			}
		}
	}
	
	/**
	 * This function is invoked, when handshake from the server is received.
	 * Attempts to send a response handshake and then receive a meaningful 
	 * message from the server. If something goes wrong in the workflow,
	 * connection is terminated
	 * @return - true, if workflow completed successfully 
	 * 			 false, otherwise
	 */
	private boolean receiveMessage() {
		boolean communicationCompleted = false;
		try {
			//ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream());
			Message handshakeResponse = new Message(Constants.RequestType.HANDSHAKE, client.getId());
			ooStream.writeObject(handshakeResponse);
		} catch (IOException e) {
			return false;
		}
		Timer timer = new Timer();
		try {
			ExternalSocketCloser interrupter = new ExternalSocketCloser(socket);
			while (! communicationCompleted) {
				timer.schedule(interrupter, 3000);
				Message serverMsg = (Message) oiStream.readObject();
				if (! serverMsg.getCommand().equals(Constants.RequestType.HANDSHAKE)) {
					interpretMessage(serverMsg);
					communicationCompleted = true;
					socket.close();
				}
			}
		} catch (IOException e) {
			System.out.println("Socket read interrupted!");
		} catch (ClassNotFoundException e) {
			
		} finally {
			timer.cancel();
		}
		return communicationCompleted;
	}

	/**
	 * Decides how to act based on the received message's command
	 * @param message - received message
	 */
	private void interpretMessage(Message message) {
		switch (message.getCommand()) {
		case SYNC:
			int id = (Integer) message.getArgs()[0];
			client.assignId(id);
			break;
		case HANDSHAKE:
			receiveMessage();
			break;
		case RESULT:
			int taskId = (int) message.getArgs()[1];
			Object result = message.getArgs()[2];
			client.resultReceived(taskId, result);
			break;
		default:
			System.out.println("Unrecognized command received: " + message.getCommand());
			break;
		}
	}
}
