package beans;

import java.util.List;
import java.util.Map;

public class Cart {

    // String is seller code, and then there is the list of each carted product sold by that seller
    private Map<String, List<CartedProduct>> cart;

    public Map<String, List<CartedProduct>> getCart() {
        return cart;
    }

    public void setCart(Map<String, List<CartedProduct>> cart) {
        this.cart = cart;
    }
}