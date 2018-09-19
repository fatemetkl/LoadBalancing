package Server.RL;

public class StateActionPair {

    private State state;
    private Action action;

    public StateActionPair(State state, Action action) {
        this.state = state;
        this.action = action;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
