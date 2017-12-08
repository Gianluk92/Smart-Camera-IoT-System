package MN;

import java.util.ArrayList;
import java.util.List;

public class AE {
	
	private String rn;
	private List<String> Container;
	
	public AE(String rn){
		this.Container = new ArrayList<String>(); 
		this.rn = rn;
	}
	
	public String getRn() {
		return rn;
	}

	public List<String> getContainer() {
		return Container;
	}

	public void setContainer(String container) {
		Container.add(container);
	}


}
