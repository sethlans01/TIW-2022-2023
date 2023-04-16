package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.Homepage;
import beans.Product;

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
                homepage.setName(resultSet.getString("Utente"));
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
        for (Product element : temp) {
            // Grab a ProductDAO
            ProductDao pDAO = new ProductDao(connection);
            // Grab code of the product from the Bean
            String productCode = element.getCode();
            // Query the name
            element.setName(pDAO.getProductName(productCode));
        }
        homepage.setLastFive(temp);

        return homepage;
    }

}
