package dao;

import beans.Homepage;
import beans.Product;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LastFiveDAO {

    private Connection connection;

    public LastFiveDAO(Connection connection){
        this.connection = connection;
    }

    public Homepage getHomepageBean(String email) throws SQLException {

        // Instantiate a homepage bean
        Homepage homepage = null;
        // Set the base structure of the SQL query
        String performedAction = " extracting last 5 seen products";
        String query = "SELECT * FROM UltimiCinque WHERE Utente = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Product> temp = new ArrayList<>();

        try {
            // Prepare and execute the query
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            // Parse the results and create a Homepage bean
            homepage = new Homepage();
            while(resultSet.next()) {
                homepage.setEmail(resultSet.getString("Utente"));
                // Set, for all products, the code
                for(int i = 0; i < 5; i++){
                    Product prodTemp = new Product();
                    int a = i + 1;
                    String productCode = resultSet.getString("Prod" + a);
                    if(productCode != null){
                        prodTemp.setCode(productCode);
                        temp.add(prodTemp);
                    }
                }
            }
        }catch(SQLException e) {
            throw new SQLException("Error accessing the DB while" + performedAction);
        }finally {
            try {
                    resultSet.close();
            }catch (Exception e) {
                throw new SQLException("Error closing the result set while" + performedAction);
            }
            try {
                preparedStatement.close();
            }catch (Exception e) {
                throw new SQLException("Error closing the statement while" + performedAction);
            }
        }

        // Call Product DAO to get the name of each product in the list
        for(int i = 0; i < temp.size(); i++){
            // Grab a ProductDAO
            ProductDAO pDAO = new ProductDAO(connection);
            // Grab code of the product from the Bean
            String productCode = temp.get(i).getCode();
            // Query the name
            temp.get(i).setName(pDAO.getProductName(productCode));
        }

        // Check if there are 5 products: if not add products from a default category
        if(temp.size() < 5){
            temp = addFromDefault(temp);
        }
        homepage.setLastFive(temp);

        return homepage;
    }

    private List<Product> addFromDefault(List<Product> temp) throws SQLException {

        Random random = new Random();

        // Choose randomly the number of the row to get with the query
        int productsToAdd = 5 - temp.size();
        List<Integer> keys = new ArrayList<>();
        for(int i = 0; i < productsToAdd; i++){
            int randomNumber = random.nextInt(5) + 1;
            if(!keys.contains(randomNumber) && temp.stream().map(Product::getCode).noneMatch(x -> x.equals(String.valueOf(randomNumber)))){
                keys.add(randomNumber);
            } else {
                System.out.println(randomNumber);
                i--;
            }
        }

        // Do the query and add the products
        for(Integer key : keys){
            ProductDAO pDAO = new ProductDAO(connection);
            Product current;
            current = pDAO.getDefaultProduct(key);
            temp.add(current);
        }

        return temp;

    }

}