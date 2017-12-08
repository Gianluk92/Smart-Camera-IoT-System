package MN;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CameraResource extends Thread 
{
	private class timer {
		public String room;
		public long time;
		public boolean on;
		
		public timer(String room, long time){
			this.room = room;
			this.time = time;
			this.on = false;
		}
	}
	public LinkedBlockingQueue<MessageObject> queue;
	private List<Sensor> actuator;
	private ManagerOm2m onem2m;
	private JSONObject config;
	private List<timer> CamerasTimer;
	
	public CameraResource(LinkedBlockingQueue<MessageObject> queue) throws InterruptedException, IOException
	{
		this.actuator = new ArrayList<Sensor>();
		this.CamerasTimer = new ArrayList<timer>();
		this.onem2m = new ManagerOm2m();
		this.queue = queue;
		try {
			this.config = (JSONObject) new JSONParser().parse(new FileReader("src/main/java/MN/config_house2.json"));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void setActuator(Sensor actuator) throws InterruptedException {
		this.actuator.add(actuator);
		String AE = "room"+actuator.getRoom();
		this.CamerasTimer.add(new timer(AE, 0));
		onem2m.createAE(AE, AE+"-API", ManagerOm2m.MN_CLIENT);
		onem2m.createCNT("CAMERA", ManagerOm2m.MN_CLIENT+AE+"/",AE);
	}

	public List<Sensor> getActuator() {
		return actuator;
	}

	private int checkTimer(String AE) {
		int i;
		for(i=0; i<CamerasTimer.size();i++)
			if(CamerasTimer.get(i).room.equals(AE))
				return i;	
		return -1;
	}
	
	private void PIRlogic(String AE, String value)
	{
		int i = checkTimer(AE);
		if(value.equals("OFF"))
			{
				
				if(System.currentTimeMillis() >= CamerasTimer.get(i).time && CamerasTimer.get(i).on){
					onem2m.createCI(value, ManagerOm2m.MN_CLIENT+AE+"/CAMERA" , "OFF PIR");
					CamerasTimer.get(i).on = false;
					CamerasTimer.get(i).time = 0;
//					System.out.println("Camera: "+AE+" spenta dal pir");
				}
			}
		else {
				onem2m.createCI(value, ManagerOm2m.MN_CLIENT+AE+"/CAMERA" , "ON PIR");
				CamerasTimer.get(i).on = true;
//				System.out.println("accendo "+AE+" PIR");
		}
	}
	
	private void LIGHTlogic(String AE)
	{
		int i = checkTimer(AE);
		if(CamerasTimer.get(i).on)
			CamerasTimer.get(i).time = System.currentTimeMillis() + 60000;
		else{
			CamerasTimer.get(i).on = true;
			CamerasTimer.get(i).time = System.currentTimeMillis() + 10000;
		}
	}
	
	private void DOORWINlogic(String AE, String value, String ip)
	{
		JSONArray doors = (JSONArray) config.get("doors");
		Iterator<?> it = doors.iterator();
		while (it.hasNext()) {
			JSONObject door = (JSONObject) it.next();
			if(door.get("ip").toString().contains(ip)){
				String neighbors = (String) door.get("neighbors");
				onem2m.createCI(value, ManagerOm2m.MN_CLIENT+neighbors+"/CAMERA" , "ON NEIGHBORS");
				for(int j=0; j<CamerasTimer.size();j++)
					if(CamerasTimer.get(j).room.equals(neighbors)){
						CamerasTimer.get(j).time = System.currentTimeMillis() + 10000;
						CamerasTimer.get(j).on = true;
						break;
					}
	    	}
	    }
		int i = checkTimer(AE);
		if(CamerasTimer.get(i).on)
			CamerasTimer.get(i).time = System.currentTimeMillis() + 60000;
		else{
			CamerasTimer.get(i).on = true;
			CamerasTimer.get(i).time = System.currentTimeMillis() + 10000;
		}

	}
	
	public void run()
	{
		MessageObject m = null;
		System.out.println("Thread camera resource");
		
		while(true)
		{
			
			try {
				m = queue.take();
//				System.out.println(m.getRoom()+" "+m.getType()+" "+m.getValue());
			} catch (InterruptedException e) {
				System.out.println("Error entering data in the queue");
				e.printStackTrace();
			}
			String value = Boolean.toString(m.getValue())=="true"? "ON" :"OFF";
			String AE = "room"+m.getRoom();
			String type = m.getType();
			String ip = m.getIp();
			
			//PIR MSG
			if(type.equals("PIR"))	
				PIRlogic(AE, value);
			
			// LIGHT OR DOORWIN MSG
			else 
				if(value.equals("ON")) 
				{
					onem2m.createCI(value, ManagerOm2m.MN_CLIENT+AE+"/CAMERA" , "ON "+type);
//					System.out.println("accendo "+AE+" "+type);
					
					if(type.equals("LIGHT"))
						LIGHTlogic(AE);
					
					if(type.contains("DOORWIN"))
						DOORWINlogic(AE, value, ip);
				}
		}
	}
}
