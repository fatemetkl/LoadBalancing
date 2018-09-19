package Client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import Tasks.PasswordBruteforce;
import Tasks.PasswordBruteforce2;
import Tasks.PrimeCalculator;
import Tasks.Timer;
import Tasks.Timer2;
import Common.ResultObserver;
import Common.Task;

public class BenchmarkTester2 implements ResultObserver {

	public static boolean makeTasksRandom = false;
	
	private ArrayList<Task> taskSet;
	private LinkedBlockingQueue<Integer> receivedResults = new LinkedBlockingQueue<Integer>();
	
	public BenchmarkTester2(int numTasks) {
		taskSet = generateTasks2(numTasks);
	}

	private ArrayList<Task> generateTasks(int numTasks) {
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
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		makeTasksRandom = false;
		return tasks;
	}
	private ArrayList<Task> generateTasks2(int numTasks) {
		ArrayList<Class<?>> taskTypes = new ArrayList<Class<?>>();
		taskTypes.add(PasswordBruteforce.class);
		taskTypes.add(PrimeCalculator.class);
		taskTypes.add(Timer.class);
		ArrayList<Task> tasks = new ArrayList<Task>();
		for (int i = 0; i < numTasks; i++) {
			Task nextTask = null;
			switch(i % 3){
			case 0:
				//nextTask = new Timer(10, 20);
				//nextTask = new PrimeCalculator(1, 999999);
				nextTask = new PasswordBruteforce2("QWERTY");
				break;
			case 1:
				nextTask = new Timer2(10, 20);
				//nextTask = new PrimeCalculator(1, 999999);
				//nextTask = new PasswordBruteforce("QWER");
				break;
			case 2:
				//nextTask = new Timer(10, 20);
				//nextTask = new PrimeCalculator(1, 999999);
				nextTask = new PasswordBruteforce2("QWERTY");
				break;
			/*case 2:
				//nextTask = new Timer(10, 20);
				//nextTask = new PrimeCalculator(1, 999999);
				nextTask = new PasswordBruteforce("QWERTY");
				break;
			case 3:
				//nextTask = new Timer(10, 20);
				//nextTask = new PrimeCalculator(1, 999999);
				nextTask = new PasswordBruteforce("QWERTY");
				break;
			case 4:
				//nextTask = new Timer(10, 20);
				//nextTask = new PrimeCalculator(1, 999999);
				nextTask = new PasswordBruteforce("QWERTY");
				break;*/
			//case 2: 
				//nextTask = new Timer(10, 20);
				//nextTask = new PrimeCalculator(1, 999999);
				//nextTask = new PasswordBruteforce("QWER");
				//break;
			}
			//Task nextTask = (Task) nextTaskType.newInstance();
			tasks.add(nextTask);
		}
		return tasks;
	}
	
	public synchronized long runBenchmarkTasks(Client client) {
		
		client.addObserver(this);
		long startTime = System.currentTimeMillis();
		
		Set<Integer> sentTaskIds = new HashSet<Integer>();
		for (Task task : taskSet) {
			client.sendNewTask(task);
			try {Thread.sleep(100);}
			catch (InterruptedException e) {}
			sentTaskIds.add(task.getId());
		}
		while (sentTaskIds.size() > 0) {
			try {
				Integer receivedId = receivedResults.take();
				if (sentTaskIds.remove(receivedId)) {
					System.out.println("Task " + receivedId + " removed. Still waiting for " + sentTaskIds.size() + " tasks.");
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
