import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ServSQL {
	/* Para funcionar precisa ter o JDBC driver jar adicionado ao build path do projeto
	 * Jar esta no ficheiro SQL que esta no projeto
	 * Servidor db da feup nao tava a dar para aceder por isso criei noutro sitio
	 * Credenciais acesso database estao no construtor desta classe */
	private Connection c;
	private Statement stmt;
	private ResultSet rs;
	
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
			" DATA_INICIO TIMESTAMP," +
			" DATA_FIM TIMESTAMP," +
			" TIPO TEXT," +
			" DIAS TEXT," +
			" NOTAS TEXT," +
			" LOCAL TEXT," +
			" LAST_EDIT TIMESTAMP NOT NULL," +
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
	
	public void addEvento(ArrayList<Evento> eventos, Evento novo) { // Recebe array eventos servidor e um evento e adiciona ao array servidor e a database
		try {
			String sql = "INSERT INTO EVENTOS (USER_ID,EVENT_ID,NOME,DATA_INICIO,DATA_FIM,TIPO,DIAS,NOTAS,LOCAL,LAST_EDIT,ALARME,COR) VALUES ("
			+ novo.user_id + ", "
			+ novo.event_id + ", "
			+ "'" + novo.nome + "', "
			+ "'" + novo.data_fim + "', "
			+ "'" + novo.data_inicio + "', "
			+ "'" + novo.tipo + "', "
			+ "'" + novo.dias + "', "
			+ "'" + novo.notas + "', "
			+ "'" + novo.local + "', "
			+ "'" + novo.last_edit + "', "
			+ novo.alarme + ", "
			+ "'" + novo.cor + "'"
			+ ");";
			stmt.executeUpdate(sql);
			eventos.add(novo);
	        
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
    		System.exit(0);
		}
	}
	
	public void delEvento(ArrayList<Evento> eventos, Evento del) { // Recebe array eventos servidor e um evento e retira ao array servidor e a database
		try {
			String sql = "DELETE from EVENTOS where USER_ID = " + del.user_id + " AND EVENT_ID = " + del.event_id + ";";
			stmt.executeUpdate(sql);
			getEventos(eventos);
	        
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
    		System.exit(0);
		}
	}
	
	public void getEventos(ArrayList<Evento> eventos) { // Mete no array eventos do servidor os eventos na database
		try {
			ResultSet rs = stmt.executeQuery( "SELECT * FROM public.eventos;" );
			eventos.clear();
	        while ( rs.next() ) {
	        	Evento aux = new Evento();
    			aux.user_id = rs.getInt("user_id");
    			aux.event_id = rs.getInt("event_id");
    			aux.nome = rs.getString("nome");
    			aux.data_inicio = rs.getString("data_inicio");
    			aux.data_fim = rs.getString("data_fim");
    			aux.tipo = rs.getString("tipo");
    			aux.dias = rs.getString("dias");
    			aux.notas = rs.getString("notas");
    			aux.local = rs.getString("local");
    			aux.last_edit = rs.getString("last_edit");
    			aux.alarme = rs.getInt("alarme");
    			aux.cor = rs.getString("cor");
	            eventos.add(aux);
	        }
	        
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
    		System.exit(0);
		}
	}
	
	public void addUser(int userid) { // Adiciona um novo user na tabela users
		try {
			String sql = "INSERT INTO USERS (USER_ID) VALUES ("
			+ userid
			+ ");";
			stmt.executeUpdate(sql);
	        
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
    		System.exit(0);
		}
	}
	
	public void delUser(int userid, ArrayList<Evento> eventos) { // Apaga um user e os seus eventos das tabelas e do array eventos
		try {
			String sql = "DELETE FROM eventos WHERE user_id = " + userid;
			stmt.executeUpdate(sql);
			sql = "DELETE FROM users WHERE user_id = " + userid;
			stmt.executeUpdate(sql);
			getEventos(eventos);
	        
		} catch ( Exception e ) {
			System.err.println( e.getClass().getName()+": "+ e.getMessage() );
    		System.exit(0);
		}
	}
}
