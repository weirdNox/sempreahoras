
public class Evento {
	public int user_id;
	public int event_id;
	public String nome;
	public String data_inicio;
	public String data_fim;
	public String cor;
	public String tipo;
	public String dias;
	public String notas;
	public String local;
	public String last_edit;
	public int alarme;
	
	public Evento(int uid, int eid, String nom, String beg_date, String end_dat, String type, String days, String notes, String loc, String last_ed, int alarme, String cor) {
		user_id = uid;
		event_id = eid;
		nome = nom;
		data_inicio = beg_date;
		data_fim = end_dat;
		this.cor = cor;
		tipo = type;
		dias = days;
		notas = notes;
		local = loc;
		last_edit = last_ed;
		this.alarme = alarme;
	}
	public Evento() {
		
	}
	
	
}
