package beans;

import java.util.Map;

public class Preorder {

    // The key is the seller code, the value is the preorder costs of all the elements in the cart sold by the seller
    private Map<String, PreorderCost> costs;

    public Map<String, PreorderCost> getCosts() {
        return costs;
    }

    public void setCosts(Map<String, PreorderCost> costs) {
        this.costs = costs;
    }
}