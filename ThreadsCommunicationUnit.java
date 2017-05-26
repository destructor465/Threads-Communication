package rtr.destructor.library.concurrent;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * Handles communication between threads.<br>
 * <b>All communicating threads must share same instance of CommunicationUnit to be able to communicate.
 * If threads are divided into groups, one CommunicationUnit per group will let communicate only grouped threads.</b>
 * 
 * @author Destructor
 */
public class ThreadsCommunicationUnit {
	private int transferPacketID = 0; // TODO: no use
	
	private ConcurrentHashMap<String, TransferQueue<TransferData>> receiveQueues;
	
	public ThreadsCommunicationUnit() {
		receiveQueues = new ConcurrentHashMap<String, TransferQueue<TransferData>>();
	}
	
	public class TransferData {
		public int ID;
		public String message;
		public String senderThread, receiverThread; // Names of sender and receiver threads
		public Object data;
		
		public TransferData(String message, String senderThread, String receiverThread, Object data) {
			ID = transferPacketID++;
			this.message = message;
			this.senderThread = senderThread;
			this.receiverThread = receiverThread;
			this.data = data;
		}
	}
	
	/**
	 * Every thread must call this, before any unitilization of ThreadsCommunicationUnit.
	 */
	public void registerThread() {
		if (!receiveQueues.containsKey(Thread.currentThread().getName())) {
			receiveQueues.put(Thread.currentThread().getName(), new LinkedTransferQueue<TransferData>());
			System.out.println("Thread registered: " + Thread.currentThread().getName());
		}
	}
	
	/**
	 * Sends message to specific thread.
	 * @param message Message to send.
	 * @param receiverThread Name of receiver thread, if null, message will be sent to all registered threads.
	 * @param data Additional data to send.
	 * @param waitUntilTransfered If true, sender thread will wait until message is transfered.
	 * <b>Note:</b><br>
	 * Even if waitUntilTransfered is false, sender thread will still wait if receiver thread doesn't have free space in it's queue.
	 */
	public void send(String message, String receiverThread, Object data, boolean waitUntilTransfered) {
		if (!receiveQueues.containsKey(Thread.currentThread().getName())) {
			throw new RuntimeException("Sender thread isn't registered: " + Thread.currentThread().getName());
		}
		
		TransferData transferData = new TransferData(message, Thread.currentThread().getName(), receiverThread, data);
		
		if (receiverThread == null) { // Send to all available threads
			for(Entry<String, TransferQueue<TransferData>> entry : receiveQueues.entrySet()){
				String receiver = entry.getKey();
				TransferQueue<TransferData> queue = entry.getValue();
				
				// Don't send message to itself
				if (receiver.equals(Thread.currentThread().getName()))
					continue;
				
				try {
					if (waitUntilTransfered) {
						queue.transfer(transferData);
					} else {
						queue.put(transferData);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else { // Send to receiver thread
			if (!receiveQueues.containsKey(receiverThread)) {
				throw new RuntimeException("Receiver thread isn't registered: " + receiverThread);
			}
			
			TransferQueue<TransferData> queue = receiveQueues.get(receiverThread);
			
			try {
				if (waitUntilTransfered) {
					queue.transfer(transferData);
				} else {
					queue.put(transferData);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks if thread has any awaiting messages and returns first one.
	 * @param waitUntilReceived If true, current thread will wait until any message will be received. If false and no messages available, this will return null.
	 */
	public TransferData receive(boolean waitUntilReceived) {
		if (!receiveQueues.containsKey(Thread.currentThread().getName())) {
			throw new RuntimeException("Receiver thread doesn't exist: " + Thread.currentThread().getName());
		}
		
		TransferQueue<TransferData> queue = receiveQueues.get(Thread.currentThread().getName());
		
		if (waitUntilReceived) {
			try {
				return queue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return queue.poll();
		}
	}
}