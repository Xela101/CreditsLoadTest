package com.credits.test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

/*
 * RawTransactionClient is used to send Thrift transaction packets as fast as possible.
 */
public class RawConnectionClient {
    private Selector selector;
    
    private static final int BUFFER_SIZE = 5120;
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    
    //Not going to bother with synchronization around this.
    public ArrayList<SocketChannel> Sockets = new ArrayList<SocketChannel>();
	
	RawConnectionClient(String apiServerHost, int apiServerPort) {
		try {
			selector = Selector.open();

			for(int i=0;i<Config.maxConnectionsPerThread;i++) {
				InetSocketAddress nodeAddress = new InetSocketAddress(apiServerHost, apiServerPort);
				SocketChannel clientSocket = SocketChannel.open();
				//Socket socket = clientSocket.socket();
				//socket.setSendBufferSize(socket.getSendBufferSize()*10);
				//socket.setSendBufferSize(socket.getReceiveBufferSize()*10);
				clientSocket.configureBlocking(false);
				clientSocket.connect(nodeAddress);
				
				Sockets.add(clientSocket);
	
				clientSocket.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ, null);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error connecting to socket!");
		}
	}
	
	private void connect(SelectionKey key) throws IOException {
		SocketChannel client = (SocketChannel)key.channel();
		while (client.isConnectionPending())
			client.finishConnect();
		System.out.println("Connected!");
	}
	
	private void recv(SelectionKey key) throws IOException {
		SocketChannel client = (SocketChannel)key.channel();
		readBuffer.clear();
		
		int bytesRead;
		try {
			bytesRead = client.read(readBuffer);
		} 
		catch (IOException e) {
	        key.cancel();
	        client.close();
	        Sockets.remove(client);

	        System.out.println("Node shutdown connection.");
	        return;
	    }
		
        if(bytesRead==-1) {
        	System.out.println("Socket disconnected.");
            key.channel().close();
            key.cancel();
            Sockets.remove(client);

            return;
        }

        client.register(selector, SelectionKey.OP_READ);
	}
	
	public void send(SocketChannel client, byte[] data) throws IOException {
		  ByteBuffer buffer = ByteBuffer.wrap(data);
		  int status = client.write(buffer);
		  if(status==0)
			  System.out.println(String.format("Write buffer full"));
		  else
			  System.out.println(String.format("Sent packets"));
	}
	
	public void start() {
		try {
			new Thread(() ->{
				for (;;) {
					try {
			              selector.select();
			              
			              Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
			              while (selectedKeys.hasNext()) {
							  SelectionKey key = (SelectionKey)selectedKeys.next();
							  selectedKeys.remove();
							  
							  if (!key.isValid()) {
				                  continue;
				              }
							  
							  if(key.isConnectable()) {
								  connect(key);
							  }
							  if (key.isReadable()) {
								  recv(key);
			                  }
			              }
					}
					catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					}
				} 
			}) {{start();}};
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
