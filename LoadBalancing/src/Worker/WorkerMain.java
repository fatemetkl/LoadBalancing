package Worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import Common.Constants;

public class WorkerMain {
	
	/** BufferedReader for non-blocking input */
	private static BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String[] args) {
		
		// obtain listening port from arguments
		// if not provided, use default from Constants
		InetAddress serverAddress = null;
		int serverPort = -1;
		if (args.length > 0) {
			try {
				serverAddress = InetAddress.getByName(args[0]);
			} catch (UnknownHostException e) {
				// ignore
			}
			if (args.length > 1) {
				try {
					serverPort = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
		
		if (serverAddress == null) {
			try {
				serverAddress = getServerAddress();
			} catch (UnknownHostException e) {
				System.out.println(e.getMessage());
				return;
			} catch (IOException e) {
				System.out.println(e.getMessage());
				return;
			}
		}
		
		if (serverPort < 0) {
			try {
				serverPort = getPort();
			} catch (IOException e) {
				// ignore
			}
		}
		
		int listenerPort = Constants.SUBSCRIBER_PORT;
		// create client, ui and connection listener and run the UI
		// if listening port is occupied, terminate application
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
		
		Worker worker = new Worker(listenerPort);
		worker.setServerInfo(serverAddress, serverPort);			
		if (worker.sync(Constants.NULL_ID)) {
			Receiver receiver = new Receiver(ss, worker);
			ResultSender resultSender = new ResultSender(worker);
			PerformanceReporter reporter = new PerformanceReporter(worker, Thread.currentThread());
			resultSender.start();
			receiver.start();
			reporter.start();
			try {
				receiver.join();
				resultSender.join();
				reporter.join();
			} catch (InterruptedException e) {
				// ignore
			}                    
			try {
				ss.close();
			} catch (IOException e) {
				// ignore
			}
		} else {
			System.out.println("Unable to talk to the server...Terminating.");
		}	
	}
	
	/**
	 * Method for getting input for server address and verifying it is valid
	 * @return - InetAddress object instantiated from the input
	 * @throws IOException
	 */
	private static InetAddress getServerAddress() throws IOException {
		InetAddress serverAddress = null;
		while (serverAddress == null) {
			System.out.print("Enter server address: ");
			String input = inputReader.readLine();
			if(input.equals("")) input = Constants.SERVER_HOST;
			try {
				serverAddress = InetAddress.getByName(input);
			} catch (UnknownHostException e) {
				System.out.println("Unknown host exception: " + e.getMessage());
			}
		}
		return serverAddress;
	}
	
	/**
	 * Method for getting port number
	 * @return - port number
	 * @throws IOException
	 */
	private static int getPort() throws IOException {
		Integer port = null;
		while (port == null) {
			System.out.print("Enter port number: ");
			String portString = inputReader.readLine();
			if(portString.equals("")) portString = Constants.SERVER_PORT + "";
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
				System.out.println("Port number needs to be an integer.");
			}
		}	
		return port;
	}
	
}
