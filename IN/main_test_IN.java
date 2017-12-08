package IN;


import java.net.SocketException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class main_test_IN {
	static String resp = "";
	
	public static void main (String argv[]) throws SocketException, InterruptedException{
		ManagerOm2m onem2m = new ManagerOm2m();
		List<String> nameMNs;
//		These Queue are created here for be shared between consumers and producer
		LinkedBlockingQueue<Message> light_queue = new LinkedBlockingQueue<Message>();
		LinkedBlockingQueue<Message> pir_queue = new LinkedBlockingQueue<Message>();
		LinkedBlockingQueue<Message> doorwin_queue = new LinkedBlockingQueue<Message>();
		LinkedBlockingQueue<Message> camera_queue = new LinkedBlockingQueue<Message>();
//		consumers
		SubscribingDoor door_service = new SubscribingDoor(doorwin_queue);
		SubscribingPir pir_service = new SubscribingPir(pir_queue);
		SubscribingLight light_service = new SubscribingLight(light_queue);
		SubscribingCamera camera_service = new SubscribingCamera(camera_queue);
//		producer
		SubscribeResource light = new SubscribeResource("light",light_queue);
		SubscribeResource door = new SubscribeResource("doorwin",doorwin_queue);
		SubscribeResource pir = new SubscribeResource("pir",pir_queue);
		SubscribeResource camera = new SubscribeResource("camera",camera_queue);
		CoAPMonitor server = new CoAPMonitor();

		server.addResource(light);
		server.addResource(pir);
		server.addResource(door);
		server.addResource(camera);
		server.addEndpoints();
		
		door_service.start();
		pir_service.start();
		light_service.start();
		camera_service.start();
		server.start();
		
//		check the actual state of oneM2M. 
		onem2m.getOm2mState("in-cse");
		
		while(true){

//			check if newMn are entered in the network
			nameMNs = onem2m.getMNs();
			for(int i=0;i<nameMNs.size();i++){
				onem2m.getOm2mState(nameMNs.get(i));
				onem2m.createAE("house-"+nameMNs.get(i), "house"+i+"-API", onem2m.IN_CLIENT);
			}

			for(int i=0;i<onem2m.getMyAE().size();i++){
				AE tmp = onem2m.getMyAE().get(i);			
				onem2m.createCNT(tmp.getRn(),onem2m.IN_CLIENT+"in-name/house-"+tmp.getMn(),"house-"+tmp.getMn());
				for(int j=0;j<onem2m.getMyAE().get(i).getContainer().size();j++)
					if(onem2m.createCNT(tmp.getContainer().get(j)+"-"+tmp.getRn(), onem2m.IN_CLIENT+"in-name/house-"+tmp.getMn()+"/"+tmp.getRn(), "house-"+tmp.getMn()))
//							TODO: se lanciato una sola volta inserisce la sottoscrizione altrimenti non riesce	we execute the subscription one time for each sensor in the network
//	 						correggere la parte relativa al nome di reg
						onem2m.createSubscription("coap://192.168.1.150:5686/"+tmp.getContainer().get(j).replaceAll("[^A-Za-z]", "").toLowerCase(),
						"coap://192.168.1.150:5680/~/"+tmp.getMn()+"/"+tmp.getMn_n()+"/"+tmp.getRn()+"/"+tmp.getContainer().get(j).toUpperCase());
				
				if(onem2m.createCNT(tmp.getRn()+"-status",onem2m.IN_CLIENT+"in-name/house-"+tmp.getMn(),"house-"+tmp.getMn())){
//					create a connection with the mn that permit to turn on and off the system
//					if a new mn is added we must add a new server to use as subscription location
					String poa = onem2m.getPOA(tmp.getMn());
					System.out.println(poa);
					onem2m.createSubscription(poa+"state",
						"coap://192.168.1.150:5680/~/in-cse/in-name/house-"+tmp.getMn()+"/"+tmp.getRn()+"-status");
					onem2m.createCI("off", "coap://192.168.1.150:5680/~/in-cse/in-name/house-"+tmp.getMn()+"/"+tmp.getRn()+"-status", "start-status");
				}
						
			}
			Thread.sleep(90000);
		}
	}
}
