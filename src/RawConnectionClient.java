import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/*
 * RawTransactionClient is used to send Thrift transaction packets as fast as possible.
 */
public class RawConnectionClient {
	private Socket clientSocket;
	private DataOutputStream serverOutputStream;
	private BufferedReader serverInputStream;

	RawConnectionClient(String apiServerHost, int apiServerPort) {
		try {
			clientSocket = new Socket(apiServerHost, apiServerPort);
			serverOutputStream = new DataOutputStream(clientSocket.getOutputStream());
			serverInputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			Thread thread = new Thread() {
				public void run() {
					char[] buffer = new char[1500];
					while(true) {
						try {
							serverInputStream.read(buffer, 0, buffer.length);
							Thread.sleep(10);
						} 
						catch (IOException ioException) {
							ioException.printStackTrace();
						}
						catch (InterruptedException interruptedException) {
							interruptedException.printStackTrace();
						}
					}
				}
			};
			thread.start();
			
			System.out.println(String.format("Connected %s:%d", apiServerHost, apiServerPort));
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Send data
	public void send(byte[] packet) throws Exception {
		serverOutputStream.write(packet);
        System.out.println(String.format("Sent packets"));
	}
}
