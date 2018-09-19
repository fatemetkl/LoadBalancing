package Server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class implements the main thread of the server. It initiates and start
 * all the necessary threads for asynchronous communication with the clients
 * and establishes the references for data structures used to on the server  
 */
public class PubSubService implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6363967039037827625L;
	
	/** The event manager used by this server. */
	private EventManager em;
	
	/** The filename used to store/restore serialized version of server object*/
	private static final String SERVER_FILENAME = "pubSub.server";
	
	/**
	 * Instantiates a new pub sub service. Initiates event manager (for data
	 * structures) and listening service (for accepting incoming connections)  
	 */
	public PubSubService(int loadBalancingAlg){
		em = new EventManager(loadBalancingAlg);
		Thread incomingGate	= new Thread(new PubSubGate(em));
		incomingGate.start();
	}
	
	/**
	 * Restores the server from a serialized object from a file.
	 *
	 * @param loadFile the load file that contains serialized object
	 * @return the pub sub service object
	 */
	public static PubSubService load(String loadFile){
		PubSubService ps = null;
		 try{			 
	         FileInputStream fileIn = new FileInputStream(loadFile);
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         ps = (PubSubService ) in.readObject();
	         ps.postLoadSetup();
	         in.close();
	         fileIn.close();
		 }catch(IOException i){
			 i.printStackTrace();
		 }catch(ClassNotFoundException c){
			 System.out.println("PubSub server class not found in file " + loadFile);
	         c.printStackTrace();
		 }
		 return ps;
	}
	
	/**
	 * List pending events (events that were not delivered to the corresponding
	 * clients because clients were not online).
	 *
	 * @return the string
	 */
	public String listPendingEvents(){
		return em.listPendingEvents();
	}
	/**
	 * Get information about active worker nodes 
	 *
	 * @return the string representation of active workers
	 */
	public String listActiveWorkers(){
		return em.listActiveWorkers();
	}
	/**
	 * Get information about all accounts (worker nodes and clients)registered
	 * with this node 
	 *
	 * @return the string representation of all accounts
	 */
	public String listAccounts(){
		return em.listAccounts();
	}
	/**
	 * Get information about all clients that can submit the tasks for execution 
	 *
	 * @return the string representation of active workers
	 */
	public String listUsers(){
		return em.listUsers();
	}
	/**
	 * Get information about all tasks that are currently being processed 
	 * by the server 
	 *
	 * @return the string representation of active workers
	 */
	public String listTasks(){
		return em.listTasks();
	}
	/**
	 * Get information about all tasks that are currently being processed 
	 * by the server 
	 *
	 * @return the string representation of active workers
	 */
	public String listPerofmance(){
		return em.listPerformance();
	}
	/**
	 * Get information about all tasks that are currently being processed 
	 * by the server 
	 *
	 * @return the string representation of active workers
	 */
	public String listTaskTypesCpuShare(){
		return em.listTaskTypesCpuShare();
	}
	/**
	 * Get information about all tasks that are currently being processed 
	 * by the server 
	 *
	 * @return the string representation of active workers
	 */
	public String listTasksMetadata(){
		return em.listTasksMetadata();
	}
	/**
	 * Get information about all tasks that are currently being processed 
	 * by the server 
	 *
	 * @return the string representation of active workers
	 */
	public String listStats(){
		return em.listStats();
	}
	/**
	 * Post load setup. This method restores some server's resources after 
	 * loading the object from serialized file (such as starting listening thread). 
	 */
	public void postLoadSetup(){
		em.postLoadSetup();
		Thread incomingGate	= new Thread(new PubSubGate(em));
		incomingGate.start();
	}
	
	/**
	 * Save(serialize) the server into a file with the name defined by
	 * PubSubService.FILENAME field.
	 */
	public void save(){
		try{
			String fileName = PubSubService.SERVER_FILENAME;
			FileOutputStream fileOut = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			System.out.print("\n===Saved into \"" + fileName + "\" file.===\n");
		}
		 catch(IOException i){
			 i.printStackTrace();
		 }
	}
}
