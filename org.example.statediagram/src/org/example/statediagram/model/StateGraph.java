package org.example.statediagram.model;

import java.util.ArrayList;
import java.util.List;

public class StateGraph {
	private final List<State> states = new ArrayList<>();
    private final String name;

    public StateGraph(String name) {
        this.name = name;
    }

    public void addState(State state) {
        state.setIndex(states.size());
    	states.add(state);
    }

    public List<State> getStates() {
        return states;
    }

    public State getState(int idx) {
        return states.get(idx);
    }

    public String getName() {
        return name;
    }

    public void clear() {
        states.clear();
    }
}
