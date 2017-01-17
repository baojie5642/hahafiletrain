package com.baojie.hahafiletrain.MessageInfo;

import java.io.Serializable;

public class MessageForTrans  implements Serializable {
	private static final long serialVersionUID = 2016051710593255555L;
	private final String messageType;
	private final InnerMessage innerMessage;
	
	private MessageForTrans(final String messageType,final InnerMessage innerMessage){
		super();
		this.messageType=messageType;
		this.innerMessage=innerMessage;
	}

	public static MessageForTrans createMessageForTrans(final String messageType,final InnerMessage innerMessage){
		MessageForTrans messageForTrans=new MessageForTrans(messageType, innerMessage);
		return messageForTrans;	
	}

	public String getMessageType() {
		return messageType;
	}

	public InnerMessage getInnerMessage() {
		return innerMessage;
	}
	
}
