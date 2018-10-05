package com.credits.test;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Date;
import java.util.List;

import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.data.TransactionFlowData;
import com.credits.leveldb.client.thrift.Amount;
import com.credits.leveldb.client.util.LevelDbClientConverter;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.utils.struct.TransactionStruct;

public class Utils {
	public static boolean validateKeys(String publicKey, String privateKey) {
		try {
			byte[] publicKeyByteArr = Converter.decodeFromBASE58(publicKey);
			byte[] privateKeyByteArr = Converter.decodeFromBASE58(privateKey);
			  
			if (privateKeyByteArr.length <= 32) {
				return false;
			}
			for (int i = 0; (i < publicKeyByteArr.length) && (i < privateKeyByteArr.length - 32); i++) {
				if (publicKeyByteArr[i] != privateKeyByteArr[(i + 32)]) {
					return false;
				}
			}
			return true;
		} 
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return false;
	 }
	
	public static void open(String pubKey, String privKey) {
		AppState.account = pubKey;
		if (AppState.newAccount) {
			try {

			} catch (Exception e) {
				System.err.println(e.toString());
			}
		} else {
			try {
				byte[] publicKeyByteArr = Converter.decodeFromBASE58(pubKey);
				byte[] privateKeyByteArr = Converter.decodeFromBASE58(privKey);
				AppState.publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
				AppState.privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
			} catch (Exception e) {
				if (e.getMessage() != null) {
					System.err.println(e.toString());
				}
			}
		}
		if (validateKeys(pubKey, privKey)) {
			System.out.println("Valid key. Login successful");

		} else {

		}
	}
	
	//Generate transaction hash.
	public static String[] generateKeyPair() {
		String[] pair = new String[2];
		
		Boolean validatedKeys = false;
		while(!validatedKeys) {
			KeyPair keyPair = Ed25519.generateKeyPair();
			pair[0] = Converter.encodeToBASE58(Ed25519.publicKeyToBytes(keyPair.getPublic()));
			pair[1] = Converter.encodeToBASE58(Ed25519.privateKeyToBytes(keyPair.getPrivate()));
			validatedKeys = validateKeys(pair[0], pair[1]);
		}
		
		System.out.println(String.format("Public: %s", pair[0]));
		System.out.println(String.format("Private: %s", pair[1]));
		return pair;
	}
	
	public static long generateTransactionInnerId() {
        return new Date().getTime();
    }
	
	//Create transaction.
	public static TransactionFlowData createTransaction(String sourcePublicKey, PrivateKey sourcePrivateKey, String targetPublicKey, BigDecimal amount, BigDecimal balance, String currency) {
		try {
			long innerId = Utils.generateTransactionInnerId();
			BigDecimal fee = BigDecimal.ZERO;

			TransactionStruct tStruct = new TransactionStruct(innerId, sourcePublicKey, targetPublicKey, amount, fee, (byte)1, (byte[])null);
			ByteBuffer signature = Utils.signTransactionStruct(tStruct, sourcePrivateKey);
			
			TransactionFlowData transaction = new TransactionFlowData(innerId, Converter.decodeFromBASE58(sourcePublicKey), Converter.decodeFromBASE58(targetPublicKey), amount, balance, (byte)1, signature.array(), fee);			
			return transaction;
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//Generate a grouped transaction packet to make use of TCP congestion alg.
	public static byte[] getGroupedTransactionPacket(List<byte[]> packets) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteStream);
		
		try {
			for(int i=0;i<packets.size();i++) {
				dataOutputStream.write(packets.get(i));
			}
		
			return byteStream.toByteArray();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ByteBuffer signTransactionStruct(TransactionStruct tStruct, PrivateKey privateKey) {
	      ByteBuffer signature;
	      try {
	         byte[] tArr = tStruct.getBytes();
	         String arrStr = "";

	         for(int i = 0; i < tArr.length; ++i) {
	            arrStr = arrStr + (i == 0 ? "" : ", ") + tArr[i];
	         }

	         byte[] signatureArr = Ed25519.sign(tArr, privateKey);
	         arrStr = "";

	         for(int i = 0; i < signatureArr.length; ++i) {
	            arrStr = arrStr + (i == 0 ? "" : ", ") + signatureArr[i];
	         }

	         signature = ByteBuffer.wrap(signatureArr);
	      } catch (Exception var6) {
	         signature = ByteBuffer.wrap(new byte[0]);
	      }

	      return signature;
	   }
	
	//Construct a raw thrift transaction packet. Main raw payload for fast transactions ;)
	public static byte[] getTransactionPacket(TransactionFlowData transaction) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(byteStream);
			
			int version = 0x80010000|(byte)1;
			String call = "TransactionFlow";
			
			dataOutputStream.writeInt(version);
			dataOutputStream.writeInt(call.length());
			dataOutputStream.write(call.getBytes());
			dataOutputStream.writeInt(0);
			
			dataOutputStream.writeByte(12);
			dataOutputStream.writeShort(1);
			
			dataOutputStream.writeByte(10);
			dataOutputStream.writeShort(1);
			dataOutputStream.writeLong(transaction.getInnerId());
			
			dataOutputStream.writeByte(11);
			dataOutputStream.writeShort(2);
			dataOutputStream.writeInt(transaction.getSource().length);
			dataOutputStream.write(transaction.getSource());
			
			dataOutputStream.writeByte(11);
			dataOutputStream.writeShort(3);
			dataOutputStream.writeInt(transaction.getTarget().length);
			dataOutputStream.write(transaction.getTarget());
			
			dataOutputStream.writeByte(12);
			dataOutputStream.writeShort(4);
			
			Amount amountValue = LevelDbClientConverter.bigDecimalToAmount(transaction.getAmount());
			dataOutputStream.writeByte(8);
			dataOutputStream.writeShort(1);
			dataOutputStream.writeInt(amountValue.integral);
			
			dataOutputStream.writeByte(10);
			dataOutputStream.writeShort(2);
			dataOutputStream.writeLong(amountValue.fraction);
			dataOutputStream.writeByte(0);
			
			dataOutputStream.writeByte(12);
			dataOutputStream.writeShort(5);
			
			Amount balanceValue = LevelDbClientConverter.bigDecimalToAmount(transaction.getBalance());
			dataOutputStream.writeByte(8);
			dataOutputStream.writeShort(1);
			dataOutputStream.writeInt(balanceValue.integral);
			
			dataOutputStream.writeByte(10);
			dataOutputStream.writeShort(2);
			dataOutputStream.writeLong(balanceValue.fraction);
			dataOutputStream.writeByte(0);
			
			dataOutputStream.writeByte(3);
			dataOutputStream.writeShort(6);
			dataOutputStream.write(transaction.getCurrency());
			
			dataOutputStream.writeByte(11);
			dataOutputStream.writeShort(7);
			dataOutputStream.writeInt(transaction.getSignature().length);
			dataOutputStream.write(transaction.getSignature());
			
			dataOutputStream.writeByte(12);
			dataOutputStream.writeShort(9);
			
			Amount feeValue = LevelDbClientConverter.bigDecimalToAmount(transaction.getFee());
			dataOutputStream.writeByte(8);
			dataOutputStream.writeShort(1);
			dataOutputStream.writeInt(feeValue.integral);
			
			dataOutputStream.writeByte(10);
			dataOutputStream.writeShort(2);
			dataOutputStream.writeLong(feeValue.fraction);
			dataOutputStream.writeByte(0);
			dataOutputStream.writeByte(0);
			
			dataOutputStream.writeByte(0);
			
			return byteStream.toByteArray();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
