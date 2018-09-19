package Common;

import java.io.Serializable;

/**
 * This class represents various performance statistics related to the worker
 * node.
 * @author Oleg Vyshnvyetskyi
 *
 */
public class Performance implements Serializable {

	private static final long serialVersionUID = -6070901163850600461L;
	
	//@SuppressWarnings("unused")
	private int uncompletedTaskCount;
	//@SuppressWarnings("unused")
	private double SystemCpuLoad;
	
	public void setCpuLoad(double cpuLoad) {
		SystemCpuLoad = cpuLoad;
	}
	
	public void setUncompletedTaskCount(int numFreeThreads) {
		uncompletedTaskCount = numFreeThreads;
	}
	public double getCpuLoad() {
		return SystemCpuLoad;
	}
	
	public int getUncompletedTaskCount() {
		return uncompletedTaskCount;
	}
	@Override
	public String toString(){
		return "uncompletedTaskCount: " + uncompletedTaskCount + 
				" SystemCpuLoad: " + SystemCpuLoad;
	}
}
