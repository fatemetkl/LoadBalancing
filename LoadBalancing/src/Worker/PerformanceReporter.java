package Worker;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import Common.Performance;

import com.sun.management.OperatingSystemMXBean;

public class PerformanceReporter extends Thread {
	
	public boolean terminated = false;
	
	private Worker worker;
	private final Thread mainThread;
	
	public PerformanceReporter(Worker _worker, Thread _mainThread) {
		worker = _worker;
		mainThread = _mainThread;
	}
	
	public void run() {
		Performance stats = new Performance();
		while ( mainThread.isAlive() ) {
			OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
			
			// What % load the overall system is at, from 0.0-1.0
			stats.setCpuLoad(osBean.getSystemCpuLoad());
			stats.setUncompletedTaskCount(worker.getActiveTaskCount());
			try {
				worker.sendStat(stats);
			} catch (IOException e1) {
				// ignore
			}
			
			// What % CPU load this current JVM is taking, from 0.0-1.0
			//System.out.println(osBean.getProcessCpuLoad());
		
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
}