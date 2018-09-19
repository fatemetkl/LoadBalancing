package Worker;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Timer;

import Common.Constants;
import Common.Message;
import Common.ExternalSocketCloser;
import Common.Task;

/**
 * This is a listening thread, which receives and
 * interprets messages from the server and makes 
 * worker invoke corresponding methods
 * @author Sviatoslav Sivov
 */
public class Connection extends Thread {

	/** Socket for connection with the server */
	private final Socket socket;
	
	/** Governing worker object */
	private final Worker worker;
	
	/** Read messages from this stream for communication with the server */
	private ObjectInputStream oiStream;
	
	ObjectOutputStream ooStream;
	
	/**
	 * Create a new connection using given socket and a 'manager' worker
	 * @param _socket - connection socket
	 * @param _worker - worker state object
	 */
	public Connection(Socket _socket, Worker _worker) {
		super();
		socket = _socket;
		worker = _worker;
		try {
			oiStream = new ObjectInputStream(socket.getInputStream());
			ooStream = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			oiStream = null;
		}
	}
	
	/**
	 * Receive and interpret messages until connection is terminated
	 */
	public void run() {
		if (oiStream == null) {
			System.err.println("oiStream == null");
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
			Message handshakeResponse = new Message(Constants.RequestType.HANDSHAKE, worker.getId());
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
			e.printStackTrace();
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
			worker.assignId(id);
			break;
		case HANDSHAKE:
			receiveMessage();
			break;
		case NEW_TASK:
			Task task = (Task) message.getArgs()[1];
			worker.newTask(task);
			break;
		default:
			System.out.println("Unrecognized command received: " + message.getCommand());
			break;
		}
	}
}
