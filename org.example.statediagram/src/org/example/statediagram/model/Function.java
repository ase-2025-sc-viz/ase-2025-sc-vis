package org.example.statediagram.model;

public class Function {
	private int fIndex;
	private long fStartTime;
	private long fEndTime;
	
	public Function(int index, long startTime) {
		fIndex = index;
		fStartTime = startTime;
	}
	
	public void setEndTime(long endTime) {
		fEndTime = endTime;
	}
	
	public int getIndex() {
		return fIndex;
	}
	
	public long getStartTime() {
		return fStartTime;
	}
	
	public long getEndTime() {
		return fEndTime;
	}
	
	@Override
	public String toString() {
	    return "Function{" +
	    	   "index=" + fIndex +
	           ", startTime=" + fStartTime +
	           ", endTime=" + fEndTime +
	           '}';
	}
}
