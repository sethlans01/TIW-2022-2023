package beans;

import java.util.List;

public class Homepage {

    private String name;
    private List<Product> lastFive;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getLastFive() {
        return lastFive;
    }

    public void setLastFive(List<Product> lastFive) {
        this.lastFive = lastFive;
    }
}
