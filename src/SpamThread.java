import java.security.PrivateKey;
import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
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
    	  
    	  byte[] privateKeyByteArr = Converter.decodeFromBASE58(Config.wallet1PrivateKey);
    	  PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
    	  
    	  if(Config.sendReplayAttackSample) {
    		  cAPI.setSendData(() -> { 
        		  return Config.sample;
        	  });
    	  }
    	  else {
    		  cAPI.setSendData(() -> { 
    			  Transaction transaction = Utils.createTransaction(Config.wallet1PublicKey, privateKey, Config.wallet2PublicKey, 1d, "cs");
				  byte[] transactionData = Utils.getTransactionPacket(transaction);
				  return transactionData;
        	  });
    	  }
    	  
    	  cAPI.start();
      } 
      catch (Exception e) {
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
