package Server;

import java.io.Serializable;
import java.net.InetAddress;

import Common.Constants;
import Common.Performance;


/**
 * The Class Account. Represents all the information that the server stores 
 * about the client
 */
public class Account implements Comparable<Account>,  Serializable{
	
	/** The Constant serialVersionUID. used for serialization.  */
	private static final long serialVersionUID = 8349923940225462075L;

	/** The _id. The server maintains the IDs of the clients. Each client has
	 * a unique ID that is used for authentication between the server and the 
	 * client 
	 * */
	private int _id;
	
	/** The Internet address that the this client last connected from to the 
	 * server. Every time the client connects to the server, this field is 
	 * update to ensure delivery of the messages from the server to this client 
	 * */
	private InetAddress _location;
	
	/** The port that the this client is listening on for server's connections. 
	 * Every time the client connects to the server, this field is 
	 * update to ensure delivery of the messages from the server to this client.
	 */
	private int _portNumber;
	Constants.ClientType _accountType;
	
	private Performance _performance;
	/**
	 * Gets the port number that this client expects the server's connections.
	 *
	 * @return the _port number
	 */
	public int get_portNumber() {
		return _portNumber;
	}
	
	/**
	 * Instantiates a new account.
	 *
	 * @param id the id
	 * @param location the Internet address that this client is located on
	 * @param portNumber 	the port number that this client expects the 
	 * 						server's connections.
	 */
	public Account(int id, InetAddress location, int portNumber, 
			Constants.ClientType accountType) {
		_id = id;
		_location = location;
		_portNumber = portNumber;
		_accountType = accountType;
	}
	
	/**
	 * Gets the location.
	 *
	 * @return the location
	 */
	public InetAddress getLocation() {
		return _location;
	}
	
	/**
	 * Sets the location (both Internet address and port numbers for server to
	 * communicate to the client).
	 *
	 * @param location the location
	 * @param portNumber the port number
	 */
	public void setLocation(InetAddress location, int portNumber) {
		this._location = location;
		this._portNumber = portNumber;
	}
	
	/**
	 * Gets the _id.
	 *
	 * @return the _id
	 */
	public int get_id() {
		return _id;
	}
	
	public void updatePerformance(Performance p){
		_performance = p;
	}
	
	/**
	 * Accounts ID uniquely identifies the client.  
	 */
	@Override
	public String toString(){
		return _accountType + " ID:" + _id + " " + _location + ":" + _portNumber;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o){
		if(((Account) o)._id == this._id)
			return true;
		else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode(){
		return this._id;
	}
	
	/**
	 * Accounts ID uniquely identifies the client. This method is used by some 
	 * collections for proper storage of the objects of this class
	 */
	@Override
	public int compareTo(Account o) {		
		return this._id - o._id;
	}

	public Performance getPerformance() {
		return _performance;
	}

	public void setPerformance(Performance _performance) {
		this._performance = _performance;
	}
}
