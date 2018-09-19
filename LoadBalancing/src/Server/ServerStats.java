package Server;

import java.util.HashMap;
import java.util.Map;
/*This class represents a log of the server containing the various information
 * about the tasks processed by the server. This information should aid the 
 * load balancers.  
 * */
public class ServerStats {
	long startTime;
	long endTime;
	/** The tasks that have been completed 
	 * Key - task id (as assigned by the server)
	 * Value - metadata associated with the task*/
	private Map<Integer, TaskMetadata> completedTasks;
	/** The task types that have been previously submitted 
	 * Key - task class UID
	 * Value - average CPU load*/
	private Map<Long, TaskTypeCpuShare> taskTypeCpuShares;
	public ServerStats(){
		this.startTime = System.currentTimeMillis();
		completedTasks = new HashMap<Integer, TaskMetadata>();
		taskTypeCpuShares = new HashMap<Long, ServerStats.TaskTypeCpuShare>();
	}
	public double getTaskTypesCpuShare(Long taskUID){
		if(taskTypeCpuShares.get(taskUID) != null){
		return 
				taskTypeCpuShares.get(taskUID).totalCpuShare / 
				taskTypeCpuShares.get(taskUID).totalCount;
		}
		return -1;
	}
	public void resetStats(){
		this.startTime = System.currentTimeMillis();
		completedTasks = new HashMap<Integer, TaskMetadata>();
		taskTypeCpuShares = new HashMap<Long, ServerStats.TaskTypeCpuShare>();
	}
	public void logTask(TaskMetadata tm){
		//save all information related to this task for the future use
		completedTasks.put(tm.getTask().getId(), tm);
		//update the profile of this task's type
		long taskUID = tm.getTask().getUID();
		this.updateTaskTypeCpuShare(taskUID, tm.getCpuShare());
	}
	/*public void startTimer(){
		//reset start time when the first task received (we don't wan to include
				//time spent waiting for the task when calculating performance) 
				if(taskTypeCpuShares.size() == 0){
					this.startTime = System.currentTimeMillis();
				}
	}*/
	private synchronized void updateTaskTypeCpuShare(long taskUID, double cpuShare){
		//end time is the time when the last task was received. when running 
		//the tasks in bulk, we don't want to include idle time after all tasks
		//are completed
		endTime = System.currentTimeMillis();
		if(taskTypeCpuShares.get(taskUID) != null){
			taskTypeCpuShares.get(taskUID).totalCount += 1;
			taskTypeCpuShares.get(taskUID).totalCpuShare += cpuShare;
		}
		else{
			TaskTypeCpuShare t = new TaskTypeCpuShare(cpuShare);
			taskTypeCpuShares.put(taskUID, t);
		}
		//System.out.println("CPU Share updated for " + taskUID + ":" + this.getTaskTypesCpuShare(taskUID));		
	}
	/**
	 * Calculates Throughput of this server i.e. average number of tasks 
	 * processed per minute
	 * @return throughput of this server (per minute)
	 */
	public double getThroughput(){
		double thp = completedTasks.entrySet().size() / 
				((endTime - startTime)/(Math.pow(10, 3)*60));
		return thp;
	}
	/**
	 * Calculates average execution time for all tasks completed by this server
	 * @return average execution time for all tasks completed by this server
	 */
	public double getAverageExecutionTime(){
		double aet = 0;
		for(Map.Entry<Integer, TaskMetadata> entry: completedTasks.entrySet()){
			aet += (entry.getValue().getEndTime() - entry.getValue().getStartTime()) / Math.pow(10, 3);
		}
		aet /= completedTasks.entrySet().size();
		return aet;
	}
	public String statsToString(){
		return "Total Processed: " + completedTasks.entrySet().size() + 
				"\nThroughput: " + this.getThroughput() + 
				"\nAvg. exec. time: " + this.getAverageExecutionTime(); 
	}
	public String tasksToString(){
		String res = "";
		int counter = 0;
		for(Map.Entry<Integer, TaskMetadata> entry: completedTasks.entrySet()){
			res += "Id:" + entry.getValue().getTask().toString().replace("\n", " ") + " execution time:" + 
					(entry.getValue().getEndTime() - entry.getValue().getStartTime()) / Math.pow(10, 3) + "\n";
			counter++;
		}
		res += "Total found: " + counter + "\n";
		return res;
	}
	public String taskTypesToString(){
		String res = "";
		for(Map.Entry<Long, TaskTypeCpuShare> entry: taskTypeCpuShares.entrySet()){
			res += "Id:" + entry.getKey() + " CPU load:" + this.getTaskTypesCpuShare(entry.getKey()) + "\n";
		}
		return res;
	}
	//represents task type-related metrics: number of task of a particual type
	//computed and the total share of cpu used. This metrics is used to 
	//determined how cpu intensive a particular task is
	class TaskTypeCpuShare{
		//number of tasks of this type executed
		int totalCount;
		//total cpu share (in %) used by all tasks
		double totalCpuShare;
		public TaskTypeCpuShare(double cpuShare){
			totalCpuShare = cpuShare;
			totalCount = 1;
		}
	}
}
