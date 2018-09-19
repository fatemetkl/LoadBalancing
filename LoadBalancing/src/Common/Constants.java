package Common;
/**
 * The Class Constants. This class incorporated various constants that are 
 * used among multiple classes (server, workers, users) 
 */
public final class Constants {
	
	/** This Constant defines "null" id letting the server know that the new
	 * actual id is needed. */
	public final static int NULL_ID = -1;
	
	/** This Constant defines the default port that the client expects  
	 * servers' connections on. This port may change if it is not available
	 * on the client's host. The client will communicate its actual port to the 
	 * server upon initialization 
	 * */
	public final static int SUBSCRIBER_PORT = 20000;
	
	/** This Constant defines the default location of the server. This value 
	 * is used if a client is started without a host value passed as a 
	 * parameter */
	public final static String SERVER_HOST = "california.cs.rit.edu";
	
	/** This Constant defines the default port that the server expects  
	 * clients' connections on. This port will not change.
	 * */
	public final static int SERVER_PORT = 19999;
	/**
	 * This is the number of threads in the thread pool each worker has
	 */
	public final static int THREADS_IN_WORKER = 4;
	
	/**
	 * This Enum defines the types of clients that the server works with*/
	public enum ClientType {
		/**worker client executes computational tasks submitted to the server*/
		WORKER,
		/**user client submits computational tasks to the server for execution*/
		USER
	}
	/**
	 * This Enum defines the types of messages (events) sent between the client
	 * and the server
	 * ===============================
	 * arguments[0]: int client ID*/
	public enum RequestType {
		
		/** The message contains performance statistics from the worker node.
		 * arguments[1]: Performance object*/
		STATS,
		
		/** The message contains a new computational task.
		 * =============================== 
		 * This includes CPU load and the number of threads in use from the thread pool of the respective worker 
		 * arguments[1]: Runnable object that extends Task to run*/
		NEW_TASK, 
		
		/** The message contains a computational task that has been previously 
		 * submitted that should be cancelled. */
		ABORT, 
		
		/** The message contains a computational task that needs to be removed 
		 * from the worker's queue per server's request as a result of 
		 * reassignment due to load balancing. */
		REMOVE_TASK, 
		
		/** The message contains a result of computational task.
		 *  args[0] - worker ID
		 *  args[1] - task ID
		 *  args[2] - result object 
		 *  args[3] - fraction of CPU used during task execution */
		RESULT, 
		
		/** The client sends this type of message upon startup to update its 
		 * location (host and port) so the server can re-send any pending 
		 * messages that were generated while the client was offline. 
		 * ===============================
		 * arguments[1]: int port number that the client is listenning on*/
		SYNC,
		/** 
		 * Server sends this type of message to initialize connection 
		 *  - no args
		 * Client responds to server with this message with ID 
		 *  - args[0] = client's ID
		 * */
		HANDSHAKE,
		/**
		 * Client sends to server this message with ID 
		 *  - args[0] = client's ID
		 *  int args[1] = ID of the algorithm. 
		 *  	0: roundRobinLoadBalancer
				1: queueLengthLoadBalancer
				2: fittingCpuShareLoadBalancer
				3: minCpuShareLoadBalancer
				4: minCpuLoadQueueBalancer
		 */
		SET_LOAD_LB
	}
	
	public static final int NUMBER_OF_BALANCING_ALGORITHMS = 5;
	
	/* TASK LIMITATIONS */
	
	public final static int PASSWORD_MIN_LENGTH = 2;
	public final static int PASSWORD_MAX_LENGTH = 5;
	
	public final static int TIMER_MAX_LENGTH = 10;
	
	public final static int PRIME_UPPER_BOUNDARY = 1000000;
	
	/* DEBUG FLAGS */
	
	public static final boolean CLIENT_DEBUG = false;
	
}

