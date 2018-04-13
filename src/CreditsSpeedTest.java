/*
 * Title: Credits TPS Speed test.
 * Author: Alex White.
 * Version: 1.2.0.1
 * Description: Attempts a network packet flood on a credit node to test TPS and Block size.
 */

/*
 * This is a quick and dirty build for testing purposes don't expect perfect error handling and inheritance. (I may clean it up later)
 */

import com.credits.common.utils.TcpClient;
import com.credits.leveldb.client.ApiClient;
import com.credits.wallet.desktop.AppState;

public class CreditsSpeedTest {
	//Main entry point.
	public static void main(String[] args) {
		if(!Config.fromFile("config.properties")) {
			System.out.println("Error loading config file.");
			return;
		}
		
		Config.autoGenerateSameKeyPair();
		
		AppState.apiClient = ApiClient.getInstance(Config.ip, Config.port);
		
		try {
			Double balance = 0d;
			//Keep funding random accounts until we have a working account.
			while(balance<=0) {
				balance = AppState.apiClient.getBalance(Config.wallet1PublicKey, "cs");
				TcpClient.sendRequest(Config.fundIp, Config.fundPort, Config.wallet1PublicKey);
				balance = AppState.apiClient.getBalance(Config.wallet1PublicKey, "cs");
				System.out.println(balance);
				
				//If funding does not work(Bug on credits side) on this key pair generate new pair.
				if(balance<=0) {
					System.out.println("Error funding account, generating new pair.");
					Config.autoGenerateSameKeyPair();
				}
			}
			
			//Sample uses replay attack for faster packet generation, works in current build will need to be changed when fixed.
			Config.sample = Utils.generateSample();
			
			TPSSpammer spammer = new TPSSpammer(); 
			spammer.spam();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
