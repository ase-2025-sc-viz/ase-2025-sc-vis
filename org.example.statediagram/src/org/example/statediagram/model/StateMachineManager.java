package org.example.statediagram.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;


public class StateMachineManager {
    private static StateMachineManager fInstance;
    private final Map<String, StateGraph> fGraphs = new HashMap<>();
    private final Map<ITmfTrace, Map<String, StateGraph>> fTraceGraph = new HashMap<>();
	private ITmfTrace fCurrentTrace;

    
    private StateMachineManager() {
    	fCurrentTrace = TmfTraceManager.getInstance().getActiveTrace();
        TmfSignalManager.registerVIP(this);
    }

    public static StateMachineManager getInstance() {
        if (fInstance == null) {
            fInstance = new StateMachineManager();
        }
        return fInstance;
    }

    public void addGraph(StateGraph graph) {
    	fTraceGraph.computeIfAbsent(fCurrentTrace, key -> new HashMap<String, StateGraph>()).put(graph.getName(), graph);
    }

    public StateGraph getGraph(String name) {
        return fTraceGraph.get(fCurrentTrace).get(name);
    }
    public StateGraph getOrCreateGraph(ITmfTrace trace, String name) {
    	return fTraceGraph.computeIfAbsent(trace, key -> new HashMap<String, StateGraph>()).computeIfAbsent(name, key -> new StateGraph(key) );	
    }

    public Collection<StateGraph> getAllGraphs() {
        return fTraceGraph.getOrDefault(fCurrentTrace, new HashMap<String, StateGraph>()).values();
    }
    
    public void removeGraph(String name) {
        fTraceGraph.get(fCurrentTrace).remove(name);
    }

    public void clear(ITmfTrace trace) {
    	fTraceGraph.get(trace).clear();
    }
    
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {		
		fCurrentTrace = signal.getTrace();
    }
}