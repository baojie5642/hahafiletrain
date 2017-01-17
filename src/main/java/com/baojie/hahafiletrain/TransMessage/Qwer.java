package com.baojie.hahafiletrain.TransMessage;

public class Qwer implements Runnable {
	
	private volatile String llxx;
	
	private volatile TestWithString testWithString;
	
	private Qwer(final String  llxx,final TestWithString testWithString){
		super();
		this.llxx=llxx;
		this.testWithString=testWithString;
	}
	
	public static Qwer createQwer(final String llxx ,final TestWithString testWithString){
		Qwer qwer=new Qwer(llxx,testWithString);
		return qwer;
	}
	
	@Override
	public void run(){
		System.out.println("******************************print in thread begin*******************************");
		sysOutOld();
		synchronized(this){
			llxx="llxx-in-thread";
		    testWithString.setInner("inner-in-thread");
		}
		System.out.println();
		System.out.println("**** After Change ****");
		System.out.println();
		sysOutNew();
		System.out.println();
		System.out.println("**** Change in function begin****");
		System.out.println();
		changeLLXX(llxx);
		changeINNER(testWithString);
		System.out.println("**** Change in function end****");
		System.out.println();
		System.out.println("******************************print in thread end*******************************");
		System.out.println();
	}
	
	private void sysOutOld(){
		System.out.println("******sysOutOld******begin*****");
		System.out.println("old-llxx : " +llxx);
		System.out.println( "old-inner : " +testWithString.getInner());
		System.out.println("******sysOutOld******end*****");
	}
	
	private void sysOutNew(){
		System.out.println("******sysOutNew*****begin*****");
		System.out.println("new-llxx : " +llxx);
		System.out.println( "new-inner : " +testWithString.getInner());
		System.out.println("******sysOutNew****end*****");
	}
	
	private void changeLLXX(String workString){
		synchronized(this){
			workString="llxx has change";
		}
		System.out.println(workString);
		System.out.println(llxx);
		System.out.println();
	}
	
	private void changeINNER(TestWithString inner){
		synchronized (this) {
			inner.setInner("change inner");
		}
		System.out.println(inner.getInner());
		System.out.println(testWithString.getInner());
		System.out.println();
	}
}
