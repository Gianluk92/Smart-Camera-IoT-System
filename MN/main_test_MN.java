package MN;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.californium.core.network.config.NetworkConfig;

public class main_test_MN 
{
	public static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
	public final static String BR_URL = "http://[aaaa::212:7401:1:101]/"; //Border-Router IPv6
	private static final String USER_AGENT = "Mozilla/5.0";
	private static final boolean DEBUG = false;
	
	public static List<String> GET_sensor_ip(String BRIp) throws IOException 
	{
		List<String> hops = new ArrayList<String>();
		URL obj = new URL(BRIp);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", USER_AGENT);
		int responseCode = con.getResponseCode();
		
		if (responseCode == HttpURLConnection.HTTP_OK) 
		{ 			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) 
			{	
				int start = inputLine.indexOf("aaaa");
				int end = inputLine.indexOf("/", start);
				if(start != -1)
				{
					hops.add(inputLine.substring(start, end));
				}
			}
			in.close();
			if(DEBUG)
				System.out.println("GET request ended. Number of sensor found: "+ hops.size());
		} 
		else
			if(DEBUG)
				System.out.println("GET request failed");
		return hops;
	}
	
//	public static void show_info(List<Sensor> sensors)
//	{
//		for(int i =0;i<sensors.size();i++)
//		{
//			System.out.println("\n---------Sensor--------");
//			System.out.println("Ip:		"+sensors.get(i).getIp());
//			System.out.println("Room number:	"+sensors.get(i).getRoom());
//			System.out.println("Type:		"+sensors.get(i).getType());
//			System.out.println("N. Resource:	"+sensors.get(i).getNum_res());
//			System.out.println("Actuator:	"+sensors.get(i).isActuator());
//			for(int j=0; j<sensors.get(i).getNum_res();j++){
//				System.out.println("\n	---------Resource--------");
//				System.out.println("	Name:			"+sensors.get(i).getResource().get(j).getTitle());
//				System.out.println("	Observable:		"+sensors.get(i).getResource().get(j).isObservable());
//				System.out.println("	Type:			"+sensors.get(i).getResource().get(j).getType());
//				System.out.println("	Rt:			"+sensors.get(i).getResource().get(j).getRt());
//				System.out.println("	If:			"+sensors.get(i).getResource().get(j).getIf_());
//			}
//		}
//	}
	
	public static void main(String	args[]) throws InterruptedException, IOException
	{
		
		List<String> ipv6 = new ArrayList<String>();
		List<Sensor> sensors = new ArrayList<Sensor>();
		LinkedBlockingQueue<MessageObject> queue = new LinkedBlockingQueue<MessageObject>();
		CameraResource sharedCam = new CameraResource(queue);
	
		LinkedBlockingQueue<String> activationQueue = new LinkedBlockingQueue<String>();

//		this is a System status variable, control which thread has to be active or not using the values sent by the site
		SystemThread status = new SystemThread(activationQueue);
		
//		create and starts an observing monitor
		SubscribingState state = new SubscribingState("state", activationQueue);
		CoAPMonitor server = new CoAPMonitor();
		server.addResource(state);
		server.addEndpoints();
		server.start();
		
		do{
			ipv6 = GET_sensor_ip(BR_URL);
			for(int i=0;i<ipv6.size();i++){
				boolean exist = false;
				for(int j=0;j<sensors.size();j++)
					if(ipv6.get(i).equals(sensors.get(j).getIp()))
						exist = true;

				if(!exist)
				{
					Sensor s = new Sensor(ipv6.get(i), queue);
					if(s.isActuator()) 
						sharedCam.setActuator(s);
					sensors.add(s);
					s.visit();
				}
			}
			
			for(int i =0;i<sensors.size();i++){
				if(!sensors.get(i).isObserved() && !sensors.get(i).isActuator()){
					status.AddHandler(sensors.get(i).observe());
				}
			}
			
			if(!status.isAlive())
				status.start();
			
			if(!sharedCam.isAlive())
				sharedCam.start();
			
			Thread.sleep(90000);
			System.out.println("-----------Checking for new Sensors------------");
		}while(true);
	}
}
