package com.baojie.hahafiletrain.MessageInfo;

public abstract class BaseMessage {
private final String stringContent;
private final long longContent;

protected BaseMessage(final String stringContent,final long longContent){
	super();
	this.stringContent=stringContent;
	this.longContent=longContent;
}

protected String  getStringContent(){
	return stringContent;
}

protected long getLongContent(){
	return longContent;
}
}
