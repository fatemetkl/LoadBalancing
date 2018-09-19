package Client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;

import Common.Constants;
import Common.Message;
import Common.ResultObserver;
import Common.Task;
import Common.ExternalSocketCloser;

/**
 * Class represents the state of the client.
 * Holds essential data and handles interaction logic,
 * directed to the client.
 * @author Sviatoslav Sivov
 */
public class Client {
	
	/** ID of the client */
	private volatile int id;
	
	/** Port, on which client is listening */
	private final int listeningPort;
	
	/** Flag, indicating that server knows this client under the stated ID */
	private volatile boolean isRegistered = false;
	
	/** Server's address */
	private InetAddress serverAddress = null;
	
	/** Server's port */
	private int serverPort;
	
	/** Observers interested in received results */
	private ArrayList<ResultObserver> observers = new ArrayList<ResultObserver>();
	
	/**
	 * Create a client 'without' ID
	 * Default ID will be 0, will be re-assigned
	 * a real ID by the server at initial contact
	 * @param _port - port, on which client receives messages
	 */
	public Client(int _port) {
		listeningPort = _port;
		id = Constants.NULL_ID;
	}
	
	/**
	 * Create a client with the specific ID
	 * If this ID doesn't get approved by the server, 
	 * it will be assigned a new ID by the server at initial contact
	 * @param _port - port, on which client receives messages
	 */
	public Client(int _id, int _port) {
		listeningPort = _port;
		id = _id;
	}
	
	/**
	 * Accessor
	 * @return - server's address
	 */
	public InetAddress getServerAddress() {
		return serverAddress;
	}
	
	/**
	 * Accessor
	 * @return - server's port
	 */
	public int getServerPort() {
		return serverPort;
	}
	
	/**
	 * Accessor
	 * @return - port, on which client receives data
	 */
	public int getListeningPort() {
		return listeningPort;
	}
	
	/**
	 * Accessor
	 * @return - client ID
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Is client recognized by the server ?
	 * @return true, if recognized,
	 *         false, otherwise
	 */
	public boolean isRegistered() {
		return isRegistered;
	}
	
	/**
	 * Assign server's connection information to the client
	 * @param address - server's ip address
	 * @param port - server's receiving port
	 */
	public void setServerInfo(InetAddress address, int port) {
		serverAddress = address;
		serverPort = port;
	}
	
	/**
	 * Method used, when server assigns a new ID to the client
	 * @param newId - client's new ID
	 */
	public void assignId(int newId) {
		if (id != newId) {
			id = newId;
		}
		isRegistered = true;
		System.out.println("Obtained id: " + newId);
	}
	
	public void addObserver(ResultObserver observer) {
		observers.add(observer);
	}
	
	public void removeObserver(ResultObserver observer) {
		observers.remove(observer);
	}
	
	/**
	 * Sends a new task to the server for computation
	 * @param task - task object to send
	 */
	public void sendNewTask(Task task) {
		Message sendTask = new Message(Constants.RequestType.NEW_TASK, id, task);
		sendMessage(sendTask);
			
	}
	
	public void sendMessage(Message message) {
		try {
			Socket socket = new Socket(serverAddress, serverPort);
			ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream());
			ooStream.writeObject(message);
			socket.shutdownOutput();
			socket.shutdownInput();
			socket.close();
		} catch (Exception e) {
			System.out.println("Sending message " + message.getCommand() + " failed...");
		}
	}
	
	/**
	 * Computation was completed by the server,
	 * present result to the user
	 * @param taskId - id of the completed task
	 */
	public void resultReceived(int taskId, Object result) {
		for (ResultObserver observer : observers) {
			observer.resultReceived(taskId, result);
		}
		if (Constants.CLIENT_DEBUG) {
			System.out.println("\nResult received for task id: "  + taskId + "\nResult: " + result + "\n");
		}
	}
	
	/**
	 * Implements initial communication with the server workflow
	 *  - client sends sync with its id to the server
	 *  - server responds with another sync message, containing assigned id
	 * If something goes wrong in the workflow, connection is terminated
	 * @param syncId - client's self-assigned id
	 * @return true, if sync was successful and server-assigned id received
	 * 		   false, otherwise
	 */
	public boolean sync(int syncId) {
		boolean syncSuccessful = false;
		Message helloMsg = new Message(Constants.RequestType.SYNC, syncId, listeningPort, Constants.ClientType.USER);
		Socket socket = null;
		try {
			socket = new Socket(serverAddress, serverPort);
			ObjectOutputStream ooStream = new ObjectOutputStream(socket.getOutputStream());
			ooStream.writeObject(helloMsg);
			System.out.println("Message sent! Command: " + helloMsg.getCommand());
		} catch (Exception e) {
			System.out.println("Sending failed...Command: " + helloMsg.getCommand());
			return false;
		}
		Timer timer = new Timer();
		try {
			ExternalSocketCloser interrupter = new ExternalSocketCloser(socket);
			ObjectInputStream oiStream = new ObjectInputStream(socket.getInputStream());
			while (! syncSuccessful) {	
				timer.schedule(interrupter, 3000);
				Message serverMsg = (Message) oiStream.readObject();
				if (serverMsg.getCommand().equals(Constants.RequestType.SYNC)) {
					int id = (int) serverMsg.getArgs()[0];
					this.assignId(id);
					syncSuccessful = true;
				}
			}
			socket.close();
		} catch (IOException e) {
			System.out.println("SYNC interrupted.");
		} catch (ClassNotFoundException e) {
			// ignore
		} finally {
			timer.cancel();
		}
		return syncSuccessful;
	}
}