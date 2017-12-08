package IN;

public class Message {
	private String sensor;
	private String room;
	private String ae;
	private String value;
	private String sur;
	
	public Message(String sensor, String room,String ae,String value,String sur){
		this.sensor = sensor;
		this.room = room;
		this.ae = ae;
		this.value = value;
		this.sur = sur;
	}
	
	public String getSensor() {
		return sensor;
	}
//	public void setSensor(String sensor) {
//		this.sensor = sensor;
//	}
	public String getRoom() {
		return room;
	}
//	public void setRoom(String room) {
//		this.room = room;
//	}
	public String getAe() {
		return ae;
	}
//	public void setAe(String ae) {
//		this.ae = ae;
//	}
	public String getValue() {
		return value;
	}
//	public void setValue(String value) {
//		this.value = value;
//	}
	public String getSur() {
		return sur;
	}
//	public void setSur(String sur) {
//		this.sur = sur;
//	}
	
}
