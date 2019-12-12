import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.sql.PreparedStatement;

import org.json.JSONException;
import org.json.JSONObject;

public class ServSQL {
	/* Para funcionar precisa ter o JDBC driver jar adicionado ao build path do projeto
	 * Jar esta no ficheiro SQL que esta no projeto
	 * Servidor db da feup nao tava a dar para aceder por isso criei noutro sitio
	 * Credenciais acesso database estao no construtor desta classe */
	private Connection c;
	private PreparedStatement pstmt;
	
	public ServSQL() { // Construtor abre conexão ao servidor DB
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://rogue.db.elephantsql.com:5432/csfetqrr",
					"csfetqrr", "pIFggVM5LgRjNAkMqbvRg-RqhpyshduP");
			}
		catch (Exception e) {
			System.out.println("oops");
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
			}
		System.out.println("Opened database successfully");
	}
	
	public void close() {
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void tableEventos() { // Apaga e cria uma nova tabela eventos
		try {
			pstmt = c.prepareStatement(
					"DROP TABLE EVENTOS");
			pstmt.executeUpdate();
			pstmt = c.prepareStatement("CREATE TABLE EVENTOS " +
				"(USER_ID INTEGER NOT NULL," +
				" EVENT_ID INTEGER NOT NULL," +
				" NOME TEXT NOT NULL," +
				" DATA_INICIO TEXT," +
				" DATA_FIM TEXT," +
				" TIPO TEXT," +
				" DIAS TEXT," +
				" NOTAS TEXT," +
				" LOCAL TEXT," +
				" LAST_EDIT TEXT NOT NULL," +
				" ALARME INTEGER," +
				" COR TEXT," + 
				" UNIQUE (USER_ID, EVENT_ID)" +
				")");
	        pstmt.executeUpdate();
			
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        	System.exit(0);
		}
	}
	
	public void tableUsers() { // Apaga e cria uma nova tabela Users
		try {
			pstmt = c.prepareStatement(
					"DROP TABLE EVENTOS");
			pstmt.executeUpdate();
			pstmt = c.prepareStatement(
					"CREATE TABLE USERS " +
					"(USER_ID INTEGER PRIMARY KEY NOT NULL" +
					")");
	        pstmt.executeUpdate();
			
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        	System.exit(0);
		}
	}
	
	public void tableTarefas() { // Apaga e cria uma nova tabela Users
		try {
			pstmt = c.prepareStatement(
					"DROP TABLE TAREFAS");
			pstmt.executeUpdate();
			pstmt = c.prepareStatement(
					"CREATE TABLE TAREFAS " +
					"(TAREFA_ID SERIAL PRIMARY KEY NOT NULL," +
					" USER_ID INTEGER NOT NULL," +
					" NOME TEXT," +
					" CORPO TEXT" +
					")");
	        pstmt.executeUpdate();
			
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        	System.exit(0);
		}
	}
	
	public String addEvento(String json, AtomicInteger event_id) { // Recebe string json do cliente e adiciona evento
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			try {
				pstmt = c.prepareStatement("INSERT INTO EVENTOS (USER_ID,EVENT_ID,NOME,DATA_INICIO,DATA_FIM,TIPO,DIAS,NOTAS,LOCAL,LAST_EDIT,ALARME,COR) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				pstmt.setInt(1, obj.getInt("user_id"));
				pstmt.setInt(2, event_id.get());
				pstmt.setString(3, obj.getString("nome"));
				pstmt.setString(4, obj.getString("data_inicio"));
				pstmt.setString(5, obj.getString("data_fim"));
				pstmt.setString(6, obj.getString("tipo"));
				pstmt.setString(7, obj.getString("dias"));
				pstmt.setString(8, obj.getString("notas"));
				pstmt.setString(9, obj.getString("local"));
				pstmt.setString(10, obj.getString("last_edit"));
				pstmt.setInt(11, obj.getInt("alarme"));
				pstmt.setString(12, obj.getString("cor"));
				pstmt.executeUpdate();
		        
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				return "Erro: " + e.getClass().getName()+": "+ e.getMessage();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return "Erro: Mensagem recebida nao tem formato json";
		}
		return "" + event_id;
	}
	
	public String delEvento(String json) { // Recebe string json do cliente e apaga evento da database
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			try {
				pstmt = c.prepareStatement("DELETE from EVENTOS where USER_ID = ? AND EVENT_ID = ?");
				pstmt.setInt(1, obj.getInt("user_id"));
				pstmt.setInt(2, obj.getInt("event_id"));
				pstmt.executeUpdate();
		        
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				return "Erro: " + e.getClass().getName()+": "+ e.getMessage();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return "Erro: Mensagem recebida nao tem formato json";
		}
		return "Evento deleted";
	}
	
	public String getEventos(String json) { // Recebe string json do cliente e retorna json com os eventos associados ao user
		JSONObject obj;
		JSONObject aux;
		JSONObject eventos;
		int count = 0;
		try {
			obj = new JSONObject(json);
			eventos = new JSONObject();
			try {
				pstmt = c.prepareStatement( "SELECT * FROM public.eventos WHERE user_id = ?" );
				pstmt.setInt(1, obj.getInt("user_id"));
				ResultSet rs = pstmt.executeQuery();
		        while ( rs.next() ) {
		        	aux = new JSONObject();
	    			aux.put("user_id", rs.getInt("user_id"));
	    			aux.put("event_id",rs.getInt("event_id"));
	    			aux.put("nome", rs.getString("nome"));
	    			aux.put("data_inicio", rs.getString("data_inicio"));
	    			aux.put("data_fim", rs.getString("data_fim"));
	    			aux.put("tipo", rs.getString("tipo"));
	    			aux.put("dias", rs.getString("dias"));
	    			aux.put("notas", rs.getString("notas"));
	    			aux.put("local", rs.getString("local"));
	    			aux.put("last_edit", rs.getString("last_edit"));
	    			aux.put("alarme", rs.getInt("alarme"));
	    			aux.put("cor", rs.getString("cor"));
	    			eventos.put("" + count, aux);
	    			count ++;
		        }
		        eventos.put("n", count);
		        
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				return "Erro: " + e.getClass().getName()+": "+ e.getMessage();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return "Erro: Mensagem recebida nao tem formato json";
		}
		return eventos.toString();
	}
	
	public String addUser(String json) { // Adiciona um novo user na tabela users
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			try {
				pstmt = c.prepareStatement("INSERT INTO USERS (USER_ID) VALUES (?)");
				pstmt.setInt(1, obj.getInt("user_id"));
				pstmt.executeUpdate();
		        
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				return "Erro: " + e.getClass().getName()+": "+ e.getMessage();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return "Erro: Mensagem recebida nao tem formato json";
		}
		return "User adicionado!";
	}
	
	public String delUser(String json) { // Apaga um user e os seus eventos das tabelas
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			try {
				pstmt = c.prepareStatement("DELETE FROM eventos WHERE user_id = ?");
				pstmt.setInt(1, obj.getInt("user_id"));
				pstmt.executeUpdate();
				pstmt = c.prepareStatement("DELETE FROM users WHERE user_id = ?");
				pstmt.setInt(1, obj.getInt("user_id"));
				pstmt.executeUpdate();
				
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				return "Erro: " + e.getClass().getName()+": "+ e.getMessage();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return "Erro: Mensagem recebida nao tem formato json";
		}
		return "User apagado!";
	}
	
	public int getEvent_id() {
		int event_id = 0;
		try {
			pstmt = c.prepareStatement( "SELECT MAX(event_id) FROM public.eventos AS max" );
			ResultSet rs = pstmt.executeQuery();
	        while ( rs.next() ) {
	        	event_id = rs.getInt("max") + 1;
	        }
	        
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		return event_id;
	}

	public String updateEvento(String json) {
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			try {
				pstmt = c.prepareStatement("UPDATE EVENTOS SET nome = ?, data_inicio = ?, data_fim = ?, tipo = ?, dias = ?, notas = ?, local = ?, last_edit = ?, alarme = ?, cor = ? WHERE event_id = ?");
				pstmt.setString(1, obj.getString("nome"));
				pstmt.setString(2, obj.getString("data_inicio"));
				pstmt.setString(3, obj.getString("data_fim"));
				pstmt.setString(4, obj.getString("tipo"));
				pstmt.setString(5, obj.getString("dias"));
				pstmt.setString(6, obj.getString("notas"));
				pstmt.setString(7, obj.getString("local"));
				pstmt.setString(8, obj.getString("last_edit"));
				pstmt.setInt(9, obj.getInt("alarme"));
				pstmt.setString(10, obj.getString("cor"));
				pstmt.setInt(11, obj.getInt("event_id"));
				pstmt.executeUpdate();
		        
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				return "Erro: " + e.getClass().getName()+": "+ e.getMessage();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return "Erro: Mensagem recebida nao tem formato json";
		}
		return "Updated!";
	}
	
	public String addTarefa(String json) { // Recebe string json do cliente e adiciona tarefa
		JSONObject obj;
		int tarefa_id = 0;
		try {
			obj = new JSONObject(json);
			try {
				pstmt = c.prepareStatement("INSERT INTO TAREFAS (USER_ID,NOME,CORPO) VALUES (?, ?, ?)");
				pstmt.setInt(1, obj.getInt("user_id"));
				pstmt.setString(2, obj.getString("nome"));
				pstmt.setString(3, obj.getString("corpo"));
				pstmt.executeUpdate();
		        
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				return "Erro: " + e.getClass().getName()+": "+ e.getMessage();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return "Erro: Mensagem recebida nao tem formato json";
		}
		try {
			pstmt = c.prepareStatement( "SELECT tarefa_id FROM public.tarefas WHERE user_id = ? AND nome = ? AND corpo = ?" );
			pstmt.setInt(1, obj.getInt("user_id"));
			pstmt.setString(2, obj.getString("nome"));
			pstmt.setString(3, obj.getString("corpo"));
			ResultSet rs = pstmt.executeQuery();
	        while ( rs.next() ) {
	        	tarefa_id = rs.getInt("tarefa_id");
	        }
	        
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		}
		return "" + tarefa_id;
	}
	
	public String delTarefa(String json) { // Apaga a tarefa enviada
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			try {
				pstmt = c.prepareStatement("DELETE FROM tarefas WHERE tarefa_id = ?");
				pstmt.setInt(1, obj.getInt("tarefa_id"));
				pstmt.executeUpdate();
				
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				return "Erro: " + e.getClass().getName()+": "+ e.getMessage();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return "Erro: Mensagem recebida nao tem formato json";
		}
		return "Tarefa apagada!";
	}
	
	public String updateTarefa(String json) { // Update a tarefa
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			try {
				pstmt = c.prepareStatement("UPDATE tarefas SET nome = ?, corpo = ? WHERE tarefa_id = ?");
				pstmt.setString(1, obj.getString("nome"));
				pstmt.setString(2, obj.getString("corpo"));
				pstmt.setInt(3, obj.getInt("tarefa_id"));
				pstmt.executeUpdate();
				
			} catch ( Exception e ) {
				System.err.println( e.getClass().getName()+": "+ e.getMessage() );
				return "Erro: " + e.getClass().getName()+": "+ e.getMessage();
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return "Erro: Mensagem recebida nao tem formato json";
		}
		return "Tarefa updated!";
	}
}