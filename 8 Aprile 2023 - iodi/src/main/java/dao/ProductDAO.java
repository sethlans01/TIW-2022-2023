package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {

    private Connection connection;

    public ProductDAO(Connection connection){
        this.connection = connection;
    }

    public String getProductName(String code) throws SQLException{

        // Instantiate a homepage bean
        String productName = null;
        // Set the base structure of the SQL query
        String performedAction = " extracting product's name";
        String query = "SELECT Nome FROM Prodotto WHERE Codice = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Prepare and execute the query
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, code);
            resultSet = preparedStatement.executeQuery();

            // Parse the results and create a string
            while(resultSet.next()) {
                productName = resultSet.getString("Nome");
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
        return productName;

    }

}
