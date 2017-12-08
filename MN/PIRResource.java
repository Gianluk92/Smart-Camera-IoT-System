package MN;

import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

public class PIRResource extends Thread implements CoapHandler
{
	private String AE;
	private int room;
	private String resource;
	private static ManagerOm2m onem2m;
	private int cnf;
	private String ip;
	private LinkedBlockingQueue<Boolean> resp;
	private LinkedBlockingQueue<MessageObject> queue;
	private boolean wake;
	
	public PIRResource(int room, String resource, String ip, LinkedBlockingQueue<MessageObject> queue) throws InterruptedException
	{
		this.resp = new LinkedBlockingQueue<Boolean>();
		this.resource = resource;
		this.room = room;
		this.AE = "room"+room;
		this.cnf = 0;
		this.ip = ip; 
		this.queue = queue;
		this.wake = false;
		
		onem2m = new ManagerOm2m();
		onem2m.createAE(AE, AE+"-API", ManagerOm2m.MN_CLIENT);
		onem2m.createCNT(resource, ManagerOm2m.MN_CLIENT+AE,AE);
		System.out.println("Starting Monitor for: "+AE+"/"+resource);
	}
	
	
	public void onLoad(CoapResponse response) 
	{
		if(wake)
			try {
				if(response.getResponseText().equals("1"))
					this.resp.put(true);
				else
					this.resp.put(false);
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
	
	public void run()
	{
		boolean tmp;
		String res = ManagerOm2m.MN_CLIENT+AE+"/"+resource;
		while(true)
		{
			try {
//				tooke the value from the list dedicated to PIR. Usually a pir sensor write continuously an OFF value. 
//				When it change its status send just a single message with TRUE value. 
				tmp = this.resp.take();	
				String val = tmp? "ON" :"OFF";
				
				while(!onem2m.createCI(val, res, Integer.toString(cnf)));
				
				cnf++;
//				devo scrivere in coda tutto quello che il PIR ci manda perchè ci serve sapere 
//				il suo valore sempre, per la logica di accensione delle telecamere (cameraresource wait in blocking queue)
				queue.put(new MessageObject(room, resource, tmp, this.ip));			
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}
}
