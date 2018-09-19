package Common;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Task base class. Uses 'Run' method for performing
 * task execution. At the end of the execution, 'result'
 * field must be set to the result of the task.
 * All tasks should be subclassed from this class.
 * @author Sviatoslav Sivov
 */
public abstract class Task implements Callable<Task>, Comparable<Task>, Serializable {

	private static final long serialVersionUID = -3839712962975304452L;

	public abstract int getTaskType();

	/**
	 * Commenting header to identify the output of the task
	 */
	protected String description = null;
	
	/** Unique identifier of this task.
	 * The server maintains the IDs of the tasks it processes. Each task has
	 * a unique ID that is used for keeping track of the task. */
	protected int id = 0;
	
	/** Field for the result of the computation.
	 * This result will be returned to the client */
	protected Object result;

	protected PrintWriter writer = null;
	
	protected double cpuShareUsed = -1;
	
	public long getUID(){
		return serialVersionUID;
	}
	
	/**
	 * Accessor
	 * @return - description of the task
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Accessor
	 * @return - taks id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Set id for the task
	 * @param _id - task's new id
	 */
	public void setId(int _id) {
		this.id = _id;
	}

	/**
	 * @return Result of the computation
	 */
	public Object getResult() {
		return result;
	}
	
	/**
	 * Creates a writer to write output to file.
	 * @return - file writer, or
	 * 			 null, if couldn't write to file
	 */
	public long pendTime;
	public long WaitingTime;

	protected void getWriter() {
		String folderPath = String.format("Task Results/%s", this.getClass().getSimpleName());
		(new File(folderPath)).mkdirs();
		String fileName = String.format("%s/%s-%d.txt",
				folderPath, this.getClass().getSimpleName(), id);
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.write(description + "\n\n");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {}
	}
	
	public double getCpuShareUsed() {
		return cpuShareUsed;
	}
	
	/**
	 * Default constructor creates a random id
	 */
	public Task() {
		super();
		id = Math.abs((new Random()).nextInt());
		description = String.format("Task type: %s, id: %d", this.getClass().getSimpleName(), id);
	}
	
	/**
	 * Generic call method for all tasks. Attempts to create writer to write to files
	 * and logs amount of time used for execution and CPU share used.
	 */
	public final Task call() {
		getWriter();
		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		long cpuTimeUsedStart = threadBean.getCurrentThreadCpuTime();
		long startTime = System.currentTimeMillis();
		
		result = runCoreLogic();
		
		/* This value is in nanoseconds */
		long cpuTimeUsedEnd = threadBean.getCurrentThreadCpuTime();
		long endTime = System.currentTimeMillis();
		WaitingTime=startTime-pendTime;
		long cpuTimeUsedByTask = cpuTimeUsedEnd - cpuTimeUsedStart;
		// adding 1 to cover to get ceiling value instead of floor
		long executionTime = endTime - startTime + 1;


		cpuShareUsed = ((double)cpuTimeUsedByTask / Math.pow(10, 6)) / (double)executionTime;
		
		if (cpuShareUsed >= 1) {
			System.out.println("\n<Search token>");
			System.out.println("Task: " + this.description);
			System.out.println("CPU Share " + cpuShareUsed + " !!");
			System.out.println("Start CPU time: " + cpuTimeUsedStart);
			System.out.println("End CPU time: " + cpuTimeUsedEnd);
			System.out.println("Start system time: " + startTime);
			System.out.println("End system time: " + endTime);
		}
		
		if (writer != null) {
			writer.close();
		}
		getWriter();
		if (writer != null) {
			writer.write("Task " + id + " finished in " + executionTime + " seconds "
					+ "and consumed " + cpuShareUsed + " CPU time.");
			writer.close();
		}
		System.out.println();
		System.out.println("Task " + id + " finished and consumed " + cpuShareUsed + " CPU time.");
		System.out.println();
		return this;
	}
	
	/**
	 * Perform the essence functionality of the task.
	 * Each task must implement own functionality here.
	 * @return - result object
	 */
	public abstract Object runCoreLogic();
	
	/**
	 * Attempt to open a file, containing result for this task.
	 */
	public void openResultFile() {
		String folderPath = String.format("Task Results/%s", this.getClass().getSimpleName());
		String fileName = String.format("%s/%s-%d.txt",
				folderPath, this.getClass().getSimpleName(), id);
		File resultFile = new File(fileName);
		if (resultFile.exists()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.open(resultFile);
			} catch (IOException e) {}
		}
	}
	
	@Override
	public int compareTo(Task other) {
		return (this.id - other.id);
	}
	
	@Override
	public boolean equals(Object other){
		if(! (other instanceof Task)){
			return false;
		}
		return ((Task) other).id == this.id;
	}
	@Override
	public String toString(){
		return description;
	}

}
