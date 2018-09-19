package Server;

import Common.Message;
import Common.Task;
import Server.RL.Action;
import Server.RL.Policy;
import Server.RL.State;
import Server.RL.UpdatingItem;

import java.lang.reflect.Array;
import java.util.*;

public class QlearningLoadBalancer {

//    private Map<StateActionPair, Float> qValues = new HashMap<>();

    private Map<Integer, UpdatingItem> updatingItemMap = new HashMap<>();


    private Double[][] w = null;

    private double alpha = 0.2;
    private double gamma = 0.2;

    public int disPtch(List<Account> activeWorkers, Message message) {
        Task task = (Task) message.getArgs()[1];

        State state = new State(activeWorkers);
        if (w == null) {
            w = new Double[activeWorkers.size()][2 * state.cpuLoads.size()];
            for (int i = 0; i < activeWorkers.size(); i++) {
                w[i] = new Double[2 * state.cpuLoads.size()];
                for (int j = 0; j < 2 * state.cpuLoads.size(); j++) {
                    w[i][j] = 0.0;
                }
            }
        }
        int action = Policy.getAction(w, state, activeWorkers.size());

        UpdatingItem item = new UpdatingItem();
        item.setState(state);
        item.setAction(action);
        updatingItemMap.put(task.getId(), item);

        return action;
    }

    public State update(List<Account> activeWorkers, TaskMetadata tm, TaskMetadata[] pendingTasks,State statePrime) {
      //  State statePrime = new State(activeWorkers);
        statePrime.setPendingTasks(pendingTasks);
        UpdatingItem item = updatingItemMap.get(tm.getTask().getId());
        item.setStatePrime(statePrime);// active workers after completing the task
        item.setReward();

        Double[] qStatePrime = Policy.getQ(w, item.getStatePrime(), activeWorkers.size());
        Double[] qState = Policy.getQ(w, item.getState(), activeWorkers.size());
        Double[] gradians = Policy.gradian(w, item.getState(), activeWorkers.size());
        for (int i = 0; i < w[item.getAction()].length; i++) {
            w[item.getAction()][i] += alpha * (item.getReward() + gamma * Collections.max(Arrays.asList(qStatePrime)) - qState[item.getAction()]) * gradians[i];
        }

        double sumW = 0;
        for (int i = 0; i < w[item.getAction()].length; i++) {
            sumW += w[item.getAction()][i];
        }
        for (int i = 0; i < w[item.getAction()].length; i++) {
            if (sumW != 0)
                w[item.getAction()][i] /= sumW;
        }
//        w[item.getAction()] = add(w[item.getAction()],
//                multiply(alpha *
//                                (item.getReward() +
//                                        (gamma *)
//                                        - Policy.getQ(w, item.getState(), activeWorkers.size())),
//                        ));
        // w <- w + alpha [ r + gamma * qStatePrime(s',a') - qStatePrime(s,a) ] qStatePrime'(s,a)

    }


    private Double[] multiply(double n, Double[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i] * n;
        }
        return arr;

    }

    private Double[] add(Double[] arr, Double[] n) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i] + n[i];
        }
        return arr;

    }
}
