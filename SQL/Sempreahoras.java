import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Sempreahoras {
	
	static final int PORT = 1978;

	public static void main(String[] args) {
		
		ServSQL sql = new ServSQL();
		//sql.tableEventos();
		//sql.tableUsers();
		int event_id = sql.getEvent_id();
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
                	if (outline.equals( "" + event_id )) event_id++;
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
                else if(inline.contains("fechar")) {
                	outline = "FECHAR";
                	doRun = false;
                }
                else outline = "Nenhum tipo funcao detetado";
            	System.out.print("mandei\n" + outline + "\n");
            	out.writeBytes(outline + "\n");
                out.flush();
                socket.close();
                brinp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}

