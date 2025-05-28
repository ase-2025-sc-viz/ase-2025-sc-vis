package org.example.statediagram.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

public class TransactionManager {
	
	private static TransactionManager fInstance = null;
	private Map<ITmfTrace, List<Transaction>> fTraceTransactions = new HashMap<>();  
	private Map<ITmfTrace, List<Function>> fTraceFunctions = new HashMap<>();
	private Map<ITmfTrace, Map<Integer, Long>> fFunctionToEntryIdMap = new HashMap<>();
	private ITmfTrace fCurrentTrace;
	private long fSizeArrow;
	private Map<ITmfTrace, Integer> fDepth = new HashMap<>();

	private TransactionManager() {
		fCurrentTrace = TmfTraceManager.getInstance().getActiveTrace();
        TmfSignalManager.registerVIP(this);         
	}
	
	public static synchronized void dispose() {  
	    TransactionManager manager = fInstance;  
	    if (manager != null) {  
	        TmfSignalManager.deregister(manager);  
	    }  
	    fInstance = null;  
	}
	
	public static TransactionManager getInstance() {
		if (fInstance == null) {
			fInstance = new TransactionManager();
		}
		return fInstance;
	}

	public List<Transaction> getTransactions(ITmfTrace trace) {
		return fTraceTransactions.getOrDefault(trace, new ArrayList<Transaction>());
	}
	
	public List<Function> getFunctions() {
		return fTraceFunctions.getOrDefault(fCurrentTrace, new ArrayList<Function>());
	}
	
	public long getSizeArrow() {
		return fSizeArrow;
	}

	public int getDepth() {
		return (int) ((int) fDepth.getOrDefault(fCurrentTrace, 0)*1.7);
	}
	
	public void addTransaction(ITmfTrace trace, ITmfEventField eventField) {
	    ITmfEventField tsField = eventField.getField("ts");  
	    long time = (tsField != null) ? Long.parseLong(tsField.getFormattedValue()) : 0;  
	      
	    ITmfEventField tidField = eventField.getField("tid");  
	    int receiver = (tidField != null) ? Integer.parseInt(tidField.getFormattedValue()) : 0;  
	      
	    ITmfEventField amountField = eventField.getField("args/amount");  
	    String amount = (amountField != null) ? amountField.getFormattedValue() : "	";  
	      
	    ITmfEventField tokenSymbolField = eventField.getField("args/token_symbol");  
	    String type = (tokenSymbolField != null) ? tokenSymbolField.getFormattedValue() : "";  
	      
	    ITmfEventField tokenNameField = eventField.getField("args/token_name");  
	    String tokenName = (tokenNameField != null) ? tokenNameField.getFormattedValue() : "";  
	      	
		int sender = receiver;
		if ("ETH".equals(type)) {
			sender--;
		}
		fTraceTransactions.computeIfAbsent(trace, k -> new ArrayList<>()).add(new Transaction(sender, receiver, time, amount, type, tokenName));
	}
	
	
	public void registerFunctionEntryId(ITmfTrace trace, int functionId, long entryId) {  
	    fFunctionToEntryIdMap.computeIfAbsent(trace, k -> new HashMap<>()).put(functionId, entryId);  
	}  
	
	public long getEntryIdForFunction(ITmfTrace trace, int functionId) {  
	    Map<Integer, Long> map = fFunctionToEntryIdMap.get(trace);  
	    return map != null ? map.getOrDefault(functionId, -1L) : -1L;  
	}
	
	@TmfSignalHandler  
    public synchronized void traceOpened(TmfTraceOpenedSignal signal) { 
		System.out.println("Signal received : " + signal);
		ITmfTrace trace = signal.getTrace();
		fCurrentTrace = trace;
		ITmfContext ctx = trace.seekEvent(0); 
		

        ITmfEvent event;
        
        Map<Integer, List<Function>> functions = new HashMap<>();
        int index = 0;
        
        
        while ((event = trace.getNext(ctx)) != null) {
            ITmfEventField content = event.getContent();
            
            if("B".equals( content.getField("ph").getFormattedValue())) {
            	functions
	                .computeIfAbsent(Integer.parseInt(content.getField("tid").getFormattedValue()), k -> new ArrayList<>())
	                .add(new Function(index++, Long.parseLong(content.getField("ts").getFormattedValue())));
            } else if("E".equals( content.getField("ph").getFormattedValue())) {
            	List<Function> fs = functions
            		.computeIfAbsent(Integer.parseInt(content.getField("tid").getFormattedValue()), k -> new ArrayList<>());
            	fs.get(fs.size()-1).setEndTime(Long.parseLong(content.getField("ts").getFormattedValue()));
            }
            
            ITmfEventField argsField = content.getField("args/amount");
            if (argsField != null) {
            	addTransaction(trace, content);
            }
        }
        
        Map<Integer, Function> tmp = new HashMap<>();
        for (List<Function> functionList :functions.values()) {
        	for (Function function : functionList) {
        		tmp.put(function.getIndex(), function);
        	}
        }
        
        fDepth.put(fCurrentTrace, functions.size());
        
        List<Function> finalFunctions = new ArrayList<>();
        for (int i = 0; i < tmp.size(); i++) {
        	finalFunctions.add(tmp.get(i));
        }
        fTraceFunctions.put(trace, finalFunctions);
        
        StringBuilder pathBuilder = new StringBuilder();
		pathBuilder.append("./addFiles/");
		pathBuilder.append(trace.getName().substring(0, trace.getName().length()-5));
		pathBuilder.append("_storage_report.json");
		
		
        TransactionParser.parseTransactions(fCurrentTrace, pathBuilder.toString());
        fSizeArrow = fCurrentTrace.getEndTime().getValue() - fCurrentTrace.getStartTime().getValue();
		fSizeArrow /= 50;
        
        
	}
	
	@TmfSignalHandler  
    public synchronized void traceClosed(TmfTraceClosedSignal signal) {  
		System.out.println("Signal received : " + signal);
        ITmfTrace trace = signal.getTrace();  
        fTraceTransactions.get(trace).clear();
        fTraceFunctions.get(trace).clear();
        StateMachineManager.getInstance().clear(trace);
    }  
	
	@TmfSignalHandler  
	public void traceSelected(TmfTraceSelectedSignal signal) {		
		ITmfTrace trace = signal.getTrace();
		
		if (trace == fCurrentTrace) {
			return;
		}
		
		
		fCurrentTrace = trace;
		fSizeArrow = fCurrentTrace.getEndTime().getValue() - fCurrentTrace.getStartTime().getValue();
		fSizeArrow /= 50;

//	    Display.getDefault().asyncExec(() -> {  
//	        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();  
//	        if (window != null) {  
//	            IWorkbenchPage activePage = window.getActivePage();  
//	            if (activePage != null) {  
//	                try {  	                	
//	                	
//	                    IViewPart viewPart = activePage.showView("org.eclipse.tracecompass.analysis.profiling.ui.flamechart:org.eclipse.tracecompass.incubator.traceevent.analysis.callstack",   
//	                                      null, IWorkbenchPage.VIEW_ACTIVATE);  
//	                    activePage.showView("org.example.statediagram.views.statediagramview",   
//                                null, IWorkbenchPage.VIEW_ACTIVATE);  
//	                    activePage.showView("org.eclipse.linuxtools.tmf.ui.views.statistics",   
//                                null, IWorkbenchPage.VIEW_ACTIVATE); 
//	                    
//	                    TmfSignalManager.dispatchSignal(new TmfTraceSelectedSignal(this, trace));  
//	                    
//	                } catch (PartInitException e) {  
//	                    // Gérer l'erreur  
//	                }  
//	            }  
//	        }  
//	    });  
	}
	
	@TmfSignalHandler  
	public void intervalChanged(TmfWindowRangeUpdatedSignal signal) {		
		TmfTimeRange range = signal.getCurrentRange();
		fSizeArrow = range.getEndTime().getValue() - range.getStartTime().getValue();
		fSizeArrow /= 50;
	}
		
}
