package Server.RL;

import Server.Account;
import Server.TaskMetadata;

import java.lang.reflect.Array;
import java.util.*;

public class State {

    public List<Double> cpuLoads = new ArrayList<>();
    public List<Integer> taskCounts = new ArrayList<>();
    public List<Long> waitingTimes=new ArrayList<>();
    public TaskMetadata[] pendingTasks;
    public List<Account> activeWorkers;
    public State(List<Account> activeWorkers) {
        activeWorkers.sort(new Comparator<Account>() {
            @Override
            public int compare(Account o1, Account o2) {
                return new Integer(o1.get_id()).compareTo(o2.get_id());
            }
        });

        for (Account account : activeWorkers) {
            double cpuLoad = account.getPerformance().getCpuLoad();
            int uncompletedTaskCount = account.getPerformance().getUncompletedTaskCount();
            cpuLoads.add(cpuLoad);
            taskCounts.add(uncompletedTaskCount);
        }
        this.activeWorkers=activeWorkers;


    }
    public Object[] getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(TaskMetadata pendingTasks[]) {
        this.pendingTasks = pendingTasks;
    }
}
