package com.baojie.hahafiletrain.TransMessage;

public class TestWithString {

	private volatile String inner;
	
	private TestWithString(final String inner){
		super();
		this.inner=inner;
	}

	public static TestWithString createTestWithString(final String inner){
		TestWithString testWithString=new TestWithString(inner);
		return testWithString;
	}
	
	public String getInner() {
		return inner;
	}

	public void setInner(String inner) {
		synchronized (this) {
			this.inner = inner;
		}
	}
	
}
