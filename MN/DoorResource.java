package MN;

import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

public class DoorResource extends Thread implements CoapHandler
{
//	private static final boolean DEBUG = false;
	static private int id = 0;
	private String AE;
	private String resource;
//	private boolean resp;
	private static ManagerOm2m onem2m;
	private int cnf; 
	private int room;
	private String ip;
	private boolean wake;
	private LinkedBlockingQueue<Boolean> resp;
	private LinkedBlockingQueue<MessageObject> queue;
	
	public DoorResource(int room, String resource, String ip, LinkedBlockingQueue<MessageObject> queue) throws InterruptedException
	{
		this.resp = new LinkedBlockingQueue<Boolean>();
		this.resource = resource+Integer.toString(id);
		id++;
		this.room = room;
		this.AE = "room"+room;
		this.cnf = 0;
		this.ip = ip;
		this.queue = queue;
		this.wake = false;
		
		Thread.currentThread().setName("door-room"+room);
		onem2m = new ManagerOm2m();
		if(!onem2m.createAE(AE, AE+"-API", ManagerOm2m.MN_CLIENT))
			System.err.println("Error For Creation Of "+AE);
		if(!onem2m.createCNT(this.resource, ManagerOm2m.MN_CLIENT+AE,AE))
			//System.err.println("Error For Creation Of "+AE+"/"+resource);
		System.out.println("Starting Monitor for: "+AE+"/"+this.resource);
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
		boolean ON = false;
		boolean tmp;
		String res = ManagerOm2m.MN_CLIENT+AE+"/"+resource;
		while(true)
		{
			try {
				tmp = this.resp.take();
				String val = tmp? "ON" :"OFF";
				
				while(!onem2m.createCI(val, res, Integer.toString(cnf)));
				
//				System.out.println(val+" "+cnf+" "+AE);
				cnf++;
				
				if(ON && !tmp){ // no presence
					ON = false;
					queue.put(new MessageObject(room, resource, ON, this.ip));
				
				}
				if(tmp && !ON) // presence
				{
					ON = true;
					queue.put(new MessageObject(room, resource, ON, this.ip));
					
				}
				
				
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}	
		}
	}
}