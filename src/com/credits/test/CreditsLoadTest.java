package com.credits.test;
/*
 * Title: Credits TPS Speed test.
 * Author: Alex White.
 * Twitter: @AlexTech101
 * Telegram: @Xela101
 * Email: whitexela101@gmail.com
 * Version: 1.3.0.1
 * Description: Attempts a network packet flood on a credit node to test TPS and Block size.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
import java.math.RoundingMode;
import java.security.KeyPair;
import java.util.concurrent.TimeUnit;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.ApiClient;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.util.TransactionTypeEnum;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.ApiUtils;

public class CreditsLoadTest {
	//Main entry point.
	
	
	public static void main(String[] args) throws IOException, CreditsException, InterruptedException {
		if(!Config.fromFile("config.properties")) {
			System.out.println("Error loading config file.");
			return;
		}
		
		AppState.apiClient = ApiClient.getInstance(Config.ip, Config.port);
		
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.print("This generator is made by a community member. Feel free to use the generator in any way you can.\n");
		System.out.print("Make sure you have your node setup correctly. If not, this generator will NOT work.\n");
		System.out.print("Details entered in this generator will only be saved locally. To stay safe, please use the details of your test wallet.\n");
		System.out.print("\n");
		
		File f = new File("txkeyspub");
		File f2 = new File("txkeyspri");
		
		if(f.length() == 0 || f2.length() == 0)
		{
			System.out.println("Empty keyfile detected, deleting keyfile. \n");
			f.delete();
			f2.delete();
		}
		
		if(!f.exists()){
		  System.out.print("To use the generator we need a wallet with some balance.\n");
		  System.out.print("Please enter your PUBLIC key: ");
		  String pub1 = br.readLine();
		  System.out.print("Please enter your PRIVATE key: ");
		  String pri1 = br.readLine();
		  Utils.open(pub1, pri1);
		  while(!Utils.validateKeys(pub1, pri1))
		  {
			  try {
					System.out.print("Invalid keys. Please enter valid keys.\n");
					System.out.print("Please enter your PUBLIC key: ");
					  pub1 = br.readLine();
					  System.out.print("Please enter your PRIVATE key: ");
					  pri1 = br.readLine();
					  Utils.open(pub1, pri1);
				}
				catch (Exception exception) {
					exception.printStackTrace();
				}
		  }
		  
		  byte[] acc = Converter.decodeFromBASE58(AppState.account);
		  byte currency = (byte) Config.cur;
			BigDecimal balancef = AppState.apiClient.getBalance(acc, currency);
			while(balancef.compareTo(new BigDecimal(0))<=0) {
				try {
					System.out.print("No balance found. Please enter the details of a wallet with balance.\n");
					System.out.print("Please enter your PUBLIC key: ");
					  pub1 = br.readLine();
					  System.out.print("Please enter your PRIVATE key: ");
					  pri1 = br.readLine();
					  Utils.open(pub1, pri1);
					  byte[] acc1 = Converter.decodeFromBASE58(AppState.account);
					  byte currency1 = (byte) Config.cur;
					  balancef = AppState.apiClient.getBalance(acc1, currency1);
				}
				catch (CreditsException creditsException) {
					creditsException.printStackTrace();
				}
				catch (Exception exception) {
					exception.printStackTrace();
				}
			}
			System.out.print("Balance found: " + balancef + " CS\n");
		 System.out.print("Amount of wallets to be created?: ");
		 int wallets = Integer.parseInt(br.readLine());
		 System.out.print("Creating wallets, this can take some time. \n");
		 BigDecimal walletsb = new BigDecimal(wallets+wallets);
		 BigDecimal senda1 = balancef.divide(walletsb, 2, RoundingMode.HALF_UP);
		 BigDecimal senda = senda1.setScale(2, 0);
		 f.createNewFile();
		  FileWriter fkey1 = new FileWriter(f);
		  BufferedWriter fkey = new BufferedWriter(fkey1);
		  FileWriter fkey2 = new FileWriter(f2);
		  BufferedWriter fpri = new BufferedWriter(fkey2);
		  	fkey.write(pub1);
			fkey.write(System.getProperty("line.separator"));
			fpri.write(pri1);
			fpri.write(System.getProperty("line.separator"));
			
		 for (int w = 0; w < wallets; w++) {
			 String[] pair = new String[2];
				
				Boolean validatedKeys = false;
				while(!validatedKeys) {
					KeyPair keyPair = Ed25519.generateKeyPair();
					pair[0] = Converter.encodeToBASE58(Ed25519.publicKeyToBytes(keyPair.getPublic()));
					pair[1] = Converter.encodeToBASE58(Ed25519.privateKeyToBytes(keyPair.getPrivate()));
					validatedKeys = Utils.validateKeys(pair[0], pair[1]);
				}
				fkey.write(pair[0]);
				fkey.write(System.getProperty("line.separator"));
				fpri.write(pair[1]);
				fpri.write(System.getProperty("line.separator"));
				TimeUnit.SECONDS.sleep(1);
				byte[] acc2 = Converter.decodeFromBASE58(pair[0]);
				byte currency2 = (byte) Config.cur;
				BigDecimal balancen = AppState.apiClient.getBalance(acc2, currency2);
				while(balancen.compareTo(new BigDecimal(0))<=0) {
					try {
						BigDecimal fee1 = BigDecimal.ZERO;
						TransactionTypeEnum EXECUTE_TRANSACTION = null;
						ApiUtils.callTransactionFlow(ApiUtils.generateTransactionInnerId(), pub1, pair[0], senda, balancef, (byte)1, fee1, (TransactionTypeEnum) EXECUTE_TRANSACTION);
						TimeUnit.SECONDS.sleep(4);
					}
					catch (LevelDbClientException leveldbclientException) {
						leveldbclientException.printStackTrace();
					}
					catch (CreditsException creditsException) {
						creditsException.printStackTrace();
					}
					catch (Exception exception) {
						exception.printStackTrace();
					}
					
					balancen = AppState.apiClient.getBalance(acc2, currency2);
					System.out.println("Balance: " + balancen);
					if(balancen.compareTo(new BigDecimal(0))<=0) {
						System.out.println("Error funding account, trying again.");
						TimeUnit.SECONDS.sleep(2);
					}
				}
		 }
		 System.out.print(wallets + " wallets created.\n");
		 System.out.print("Public keys stored in file txkeyspub.\n");
		 System.out.print("Private keys stored in file txkeyspri.\n");
		 fkey.close();
		 fpri.close();
		}else{
		  System.out.println("Keyfile found.");
		}

		System.out.println("Starting generator.");
		TimeUnit.SECONDS.sleep(3);
		
		
		
		TPSSpammer spammer = new TPSSpammer(); 
		spammer.spam();
	}
}
