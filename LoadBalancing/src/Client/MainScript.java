package Client;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import Common.Constants;
import Common.Message;


/** 
 * Class with the main method for running clients'
 * command line interface user interaction,
 * allowing users to send tasks to server for execution
 * @author Sviatoslav Sivov
 */
public class MainScript {
	
	/**
	 * @param args[0] - listening port for the client
	 */
	public static void main(String[] args) {
				
		// obtain listening port from arguments
		// if not provided, use default from Constants
		InetAddress serverAddress = null;
		int serverPort = -1;
		int numTasks = -1;
		
		try {
			numTasks = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Usage: java BenchmarkTester <number of tasks> <?server address> <?server port>");
			return;
		}
		
		if (args.length > 0) {
			try {
				serverAddress = InetAddress.getByName(args[1]);
			} catch (UnknownHostException e) {
				// ignore
			}
			if (args.length > 1) {
				try {
					serverPort = Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
		
		int listenerPort = Constants.SUBSCRIBER_PORT ;
		// create client, ui and connection listener and run the UI
		// if listening port is occupied, terminate application
		try {
			ServerSocket ss = null;
			try {
				while(listenerPort < Integer.MAX_VALUE){
					try{
						ss = new ServerSocket(listenerPort);
						break;
					}
					catch(java.net.BindException e){
						//current port is used. try next port
						listenerPort++;
					}
				}
				System.out.println("Waiting for the server at " + listenerPort);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Client client = new Client(listenerPort);
			client.setServerInfo(serverAddress, serverPort);
			if (!client.sync(Constants.NULL_ID)) {
				System.out.println("Sync failed. Finishing.");
				return;
			}
			ConnectionListener conListener = new ConnectionListener(ss, client);
			conListener.start();
			
			/* Useful code starts here */
			BenchmarkTester2 bt = new BenchmarkTester2(numTasks);
			Long[] runTimes = new Long[Constants.NUMBER_OF_BALANCING_ALGORITHMS];
			for (int i = 0; i < Constants.NUMBER_OF_BALANCING_ALGORITHMS; i++) {
				Message setBalanceType = new Message(Constants.RequestType.SET_LOAD_LB, client.getId(), i);
				client.sendMessage(setBalanceType);
				System.out.println("\n__________________________________________");
				System.out.println("Benchmarking on algorithm " + i + ":");
				long runTime = bt.runBenchmarkTasks(client);
				runTimes[i] = runTime;
				System.out.println(numTasks + " tasks finished in " + runTime + " milliseconds.");
				System.out.println("__________________________________________");
			}
			System.out.println("\n______________________________________________");
			System.out.println("RESULTS:");
			System.out.println("______________________________________________");
			for (int i = 0; i < Constants.NUMBER_OF_BALANCING_ALGORITHMS; i++) {
				System.out.println("Algorithm " + i + " performance: " + runTimes[i] + " seconds.");
			} 
			
			/* And ends here */
			
			ss.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
 