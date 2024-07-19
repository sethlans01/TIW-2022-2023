package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CartCheckerDAO {

    private Connection connection;

    public CartCheckerDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean checkProductCodeAndName(String productCode, String productName) throws SQLException {

        boolean result = false;

        String performedAction = " checking product code and product name of a carted product";
        String query = "SELECT COUNT(*) FROM Prodotto WHERE Codice = ? AND Nome = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, productCode);
            preparedStatement.setString(2, productName);
            resultSet = preparedStatement.executeQuery();
            int amount = 0;

            while(resultSet.next()) {
                amount = resultSet.getInt(1);
            }
            
            if(amount > 0){
                result = true;
            }

        }catch(SQLException e) {
            throw new SQLException("Error accessing the DB when" + performedAction);
        }finally {
            try {
                resultSet.close();
            }catch (Exception e) {
                throw new SQLException("Error closing the result set when" + performedAction);
            }
            try {
                preparedStatement.close();
            }catch (Exception e) {
                throw new SQLException("Error closing the statement when" + performedAction);
            }
        }

        return result;

    }

    public boolean checkSellerCodeAndPrice(String sellerCode, float price, String productCode) throws SQLException{

        boolean result = false;

        String performedAction = " checking seller code and product price of a carted product";
        String query = "SELECT COUNT(*) FROM PrezziProdotti WHERE Prodotto = ? AND Fornitore = ? AND Prezzo = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, productCode);
            preparedStatement.setString(2, sellerCode);
            preparedStatement.setFloat(3, price);
            resultSet = preparedStatement.executeQuery();
            int amount = 0;

            while(resultSet.next()) {
                amount = resultSet.getInt(1);
            }

            if(amount > 0){
                result = true;
            }

        }catch(SQLException e) {
            throw new SQLException("Error accessing the DB when" + performedAction);
        }finally {
            try {
                resultSet.close();
            }catch (Exception e) {
                throw new SQLException("Error closing the result set when" + performedAction);
            }
            try {
                preparedStatement.close();
            }catch (Exception e) {
                throw new SQLException("Error closing the statement when" + performedAction);
            }
        }

        return result;



    }
}