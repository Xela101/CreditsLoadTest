package com.credits.test;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//Configuration class
public class Config {
	public static String ip;
    public static int port;
    public static String fundIp;
    public static int fundPort;
    public static int maxThreads;
    public static int maxConnectionsPerThread;
	
    public static String wallet1PublicKey;
    public static String wallet1PrivateKey;
    public static String wallet2PublicKey;
    public static String wallet2PrivateKey;
    public static String currency = "cs";
    
    //Load configuration file and handle exceptions the old school way.
    public static void autoGenerateSameKeyPair() {
		String[] pair = Utils.generateKeyPair();
		wallet1PublicKey = pair[0];
		wallet1PrivateKey = pair[1];
		wallet2PublicKey = pair[0];
		wallet2PrivateKey = pair[1];
    }
    
    public static void autoGenerateKeyPair() {
		String[] pair1 = Utils.generateKeyPair();
		String[] pair2 = Utils.generateKeyPair();
		wallet1PublicKey = pair1[0];
		wallet1PrivateKey = pair1[1];
		wallet2PublicKey = pair2[0];
		wallet2PrivateKey = pair2[1];
    }
    
    //Load configuration file and handle exceptions the old school way.
    public static Boolean fromFile(String file) {
    	try {
    		fromFileInternal(file);
    		return true;
		}
    	catch (IOException ioException) {
			ioException.printStackTrace();
		} 
    	catch (IllegalArgumentException iaException) {
    		iaException.printStackTrace();
 		}
    	return false;
    }
    
    //Loads in the configuration from file.
    public static void fromFileInternal(String file) throws IOException, IllegalArgumentException {
    	InputStream inputStream = new FileInputStream("config.properties");
    	try {
			Properties prop = new Properties();
			prop.load(inputStream);
			
			ip = prop.getProperty("ip");
			port = Integer.parseInt(prop.getProperty("port"));
			fundIp = prop.getProperty("fundIp");
			fundPort = Integer.parseInt(prop.getProperty("fundPort"));
			maxThreads = Integer.parseInt(prop.getProperty("maxThreads"));
			maxConnectionsPerThread = Integer.parseInt(prop.getProperty("maxConnectionsPerThread"));
		} 
    	catch (IOException ioException) {
			throw ioException;
		} 
    	catch (IllegalArgumentException iaException) {
    		 throw iaException;
 		}
    	finally {
			inputStream.close();
		}
    }
}
