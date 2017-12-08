package IN;

import java.util.concurrent.LinkedBlockingQueue;

//import java.util.ArrayList;
//import java.util.List;

public class SubscribingCamera extends Thread{
	

	private ManagerOm2m onem2m = new ManagerOm2m();
	private LinkedBlockingQueue<Message> queue;
	
	public SubscribingCamera(LinkedBlockingQueue<Message> queue) 
	{
		this.queue = queue;
	}

	public void run(){
		while(true){
			try {
				Message tmp = this.queue.take();
				System.out.println(tmp.getRoom()+" "+tmp.getAe()+" "+tmp.getSensor()+" "+tmp.getSur());
				onem2m.createCI(tmp.getValue(),onem2m.IN_CLIENT+"in-name/house-"+tmp.getAe()+"/"+tmp.getRoom()+"/"+tmp.getSensor()+"-"+tmp.getRoom(), tmp.getSur());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
