package Client;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import Common.Constants;

/** 
 * Class with the main method for running clients'
 * command line interface user interaction,
 * allowing users to send tasks to server for execution
 * @author Sviatoslav Sivov
 */
public class ClientMain {
	
	/**
	 * @param args[0] - listening port for the client
	 */
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
			UI ui = new UI(client);
			ConnectionListener conListener = new ConnectionListener(ss, client);
			conListener.start();
			ui.run();
			ss.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
 