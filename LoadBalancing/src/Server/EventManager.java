package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import Common.Constants;
import Common.Constants.RequestType;
import Common.Message;
import Common.Performance;
import Common.Task;
import Server.RL.State;


/**
 * This class handles the acceptance of new messages and delivery of the
 * messages to the subscribers. Each new message has a topic associated with it
 * that the clients can subscribe to. Clients can also subscribe to receive
 * the messages based on the keywords. Each word in a title of a message is
 * considered a keyword; a message is sent to keyword's subscribers for each
 * word in the title of the message.
 */
public class EventManager implements Serializable {

    /**
     * The Constant serialVersionUID. used for serialization.
     */
    private static final long serialVersionUID = -3334625294865101721L;

    /**
     * The next account id. Each new client gets a new unique ID
     */
    private int nextAccountID = 0;

    /**
     * The next event id. Each new event gets a new unique ID
     */
    private static int nextTaskID = 0;

    /**
     * The queue to store incoming tasks before they are processed.
     */
    private BlockingQueue<Message> newMessages;

    /** The list of all topics that this server accepts. */
    //private List<Topic> topics;

    /** The data structure to keep a record of topic subscriptions for each
     * client. */
    //private Map<Topic, Set<Account>> topicSubscribers;

    /**
     * The accounts representing worker nodes that are online as far as serve is aware.
     */
    private List<Account> activeWorkers;

    /**
     * The accounts representing user nodes.
     */
    private List<Account> users;

    /**
     * Both workers and users accounts. Joint data structures used to improve
     * performance
     */
    private List<Account> accounts;

    /** The server copy of each worker's queue. */
    //private Map<Account, Set<Message>> workersActiveTasks;

    /**
     * The messages pending the recipient to come back online.
     */
    private Map<Account, Set<Message>> pendingMessages;

    private ServerStats stats;
    private QlearningLoadBalancer rlLoadBalancer;
    //private Map<Long, >
    //long serialVersionUID

    /**
     * The tasks that have been submitted with the server and have not been
     * computed yet
     * Key - task id (as aassigned by the server)
     * Value - metadata associated with the task
     */
    private Map<Integer, TaskMetadata> pendingTasks;

    /**
     * The accounts to sync.
     */
    private BlockingQueue<Account> accountsToSync;

    /**
     * indicates which load balancing algorithm to use
     */
    int loadBalancingAlg;

    /**
     * This function loads the object with initial data for demo/testing
     *
     * @throws UnknownHostException
     */
    private void preLoad() throws UnknownHostException {
        //the actual host and port for each client will be updated upon first
        //connection to the server
        //Account ac0 = this.getAccount(Constants.NULL_ID, InetAddress.getLocalHost(),
        //	Constants.SUBSCRIBER_PORT, Constants.ClientType.USER);
    }

    private void displayLoadBalancer() {
        switch (loadBalancingAlg) {
            case 0:
                System.out.println("Load balancer set to: roundRobinLoadBalancer");
                break;
            case 1:
                System.out.println("Load balancer set to: queueLengthLoadBalancer");
                break;
            case 2:
                System.out.println("Load balancer set to: fittingCpuShareLoadBalancer");
                break;
            case 3:
                System.out.println("Load balancer set to: minCpuShareLoadBalancer");
                break;
            case 4:
                System.out.println("Load balancer set to: minCpuLoadQueueBalancer");
                break;
            case 5:
                System.out.println("Load balancer set to: reinforcement learner agent");
                break;
            default:
                System.out.println("Load balancer set to: roundRobinLoadBalancer");
                break;
        }
    }

    /**
     * Instantiates the data structures used by event manager and starts up
     * the threads needed to service it: real-time event delivery services
     * and event re-delivery for the clients that were off-line.
     */
    public EventManager(int loadBalancingAlg) {
        this.loadBalancingAlg = loadBalancingAlg;
        displayLoadBalancer();
        newMessages = new LinkedBlockingDeque<Message>();
        //topics = new LinkedList<Topic>();
        activeWorkers = new LinkedList<Account>();
        accounts = new LinkedList<Account>();
        users = new LinkedList<Account>();
//        activeWorkers = new LinkedList<Account>();
        //topicSubscribers = new HashMap<Topic, Set<Account>>();
        pendingMessages = new HashMap<Account, Set<Message>>();
        accountsToSync = new LinkedBlockingDeque<Account>();
        //pendingTasks = new HashMap<Integer, TaskMetadata>();
        pendingTasks = new ConcurrentHashMap<Integer, TaskMetadata>();
        stats = new ServerStats();
        rlLoadBalancer = new QlearningLoadBalancer();
        Thread eventsPusher = new Thread(new EventPublisher(newMessages));
        eventsPusher.start();
        Thread synchronizer = new Thread(new Synchronizer(accountsToSync));
        synchronizer.start();
        try {
            preLoad();
        } catch (UnknownHostException e) {
            System.err.println("Demo preload has failed:");
            e.printStackTrace();
        }
    }

    public String listAccounts() {
        String res = "";
        for (Account ac : accounts) {
            res += ac.toString() + "\n";
        }
        return res;
    }

    public String listActiveWorkers() {
        String res = "";
        for (Account ac : activeWorkers) {
            res += ac.toString() + "\n";
        }
        return res;
    }

    public String listUsers() {
        String res = "";
        for (Account ac : users) {
            res += ac.toString() + "\n";
        }
        return res;
    }

    public String listTasks() {
        String res = "";
        for (Map.Entry<Integer, TaskMetadata> entry : pendingTasks.entrySet()) {
            res += entry.getValue().toString().replace("\n", " ") + "\n";
        }
        res += "Total found: " + pendingTasks.entrySet().size() + "\n";
        return res;
    }

    public String listPerformance() {
        String res = "";
        for (Account ac : activeWorkers) {
            res += "Account " + ac.get_id() + ": " + ac.getPerformance() + "\n";
        }
        return res;
    }

    public String listTaskTypesCpuShare() {
        return "Known task types and average CPU loads:\n" + stats.taskTypesToString();
    }

    public String listTasksMetadata() {
        return "Completed tasks and related metadata:\n" + stats.tasksToString();
    }

    public String listStats() {
        return stats.statsToString();
    }

    /**
     * This function is used if the object was initialized from the serialized
     * file to start the service threads.
     */
    public void postLoadSetup() {
        //TODO update with data strucutures added in group project
        Thread eventsPusher = new Thread(new EventPublisher(newMessages));
        eventsPusher.start();
        Thread synchronizer = new Thread(new Synchronizer(accountsToSync));
        synchronizer.start();
        //System.out.println("Next available ID: " + nextAccountID);
    }

    public Account getAccount(int accountId) throws IllegalArgumentException {
        for (Account ac : accounts) {
            if (ac.get_id() == accountId) {
                return ac;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * This function returns the Account object representing the client based
     * on ID. If ID is not found, new Account object is created and added to
     * the list of clients that this event manager is aware of.
     *
     * @param accountID  the id of the account(client) to find
     * @param location   current location of the client
     * @param portNumber current port that client is listening on.
     * @return the account object representing the client with accountID ID
     */
    public Account getAccount(int accountID, InetAddress location,
                              int portNumber, Constants.ClientType accountType) {
        for (Account ac : accounts) {
            if (ac.get_id() == accountID) {
                ac.setLocation(location, portNumber);
                switch (accountType) {
                    case WORKER:
                        activeWorkers.add(ac);
                    default:
                        break;
                }
                return ac;
            }
        }
        Account newAccount = null;
        synchronized (this) {
            newAccount = new Account(nextAccountID++, location, portNumber, accountType);
            accounts.add(newAccount);
            switch (accountType) {
                case WORKER:
                    activeWorkers.add(newAccount);
                    //workersActiveTasks.put(newAccount,
                    //new HashSet<Message>());
                    break;
                case USER:
                    users.add(newAccount);
                    break;
            }

        }
        return newAccount;
    }

    /**
     * Adds the event to the queue. The message delivery threads will later
     * deliver the event to the subscribers
     *
     * @param event the event
     */
    public void addTask(Message message) {
        TaskMetadata tm = null;
        int originalTaskId;
        Task t = (Task) message.getArgs()[1];
        originalTaskId = t.getId();
        synchronized (this) {
            t.setId(EventManager.nextTaskID);
            //System.out.println("===ID " + EventManager.nextTaskID + " assigned to task");
            nextTaskID++;
            Account owner = getAccount((int) message.getArgs()[0]);
            tm = new TaskMetadata(owner, null, t, originalTaskId);
            pendingTasks.put(t.getId(), tm);
            tm.setPendTime();
			/*System.out.println("Put " + pendingTasks.get(t.getId()) + "/" +
					t.getId() + "/" + t + "/" +  
			" into pending queue");*/
            //generate new id for the task
            //synchronized(this){
            //((Task)message.getArgs()[1]).setId(EventManager.nextTaskID++);
            //}
            newMessages.add(message);
            //return ((Task)message.getArgs()[1]).getId();
        }
    }

    public void addResult(Message message) {
        //add this message to the queue
        newMessages.add(message);
        // TODO remove the task from worker's queue
    }

    public void setLoadBalncer(int lb) {
        loadBalancingAlg = lb;
        stats.resetStats();
        displayLoadBalancer();
    }

    public void addStats(Message message) {
        //add this message to the queue
        //newMessages.add(message);
        int accountId = (int) message.getArgs()[0];
        Performance p = (Performance) message.getArgs()[1];
        try {
            Account ac = getAccount(accountId);
            ac.updatePerformance(p);
        } catch (IllegalArgumentException e) {
            System.err.println("Account with id " + accountId + " reported its "
                    + "stats, but the server does not have a record of such "
                    + "account. Maybe PerformanceReport is alive from the previous run?");
        }
        //System.out.println("Performance for account " + accountId + ": " + p);
        // TODO remove the task from worker's queue
    }

    /**
     * Try to re-send all the messages to the client "ac" that were generated
     * for the client(based on client's subscriptions) while the client was
     * offline.
     *
     * @param ac the account to synchronize
     */
    public void sync(Account ac) {
        if (pendingMessages.containsKey(ac)) {
            try {
                accountsToSync.put(ac);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Get string representation of pending events; i.e. the events that were
     * not delivered to the clients because the clients were not accessible.
     *
     * @return the string representation of pending events
     */
    public String listPendingEvents() {
        String allPending = "";
        Iterator<Map.Entry<Account, Set<Message>>> it = pendingMessages.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Account, Set<Message>> entry = it.next();
            allPending += entry.getKey() + " => ";
            Iterator<Message> jt = entry.getValue().iterator();
            while (jt.hasNext()) {
                allPending += jt.next() + ";";
            }
            allPending += "\n";
        }
        return allPending;
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        //new EventManager().startService();
    }

    /**
     * The class represents event delivery service. This object is attached to
     * a queue of events received by the server that need to be delivered to the
     * appropriate subscribers.
     */
    private class EventPublisher implements Runnable {

        /**
         * The event queue that this object serves.
         */
        private BlockingQueue<Message> _queue;
        private transient Socket kkSocket;
        private ObjectOutputStream oos;
        private ObjectInputStream ios;

        private boolean pullMessages = true;
        //private topicSubscribers

        /**
         * Instantiates a new event listener i.e. attaches the event queue that
         * this object will serve.
         *
         * @param queue the queue to serve
         */
        public EventPublisher(BlockingQueue<Message> queue) {
            _queue = queue;
        }

        //field used for round robin load balancer
        private int nextWorker = -1;

        /*This load balancer returns an active worker in a round robin fashion.
         * @Returns: the account (active worker) that is next in relation to the
         * 			 previously returned worker. Null if no active
         * 			 workers found.
         */
        private Account roundRobinLoadBalancer(Message message) {
            if (activeWorkers.size() > 0) {
                return activeWorkers.get(++nextWorker % activeWorkers.size());
            } else {
                return null;
            }
        }

        /*This load balancer returns an active worker with the lowest number of
         * tasks currently in a queue or being executed.
         * @Returns: the account (worker) with the lowest number of tasks
         * 			 currently in a queue or being executed. Null if no active
         * 			 workers found.
         */
        private Account queueLengthLoadBalancer(Message message) {
            HashMap<Account, Integer> queuesLength = new HashMap<Account, Integer>();
            if (activeWorkers.size() > 0) {
                for (Account ac : activeWorkers) {
                    queuesLength.put(ac, new Integer(0));
                }
                for (Map.Entry<Integer, TaskMetadata> entry : pendingTasks.entrySet()) {
                    Account ac = ((TaskMetadata) entry.getValue()).getExecutor();
                    if (ac != null) {
                        if (queuesLength.containsKey(ac)) {
                            queuesLength.put(ac, queuesLength.get(ac) + 1);
                        } else {
                            queuesLength.put(ac, 1);
                        }
                    }
                }
                int minQueue = Integer.MAX_VALUE;
                Account minQueueAccount = null;
                for (Map.Entry<Account, Integer> entry : queuesLength.entrySet()) {
                    if (entry.getValue() < minQueue) {
                        minQueueAccount = entry.getKey();
                        minQueue = entry.getValue();
                    }
                    //System.out.println(entry.getKey() + ": " +
                    //	entry.getValue());
                }
                //System.out.println(minQueueAccount + ": " + minQueue);
                return minQueueAccount;
            } else {
                return null;
            }
        }

        /*This load returns an active worker with the lowest cpu load (from the
         * latest reported worker's load)
         * @Returns: the account (worker) with the lowest cpu load. Null if
         * 			 no active workers found.
         */
        private Account minCpuShareLoadBalancer(Message message) {
            if (activeWorkers.size() > 0) {
                double minCpuShare = Double.MAX_VALUE;
                Account minCpuShareAccount = null;
                for (Account ac : activeWorkers) {
                    if (minCpuShare > ac.getPerformance().getCpuLoad()) {
                        minCpuShare = ac.getPerformance().getCpuLoad();
                        minCpuShareAccount = ac;
                    }
                }
                return minCpuShareAccount;
            } else {
                return null;
            }
        }

        /*This load balancer returns an active worker with the cpu load low
         * enough that the new task can have enough cpu to be processed.
         * Task's cpu share is estimated based on the cpu share used by the
         * tasks of the same type in the past. If this is the first task of this
         * type, the worker with lowest current cpu load will be used
         * @Returns: the account (worker). Null if no active workers found.
         */
        private Account fittingCpuShareLoadBalancer(Message message) {
            HashMap<Account, Integer> queuesLength = new HashMap<Account, Integer>();
            if (activeWorkers.size() > 0) {
                Task t = (Task) message.getArgs()[1];
                long taskTypeUID = t.getUID();
                double taskTypeCpuShare = stats.getTaskTypesCpuShare(taskTypeUID);
                //check if the server has processed the tasks of this type in
                //the past
                if (taskTypeCpuShare != -1) {
                    for (Account ac : activeWorkers) {
                        if (taskTypeCpuShare < (1 - ac.getPerformance().getCpuLoad())) {
                            return ac;
                        }
                    }
                }
                return minCpuShareLoadBalancer(message);
            } else {
                return null;
            }
        }

        /*This load balancer returns an active worker with the lowest cpu load
         * based on the tasks already in workers' queues. Each tasks cpu load
         * is estimated based on the cpu share used by the tasks of the same
         * type in the past.
         * @Returns: the account (worker). Null if no active workers found.
         */
        private Account minCpuLoadQueueBalancer(Message message) {
            double unknownTaskTypeWeight = 0.5;
            HashMap<Account, Double> queuesLength = new HashMap<Account, Double>();
            if (activeWorkers.size() > 0) {
                for (Account ac : activeWorkers) {
                    queuesLength.put(ac, new Double(0));
                }
                for (Map.Entry<Integer, TaskMetadata> entry : pendingTasks.entrySet()) {
                    Account ac = ((TaskMetadata) entry.getValue()).getExecutor();
                    if (ac != null) {
                        long taskTypeUID = ((TaskMetadata) entry.getValue()).getTask().getUID();
                        double taskTypeCpuShare = stats.getTaskTypesCpuShare(taskTypeUID);
                        if (taskTypeCpuShare == -1) {
                            taskTypeCpuShare = unknownTaskTypeWeight;
                        }
                        if (queuesLength.containsKey(ac)) {
                            queuesLength.put(ac, queuesLength.get(ac) + taskTypeCpuShare);
                        } else {
                            queuesLength.put(ac, taskTypeCpuShare);
                        }
                    }
                }
                double minQueue = Double.MAX_VALUE;
                Account minQueueAccount = null;
                for (Map.Entry<Account, Double> entry : queuesLength.entrySet()) {
                    if (entry.getValue() < minQueue || minQueueAccount == null) {
                        minQueueAccount = entry.getKey();
                        minQueue = entry.getValue();
                    }
                }
                System.out.println("minCpuLoadQueue Load Balancer" + minQueueAccount + ": " + minQueue);
                return minQueueAccount;
            } else {
                return null;
            }
        }

        private Account reinforcementLearningBalancer(Message message) {
            if (activeWorkers.size() > 0) {
                int index = rlLoadBalancer.disPtch(activeWorkers, message);
                return activeWorkers.get(index);
            } else {
                return null;
            }
        }

        /*
         * Master load balancer - allows to switch between different algorithms
         */
        public Account loadBalancer(Message message) {
            switch (loadBalancingAlg) {
                case 0:
                    return roundRobinLoadBalancer(message);
                case 1:
                    return queueLengthLoadBalancer(message);
                case 2:
                    return fittingCpuShareLoadBalancer(message);
                case 3:
                    return minCpuShareLoadBalancer(message);
                case 4:
                    return minCpuLoadQueueBalancer(message);
                case 5:
                    return reinforcementLearningBalancer(message);
                default:
                    return roundRobinLoadBalancer(message);
            }
            //return roundRobinLoadBalancer(message);
            //return queueLengthLoadBalancer(message);
            //return fittingCpuShareLoadBalancer(message);
            //return minCpuShareLoadBalancer(message);
            //return minCpuLoadQueueBalancer(message);
        }

        private void handshake(Account client) throws IOException, ClassNotFoundException {
            //get the location (host and port) of the client
            InetAddress host = client.getLocation();
            int port = client.get_portNumber();
            kkSocket = new Socket(host, port);
            oos = new ObjectOutputStream(kkSocket.getOutputStream());
            ios = new ObjectInputStream(kkSocket.getInputStream());
            //the following block simulates the authentication
            //protocol:
            //1. Server connects to the client
            //2. Server sends handshake message
            //3. Client responds with handshake message with argument[0] set to its id
            //4. Server verifies that the ID supplied by the client
            //	 matches the ID of the client expected the server
            //5. If matched:
            //		Server sends a single "Message" object
            //		otherwise:
            //		drop connection and throw java.net.ConnectException
            //===authentication protocol===
            Message handshake = new Message(RequestType.HANDSHAKE, (Object) null);
			/*System.out.println("Sending handshake to the client. id: " + 
					client.get_id() + 
					" location:" + client.getLocation() +
					" port: " + client.get_portNumber() );*/
            oos.writeObject(handshake);
            oos.flush();
            handshake = (Message) ios.readObject();
            int id = (int) handshake.getArgs()[0];
            if (id != client.get_id()) {
                System.err.println("Authentication failed. Id sent: " + client.get_id() +
                        "Client returned id " + id);
                kkSocket.close();
                throw new java.net.ConnectException();
            } else {
                //System.out.println("Authentication succedded with " + client);
            }
            //===authentication protocol end===
        }

        /**
         * Delivers a single event to all subscribers to the event's topic and/
         * or to any of the keywords associated with this event
         *
         * @param event the event
         */
        private void processMessage(Message message) {
            TaskMetadata tm = null;
            Account recipient = null;
            //int originalTaskId;
            synchronized (_queue) {
                System.out.println("Processing message " + message.toString().replace("\n", " "));
            }
            try {
                switch (message.getCommand()) {
                    case NEW_TASK:
                        recipient = loadBalancer(message);
                        if (recipient == null) {
                            pullMessages = false;
                            System.err.println("Load balance failed to provide a worker");
                            throw new java.net.ConnectException();
                        }
                        synchronized (_queue) {
                            //System.out.println("Worker assigned to the message is: " + recipient);
                        }
                        handshake(recipient);
                        synchronized (_queue) {
                            //System.out.println("Sending message " + message);
                        }
                        oos.writeObject(message);
                        oos.flush();
                        Task t = (Task) message.getArgs()[1];
                        tm = pendingTasks.get(t.getId());
                        if (tm != null) {
                            tm.setExecutor(recipient);
                            tm.setStartTime();

                        } else {
                            System.err.println("ERROR Could not find task " + t.getId() + " in pendingTasks");
                        }
                        //synchronized(_queue){
                        //System.out.println("The message has been sent to: " + recipient);
                        //}
                        //pendingTasks.put(t.getId(), tm);
                        break;
                    case RESULT:
                        int taskId = (int) (message.getArgs()[1]);
                        if (!pendingTasks.containsKey(taskId)) {
                            throw new IOException("Worker has sent RESULT with task id of " +
                                    taskId + " but server has no record of such task");
                        } else {
                            tm = pendingTasks.get(taskId);
                            tm.setEndTime();
                            double cpuShare = (double) (message.getArgs()[3]);
                            tm.setCpuShare(cpuShare);
                            //reset task's Id to whatever the client initially assigned
                            message.getArgs()[1] = tm.getOriginalTaskId();
                            recipient = tm.getInitiator();
                            pendingTasks.remove(taskId);
                        }
                        synchronized (_queue) {
                            //System.out.println("Sending result to client: " + recipient);
                        }
                        stats.logTask(tm);
                        handshake(recipient);
                        oos.writeObject(message);
                        oos.flush();
                        synchronized (_queue) {
                            System.out.println("The message has been sent to: " + recipient +
                                    "; " + (System.currentTimeMillis() / 1000));
                        }
                        if (loadBalancingAlg == 5) {
                            TaskMetadata[] taskMetadata = new TaskMetadata[pendingTasks.size()];
                            int i = 0;
                            for (TaskMetadata m: pendingTasks.values()){
                                taskMetadata[i++] = m;
                            }
                            State statePrime = new State(activeWorkers);
                            statePrime=rlLoadBalancer.update(activeWorkers, tm, taskMetadata,statePrime);

                        }
                        break;
                    case STATS:
                        addStats(message);
                        break;
				/*case SET_LOAD_LB:
					setLoadBalncer(message);*/
                    default:
                        System.err.println("Invalid message command:" + message.getCommand());
                        break;
                }
                kkSocket.close();
            }
			/*try{
				handshake(recipient);
				oos.writeObject(message);
				oos.flush();
				switch(message.getCommand()){
				case NEW_TASK:
					Task t = (Task)message.getArgs()[1];
					Account owner = getAccount((int)message.getArgs()[0]);
					TaskMetadata tm = new TaskMetadata(owner, recipient, t, originalTaskId);
					pendingTasks.put(t.getId(), tm);
					break;
				case RESULT:
					break;
				default:
					break;
				}
				kkSocket.close();
			}*/ catch (java.net.ConnectException e) {
                synchronized (_queue) {
                    System.err.println("Failed to send the message to: " + recipient);
                }
                //the client did not supply the correct ID or no one is
                //listening on the host/port associated with the client
                //i.e. the client is offline. Store the event for
                //re-delivery later (if it's task result) or put back into the
                //queue so the task is sent to another worker(if it's new task
                //to execute).
                switch (message.getCommand()) {
                    case NEW_TASK:
                        //put event back into queue to deliver to another worker
                        newMessages.add(message);
                        //addTask(message);
                        //remove the node from a list of active worker nodes
                        if (recipient != null) {
                            Iterator<Account> it = activeWorkers.iterator();
                            while (it.hasNext()) {
                                Account ac = it.next();
                                if (ac.equals(recipient)) {
                                    it.remove();
                                }
                            }
                        }
                        break;
                    case RESULT:
                        System.out.println("Client " + recipient.get_id() +
                                " is offline.");
                        addToPending(recipient, message);
                        break;
                    default:
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * Adds to the list of pending event - event that the server failed
         * to deliver b/c the client is not accessible.
         *
         * @param ac the account for which the delivery failed
         * @param e  the event that was not delivered in real-time
         */
        private void addToPending(Account ac, Message m) {
            if (!pendingMessages.containsKey(ac)) {
                Set<Message> es = new HashSet<Message>();
                es.add(m);
                pendingMessages.put(ac, es);
            } else {
                pendingMessages.get(ac).add(m);
            }
        }

        /**
         * Infinitely pull the event from the queue and process them(deliver)
         * to the corresponding clients
         */
        public void run() {
            System.out.println("Waiting for the events to send to the subscribers...");
            while (true) {
                if (!pullMessages) {
                    try {
                        synchronized (_queue) {
                            _queue.wait(5000);
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Check for new workers...");
                    }
                }
                Message m;
                try {
                    m = _queue.take();
                    synchronized (_queue) {
                        //System.out.println("Message " + m + " has been pulled from a queue");
                    }
                    processMessage(m);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * The class represent event re-delivery service. This object is attached to
     * a queue of accounts that notified the server that they are back online.
     * This server re-sends any events that were generated for the client while
     * the client was offline. From clients prospective this service is
     * indistinguishable from the EventListener
     */
    private class Synchronizer implements Runnable {

        /**
         * The queue of the clients that need just got back online and need
         * be updated with the events that were generated while the clients were
         * offline
         */
        private BlockingQueue<Account> myAccountsToSync;

        private transient Socket kkSocket;
        private ObjectOutputStream oos;
        private ObjectInputStream ios;

        /**
         * Instantiates a new synchronizer. Attaches the queue of clients to
         * synchronize that this object will be serving
         *
         * @param accountQueue the account queue
         */
        public Synchronizer(BlockingQueue<Account> accountQueue) {
            myAccountsToSync = accountQueue;
        }

        private void handshake(Account client) throws IOException, ClassNotFoundException {
            //get the location (host and port) of the client
            InetAddress host = client.getLocation();
            int port = client.get_portNumber();
            kkSocket = new Socket(host, port);
            ObjectOutputStream oos =
                    new ObjectOutputStream(kkSocket.getOutputStream());
            ObjectInputStream ios =
                    new ObjectInputStream(kkSocket.getInputStream());
            //the following block simulates the authentication
            //protocol:
            //1. Server connects to the client
            //2. Server sends handshake message
            //3. Client responds with handshake message with argument[0] set to its id
            //4. Server verifies that the ID supplied by the client
            //	 matches the ID of the client expected the server
            //5. If matched:
            //		Server sends a single "Message" object
            //		otherwise:
            //		drop connection and throw java.net.ConnectException
            //===authentication protocol===
            Message handshake = new Message(RequestType.HANDSHAKE, (Object) null);
			/*System.out.println("Sending handshake to the client. id: " + 
					client.get_id() + 
					" location:" + client.getLocation() +
					" port: " + client.get_portNumber() );*/
            oos.writeObject(handshake);
            handshake = (Message) ios.readObject();
            int id = (int) handshake.getArgs()[0];
            if (id != client.get_id()) {
                System.err.println("Authentication failed. Client " +
                        "returned id " + id);
                kkSocket.close();
                throw new java.net.ConnectException();
            } else {
                //System.out.println("Authentication succedded");
            }
            //===authentication protocol end===
        }

        /**
         * Resend the events to the client "ac".
         *
         * @param ac the account to update
         */
        private void resendEvents(Account ac) {
            //TaskMetadata tm = null;
            //Account recipient = null;
            //int originalTaskId;
            Set<Message> messages = pendingMessages.get(ac);
            if (messages != null) {
				/*int port = ac.get_portNumber();
				InetAddress host = ac.getLocation();*/
                Iterator<Message> messageIter = messages.iterator();
                while (messageIter.hasNext()) {
                    Message message = messageIter.next();
                    try {
                        switch (message.getCommand()) {
                            case RESULT:
                                handshake(ac);
                                oos.writeObject(message);
                                oos.flush();
                                break;
                            default:
                                System.err.println("Invalid message command:" + message.getCommand());
                                break;
                        }
                        kkSocket.close();
                        //remove the event from the redelivery list only if it
                        //was successfully re-delivered.
                        messageIter.remove();
                    } catch (java.net.ConnectException exc) {
                        //if client is not not accessible, add the account for
                        //later re-delivery again
                        System.out.println("Client " + ac.get_id() +
                                " is offline.");
                        addToPending(ac, message);
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                //make sure that no new event were placed for re-delivery to
                //this client before deleting the list
                if (pendingMessages.get(ac).size() == 0) {
                    pendingMessages.remove(ac);
                }
            }
        }

        /**
         * Adds to the list of pending event - event that the server failed
         * to re-deliver b/c the client is not accessible.
         *
         * @param ac the account for which the delivery failed
         * @param m  the event that was not delivered in real-time
         */
        private void addToPending(Account ac, Message m) {
            if (!pendingMessages.containsKey(ac)) {
                Set<Message> es = new HashSet<Message>();
                es.add(m);
                pendingMessages.put(ac, es);
            } else {
                pendingMessages.get(ac).add(m);
            }
        }

        /**
         * Indefinitely pull the accounts that need to be updated from the queue
         * and process them: deliver the events generated while the client was
         * off-line
         */
        public void run() {
            System.out.println("Waiting for the accounts to sync...");
            while (true) {
                Account acc;
                try {
                    acc = myAccountsToSync.take();
                    resendEvents(acc);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
    }

}