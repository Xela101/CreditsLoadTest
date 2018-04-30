package com.credits.test;
/*
 * Title: Credits TPS Speed test.
 * Author: Alex White.
 * Version: 1.2.0.1
 * Description: Attempts a network packet flood on a credit node to test TPS and Block size.
 */

/*
 * This is a quick and dirty build for testing purposes don't expect perfect error handling and inheritance. (I may clean it up later)
 */


import java.math.BigDecimal;
import java.security.PrivateKey;
import java.util.UUID;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.common.utils.TcpClient;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.ApiClient;
import com.credits.wallet.desktop.AppState;

public class CreditsLoadTest {
	//Main entry point.
	public static void main(String[] args) {
		if(!Config.fromFile("config.properties")) {
			System.out.println("Error loading config file.");
			return;
		}
		
		Config.autoGenerateKeyPair();
		
		AppState.apiClient = ApiClient.getInstance(Config.ip, Config.port);

		BigDecimal balance = new BigDecimal(0);
		//Keep funding random accounts until we have a working account.
		while(balance.compareTo(new BigDecimal(0))<=0) {
			try {
				balance = AppState.apiClient.getBalance(Config.wallet1PublicKey, "cs");
				TcpClient.sendRequest(Config.fundIp, Config.fundPort, Config.wallet1PublicKey);
				balance = AppState.apiClient.getBalance(Config.wallet1PublicKey, "cs");
				System.out.println(balance);
			}
			catch (CreditsException creditsException) {
				creditsException.printStackTrace();
			}
			catch (Exception exception) {
				exception.printStackTrace();
			}
			//If funding does not work(Bug on credits side) on this key pair generate new pair.
			if(balance.compareTo(new BigDecimal(0))<=0) {
				System.out.println("Error funding account, trying again.");
			}
		}
		
		//Share half the funds between first key pair and second key pair, also for easy template for API testing ;)
		try {	
			byte[] privateKeyByteArr = Converter.decodeFromBASE58(Config.wallet1PrivateKey);
	    	PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
	    	
	    	String hash = Utils.generateTransactionHash();
			String innerId = UUID.randomUUID().toString();
			String source = Config.wallet1PublicKey;
			String target = Config.wallet2PublicKey;
			BigDecimal amount = balance.divide(new BigDecimal(2.0));
			String currency = "cs";
			String signature = Utils.generateSignOfTransaction(hash, innerId, source, target, amount, currency, privateKey);
			AppState.apiClient.transactionFlow(hash, innerId, source, target, amount, currency, signature);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		TPSSpammer spammer = new TPSSpammer(); 
		spammer.spam();
	}
}
