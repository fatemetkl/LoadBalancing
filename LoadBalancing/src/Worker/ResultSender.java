package Worker;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import Common.Task;

public class ResultSender extends Thread {

	public boolean terminated = false;
	
	private LinkedBlockingQueue<Future<Task>> results = null;
	private Worker worker = null;
	
	public ResultSender(Worker _worker) {
		worker = _worker;
		results = worker.getResultsQueue();
	}
	
	public void run() {
		System.out.println("Result sender started.");
		while (! terminated) {
			try {
				Future<Task> futureTask = results.take();
				worker.taskCompleted();
				Task completedTask = futureTask.get();
				int taskId = completedTask.getId();
				Object result = completedTask.getResult();
				double cpuShareUsed = completedTask.getCpuShareUsed();
				worker.sendResult(taskId, result, cpuShareUsed);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		System.out.println("Result sender finished!");
	}
	
}
