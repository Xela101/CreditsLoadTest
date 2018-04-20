import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.credits.common.exception.CreditsException;
import com.credits.common.utils.Converter;
import com.credits.crypto.Blake2S;
import com.credits.crypto.Ed25519;
import com.credits.leveldb.client.thrift.Amount;
import com.credits.leveldb.client.thrift.Transaction;

public class Utils {
	//Generate transaction hash.
	public static String[] generateKeyPair() {
		String[] pair = new String[2];
		KeyPair keyPair = Ed25519.generateKeyPair();
		pair[0] = Converter.encodeToBASE58(Ed25519.publicKeyToBytes(keyPair.getPublic()));
		pair[1] = Converter.encodeToBASE58(Ed25519.privateKeyToBytes(keyPair.getPrivate()));
		System.out.println(String.format("Public: %s", pair[0]));
		System.out.println(String.format("Private: %s", pair[1]));
		return pair;
	}
	
	//Generate transaction hash.
	public static String generateTransactionHash() throws CreditsException {
		byte[] hashBytes = Blake2S.generateHash(4);
		return com.credits.leveldb.client.util.Converter.bytesToHex(hashBytes);
	}
	
	//Generate signature of transaction.
	public static String generateSignOfTransaction(String hash, String innerId, String source, String target, Double amount, String currency, PrivateKey privateKey) throws Exception
	{
	    Amount amountValue = com.credits.leveldb.client.util.Converter.doubleToAmount(amount);
	    
	    Integer amountIntegral = Integer.valueOf(amountValue.getIntegral());
	    Long amountFraction = Long.valueOf(amountValue.getFraction());
	    
	    String transaction = String.format("%s|%s|%s|%s|%s:%s|%s", new Object[] { hash, innerId, source, target,  com.credits.common.utils.Converter.toString(amountIntegral), com.credits.common.utils.Converter.toString(amountFraction), currency });

	    byte[] signature = Ed25519.sign(transaction.getBytes(StandardCharsets.US_ASCII), privateKey);
	    
	    return com.credits.common.utils.Converter.encodeToBASE58(signature);
	}
	
	//Create transaction.
	public static Transaction createTransaction(String sourcePublicKey, PrivateKey sourcePrivateKey, String targetPublicKey, Double amount, String currency) {
		try {
			String hash = Utils.generateTransactionHash();
			String innerId = UUID.randomUUID().toString();
			
			String signatureBASE58 = Utils.generateSignOfTransaction(hash, innerId, sourcePublicKey, targetPublicKey, amount, currency, sourcePrivateKey);
			
			Amount serverAmount = com.credits.leveldb.client.util.Converter.doubleToAmount(amount);
			
			currency = String.format("%s|%s", new Object[] { currency, signatureBASE58 });
	
			return new Transaction(hash, innerId, sourcePublicKey, targetPublicKey, serverAmount, currency);
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
	
	//Creates a sample by using a replay attack, once fixed this method will be updated.
	public static byte[] generateSample() {
		byte[] privateKeyByteArr;
		try {
			privateKeyByteArr = Converter.decodeFromBASE58(Config.wallet1PrivateKey);
		
	    	PrivateKey privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
			
	    	List<byte[]> packetArray = new ArrayList<byte[]>();
	    	for(int i=0;i<Config.maxSampleSize;i++) {
	    		packetArray.add(Utils.getTransactionPacket(Utils.createTransaction(Config.wallet1PublicKey, privateKey, Config.wallet2PublicKey, 0.1d, "cs")));
	    	}
    	
    		return Utils.getGroupedTransactionPacket(packetArray);
    	} 
		catch (CreditsException creditsException) {
			creditsException.printStackTrace();
		}
		return null;
	}
	
	//Construct a raw thrift transaction packet. Main raw payload for fast transactions ;)
	public static byte[] getTransactionPacket(Transaction transaction) {
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(byteStream);
			
			int version = 0x80010000|(byte)1;
			String call = "TransactionFlow";
			
			dataOutputStream.writeInt(version);
			dataOutputStream.writeInt(call.length());
			dataOutputStream.write(call.getBytes());
			dataOutputStream.writeInt(2); //0
			
			dataOutputStream.writeByte(12);
			dataOutputStream.writeShort(1);
			
			dataOutputStream.writeByte(11);
			dataOutputStream.writeShort(1);
			dataOutputStream.writeInt(transaction.hash.length());
			dataOutputStream.write(transaction.hash.getBytes());
			
			dataOutputStream.writeByte(11);
			dataOutputStream.writeShort(2);
			dataOutputStream.writeInt(transaction.innerId.length());
			dataOutputStream.write(transaction.innerId.getBytes());
			
			dataOutputStream.writeByte(11);
			dataOutputStream.writeShort(3);
			dataOutputStream.writeInt(transaction.source.length());
			dataOutputStream.write(transaction.source.getBytes());
			
			dataOutputStream.writeByte(11);
			dataOutputStream.writeShort(4);
			dataOutputStream.writeInt(transaction.target.length());
			dataOutputStream.write(transaction.target.getBytes());
			
			dataOutputStream.writeByte(12);
			dataOutputStream.writeShort(5);
			
			dataOutputStream.writeByte(8);
			dataOutputStream.writeShort(1);
			dataOutputStream.writeInt(transaction.amount.integral);
			
			dataOutputStream.writeByte(10);
			dataOutputStream.writeShort(2);
			dataOutputStream.writeLong(transaction.amount.fraction);
			dataOutputStream.writeByte(0);
			
			dataOutputStream.writeByte(11);
			dataOutputStream.writeShort(6);
			dataOutputStream.writeInt(transaction.currency.length());
			dataOutputStream.write(transaction.currency.getBytes());
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
