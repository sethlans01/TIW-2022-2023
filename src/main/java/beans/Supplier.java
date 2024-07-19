package beans;

import java.util.List;

public class Supplier{
	
	private String code;
	private String name;
	private String score;
	private String cost;
	private List<Ship> policies;
	private String numProducts;
	private String valProducts;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getScore() {
		return score;
	}
	
	public void setScore(String score) {
		this.score = score;
	}

	public String getCost() {
		return cost;
	}

	public void setCost(String cost) {
		this.cost = cost;
	}

	public List<Ship> getPolicies() {
		return policies;
	}

	public void setPolicies(List<Ship> policies) {
		this.policies = policies;
	}

	public String getValProducts() {
		return valProducts;
	}

	public void setValProducts(String valProducts) {
		this.valProducts = valProducts;
	}

	public String getNumProducts() {
		return numProducts;
	}

	public void setNumProducts(String numProducts) {
		this.numProducts = numProducts;
	}
	
	
}