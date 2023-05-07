package beans;

import java.util.Date;
import java.util.List;

public class OrderDetails {

    private String orderNumber;
    private String address;
    private String sellerName;
    private Float totalDue;
    private Date shippingDate;
    private List<OrderedProduct> orderedProduct;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public Float getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(Float totalDue) {
        this.totalDue = totalDue;
    }

    public Date getShippingDate(){
        return shippingDate;
    }

    public void setShippingDate(Date shippingDate){
        this.shippingDate = shippingDate;
    }

    public List<OrderedProduct> getOrderedProduct() {
        return orderedProduct;
    }

    public void setOrderedProduct(List<OrderedProduct> orderedProduct) {
        this.orderedProduct = orderedProduct;
    }
}