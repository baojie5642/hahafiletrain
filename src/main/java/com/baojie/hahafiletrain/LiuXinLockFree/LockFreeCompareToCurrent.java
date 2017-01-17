package com.baojie.hahafiletrain.LiuXinLockFree;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.baojie.hahafiletrain.MessageInfo.InnerMessage;
import com.baojie.hahafiletrain.MessageInfo.MessageForTrans;
import com.baojie.hahafiletrain.Util.SysTime.RightTime;


public class LockFreeCompareToCurrent {

	public static void main(String args[]){
		final InnerMessage innerMessage=InnerMessage.createInnerMessage("test",1024);
		final MessageForTrans messageForTrans=MessageForTrans.createMessageForTrans("lockfree", innerMessage);
		//final MyLockFree<MessageForTrans> myLockFree= new MyLockFree<MessageForTrans>(50000001);
		final ConcurrentLinkedQueue<MessageForTrans> queue=new ConcurrentLinkedQueue<>();
			System.out.println(RightTime.getSysTime(null, null));
		for(int i=0;i<50000000;i++){
			//myLockFree.add(messageForTrans);
			queue.add(messageForTrans);
		}
		System.out.println(RightTime.getSysTime(null, null));

		
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
}
