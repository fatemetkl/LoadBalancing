package Client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import Tasks.PasswordBruteforce;
import Tasks.PrimeCalculator;
import Tasks.Timer;
import Common.Constants;
import Common.ResultObserver;
import Common.Task;

public class BenchmarkTester implements ResultObserver {

	public static boolean makeTasksRandom = true;
	
	private ArrayList<Task> taskSet;
	private LinkedBlockingQueue<Integer> receivedResults = new LinkedBlockingQueue<Integer>();
	
	public BenchmarkTester(int numTasks) {
		taskSet = generateTasks(numTasks);
	}

	private ArrayList<Task> generateTasks(int numTasks) {
		Set<Integer> existingTaskIds = new HashSet<Integer>();
		int duplicateCount = 0;
		ArrayList<Class<?>> taskTypes = new ArrayList<Class<?>>();
		taskTypes.add(PasswordBruteforce.class);
		taskTypes.add(PrimeCalculator.class);
		taskTypes.add(Timer.class);
		ArrayList<Task> tasks = new ArrayList<Task>();
		Random randomizer = new Random();
		makeTasksRandom = true;
		for (int i = 0; i < numTasks; i++) {
			Class<?> nextTaskType = taskTypes.get(randomizer.nextInt(taskTypes.size()));
			try {
				Task nextTask = (Task) nextTaskType.newInstance();
				tasks.add(nextTask);
				if (! existingTaskIds.add(nextTask.getId())) {
					duplicateCount++;
				}
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		makeTasksRandom = false;
		System.out.println(duplicateCount + " duplicate ids generated.");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tasks;
	}
	
	public synchronized long runBenchmarkTasks(Client client) {
		
		client.addObserver(this);
		long startTime = System.currentTimeMillis();
		
		Set<Integer> sentTaskIds = new HashSet<Integer>();
		long start = System.currentTimeMillis();
		for (Task task : taskSet) {
			client.sendNewTask(task);
			sentTaskIds.add(task.getId());
		}
		long end = System.currentTimeMillis();
		System.out.println("Tasks sent in " + (end - start) + " milliseconds.");
		while (sentTaskIds.size() > 0) {
			try {
				Integer receivedId = receivedResults.take();
				if (sentTaskIds.remove(receivedId)) {
					if (Constants.CLIENT_DEBUG) {
						System.out.println("Task " + receivedId + " removed. Still waiting for " + sentTaskIds.size() + " tasks.");
					}
				} 
			} catch (InterruptedException e) {
				// ignore
			}	
		}
		
		long endTime = System.currentTimeMillis();
		client.removeObserver(this);
		
		return (endTime - startTime);
	}

	@Override
	public void resultReceived(int taskId, Object result) {
		receivedResults.add(taskId);
	}
	
}
