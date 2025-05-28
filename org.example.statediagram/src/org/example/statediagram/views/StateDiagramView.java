package org.example.statediagram.views;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.example.statediagram.model.State;
import org.example.statediagram.model.StateGraph;
import org.example.statediagram.model.StateMachineManager;
import org.example.statediagram.model.TransactionParser;
import org.example.statediagram.signal.NodeSelectedSignal; 

public class StateDiagramView extends ViewPart {

    public static final String ID = "org.example.statediagram.views.statediagramview";

    private CTabFolder fTabFolder;
    private Composite fParentComposite;
    
    @Override
    public void createPartControl(Composite parent) {
    	
    	TmfSignalManager.register(this);  
    	fParentComposite = parent;    	    	
    	
    	fTabFolder = new CTabFolder(parent, SWT.BORDER);
        fTabFolder.setSimple(false);
        fTabFolder.setTabHeight(30);
        
        
        fTabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	CTabItem selectedTab = fTabFolder.getSelection();
                if (selectedTab != null) {
                    Control control = selectedTab.getControl();
                    if (control instanceof Composite) {
                        Composite composite = (Composite) control;
                        for (Control child : composite.getChildren()) {
                            if (child instanceof Graph) {
                                ((Graph) child).setSelection(new GraphItem[0]);
                            }
                        }
                    }
                    NodeSelectedSignal nodeSelectedSignal = new NodeSelectedSignal(this,  null);
                    TmfSignalManager.dispatchSignal(nodeSelectedSignal);
                    refresh();
                }
            }
        });
        
        fParentComposite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                
                refresh();
            }
        });
		
		for (StateGraph graph : StateMachineManager.getInstance().getAllGraphs()) {
			createGraph(graph);
		}
    
    }
    
    
    
    public void createGraph(StateGraph stateGraph) {
        
        CTabItem tabItem = new CTabItem(fTabFolder, SWT.NONE);
        tabItem.setText(stateGraph.getName());

        Composite tabComposite = new Composite(fTabFolder, SWT.NONE);
        tabItem.setControl(tabComposite);
        tabComposite.setLayout(new GridLayout(1	, false));

        Graph graph = new Graph(tabComposite, SWT.NONE);
        graph.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Map<Integer, GraphNode> nodeMap = new HashMap<>();

        for (State state : stateGraph.getStates()) {
            GraphNode node = new GraphNode(graph, SWT.NONE);
            node.setText(state.getName());
            node.setBackgroundColor(new Color(255,255,255));
            node.setForegroundColor(new Color(0,0,0));
            node.setHighlightColor(state.getColor());
            Label tooltipLabel = new Label(state.getTooltip());
            node.setTooltip(tooltipLabel);
            node.setData(state.getIndex());
            nodeMap.put(state.getIndex(), node);
        }

        for (State state : stateGraph.getStates()) {
            GraphNode sourceNode = nodeMap.get(state.getIndex());
            for (Map.Entry<String, List<State>> entry : state.getTransitions().entrySet()) {
                String event = entry.getKey();
                for (State target : entry.getValue()) {
                    GraphNode targetNode = nodeMap.get(target.getIndex());
                    GraphConnection connection = new GraphConnection(graph, ZestStyles.CONNECTIONS_DIRECTED, sourceNode, targetNode);
                    connection.setLineColor(new Color(0,0,0));
                    if(event!=null) {
                    	connection.setText(event);
                    }
                }
            }
        }
        
        graph.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Object selection = e.item;

                if (selection instanceof GraphNode) {
                    GraphNode node = (GraphNode) selection;
                    int idx = (int) node.getData();
                    NodeSelectedSignal nodeSelectedSignal = new NodeSelectedSignal(this,  stateGraph.getState(idx));
                    TmfSignalManager.dispatchSignal(nodeSelectedSignal);
                }                
            }
        });
        
        

        
        graph.setLayoutAlgorithm(new TreeLayoutAlgorithm(TreeLayoutAlgorithm.LEFT_RIGHT), true);
        tabComposite.layout();
    }

    

    @Override
    public void setFocus() {
        fTabFolder.setFocus();
    }
    
    @Override  
    public void dispose() {  
        TmfSignalManager.deregister(this);
        super.dispose();  
    }
    
    public void refresh() {
    	CTabItem selectedTab = fTabFolder.getSelection();
        if (selectedTab != null) {
            Control control = selectedTab.getControl();
            if (control instanceof Composite) {
                Composite composite = (Composite) control;
                for (Control child : composite.getChildren()) {
                    if (child instanceof Graph) {
                    	((Graph)child).setLayoutAlgorithm(new TreeLayoutAlgorithm(TreeLayoutAlgorithm.LEFT_RIGHT), true);
                        composite.layout();
                    }
                }
            }
        }
    	
    }
    
    
    @TmfSignalHandler  
	public void traceSelected(TmfTraceSelectedSignal signal) {	
    	for (CTabItem item : fTabFolder.getItems()) {
    	    item.dispose();
    	}
    	for (StateGraph graph : StateMachineManager.getInstance().getAllGraphs()) {
			createGraph(graph);
		}
    }
}

