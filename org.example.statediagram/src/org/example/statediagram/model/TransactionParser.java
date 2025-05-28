package org.example.statediagram.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.tracecompass.tmf.core.CustomColorPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.json.*;

public class TransactionParser {
	
	

	public static void parseTransactions(ITmfTrace trace, String p) {
		Path path = Paths.get(p);
		try {
			String string = Files.readString(path);
			JSONObject s = new JSONObject(string);
			JSONArray changesArray = s.getJSONArray("sequential_changes");
		
			StateMachineManager stateMachineManager = StateMachineManager.getInstance();	
			List<Function> functions = TransactionManager.getInstance().getFunctions();
			
			
			
			
			for (int i = 0; i < changesArray.length(); i++) {
				JSONObject node = changesArray.getJSONObject(i);
				StateGraph graph = stateMachineManager.getOrCreateGraph(trace, shorten(node.getString("address")));
				
				List<State> states = graph.getStates();
				
				Function function = functions.get(node.getInt("node_idx"));
				CustomColorPaletteProvider colorPaletteProvider = CustomColorPaletteProvider.INSTANCE;
				RGBAColor color = colorPaletteProvider.getColor((long)node.getInt("step_idx"));
				State state = new State(shorten( node.getString("key")), new Color(color.getRed(), color.getGreen(), color.getBlue()), function.getStartTime(), function.getEndTime());
				int transitionIndex = graph.getStates().lastIndexOf(state);
				if (transitionIndex != -1) {
					graph.getState(transitionIndex).addTransition(null, state);
				}
				
				StringBuilder tooltipBuilder = new StringBuilder();
				for (String key : node.keySet()) {
					tooltipBuilder.append(key + "\t" + node.get(key) + "\n");
				}
				state.setTooltip(tooltipBuilder.toString());
				
				graph.addState(state);	
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String shorten(String str) {
        if (str == null || str.length() <= 7) {
            return str;
        }

        String start = str.substring(0, 4);
        String end = str.substring(str.length() - 3);

        return start + "..." + end;
    }

}
