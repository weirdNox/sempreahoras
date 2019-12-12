import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class Sempreahoras {
	
	static final int PORT = 1978;

	public static void main(String[] args) {
		
		ServSQL sql = new ServSQL();
		AtomicInteger event_id = new AtomicInteger(sql.getEvent_id());
		sql.close();
        ServerSocket serverSocket = null;
        Socket socket = null;
        boolean doRun = true;
        try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
        while (doRun) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            new ConnThread(socket, event_id).start();
        }
        try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}