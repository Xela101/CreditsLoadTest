import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/*
 * RawTransactionClient is used to send Thrift transaction packets as fast as possible.
 */
public class RawConnectionClient {
    private Selector selector;
    
    private static final int BUFFER_SIZE = 5120;
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);

	IData sendData;
	public void setSendData(IData data) {
		sendData = data;
	}
	
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
	
				clientSocket.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE | SelectionKey.OP_READ, null);
			}
			
			System.out.println(String.format("Connected %s:%d", apiServerHost, apiServerPort));
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

	        System.out.println("Node shutdown connection.");
	        return;
	    }
		
        if(bytesRead==-1) {
        	System.out.println("Socket disconnected.");
            key.channel().close();
            key.cancel();

            return;
        }

        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
	}
	
	private void send(SelectionKey key) throws IOException {
		SocketChannel client = (SocketChannel)key.channel();
		ByteBuffer data = ByteBuffer.wrap(sendData.getData());
		  int status = client.write(data);
		  if(status==0) {
			  System.out.println(String.format("Write buffer full"));
			  client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
		  }
		  else {
			  System.out.println(String.format("Sent packets"));
			  client.register(selector, SelectionKey.OP_READ);
		  }
	}
	
	public void start() {
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
					  if (key.isWritable()) {
						  send(key);
	                  }
	              }
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		} 
	}
}
