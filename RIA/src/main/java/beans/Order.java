package beans;

import java.util.List;

public class Order {

    private List<OrderDetails> orderList;

    public List<OrderDetails> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<OrderDetails> orderList) {
        this.orderList = orderList;
    }
}