package Common;

import java.io.Serializable;

/**
 * Message for communication between system components (clients-server)
 * @author Sviatoslav Sivov
 */
public class Message implements Serializable {
	
	private static final long serialVersionUID = 908563164473825825L;

	/** Command for the receiver. Intention of the message */
	private final Constants.RequestType command;
	
	/** Items necessary for the corresponding command 
	 *  If message is directed to the server, arguments[0]
	 *  is identification info about the sender */
	private final Object[] arguments;
	
	public Message(Constants.RequestType cmd, Object... args) {
		command = cmd;
		arguments = args;
	}
	
	/**
	 * Accessor
	 */
	public Constants.RequestType getCommand() {
		return command;
	}
	
	/**
	 * Accessor
	 */
	public Object[] getArgs() {
		return arguments;
	}
	
	@Override
	public String toString(){
		String res = "";
		for(Object arg: arguments){
			res += arg + " ";
		}
		if(res.length() > 160){
			res = res.substring(0, 160) + "...";
		}
		return "Command: " + command.toString() + " " + res; 
	}
	
}
