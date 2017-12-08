package MN;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class Sensor
{
	private static final boolean DEBUG = false;

	private String ip;
	private String type;
	private boolean isActuator;
	private int num_res;
	private int room;
	private boolean isObserved;
	private List<SensorResource> resources;
	
	private LinkedBlockingQueue<MessageObject> queue;
	
	public Sensor(String ip, LinkedBlockingQueue<MessageObject> queue) throws InterruptedException
	{
		this.ip = ip;
		this.resources = new ArrayList<SensorResource>();
		this.setActuator(false);
		GET_resources();
		this.queue = queue;
		this.isObserved = false;
	}
	
	public void visit()
	{
		if(this.isActuator())
			System.out.println("\n---------Actuator--------");
		else
			System.out.println("\n---------Sensor--------");
		System.out.println("Ip:		"+ this.getIp());
		System.out.println("Room number:	"+ this.getRoom());
		System.out.println("Type:		"+ this.getType());
		System.out.println("N. Resource:	"+ this.getNum_res());
		
		for(int i=0; i<this.getNum_res();i++){
			System.out.println("\n	---------Resource--------");
			this.getResource().get(i).visit();
		}

	}
	
	public boolean isObserved() {
		return isObserved;
	}
	
	public String getIp() {
		return ip;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean isActuator() {
		return isActuator;
	}
	
	private void setActuator(boolean isActuator){
		this.isActuator = isActuator;
	}
	
	public int getNum_res() {
		return num_res;
	}
	
	public int getRoom() {
		return room;
	}
	
	public List<SensorResource> getResource() {
		return resources;
	}
	
	private void GET_resources() throws InterruptedException{
		CoapClient client = new CoapClient("coap://["+ip+"]:5683/.well-known/core");
		String Sensor_info[], Resource_info[][];
		String name = null;
		
		CoapResponse resource_name = client.get();
		if(resource_name == null)
			while(resource_name == null){
				resource_name = client.get();
				System.out.println("Errore get");
			}

		name = resource_name.getResponseText();
		Sensor_info = name.split(",");
		num_res = Sensor_info.length-1;
		
		if(DEBUG)
			System.out.println(ip+" has:"+num_res+" resources");
		
		Resource_info = new String[num_res+1][]; 
		for(int i = 1; i<(num_res+1);i++)
		{
			SensorResource res = new SensorResource(ip);
			Resource_info[i] = Sensor_info[i].split(";");
			for(int j = 1; j< Resource_info[i].length; j++)
			{
				String tmp = Resource_info[i][j];
				if(tmp.startsWith("title")){
					res.setTitle(tmp.substring(tmp.indexOf("\"")+1,tmp.lastIndexOf("\"")));
					if(DEBUG)
						System.out.println(res.getTitle());
				}
				if(tmp.startsWith("rt")){
					res.setRt(tmp.substring(tmp.indexOf("\"")+1,tmp.lastIndexOf("\"")));
					if(DEBUG)
						System.out.println(res.getRt());
				}
				if(tmp.startsWith("if")){
					res.setIf_(tmp.substring(tmp.indexOf("\"")+1,tmp.lastIndexOf("\"")));
					if(DEBUG)
						System.out.println(res.getIf_());
				}
				if(tmp.startsWith("type")){
					res.setType(tmp.substring(tmp.indexOf("\"")+1,tmp.lastIndexOf("\"")));
					if(DEBUG)
						System.out.println(res.getType());
				}
				if(tmp.startsWith("obs")){
					res.setObservable(true);
					if(DEBUG)
						System.out.println(res.isObservable());
				}
				
			}
			resources.add(res);
			if(res.getTitle().equals("setting")){
				room = Integer.parseInt(res.GET());
				type = res.getType();
				if(type.equals("CAMERA"))
					setActuator(true);
			}
		}
	}
	

	public Thread observe() throws InterruptedException
	{
		Object handler = null;
		for(int i=0; i<num_res; i++)
			if(resources.get(i).isObservable())
			{
				String name = resources.get(i).getTitle();
				int if_ = Integer.parseInt(resources.get(i).getIf_());
				CoapClient client = new CoapClient("coap://["+ip+"]:5683/"+name);
				
				switch(if_){
					// Light sensors
					case 1:
						handler = new LightResource(room, type, ip, queue);
						client.observe((LightResource)handler);
						break;
					// Door sensors
					case 2:
						handler = new DoorResource(room, type, ip, queue);
						client.observe((DoorResource)handler);
						break;
					// PIR sensors
					case 3:
						handler = new PIRResource(room, type, ip, queue);
						client.observe((PIRResource)handler);
						break;
				}
				this.isObserved = true;
			}
		Thread tmp = (Thread) handler;
		tmp.setName(type+"-room"+room);
		return tmp;
	}

}
