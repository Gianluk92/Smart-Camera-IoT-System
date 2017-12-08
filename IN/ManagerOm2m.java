package IN;

//import java.net.ResponseCache;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONArray;
import org.json.JSONObject;



public class ManagerOm2m extends CoapClient{
//	private static final boolean DEBUG = true;
	//public static final String MN_CLIENT = "coap://192.168.1.151:5684/~/mn-cse1/";
	public final String IN_CLIENT = "coap://192.168.1.150:5680/~/in-cse/";
	private static List<AE> myAE = new ArrayList<AE>();
	public static final String monitor = "coap://192.168.1.150:5686/monitor";
	
	public List<AE> getMyAE() {
		return myAE;
	}
	
	public boolean createAE(String rn,String api,String coap_client){
		int size = myAE.size();
		for(int i=0;i<size;i++)
			if(getMyAE().get(i).getRn().equals(rn)){
//				System.out.println("AE exist");
				return false; //AE already exists inside the internal structure
			}
		//new AE
		CoapClient client = null;
		try {
			client = new CoapClient(new URI(coap_client));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Request	req = Request.newPost();
		
		req.getOptions().addOption(new Option(267, 2));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		
		JSONObject obj = new JSONObject();
		obj.put("api", api);
		obj.put("rr", true);
		obj.put("rn", rn);
		JSONObject root = new JSONObject();
		root.put("m2m:ae", obj);
		String body = root.toString();
		req.setPayload(body);
		// Test the correctness of the answer
		if(client.advanced(req) == null)
			return false;
		else{
			myAE.add(new AE(rn,"in-cse","in-name"));
//			System.out.println("new AE: "+rn);
			return true;
		}
	}
	
	public boolean createCNT(String Resource, String coap_client,String AE){
		CoapClient container = null;
		try {
			container = new CoapClient(new URI(coap_client));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Request	req2 = Request.newPost();
		req2.getOptions().addOption(new Option(267, 3));
		req2.getOptions().addOption(new Option(256, "admin:admin"));
		req2.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req2.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj2 = new JSONObject();
		obj2.put("rn", Resource); 
		JSONObject root2 = new JSONObject();
		root2.put("m2m:cnt", obj2);
		String body2 = root2.toString();
		req2.setPayload(body2);
		
//		if(container.advanced(req2) == null) 
//			return false;
//		else 
//		{
		for(int i=0;i<myAE.size();i++){
			if(myAE.get(i).getRn().equals(AE)){
				for(int j=0;j<myAE.get(i).getContainer().size();j++)
				{
					if(myAE.get(i).getContainer().get(j).equals(Resource)){
						// container already exists
//						System.out.println("ESISTE CONTAINER: "+ Resource);
						return false;
					}
				}
				
				// new container created
				container.advanced(req2);
				myAE.get(i).setContainer(Resource);
				return true;
			}
		}
		return false;
		//}
	}
	
	public boolean createCI(String value, String coap_client, String cnf){	
		CoapClient data = null;
		try {
			data = new CoapClient(new URI(coap_client));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Request	req3 = Request.newPost();
		req3.getOptions().addOption(new Option(267, 4));
		req3.getOptions().addOption(new Option(256, "admin:admin"));
		req3.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req3.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		// Creazione richiesta JSON
		JSONObject obj3 = new JSONObject();
		obj3.put("cnf",cnf); //nome del container
		obj3.put("con", value);
		JSONObject root3 = new JSONObject();
		root3.put("m2m:cin", obj3);
		String body3 = root3.toString();
		req3.setPayload(body3);
		
		if(data.advanced(req3) == null)
			return false;
		else
			return true;
			
	}

	public void createSubscription(String monitor_resource,String to_observe){
		CoapClient client = null;
		try {
			client = new CoapClient(new URI(to_observe));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 23));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("rn", "monitor");
		obj.put("nu", monitor_resource);
		obj.put("nct", 2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub", obj);
		String body = root.toString();
		//System.out.println(body);
		req.setPayload(body);
		int i = 0;
		CoapResponse responseBody = client.advanced(req);
//		System.out.println(responseBody.getResponseText());
		while(responseBody==null && i<5){
			System.out.println("Errore");
			client.advanced(req);
			i++;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(i>=5)
			System.err.println("Impossible start monitoring for: "+monitor_resource);
	}
	
	public List<String> getMNs(){
		List<String> nameMNs = new ArrayList<String>();
		CoapClient test_res = null;
		try {
			test_res = new CoapClient(new URI(IN_CLIENT+"?fu=1&rty=16"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Request req = Request.newGet();
		req.getOptions().addOption(new Option(256,"admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
//		System.out.println(test_res.advanced(req).getPayload());
		CoapResponse responseBody = test_res.advanced(req);

		JSONObject resp = new JSONObject(responseBody.getResponseText());
		if(resp.has("m2m:uril")){
			String names = resp.get("m2m:uril").toString();
			if(names.equals("{}")) return nameMNs;
			
			String tmp[] = names.split(" ");
			for(int i = 0;i<tmp.length;i++)
				nameMNs.add((tmp[i].split("/"))[3]);
		}
		return nameMNs;
	}
	
	public String getPOA(String mn_name){
		CoapClient test_res = null;
		try {
			test_res = new CoapClient(new URI("coap://192.168.1.150:5680/~/"+mn_name));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Request req = Request.newGet();
	    req.getOptions().addOption(new Option(256,"admin:admin"));
	    req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
	    req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
	    CoapResponse responseBody = test_res.advanced(req);
	    while(!responseBody.isSuccess())
	    	System.out.println(responseBody.getCode());
	    
	    JSONObject resp = new JSONObject(responseBody.getResponseText());
	    JSONObject cb = resp.getJSONObject("m2m:cb");
	    JSONArray poa = cb.getJSONArray("poa");
	    String pointOfAccess = poa.getString(0);
	    String value[] = pointOfAccess.split(":");
	    
//	    System.out.println("coap:"+value[1]+":5687/");
	    if(value[1].equals("127.0.0.1"))
	    		return "coap://192.168.1.150:5687/";
	    else
	    	return "coap:"+value[1]+":5687/";
	}
	
	public synchronized void getOm2mState(String mn_name){
	    CoapClient test_res = null;
		try {
			test_res = new CoapClient(new URI("coap://192.168.1.150:5680/~/"+mn_name+"/?fu=1&rty=3"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Request req = Request.newGet();
	    req.getOptions().addOption(new Option(256,"admin:admin"));
	    req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
	    req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
	    CoapResponse responseBody = test_res.advanced(req);
//	    System.out.println("Responce to getState:"+responseBody.getResponseText());
	    JSONObject resp = new JSONObject(responseBody.getResponseText());
	    if(!resp.get("m2m:uril").toString().equals("{}")){
		    String names = resp.getString("m2m:uril");
		    String tmp[] = names.split(" ");
//		    System.out.println(resp);
		    for(int i =0;i<tmp.length;i++){
		    	String ae = (tmp[i].split("/"))[3];
		    	String mn = (tmp[i].split("/"))[2];
//		    	System.out.println(mn);
		    	boolean exist = false;
			    for(int j =0;j<myAE.size();j++)
			    	if(myAE.get(j).getMn().equals(mn_name) && myAE.get(j).getRn().equals(ae)){
			    		exist = true;
			    		myAE.get(j).setContainer((tmp[i].split("/"))[4]);
//			    		System.out.println("ESISTE AE NELLA LISTA : " + ae + "CNT: "+(tmp[i].split("/"))[4]);
			    		break;
			    	}
			    if(!exist){
//		    		System.out.println("LEGGO AE DA OM2M: " + ae + "CNT: "+(tmp[i].split("/"))[4]);

			    	AE new_ae = new AE(ae,mn_name,mn);
			    	myAE.add(new_ae);
			    	new_ae.setContainer((tmp[i].split("/"))[4]);
			    }
		    }
	    }
	}
	
	public void destroy(String to_observe){
	    CoapClient test_res = new CoapClient(to_observe+"/monitor");
	    Request req = Request.newDelete();
	    req.getOptions().addOption(new Option(256,"admin:admin"));
	    req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
	    req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
	    test_res.advanced(req);
	   // System.out.println(responseBody.getResponseText());
	}
}