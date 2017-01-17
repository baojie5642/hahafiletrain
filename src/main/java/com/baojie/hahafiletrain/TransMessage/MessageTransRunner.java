package com.baojie.hahafiletrain.TransMessage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class MessageTransRunner {
	
	public static void  main(String args[]) {
		 String llxxString="llxx in main";
	    TestWithString testWithString=TestWithString.createTestWithString("inner-in-main");
	   
	    Qwer qwer=Qwer.createQwer(llxxString, testWithString);
	    
	    Thread thread=new Thread(qwer,"qwer");
	    
	   LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS));
	   
	   thread.start();
	   
	   LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(3, TimeUnit.SECONDS));
	   
	   System.out.println("*****main   print    *******");
	   
	   System.out.println("llxxString in main: "+llxxString);
	   
	   System.out.println("testWithString inner in main :"+testWithString.getInner());
	   
	   
	}
	   

}
