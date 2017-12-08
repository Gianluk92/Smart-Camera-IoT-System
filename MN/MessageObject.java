package MN;

public class MessageObject {
	private int room ;
	private String type;
	private boolean value;
	private String ip;
	
	public MessageObject(int room, String type, boolean value, String ip){
		this.room = room;
		this.type = type;
		this.value = value;
		this.ip = ip;
	}
	
	public boolean getValue() {
		return value;
	}
	public void setValue(boolean value) {
		this.value = value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getRoom() {
		return room;
	}
	public void setRoom(int room) {
		this.room = room;
	}
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
}
