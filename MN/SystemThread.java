package MN;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class SystemThread extends Thread{
	private List<Thread> handlers;
	private LinkedBlockingQueue<String> activations;
	
	public SystemThread(LinkedBlockingQueue<String> activationQueue){
		this.handlers = new ArrayList<Thread>();
		this.activations = activationQueue;
	}
	
	public void AddHandler(Thread handler){
		handlers.add(handler);
	}
	
	public void run(){
		String tmp = "";
		for(int i =0;i<handlers.size();i++){
			if(!handlers.get(i).isAlive())
				handlers.get(i).start();
		}

		while(true){	
			try {
				tmp = activations.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String thread_name = tmp.split(" ")[0];
			String room = thread_name.split("-")[0];
			String value = tmp.split(" ")[1];
			
			System.out.println(room+" "+value);
			
			for(int i =0;i<handlers.size();i++){
				if(handlers.get(i).getName().contains(room)){
					if(value.equals("on")){
//						System.out.println("on "+room);
						try {
							handlers.get(i).getClass().getMethod("UnLock").invoke(handlers.get(i));
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if(value.equals("off") && handlers.get(i).isAlive()){
//						System.out.println("off "+room);
						try {
							handlers.get(i).getClass().getMethod("Lock").invoke(handlers.get(i));
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}	
//				System.out.println(handlers.get(i).getName()+" "+handlers.get(i).getState());
			}

		}
	}
}
