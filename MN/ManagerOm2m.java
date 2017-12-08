package MN;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.json.JSONObject;

public class ManagerOm2m extends CoapClient
{
	public static final String MN_CLIENT = "coap://192.168.1.150:5685/~/mn-cse2/mn-name2/";
	public static final String IN_CLIENT = "coap://192.168.1.150:5680/~/in-cse/";
	private static List<AE> myAE = new ArrayList<AE>();
	private static Semaphore sem = new Semaphore(1);
	
	/* check if there's something already write in oneM2M */
	public ManagerOm2m(){
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			System.err.println("Error: acquire semaphore");
			e.printStackTrace();
		}
		if(myAE.isEmpty())
			getOm2mState();
		sem.release();

	}
	
	public synchronized boolean createAE(String rn,String api,String coap_client) throws InterruptedException
	{
		sem.acquire();
		int size = myAE.size();
		for(int i=0;i<size;i++)
			if(getMyAE().get(i).getRn().equals(rn)){
//				System.out.println("AE exist");
				sem.release();
				return true; //AE already exists inside the internal structure
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
		if(client.advanced(req) == null){
			sem.release();
			return false;
		}
		else
		{
			myAE.add(new AE(rn));
			sem.release();
			System.out.println("new AE: "+rn);
			return true;
		}	
	}
	
	public synchronized boolean createCNT(String rn, String coap_client, String AE) throws InterruptedException
	{
		sem.acquire();
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
		obj2.put("rn", rn); 
		JSONObject root2 = new JSONObject();
		root2.put("m2m:cnt", obj2);
		String body2 = root2.toString();
		req2.setPayload(body2);
		// error in the request
//		System.out.println("creo cse");
		for(int i=0;i<myAE.size();i++){
			if(myAE.get(i).getRn().equals(AE)){
				for(int j=0;j<myAE.get(i).getContainer().size();j++)
				{
					//System.out.println("size container "+myAE.get(i).getRn()+" "+myAE.get(i).getContainer().size());
					if(myAE.get(i).getContainer().get(j).equals(rn)){
						// container already exists
						System.out.println("ESISTE CONTAINER: "+ rn);
						sem.release();
						return false;
					}
				}
				
				// new container created
				container.advanced(req2);
				myAE.get(i).setContainer(rn);
				sem.release();
				return true;
			}
		}
		sem.release();
		return false;
	}
	
	public synchronized boolean createCI(String value, String coap_client, String cnf){	
		
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
		obj3.put("cnf", "Reading-" + cnf); //nome del container
		obj3.put("con", value);
		JSONObject root3 = new JSONObject();
		root3.put("m2m:cin", obj3);
		String body3 = root3.toString();
		req3.setPayload(body3);
		
//		System.out.println(data.advanced(req3).getCode()+" "+Thread.currentThread().getName());
		data.advanced(req3).getCode();
		if(ResponseCode.isSuccess(ResponseCode.CREATED))
			return true;
		else
			return false;
			
	}

	public void createSubscription(String monitor_resource,String to_observe){
		CoapClient client = null;
		try {
			client = new CoapClient(new URI(to_observe));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		
		CoapResponse responseBody = client.advanced(req);
		//System.out.println(responseBody.getResponseText());
		while(responseBody==null){
			System.out.println("Errore");
			client.advanced(req);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void getOm2mState(){
	    CoapClient test_res = null;
		try {
			test_res = new CoapClient(new URI(MN_CLIENT+"?fu=1&rty=3"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    Request req = Request.newGet();
	    req.getOptions().addOption(new Option(256,"admin:admin"));
	    req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
	    req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
	    CoapResponse responseBody = test_res.advanced(req);
	    JSONObject resp = new JSONObject(responseBody.getResponseText());
	    if(!resp.get("m2m:uril").toString().equals("{}")){
		    String names = resp.getString("m2m:uril");
		    String tmp[] = names.split(" ");

		    for(int i =0;i<tmp.length;i++){
		    	String ae = (tmp[i].split("/"))[3];
		    	boolean exist = false;
			    for(int j =0;j<myAE.size();j++)
			    	if(myAE.get(j).getRn().equals(ae)){
			    		exist = true;
			    		myAE.get(j).setContainer((tmp[i].split("/"))[4]);
//			    		System.out.println("ESISTE AE NELLA LISTA : " + ae + "CNT: "+(tmp[i].split("/"))[4]);
			    		break;
			    	}
			    if(!exist){
//		    		System.out.println("LEGGO AE DA OM2M: " + ae + "CNT: "+(tmp[i].split("/"))[4]);

			    	AE new_ae = new AE(ae);
			    	myAE.add(new_ae);
			    	new_ae.setContainer((tmp[i].split("/"))[4]);
			    }
		    }
	    }
	}
	
	public List<AE> getMyAE() {
		return myAE;
	}
}