package com.credits.test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.SocketChannel;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Random;

import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.data.TransactionFlowData;


public class SpamThread implements Runnable{
   private Thread thread;
   
   private String name;
   
   public SpamThread(String name) {
	   this.name = name;
   }
   
	public static int random(int min, int max) {
		return min + nextInt(max - min + 1);
	}
	
	private static final Random RANDOM = new Random();
	public static int nextInt(int n) {
		return RANDOM.nextInt(n);
	}
   
   public void run() {
      System.out.println(String.format("Running %s", this.name));
      try {
    	  
    	  String fileNamepub = "txkeyspub";
      	String linepub;
      	ArrayList<String> pubkeys = new ArrayList<String>();

      	try {
      	    BufferedReader input = new BufferedReader(new FileReader(fileNamepub));
      	    if (!input.ready()) {
      	    	input.close();
      	        throw new IOException();
      	    }
      	    while ((linepub = input.readLine()) != null) {
      	        pubkeys.add(linepub);
      	    }
      	    input.close();

      	                } catch (IOException e) {
      	    System.out.println(e);
      	        }
      	
      	String fileNamepri = "txkeyspri";
      	String linepri;
      	ArrayList<String> prikeys = new ArrayList<String>();

      	try {
      	    BufferedReader input = new BufferedReader(new FileReader(fileNamepri));
      	    if (!input.ready()) {
      	    	input.close();
      	        throw new IOException();
      	    }
      	    while ((linepri = input.readLine()) != null) {
      	        prikeys.add(linepri);
      	    }
      	    input.close();

      	                } catch (IOException e) {
      	    System.out.println(e);
      	        }
    	  
      	//  AppState.apiClient = ApiClient.getInstance(Config.ip, Config.port);
    	  RawConnectionClient cAPI = new RawConnectionClient(Config.ip, Config.port);
    	  
    	  
    	  cAPI.start();
    	  
    	  BigDecimal total1 = new BigDecimal(1);
    	  
    	  while(true) {
    		  for(int i=0;i<cAPI.Sockets.size();i++) {
    			  SocketChannel socketChannel = cAPI.Sockets.get(i);
    			  if(socketChannel.isConnected()) {
	    			  TransactionFlowData transaction;
	    			  
	    			  total1.add(new BigDecimal("0.01"));
	    			  
	    			  int random = random(0, pubkeys.size() - 1);
	    				String PUBLIC_KEY = pubkeys.get(random);
	    				String PRIVATE_KEY = prikeys.get(random);
	    				int randomto = random(0, pubkeys.size() - 1);
	    				String PUBLIC_KEY2 = pubkeys.get(randomto);
	    				
	    			//	byte[] pubk = Converter.decodeFromBASE58(PUBLIC_KEY);
	    				
	    			//	final BigDecimal balance = AppState.apiClient.getBalance(pubk, (byte) 1);
	    				
	    				  byte[] privateKeyByteArr1 = Converter.decodeFromBASE58(PRIVATE_KEY);
	    		    	  PrivateKey privateKey1 = Ed25519.bytesToPrivateKey(privateKeyByteArr1);
	    				  
	    			  
	    				  transaction = Utils.createTransaction(PUBLIC_KEY, privateKey1, PUBLIC_KEY2, new BigDecimal("0.01"), total1, "cs");

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
