package com.credits.test;
import java.math.BigDecimal;
import java.nio.channels.SocketChannel;
import java.security.PrivateKey;
import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.data.TransactionFlowData;
import com.credits.leveldb.client.thrift.Transaction;

public class SpamThread implements Runnable{
   private Thread thread;
   
   private String name;
   
   public SpamThread(String name) {
	   this.name = name;
   }
   
   public void run() {
      System.out.println(String.format("Running %s", this.name));
      try {
    	  RawConnectionClient cAPI = new RawConnectionClient(Config.ip, Config.port);
    	  
    	  byte[] privateKeyByteArr1 = Converter.decodeFromBASE58(Config.wallet1PrivateKey);
    	  PrivateKey privateKey1 = Ed25519.bytesToPrivateKey(privateKeyByteArr1);
    	  
    	  byte[] privateKeyByteArr2 = Converter.decodeFromBASE58(Config.wallet2PrivateKey);
    	  PrivateKey privateKey2 = Ed25519.bytesToPrivateKey(privateKeyByteArr2);
    	  
    	  cAPI.start();
    	  
    	  BigDecimal total1 = new BigDecimal(1);
    	  BigDecimal total2 = new BigDecimal(1);
    	  
    	  while(true) {
    		  for(int i=0;i<cAPI.Sockets.size();i++) {
    			  SocketChannel socketChannel = cAPI.Sockets.get(i);
    			  if(socketChannel.isConnected()) {
	    			  TransactionFlowData transaction;
	    			  if(i%2==0) {
	    				  total1.subtract(new BigDecimal("0.01"));
	    				  total2.add(new BigDecimal("0.01"));
	    				  transaction = Utils.createTransaction(Config.wallet1PublicKey, privateKey1, Config.wallet2PublicKey, new BigDecimal("0.01"), total1, "cs");
	    			  }
	    			  else { 
	    				  total2.subtract(new BigDecimal("0.01"));
	    				  total1.add(new BigDecimal("0.01"));
	    				  transaction = Utils.createTransaction(Config.wallet2PublicKey, privateKey2, Config.wallet1PublicKey, new BigDecimal("0.01"), total2, "cs");
	    			  }
	    			  
	    			  byte[] transactionData = Utils.getTransactionPacket(transaction);
	    			  cAPI.send(socketChannel, transactionData);
    			  }
    		  }
    		  Thread.sleep(10);
    	  }
    	  
      } 
      catch (Exception e) {
    	 e.printStackTrace();
         System.out.println(String.format("Thread interrupted %s", this.name));
      }
      System.out.println(String.format("Thread exiting %s", this.name));
   }
   
   public void start () {
      System.out.println(String.format("Starting %s", this.name));
      if (thread == null) {
    	  thread = new Thread(this, this.name);
    	  thread.start();
      }
   }
}
