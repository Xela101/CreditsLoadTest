package com.credits.test;
/*
 * Title: Credits TPS Speed test.
 * Author: Alex White.
 * Twitter: @AlexTech101
 * Version: 1.3.0.1
 * Description: Attempts a network packet flood on a credit node to test TPS and Block size.
 */

/*
 * This is a quick and dirty build for testing purposes (I may clean it up later).
 * 
 * Notes: 
 * Change the "config.properties" file
 * I don't correctly set the Balance correctly in the transaction as I believe its only used for debugging purposes.
 * The spammer builds custom constructed thrift packets and then sends them using selectors to asynchronously send 
 * 		packets using multiple connections on singular threads for quickest throughput. However if the tcp buffers 
 * 		full up I don't send the remaining parts of the packets, I just send another full transaction. In a future 
 * 		build I may complete failed transactions due to tcp buffer's full.
 */


import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.UUID;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.common.utils.TcpClient;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.data.TransactionFlowData;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.ApiUtils;
import com.credits.wallet.desktop.utils.struct.TransactionStruct;

public class CreditsLoadTest {
	//Main entry point.
	public static void main(String[] args) {
		if(!Config.fromFile("config.properties")) {
			System.out.println("Error loading config file.");
			return;
		}
		
		
		//Create and fund two separate wallets, this will send funds between them both. 
		Config.wallet1PublicKey = "";
		Config.wallet1PrivateKey = "";
		
		Config.wallet2PublicKey = "";
		Config.wallet2PrivateKey = "";
		
		/*AppState.apiClient = ApiClient.getInstance(Config.ip, Config.port);
		 while(true) {
			try {	
				byte[] privateKeyByteArr = Converter.decodeFromBASE58(Config.wallet1PrivateKey);
		    	PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
		    	
		    	String hash = Utils.generateTransactionHash();
		    	long innerId = Utils.generateTransactionInnerId();
				//String innerId = UUID.randomUUID().toString();
				//String innerId = "";
				String source = Config.wallet1PublicKey;
				String target = Config.wallet2PublicKey;
				//BigDecimal amount = balance.divide(new BigDecimal(2.0));
				
				//BigDecimal balance = AppState.apiClient.getBalance(Converter.decodeFromBASE58(Config.wallet1PublicKey), (byte)1);
				//System.out.println(balance);
				//BigDecimal amount = new BigDecimal(1.0);
				BigDecimal balance = new BigDecimal(1);
				BigDecimal amount = new BigDecimal("0.1");
				BigDecimal fee = BigDecimal.ZERO;
				
				TransactionStruct tStruct = new TransactionStruct(innerId, source, target, amount, fee, (byte)1, (byte[])null);
		        ByteBuffer signature = Utils.signTransactionStruct(tStruct, privateKey);
		        TransactionFlowData transactionFlowData = new TransactionFlowData(innerId, Converter.decodeFromBASE58(source), Converter.decodeFromBASE58(target), amount, balance, (byte)1, signature.array(), fee);
		        AppState.apiClient.transactionFlow(transactionFlowData, false);

				System.out.println("Sent");
				Thread.sleep(1);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}*/
		
		TPSSpammer spammer = new TPSSpammer(); 
		spammer.spam();
	}
}
