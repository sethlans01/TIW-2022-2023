package beans;

import java.util.List;

public class Homepage {

    private String email;

    private String fullName;

    private List<Product> lastFive;

    
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Product> getLastFive() {
        return lastFive;
    }

    public void setLastFive(List<Product> lastFive) {
        this.lastFive = lastFive;
    }
}