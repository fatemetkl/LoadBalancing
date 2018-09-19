package Common;

import java.io.IOException;
import java.net.Socket;
import java.util.TimerTask;

/**
 * This is a task that is used by a timer
 * to close some socket after a given amount of time.
 * This is done to unblock some thread, which is blocked
 * on reading a socket
 * @author Sviatoslav Sivov
 */
public class ExternalSocketCloser extends TimerTask {

	/** Socket to be closed */
	private final Socket toClose;
	
	/**
	 * Task only needs to know which socket to close
	 * @param _toClose - socket to close
	 */
	public ExternalSocketCloser(Socket _toClose) {
		toClose = _toClose;
	}
	
	/**
	 * Close the socket. (Or attempt doing so)
	 */
	@Override
	public void run() {
		try {
			toClose.close();
		} catch (IOException e) {}
	}	

}
