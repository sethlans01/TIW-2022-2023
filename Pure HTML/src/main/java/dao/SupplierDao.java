package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import beans.Supplier;
import beans.Ship;


public class SupplierDao{

    private Connection connection;

    public SupplierDao(Connection connection) {
        this.connection = connection;
    }
    
    //Method to find all suppliers selling a product using the product code

    public List<Supplier> findSuppliers(String productCode) throws SQLException{
        List<Supplier> suppliers = new ArrayList<>();
        String performedAction = " finding a supplier by product code";
        String query = "SELECT * FROM PrezziProdotti WHERE Prodotto = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, productCode);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                Supplier supplier = new Supplier();
                supplier.setCode(resultSet.getString("Fornitore"));
                supplier.setCost(Float.toString(resultSet.getFloat("Prezzo")));
                suppliers.add(supplier);
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

        return suppliers;

    }
    
    //Method to find supplier name using supplier code

    public String findSupplierName(String code) throws SQLException{
        String name = new String();
        String performedAction = " finding a supplier name by code";
        String query = "SELECT Nome FROM Fornitore WHERE Codice = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, code);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                name = (resultSet.getString("Nome"));
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

        return name;
    }

    //Method to find the supplier score using supplier code
    
    public String findSupplierScore(String code) throws SQLException{
        String score = new String();
        String performedAction = " finding a supplier score by code";
        String query = "SELECT Valutazione FROM Fornitore WHERE Codice = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, code);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                score = (Integer.toString(resultSet.getInt("Valutazione")));
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

        return score;
    }
    
    //Method to find suppliers shipping policies using supplier code

    public List<Ship> findSupplierShips(String code) throws SQLException{
        List<Ship> policies = new ArrayList<>();
        String performedAction = " finding a supplier shipping info by code";
        String query = "SELECT * FROM PoliticaSpedizione WHERE Fornitore = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, code);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                Ship policy = new Ship();
                policy.setMin(Integer.toString(resultSet.getInt("Minimo")));
                policy.setMax(Integer.toString(resultSet.getInt("Massimo")));
                policy.setCost(Float.toString(resultSet.getFloat("Prezzo")));
                policy.setGratis(Integer.toString(resultSet.getInt("NoMax")));
                policy.setMinGrat(Float.toString(resultSet.getFloat("SogliaGratuità")));
                policies.add(policy);
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

        return policies;
    }
    
    //Method to check if supplier exists using supplier code and name
    
    public boolean seeSupplier(String code, String name) throws SQLException{
    	String performedAction = "Seeing if supplier exists";
    	String query = "SELECT * FROM Fornitore WHERE Codice = ? AND Nome = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, code);
            preparedStatement.setString(2, name);
            resultSet = preparedStatement.executeQuery();

            if(!resultSet.isBeforeFirst()) {
            	return false;
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
        return true;
    }  
    
}