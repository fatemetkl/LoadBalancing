package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The Class class implements UI for PubSubService class. It reads user's input
 * and makes corresponding calls to the instance of PubSubService class  
 */
public class PubSub{
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		PubSubService ps = null;
		//the server can be started as a new server or from a serialized file.
		//The file name is passed as a parameter to the main  
		if(args.length == 0){
			ps = new PubSubService(5);
		}
		else{
			//ps = PubSubService.load(args[0]);
			ps = new PubSubService(Integer.parseInt(args[0]));
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String fromUser;
		while(true){
			try{
				System.out.println(
						"a:\tlist all accounts\n" +
						"w:\tlist active workers\n" +
						"u:\tlist users\n" +
						"t:\tlist current tasks\n" +
						"p:\tlist workers' performance\n" +
						"tcpu:\tlist task types and average cpu load\n" +
						"tm:\tlist metadata for completed tasks(performance)\n" +
						"stats:\tlist performance report\n" +
						"s:\tSave\n" +
						"q:\tQuit without saving");
				System.out.print("Enter a value:");
	       	 	fromUser = br.readLine();
		       	if (fromUser.equals("Bye."))
		       		break;
		       	 	switch(fromUser) {
		       	 	case "a":
		       	 		System.out.println(ps.listAccounts());
		       	 		break;
		       	 	case "w":
		       	 	System.out.println(ps.listActiveWorkers());
		       	 		break;
		       	 	case "u":
		       	 		System.out.println(ps.listUsers());
		       	 		break;
		       	 	case "t":
		       	 		System.out.println(ps.listTasks());
		       	 		break;
		       	 	case "p":
		       	 		System.out.println(ps.listPerofmance());
		       	 		break;
		       	 	case "tcpu":
		       	 		System.out.println(ps.listTaskTypesCpuShare());
		       	 		break;
		       	 	case "tm":
		       	 		System.out.println(ps.listTasksMetadata());
		       	 		break;
		       	 case "stats":
		       	 		System.out.println(ps.listStats());
		       	 		break;
		       	 	case "s":
		       	 		ps.save();
		       	 		break;
					case "q":
		       	 		System.exit(0);
		       	 		break;
		       	 	default:
		       	 		System.err.println("Invalid command:" + fromUser);
		       	 		break;
		       	 	}
			}catch (IOException e) {
		    	 // TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
