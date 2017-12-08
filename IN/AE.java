package IN;

import java.util.ArrayList;
import java.util.List;

public class AE {
	
	private String rn;
	private String mn;
	private String mn_n;
	private List<String> Container;
	
	public AE(String rn, String mn, String mn_n){
		this.Container = new ArrayList<String>();
		this.mn = mn;
		this.mn_n = mn_n;
		this.rn = rn;
	}
	
	public void visit(){
		System.out.println("MN name:"+mn+"/"+mn_n);
		System.out.println("	AE name:"+rn);
		System.out.println("		Containers:");
		for(int i=0;i<Container.size();i++)
			System.out.println("			"+Container.get(i));
	}
	
	public String getRn() {
		return rn;
	}

	public String getMn(){
		return mn;
	}
	
	public String getMn_n() {
		return mn_n;
	}

	public List<String> getContainer() {
		return Container;
	}

	public void setContainer(String container) {
		Container.add(container);
	}


}
