package Server.RL;

public class UpdatingItem {
    private State state;
    private int action;
    private State statePrime;

    private double reward;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public State getStatePrime() {
        return statePrime;
    }

    public void setStatePrime(State statePrime) {
        this.statePrime = statePrime;
    }

    public double getReward() {
        return reward;
    }

    public void setReward() {
        double sum=0;
        for(int i=0;i<statePrime.pendingTasks.length;i++){
            sum +=statePrime.pendingTasks[i].waitingTime();
        }
        this.reward = -sum;
    }

    //aya reward jame pendingtime has ya manfish ya ekhtelafesh ba state e qabli.. explain???????
}
