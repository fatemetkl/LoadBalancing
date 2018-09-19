package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Common.Constants;
import Common.Message;


/**
 * This class handles a single connection from a client to the server.
 * It accepts any messages sent by the client, and do appropriate updates
 * the the server(adds new event to the event queue, updates clients 
 * subscriptions, etc.) 
 */
public class PubSubAgent implements Runnable{
	
	/** This object represent a socket that is used for communication with the 
	 * client. */
	private Socket _requestSocket;
	
	/** This object represents a client from server's prospective(location of
	 * the client, the port used to communicate, etc.) */
	private Account _account;
	
	/** The _manager. */
	private EventManager _manager;
	
	/** The message received from the client*/
	Message m;
    
    /** This object represents an output stream used for communication with the
     * client. */
    private ObjectOutputStream oos;
    
    /** This object represents an input stream used for communication with the
     * client. */
    private ObjectInputStream ios;
    
    /**
     * Instantiates a new pub sub agent. "manager" object is used to make any 
     * necessary updates to the server state. 
     *
     * @param requestSocket open socket used for communication with the client
     * @param manager the manager for this server
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public PubSubAgent(Socket requestSocket, EventManager manager) throws IOException {
        //System.out.println("Creating new agent");
        _manager = manager;
        _requestSocket = requestSocket;
    }
	
	/**
	 * Includes initial setup needed for all further communication with the
	 * client i.e. getting I/O stream, authorization
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void setupConnection() throws IOException{
	  //_requestSocket.getInetAddress();
	  oos = new ObjectOutputStream(_requestSocket.getOutputStream());
	  ios = new ObjectInputStream(_requestSocket.getInputStream());
	}
	
	/** This method calls "handshake" procedure, accepts the type of event that
	 * the client wants to send, and then accepts the actual and processes it 
	 * in the appropriate way. */
	@Override
	public void run() {
		//System.out.println("starting run() in PubSubAgent");
		try{
			this.setupConnection();
			m = (Message) ios.readObject();
			//System.out.println("Received command: " + m.getCommand());
			//process the event differently based on the type of event
			switch(m.getCommand()){
				case SYNC:
					this.sync(m);
					break;
				case NEW_TASK:
					publishTask(m);
					break;
				case RESULT:
					publishResult(m);
					break;
				case STATS:
					publishStats(m);
					break;
				case SET_LOAD_LB:
					setLoadBalncer(m);
					break;
				case ABORT:					
					break;
				case REMOVE_TASK:
					break;
			default:
				System.err.println("Invalid command received:" + m.toString());
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			_requestSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void publishTask(Message message) {
		//try {
			_manager.addTask(message);
			//message.getArgs()[1] = taskId;
			//oos.writeObject(message);
			//oos.flush();
		//} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
	}
	public void setLoadBalncer(Message message){
		try{
			int lb = (int)message.getArgs()[1];
			_manager.setLoadBalncer(lb);
		}
		catch(Exception ex){
			System.err.println("Failed to set new load balncer (per client's request)"
					+ " Message:" + message.toString() );
		}
	}
	public void publishResult(Message message) {
		_manager.addResult(message);
	}
	public void publishStats(Message message) {
		_manager.addStats(message);
	}

	/**
	 * Update client's (worker or user) information such as address, listenning
	 * port, etc..
	 */
	public void sync(Message message){
		try {
			//Message m = (Message)ios.readObject();
			int accountID = (int)m.getArgs()[0];
			int portNumber = (int)m.getArgs()[1];
			System.out.println("ID received:" + accountID);
			Constants.ClientType accountType = 
					(Constants.ClientType) m.getArgs()[2];
			_account = _manager.getAccount(accountID, 
					_requestSocket.getInetAddress(), portNumber, accountType);
			System.out.println("Connection established with account ID " +
				  _account.get_id() +".  location: " +
					_requestSocket.getInetAddress() + " port: " + portNumber);
			//time-decoupling:resend any pending events to the client that is online again
			m.getArgs()[0] = _account.get_id();
			oos.writeObject(m);
			oos.flush();
			_manager.sync(_account);
			//System.out.println("Done syncing");
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
