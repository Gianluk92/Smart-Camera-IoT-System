package MN;


import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

public class SubscribingState extends CoapResource{

//	private ManagerOm2m onem2m = new ManagerOm2m();
	
	LinkedBlockingQueue<String> activation;
	
	public SubscribingState(String name, LinkedBlockingQueue<String> activationQueue) 
	{
		super(name);
		getAttributes().setTitle(name.toUpperCase());
		this.activation = activationQueue;
	}

	
	 public void handlePOST(CoapExchange exchange)
    {
    	exchange.respond(ResponseCode.CREATED);
//    	System.out.println(exchange.getRequestText());
    	JSONObject contentStr = new JSONObject(new String(exchange.getRequestPayload()));
        JSONObject sgn = (JSONObject) contentStr.get("m2m:sgn");
        if(sgn.has("nev")){
	        JSONObject nev = (JSONObject) sgn.get("nev");
	        String source = sgn.getString("sur");
	        System.out.println(source.toString());
	        JSONObject rep = (JSONObject) nev.get("rep");
	        String value = rep.getString("con");
	        String split[] = source.split("/");
	        String sensor = split[4];
	        String room = split[3];
//	        String ae = split[1];
	        
//	        System.out.println("AE:"+ae+" room:"+room+" sensor:"+sensor+" value:"+value);
	        
	        activation.add(sensor+"-"+room+" "+value);
        }
    }
}
