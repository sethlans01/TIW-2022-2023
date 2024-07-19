package dao;

import beans.CartedProduct;
import beans.OrderDetails;
import beans.OrderedProduct;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrdersDAO {

    private Connection connection;

    public OrdersDAO(Connection connection){
        this.connection = connection;
    }

    public List<OrderDetails> produceOrderList(String email) throws SQLException{

        // Create a list of order details made by the requested user
        List<OrderDetails> result = new ArrayList<>();

        // Create base structure of the query
        String performedAction = " fetching order details";
        String query = "SELECT * FROM Ordini WHERE Utente = ? ORDER BY DataSpedizione DESC";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                // Create a new OrderDetails object
                OrderDetails currentOrderDetails = new OrderDetails();

                // Fill in the object with the data from the db
                currentOrderDetails.setOrderNumber(resultSet.getString("Codice"));
                String streetName = resultSet.getString("Via");
                int streetNumber = resultSet.getInt("NumeroCivico");
                String city = resultSet.getString("Città");
                int CAP = resultSet.getInt("CAP");
                String province = resultSet.getString("Provincia");
                String address = streetName + " " + streetNumber + ", " + city + ", " + CAP  + " " + province;
                currentOrderDetails.setAddress(address);
                // Set temporarily the seller code as the seller name, in order tp substitute it later
                currentOrderDetails.setSellerName(resultSet.getString("Fornitore"));
                currentOrderDetails.setTotalDue(resultSet.getFloat("Costo"));
                currentOrderDetails.setShippingDate(resultSet.getDate("DataSpedizione"));

                // Add the object to the result
                result.add(currentOrderDetails);
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

        // For each order detail set seller name
        for(OrderDetails orderDetails : result){
            // Instantiate a SupplierDAO
            SupplierDao supplierDao = new SupplierDao(connection);
            // Fetch the username from the db and set it
            orderDetails.setSellerName(supplierDao.findSupplierName(orderDetails.getSellerName()));
        }

        // For each order detail, fetch the list of the ordered products
        for(OrderDetails orderDetails : result){
            try{
                orderDetails.setOrderedProduct(fetchOrderedProductList(orderDetails.getOrderNumber(), email));
            } catch (SQLException e){
                orderDetails.setOrderedProduct(new ArrayList<>());
            }
        }

        return result;

    }

    private List<OrderedProduct> fetchOrderedProductList(String orderNumber, String email) throws SQLException{

        // Create a list of OrderedProduct
        List<OrderedProduct> result = new ArrayList<>();

        // Create base structure of the query
        String performedAction = " fetching ordered products list";
        String query = "SELECT * FROM ProdottiOrdinati WHERE Utente = ? AND Ordine = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, orderNumber);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                // Create an OrderedProduct object
                OrderedProduct orderedProduct = new OrderedProduct();

                // Fill in the objects with the data from the db
                orderedProduct.setProductCode(resultSet.getString("Prodotto"));
                orderedProduct.setQuantity(resultSet.getInt("Quantità"));

                // Add the object to the result
                result.add(orderedProduct);
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

        // For each ordered product fetch the name of the product
        for(OrderedProduct orderedProduct : result){

            // Create a ProductDAO
            ProductDAO productDAO = new ProductDAO(connection);

            try{
                orderedProduct.setProductName(productDAO.getProductName(orderedProduct.getProductCode()));
            } catch (SQLException e){
                orderedProduct.setProductName(" ");
            }

        }

        return result;
    }

    public String getNewOrderCode(String email) throws SQLException{

        int orderNumber = 0;

        // Create base structure of the query
        String performedAction = " generating order code";
        String query = "SELECT COUNT(*) FROM Ordini WHERE Utente = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                orderNumber = resultSet.getInt(1);
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

        return String.valueOf(orderNumber+1);

    }

    public void addOrder(String orderCode, String sellerCode, float orderCost, Date orderDate, String streetName,
                         int streetNumber, String city, String province, int CAP, String user,
                         List<CartedProduct> productsToAdd) throws SQLException{

        // Add a tuple to the Orders table
        // Create base structure of the query
        String performedAction = " adding an order details to the db";
        String query = "INSERT INTO Ordini VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = null;

        try {
            connection.setAutoCommit(false);

            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, orderCode);
            preparedStatement.setString(2, sellerCode);
            preparedStatement.setFloat(3, orderCost);
            preparedStatement.setDate(4, orderDate);
            preparedStatement.setString(5, streetName);
            preparedStatement.setInt(6, streetNumber);
            preparedStatement.setString(7, city);
            preparedStatement.setString(8, province);
            preparedStatement.setInt(9, CAP);
            preparedStatement.setString(10, user);
            preparedStatement.execute();

            for(CartedProduct cp : productsToAdd){
                query = "INSERT INTO ProdottiOrdinati VALUES(?, ?, ?, ?)";
                preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, orderCode);
                preparedStatement.setString(2, cp.getProductCode());
                preparedStatement.setInt(3, cp.getQuantity());
                preparedStatement.setString(4, user);
                preparedStatement.execute();
            }

            connection.commit();

            }catch(SQLException e) {
            connection.rollback();
            throw new SQLException("Error accessing the DB when" + performedAction);
        }finally {
            connection.setAutoCommit(true);
            try {
                preparedStatement.close();
            }catch (Exception e) {
                throw new SQLException("Error closing the statement when" + performedAction);
            }
        }
    }

}