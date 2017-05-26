# Threads-Communication
Allows easier communication between threads.
With ThreadsCommunicationUnit class you can easily communicate between threads, send and receive some data.

Usage:
1. Initialize ThreadsCommunicationUnit:
```java
ThreadsCommunicationUnit com = new ThreadsCommunicationUnit();
```
2. Create new threads and register them with ThreadsCommunicationUnit:
```java
Thread t1 = new Thread(new Runnable() {
  public void run() {
    com.registerThread();
  }
});

Thread t2 = new Thread(new Runnable() {
  public void run() {
    com.registerThread();
  }
});
```
3. Send and receive messages:
```java
com.send("MessageToSend", "ReceiverThreadName", "Additional data, can be anything.", false); // Send
TransferData data = com.receive(true); // Receive
```
4. Use received message:
```java
TransferData data = com.receive(true);
System.out.println(data.message);
System.out.println(data.data);
System.out.println(data.receiverThread);
```

Example:
```java
ThreadsCommunicationUnit com = new ThreadsCommunicationUnit();

Thread sender1 = new Thread(new Runnable() {
  public void run() {
    Thread.currentThread().setName("Sender1");
    com.registerThread();

    // Send messages
    for (int i = 0; i < 2; i++) {
      com.send("lol", "Receiver1", null, false);
    }
  }
});
		
Thread receiver1 = new Thread(new Runnable() {
  public void run() {
    Thread.currentThread().setName("Receiver1");
    com.registerThread();
  	
    while (true) {
      TransferData data = com.receive(true);
      System.out.println(String.format("Message received from: %s, %s, ID: %s", data.receiverThread, data.message, data.ID));
    }
  }
});
    
receiver1.start();
sender1.start();
```
