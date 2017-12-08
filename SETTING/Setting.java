package SETTING;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import MN.Sensor;

public class Setting 
{
	private static final boolean DEBUG = true;
	public final static String BR_URL = "http://[aaaa::212:7401:1:101]/"; //Border-Router IPv6

	public static List<String> GET_sensor_ip(String BRIp) throws IOException 
	{
		List<String> hops = new ArrayList<String>();
		URL obj = new URL(BRIp);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", "Mozilla/5.0");
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
	
	public static void main(String[] args) throws IOException, InterruptedException, ParseException 
	{
		List<String> ipv6;
		List<Sensor> sensors;
		int num_sensors;
		JSONObject setting = (JSONObject) new JSONParser().parse(new FileReader("src/main/java/SETTING/setting_house2.json"));

	    String home = (String) setting.get("home");
	    System.out.println("home: "+home);
	    
	    num_sensors = Integer.parseInt(setting.get("num_sensors").toString());
	    System.out.println("Number of Sensors"+num_sensors);
		
		do{
			ipv6 = new ArrayList<String>();
			sensors = new ArrayList<Sensor>();
			ipv6 = GET_sensor_ip(BR_URL);
			for(int i=0;i<ipv6.size();i++){
				boolean exist = false;
				for(int j=0;j<sensors.size();j++)
					if(ipv6.get(i).equals(sensors.get(j).getIp()))
						exist = true;

				if(!exist)
				{
					Sensor s = new Sensor(ipv6.get(i), null);
					sensors.add(s);
				}
			}
			
			for(int i=0;i<sensors.size();i++)
			{		
				JSONArray sensorsHouse = (JSONArray) setting.get("sensors");
				String roomNumber = null;
				
				Iterator<?> it = sensorsHouse.iterator();
				while (it.hasNext()) {
					JSONObject sensor = (JSONObject) it.next();
					String room = (String) sensor.get("room");
				    JSONArray ip = (JSONArray) sensor.get("ip");
				    Iterator<?> it2 = ip.iterator();
				    while (it2.hasNext()) {
				    	if(it2.next().toString().contains(sensors.get(i).getIp()))
				    		roomNumber = room;
				    }
				}
			
				for(int j=0; j<sensors.get(i).getNum_res();j++){
					if(sensors.get(i).getResource().get(j).getTitle().equals("setting"))
						sensors.get(i).getResource().get(j).POST("room="+roomNumber);
				}
			}
		}while(sensors.size() != num_sensors);
	}
}


