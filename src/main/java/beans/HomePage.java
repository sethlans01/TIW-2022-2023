package beans;

import java.util.List;

public class Homepage {

    private String name;
    private List<FullProduct> lastFive;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FullProduct> getLastFive() {
        return lastFive;
    }

    public void setLastFive(List<FullProduct> lastFive) {
        this.lastFive = lastFive;
    }
}
