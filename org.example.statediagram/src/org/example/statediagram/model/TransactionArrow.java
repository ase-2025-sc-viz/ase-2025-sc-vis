package org.example.statediagram.model;

import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphArrow;

public class TransactionArrow extends TimeGraphArrow implements ITimeGraphArrow{

	private String fType;
	private String fTokenName;
	private String fAmount;
	

	
	public TransactionArrow(long sourceId, long destinationId, long time, long duration, String amount) {
		super(sourceId, destinationId, time, duration);
		fAmount = amount;
	}
	public TransactionArrow(long sourceId, long destinationId, long time, long duration, String amount,  String type, String tokenName) {
		super(sourceId, destinationId, time, duration);
		fType = type;
		fTokenName = tokenName;
		fAmount = amount;
	}
	public TransactionArrow(long sourceId, long destinationId, long time, long duration, String amount, OutputElementStyle style)  {
		super(sourceId, destinationId, time, duration, style);
	}
	
	public TransactionArrow(long sourceId, long destinationId, long time, long duration, String amount, OutputElementStyle style , String type, String tokenName)  {
		super(sourceId, destinationId, time, duration, style);
		fType = type;
		fTokenName = tokenName;
		fAmount = amount;
	}

	public boolean isSelfTransaction() {
		return !"ETH".equals(fType);
	}
	public String getType() {
		return fType;
	}
	public String getTokenName() {
		return fTokenName;
	}
	public String getAmount() {
		return fAmount;
	}
	
	@Override
	public String toString() {
		return super.toString() + getTokenName();
	}
	
}
