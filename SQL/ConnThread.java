import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;


public class ConnThread extends Thread {
	protected Socket socket;
	AtomicInteger event_id;

    public ConnThread(Socket clientSocket, AtomicInteger event_id) {
        this.socket = clientSocket;
        this.event_id = event_id;
    }

    public void run() {
    	ServSQL sql = new ServSQL();
    	InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String inline;
        String outline;
        try {
            inline = brinp.readLine();
            System.out.print("recebi\n" + inline + "\n");
            if (inline.contains("addevento")) {
            	outline = sql.addEvento(inline, event_id);
            	if (outline.equals( "" + event_id )) event_id.getAndIncrement();
            }
            else if (inline.contains("delevento")) {
            	outline = sql.delEvento(inline);
            }
            else if (inline.contains("updateevento")) {
            	outline = sql.updateEvento(inline);
            }
            else if (inline.contains("getevento")) {
            	outline = sql.getEventos(inline);
            }
            else if (inline.contains("adduser")) {
            	outline = sql.addUser(inline);
            }
            else if (inline.contains("deluser")) {
            	outline = sql.delUser(inline);
            }
            else outline = "Nenhum tipo funcao detetado";
        	System.out.print("mandei\n" + outline + "\n");
        	out.writeBytes(outline + "\n");
            out.flush();
            socket.close();
            brinp.close();
            sql.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}