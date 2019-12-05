import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;

public class ServSQL {
	/* Para funcionar precisa ter o JDBC driver jar adicionado ao build path do projeto
	 * Jar esta no ficheiro SQL que esta no projeto
	 * Servidor db da feup nao tava a dar para aceder por isso criei noutro sitio
	 * Credenciais acesso database estao no construtor desta classe */
	private Connection c;
	private Statement stmt;
	
	public ServSQL() { // Construtor abre conexão ao servidor DB
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://rogue.db.elephantsql.com:5432/csfetqrr",
					"csfetqrr", "pIFggVM5LgRjNAkMqbvRg-RqhpyshduP");
			stmt = c.createStatement();
			}
		catch (Exception e) {
			System.out.println("oops");
			e.printStackTrace();
			System.err.println(e.getClass().getName()+": "+e.getMessage());
			System.exit(0);
			}
		System.out.println("Opened database successfully");
	}
	
	public void tableEventos() { // Apaga e cria uma nova tabela eventos
		try {
			String sql = "DROP TABLE EVENTOS";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE EVENTOS " +
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
			"UNIQUE (USER_ID, EVENT_ID)" +
			")";
	        stmt.executeUpdate(sql);
			
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        	System.exit(0);
		}
	}
	
	public void tableUsers() { // Apaga e cria uma nova tabela Users
		try {
			String sql = "DROP TABLE USERS";
			stmt.executeUpdate(sql);
			sql = "CREATE TABLE USERS " +
			"(USER_ID INTEGER PRIMARY KEY NOT NULL" +
			")";
	        stmt.executeUpdate(sql);
			
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        	System.exit(0);
		}
	}
	
	public String addEvento(String json, int event_id) { // Recebe string json do cliente e adiciona evento
		JSONObject obj;
		try {
			obj = new JSONObject(json);
			try {
				String sql = "INSERT INTO EVENTOS (USER_ID,EVENT_ID,NOME,DATA_INICIO,DATA_FIM,TIPO,DIAS,NOTAS,LOCAL,LAST_EDIT,ALARME,COR) VALUES ("
				+ obj.getInt("user_id") + ", "
				+ event_id + ", "
				+ "'" + obj.getString("nome") + "', "
				+ "'" + obj.getString("data_inicio") + "', "
				+ "'" + obj.getString("data_fim") + "', "
				+ "'" + obj.getString("tipo") + "', "
				+ "'" + obj.getString("dias") + "', "
				+ "'" + obj.getString("notas") + "', "
				+ "'" + obj.getString("local") + "', "
				+ "'" + obj.getString("last_edit") + "', "
				+ obj.getInt("alarme") + ", "
				+ "'" + obj.getString("cor") + "'"
				+ ");";
				stmt.executeUpdate(sql);
		        
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
				String sql = "DELETE from EVENTOS where USER_ID = " + obj.getInt("user_id") + " AND EVENT_ID = " + obj.getInt("event_id") + ";";
				stmt.executeUpdate(sql);
		        
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
				ResultSet rs = stmt.executeQuery( "SELECT * FROM public.eventos WHERE user_id = " + obj.getInt("user_id") );
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
				String sql = "INSERT INTO USERS (USER_ID) VALUES ("
				+ obj.getInt("user_id")
				+ ");";
				stmt.executeUpdate(sql);
		        
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
				String sql = "DELETE FROM eventos WHERE user_id = " + obj.getInt("user_id");
				stmt.executeUpdate(sql);
				sql = "DELETE FROM users WHERE user_id = " + obj.getInt("user_id");
				stmt.executeUpdate(sql);
				
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
			ResultSet rs = stmt.executeQuery( "SELECT MAX(event_id) FROM public.eventos AS max");
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
				String sql = "UPDATE EVENTOS SET nome = "
				+ "'" + obj.getString("nome") + "', data_inicio = "
				+ "'" + obj.getString("data_inicio") + "', data_fim = "
				+ "'" + obj.getString("data_fim") + "', tipo = "
				+ "'" + obj.getString("tipo") + "', dias = "
				+ "'" + obj.getString("dias") + "', notas = "
				+ "'" + obj.getString("notas") + "', local = "
				+ "'" + obj.getString("local") + "', last_edit = "
				+ "'" + obj.getString("last_edit") + "', alarme = "
				+ obj.getInt("alarme") + ", cor = "
				+ "'" + obj.getString("cor") + "'"
				+ " WHERE event_id = " + obj.getInt("event_id") + ";";
				stmt.executeUpdate(sql);
		        
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
}
