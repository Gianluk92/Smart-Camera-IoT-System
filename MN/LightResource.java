package MN;

import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

public class LightResource extends Thread implements CoapHandler{
	private String AE;
	private int room;
	private String resource;
//	private String resp;
	private static ManagerOm2m onem2m;
	private int cnf;
	private String ip;
	private boolean wake;
	private LinkedBlockingQueue<String> resp;
	private LinkedBlockingQueue<MessageObject> queue;
	
	public LightResource(int room, String resource, String ip, LinkedBlockingQueue<MessageObject> queue) throws InterruptedException
	{
		this.resp = new LinkedBlockingQueue<String>();
		this.resource = resource;
		this.room = room;
		this.AE = "room"+room;
		this.cnf = 0;
		this.ip = ip;
		this.queue = queue;
		this.wake = false; 
		
		onem2m = new ManagerOm2m();
		onem2m.createAE(AE, AE+"-API",ManagerOm2m.MN_CLIENT);
		onem2m.createCNT(resource, ManagerOm2m.MN_CLIENT+AE,AE);
		System.out.println("Starting Monitor for: "+AE+"/"+resource);
//		resp = "";
	}
	
	
	public void onLoad(CoapResponse response) {

		// fare coda per evitare perdita messaggi 
		if(wake)
			try {
				resp.put(response.getResponseText());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void onError() {
		System.err.println("Error: notification function");
	}
	
	public synchronized void Lock(){
		this.wake = false;
	}
	
	public synchronized void UnLock(){
		this.wake = true;
	}
	
	public void run(){
		boolean ON = false;
		String tmp = "";
		String res = ManagerOm2m.MN_CLIENT+AE+"/"+resource;
		while(true){
			try {
				tmp = this.resp.take();
			
				if(tmp!=""){
					int value = Integer.parseInt(tmp);
					
					while(!onem2m.createCI(tmp, res, Integer.toString(cnf)));
//					System.out.println(tmp+" "+cnf+" "+AE);
					cnf++;
					
					if(value>450 && !ON){
						ON = true;
	//					System.out.println("Light on");
						queue.put(new MessageObject(room, resource, ON, this.ip));
					
					}
					if(value<450 && ON){
						ON=false;
	//					System.out.println("Light off");
						
						queue.put(new MessageObject(room, resource, ON, this.ip));
					
					}
				
				}

			} catch (InterruptedException e1) {
//				Thread.currentThread().interrupt();
				notifyAll();
				Thread.currentThread().interrupt();
				e1.printStackTrace();
			}
//			while(Thread.interrupted())
				
		}
	}
}
