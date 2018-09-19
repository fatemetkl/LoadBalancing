package Server;

import Common.Task;

public class TaskMetadata {
	private Common.Task task;
	private Account initiator;
	private Account executor;
	private int originalTaskId;
	private long startTime;
	private long endTime;
	double cpuShare;
	private long pendTime;
	public double getCpuShare() {
		return cpuShare;
	}
	public void setCpuShare(double cpuShare) {
		this.cpuShare = cpuShare;
	}
	public TaskMetadata(Account initiator, Account executor, Task task, 
			int originalTaskId) {
		super();
		this.task = task;
		this.initiator = initiator;
		this.executor = executor;
		this.originalTaskId = originalTaskId;
		startTime = -1;
		endTime = -1;


	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime() {
		this.startTime = System.currentTimeMillis();
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime() {
		this.endTime = System.currentTimeMillis();
	}
	public long waitingTime(){
		task.WaitingTime=startTime-pendTime;
		return (task.WaitingTime);
	}
	public void setPendTime(){
		pendTime=System.currentTimeMillis();
	}
	public long getExecutionTime(){
		if(startTime == -1 || endTime == -1){
			return -1;
		}
		else{
			return endTime - startTime; 
		}
	}
	public Common.Task getTask() {
		return task;
	}
	public void setTask(Common.Task task) {
		this.task = task;
	}
	public Account getExecutor() {
		return executor;
	}
	public void setExecutor(Account executor) {
		this.executor = executor;
	}
	public Account getInitiator() {
		return initiator;
	}
	public void setInitiator(Account initiator) {
		this.initiator = initiator;
	}
	public int getOriginalTaskId() {
		return originalTaskId;
	}
	public void setOriginalTaskId(int originalTaskId) {
		this.originalTaskId = originalTaskId;
	}
	@Override
	public String toString(){
		return task.toString() + " orginal id: " + 
				originalTaskId + " " +
				(executor != null ? " executor: " + executor.toString() : " no executor assigned yet");
	}
}
