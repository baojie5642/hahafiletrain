package com.baojie.hahafiletrain.TransMessage.Client.ClientRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.baojie.hahafiletrain.MessageInfo.InnerMessage;
import com.baojie.hahafiletrain.MessageInfo.LocalUseMessage;
import com.baojie.hahafiletrain.MessageInfo.MessageForTrans;
import com.baojie.hahafiletrain.ThreadAll.StaticForAllThreadPool;
import com.baojie.hahafiletrain.TransMessage.Client.NettyClientForMessage;



public class ClientRunner implements Runnable {

	private final LocalUseMessage localUseMessage;

	private ClientRunner(final LocalUseMessage localUseMessage) {
		super();
		this.localUseMessage = localUseMessage;
	}

	public static ClientRunner createClientRunner(final LocalUseMessage localUseMessage) {
		ClientRunner clientRunner = new ClientRunner(localUseMessage);
		return clientRunner;
	}

	@Override
	public void run() {
		if (null == localUseMessage) {
			throw new NullPointerException();
		}
		final int times = localUseMessage.getRetryTimesWhenConnectionCanNotUse();
		final int waitLong = localUseMessage.getHowManySecondsWaitAfterOneRetry();
		NettyClientForMessage nettyClientForMessage = null;
		for (int i = 0; i < 9999; i++) {
			nettyClientForMessage = NettyClientForMessage.createAndInitClient(localUseMessage);
			if(null==nettyClientForMessage){
				throw new NullPointerException();
			}
			try {
				nettyClientForMessage.connectUninterruptibly(8080, "127.0.0.1");
			} catch (Exception e) {
				//e.printStackTrace();
			}
			if (localUseMessage.takeState()) {
				System.out.println("takeState   success "+i);
				InnerMessage innerMessage = (InnerMessage) localUseMessage.getReturnMessage();
				System.out.println(innerMessage.getStringContent());
				nettyClientForMessage.closeClient();
				nettyClientForMessage = null;
			} else {
				System.out.println("takeState   fail "+i);
				nettyClientForMessage.closeClient();
				nettyClientForMessage = null;
				tryConnection(times, waitLong);
			}
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS));
		}

	}

	private void tryConnection(final int times, final int waitLong) {
		NettyClientForMessage nettyClientForMessage = null;
		for (int i = 0; i < times; i++) {
			nettyClientForMessage = NettyClientForMessage.createAndInitClient(localUseMessage);
			if(null==nettyClientForMessage){
				throw new NullPointerException();
			}
			System.out.println("开始重试……"+i);
			try {
				nettyClientForMessage.connectUninterruptibly(8080, "127.0.0.1");
			} catch (Exception e) {
				//e.printStackTrace();
			}
			if (localUseMessage.takeState()) {
				System.out.println("重试……成功"+i);
				InnerMessage innerMessage = (InnerMessage) localUseMessage.getReturnMessage();
				System.out.println(innerMessage.getStringContent());
				nettyClientForMessage.closeClient();
				nettyClientForMessage = null;
				break;
			}else {
				System.out.println("重试……失败"+i);
				nettyClientForMessage.closeClient();
				nettyClientForMessage = null;
				LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(waitLong, TimeUnit.SECONDS));
			}
		}
	}

	public static void main(String args[]){
		ClientRunner clientRunner=null;
		InnerMessage innerMessage=null;
		MessageForTrans messageForTrans=null;
		LocalUseMessage localUseMessage=null;
		
		
		for(int i=0;i<200;i++){
			innerMessage=InnerMessage.createInnerMessage("Liu Xin Inner "+i,i);
			messageForTrans=MessageForTrans.createMessageForTrans("liuxin test", innerMessage);
			localUseMessage=LocalUseMessage.createLocaLoaclUseMessage(messageForTrans);
			clientRunner=ClientRunner.createClientRunner(localUseMessage);
			StaticForAllThreadPool.Client_DoWork_ThreadPool.submit(clientRunner);
			LockSupport.parkNanos(TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS));
		}
	
	}
}
