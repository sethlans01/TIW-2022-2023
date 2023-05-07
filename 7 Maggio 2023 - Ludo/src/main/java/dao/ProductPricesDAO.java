package dao;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProductPricesDAO {

    private Connection connection;

    public ProductPricesDAO(Connection connection){
        this.connection = connection;
    }

    public float getProductPrice(String sellerName, String productCode) throws SQLException {

        // Set the base structure of the SQL query
        String performedAction = " extracting a product's price";
        String query = "SELECT Prezzo FROM PrezziProdotti WHERE Fornitore = ? AND Prodotto = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        float resultValue = 0;

        try {
            // Prepare and execute the query
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, sellerName);
            preparedStatement.setString(2, productCode);
            resultSet = preparedStatement.executeQuery();

            // Parse the results
            while(resultSet.next()) {
                resultValue = resultSet.getFloat("Prezzo");
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
        return resultValue;
    }
}