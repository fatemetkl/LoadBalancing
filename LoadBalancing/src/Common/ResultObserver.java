package Common;

public interface ResultObserver {

	/**
	 * Computation was completed by the server,
	 * present result to the user
	 * @param taskId - id of the completed task
	 */
	public void resultReceived(int taskId, Object result);
	
}
