package org.example.statediagram.signal;

import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.example.statediagram.model.State;

public class NodeSelectedSignal extends TmfSignal{
	
    private final State fSelectedState;
	
	public NodeSelectedSignal(Object source, State state) {
		super(source);
		fSelectedState = state;
	}

	public State getState() {
		return fSelectedState;
	}
	
	
}
