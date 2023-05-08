package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.Product;

public class ProductDAO{

    private Connection connection;

    public ProductDAO(Connection connection) {
        this.connection = connection;
    }

    //Method  to find product information by code
    
    public Product findProduct(String code) throws SQLException{
        Product product = null;
        String performedAction = " finding information about product by code";
        String query = "SELECT * FROM Prodotto WHERE Codice = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, code);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                product = new Product();
                product.setName(resultSet.getString("Nome"));
                product.setCode(resultSet.getString("Codice"));
                product.setDescription(resultSet.getString("Descrizione"));
                product.setCategory(resultSet.getString("Categoria"));
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

        return product;

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
    
    //Method to find product if key is contained in name or description

    public List<Product> findProducts(String key) throws SQLException{
        List <Product> products = new ArrayList<>();
        String performedAction = " finding a product by keyword";
        String query = "SELECT * FROM Prodotto WHERE Nome LIKE ? OR Descrizione LIKE ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "%" + key + "%");
            preparedStatement.setString(2, "%" + key + "%");
            resultSet = preparedStatement.executeQuery();


            while(resultSet.next()) {
                Product product = new Product();
                product.setName(resultSet.getString("Nome"));
                product.setCode(resultSet.getString("Codice"));
                products.add(product);
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
        return products;

    }
    
    //Method to find minimum cost of product by product code

    public String findMinCost(String code) throws SQLException{

        String minPrice = null;
        String performedAction = " finding a product minPrice by code";
        String query = "SELECT * FROM PrezziProdotti WHERE Prezzo = (SELECT Min(Prezzo) FROM PrezziProdotti WHERE Prodotto = ?)";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, code);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                minPrice = Float.toString(resultSet.getFloat("Prezzo"));
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
        return minPrice;
    }


    public Product getDefaultProduct(Integer key) throws SQLException {

        // Result variable
        Product result = new Product();
        result.setCode(String.valueOf(key));

        // Set the base structure of the SQL query
        String performedAction = " adding default product";
        String query = "SELECT Nome FROM Prodotto WHERE Codice = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Prepare and execute the query
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, String.valueOf(key));
            resultSet = preparedStatement.executeQuery();

            // Parse the results and create a string
            while(resultSet.next()) {
                result.setName(resultSet.getString("Nome"));
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
    
    //Method to see if products exists by code and name
    
    public boolean seeProduct(String code, String name) throws SQLException{
    	String performedAction = "Seeing if product exists";
    	String query = "SELECT * FROM Prodotto WHERE Codice = ? AND Nome = ?";
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