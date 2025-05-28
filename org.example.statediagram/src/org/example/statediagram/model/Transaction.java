package org.example.statediagram.model;

public class Transaction {
	
	private int fSender;
	private int fReceiver;
	private long fTime;
	private String fAmount;
	private String fType;
	private String fTokenName;


	public Transaction(int sender, int receiver, long time, String amount, String type, String tokenName) {
		fSender = sender;
		fReceiver = receiver;
		fTime = time*1000;
		fAmount = amount;
		fType = type;
		fTokenName = tokenName;
	}
	
	public String getAmount() {
		return fAmount;
	}
	public int getReceiver() {
		return fReceiver;
	}
	public int getSender() {
		return fSender;
	}
	public long getTime() {
		return fTime;
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
	
	
	
	@Override
	public String toString() {
		return "Transaction{" +
	            "sender=" + fSender +
	            ", receiver=" + fReceiver +
	            ", time=" + fTime +
	            ", amount=" + fAmount +
	            ", type=" + fType +
	            '}';
	}

}
