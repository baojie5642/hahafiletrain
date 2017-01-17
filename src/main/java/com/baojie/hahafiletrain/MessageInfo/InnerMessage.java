package com.baojie.hahafiletrain.MessageInfo;

import java.io.Serializable;

public class InnerMessage extends BaseMessage implements InterfaceOfMessage, Serializable {

	private static final long serialVersionUID = 2016051711021055555L;

	private InnerMessage(final String stringContent,final long longContent) {
		super(stringContent,longContent);
	}

	public static InnerMessage createInnerMessage(final String stringContent,final long longContent) {
		InnerMessage innerMessage = new InnerMessage(stringContent,longContent);
		return innerMessage;
	}
@Override
	public String getStringContent(){
		return getStringContent();
	}
	@Override
	public long getLong(){
		return getLongContent();
	}

}
