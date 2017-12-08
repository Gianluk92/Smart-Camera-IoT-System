package IN;

import java.util.concurrent.LinkedBlockingQueue;

//import java.util.ArrayList;
//import java.util.List;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

public class SubscribeResource extends CoapResource{
	private LinkedBlockingQueue<Message> queue;
	
	public SubscribeResource(String name, LinkedBlockingQueue<Message> queue) {
		super(name);
		this.queue = queue;
		getAttributes().setTitle(name.toUpperCase());
	}
	
	 public void handlePOST(CoapExchange exchange)
    {
    	exchange.respond(ResponseCode.CREATED);
        JSONObject contentStr = new JSONObject(new String(exchange.getRequestPayload()));
        JSONObject sgn = (JSONObject) contentStr.get("m2m:sgn");
        if(sgn.has("nev")){
	        JSONObject nev = (JSONObject) sgn.get("nev");
	        String source = sgn.getString("sur");
	       // System.out.println(source.toString());
	        JSONObject rep = (JSONObject) nev.get("rep");
	        String value = rep.getString("con");
	        String split[] = source.split("/");
	        String sensor = split[4];
	        String room = split[3];
	        String ae = split[1];
        
	        Message tmp = new Message(sensor, room, ae, value, rep.getString("cnf"));
//	        System.out.println(sensor+room+ae+value);
	    	queue.add(tmp);
        } 
    }
}
