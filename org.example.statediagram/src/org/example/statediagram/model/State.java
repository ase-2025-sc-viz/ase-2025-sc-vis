package org.example.statediagram.model;

import java.lang.module.FindException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.swt.graphics.Color;

public class State {
    private final String fName;
    private final Color fColor;
    private final long fStart;
    private final long fEnd;
    private String fTooltip;
    private int fIndex;

    private final Map<String, List<State>> transitions = new HashMap<>();

    public State(String name, Color color, long start, long end) {
        fName = name;
        fColor = color;
        fStart = start;
        fEnd = end;
    }

    public String getName() {
        return fName;
    }
    public Color getColor() {
		return fColor;
	}
    public long getStart() {
    	return fStart;
    }
    public long getEnd() {
		return fEnd;
	}
    
    public void setTooltip(String tooltip) {
		fTooltip = tooltip;
	}
    public String getTooltip() {
		return fTooltip;
	}
    public void setIndex(int index) {
    	fIndex = index;
    }
    
    public int getIndex() {
    	return fIndex;
    }

    public void addTransition(String evenement, State etatCible) {
        transitions.computeIfAbsent(evenement, k -> new ArrayList<>()).add(etatCible);
    }

    public Map<String, List<State>> getTransitions() {
        return transitions;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof State)) {
    		return false;
    	}
    	return Objects.equals(((State)obj).fName,fName);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State{name='").append(fName)
          .append("', color=").append(fColor)
          .append(", start=").append(fStart)
          .append(", end=").append(fEnd)
          .append(", tooltip='").append(fTooltip)
          .append("', index=").append(fIndex)
          .append(", transitions={");

        for (Map.Entry<String, List<State>> entry : transitions.entrySet()) {
            sb.append(entry.getKey()).append("=[");
            List<State> targets = entry.getValue();
            for (int i = 0; i < targets.size(); i++) {
                sb.append(targets.get(i).getName());
                if (i < targets.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("], ");
        }

        if (!transitions.isEmpty()) {
            sb.setLength(sb.length() - 2); // enlever la dernière virgule et espace
        }
        sb.append("}}");
        return sb.toString();
    }
}