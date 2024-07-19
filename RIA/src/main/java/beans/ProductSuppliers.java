package beans;

import java.util.List;

public class ProductSuppliers{
	
	private Product product;
	
	private List<Supplier> suppliers;

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public List<Supplier> getSuppliers() {
		return suppliers;
	}

	public void setSuppliers(List<Supplier> suppliers) {
		this.suppliers = suppliers;
	}
	
}