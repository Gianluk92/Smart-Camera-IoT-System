package MN;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;

public class SensorResource{
	private String title;
	private boolean isObservable;
	private String type;
	private String rt;
	private String if_;
	private String ip;
	
	protected SensorResource(String ip){
		this.ip = ip;
		isObservable = false;
		title = null;
		rt = null;
		if_ = null;
		type = null;
	}
	
	public void visit()
	{
		System.out.println("	Name:			"+ this.getTitle());
		System.out.println("	Observable:		"+ this.isObservable());
		System.out.println("	Type:			"+ this.getType());
		System.out.println("	Rt:			"+ this.getRt());
		System.out.println("	If:			"+ this.getIf_());
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public boolean isObservable() {
		return isObservable;
	}
	
	public void setObservable(boolean isObservable) {
		this.isObservable = isObservable;
	}
	
	public String getRt() {
		return rt;
	}
	
	public void setRt(String rt) {
		this.rt = rt;
	}
	
	public String getIf_() {
		return if_;
	}
	
	public void setIf_(String if_) {
		this.if_ = if_;
	}
	
	public String GET(){
		CoapClient client = new CoapClient("coap://["+ip+"]:5683/"+title);
		Request req = Request.newGet();
		CoapResponse post_result = client.advanced(req);
		if(post_result == null)
			while(post_result == null){
				post_result = client.get();
				System.out.println("Errore get");
			}
		return post_result.getResponseText();
	}
	
	public boolean POST(String Payload){
		CoapClient client = new CoapClient("coap://["+ip+"]:5683/"+title);
		Request req = Request.newPost();
		req.setPayload(Payload);
		CoapResponse post_result = client.advanced(req);
		if(post_result.isSuccess()){
			System.out.println("post:"+post_result.getCode());
			return true;
		}
		else{
			System.out.println("post:"+post_result.getCode());
			return false;
		}
	}
}
