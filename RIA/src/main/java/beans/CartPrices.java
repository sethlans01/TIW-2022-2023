package beans;

import java.util.Map;

public class CartPrices {

    // String is seller code, and then there is the preorder cost of that seller in the cart
    private Map<String, PreorderCost> cartCosts;

    public Map<String, PreorderCost> getCartCosts() {
        return cartCosts;
    }

    public void setCartCosts(Map<String, PreorderCost> cartCosts) {
        this.cartCosts = cartCosts;
    }

}