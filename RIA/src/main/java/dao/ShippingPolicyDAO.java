package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShippingPolicyDAO {

    private Connection connection;

    public ShippingPolicyDAO(Connection connection){
        this.connection = connection;
    }

    public float getFreeShippingThreshold(String id) throws SQLException {

        float result = 0.0f;

        // Set the base structure of the SQL query
        String performedAction = " fetching free delivery threshold";
        String query = "SELECT * FROM PoliticaSpedizione WHERE Fornitore = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        try {
            // Prepare and execute the query
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, id);
            resultSet = preparedStatement.executeQuery();

            // Parse the results and create a OrderDetails object for each entry
            while(resultSet.next()) {
                result = resultSet.getFloat("SogliaGratuità");
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

        return result;

    }

    public float calculateShippingFee(int productAmount, String sellerId) throws SQLException{

        float result = 0.0f;

        // Set the base structure of the SQL query
        String performedAction = " calculating shipping fees";
        String query = "SELECT * FROM PoliticaSpedizione WHERE Fornitore = ? AND ((NoMax = true AND ? >= Minimo) OR " +
                "(NoMax = false AND ? >= Minimo AND ? <= Massimo))";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Prepare and execute the query
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, sellerId);
            preparedStatement.setInt(2, productAmount);
            preparedStatement.setInt(3, productAmount);
            preparedStatement.setInt(4, productAmount);
            resultSet = preparedStatement.executeQuery();

            // Parse the results and create a OrderDetails object for each entry
            while(resultSet.next()) {
                result = resultSet.getFloat("Prezzo");
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

        return result;


    }
}