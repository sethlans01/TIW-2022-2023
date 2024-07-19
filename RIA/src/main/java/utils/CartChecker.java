package utils;

import beans.Cart;
import beans.CartedProduct;
import dao.CartCheckerDAO;
import dao.SupplierDAO;

import java.sql.Connection;
import java.sql.SQLException;

public class CartChecker {

    public static boolean checkCart(Cart cart, Connection connection){
        // If cart is empty, the cart is not valid
        if(cart == null){
            return false;
        }

        // Create the necessary DAO
        SupplierDAO supplierDAO = new SupplierDAO(connection);

        for(String key : cart.getCart().keySet()){

            // Check if the key of the entry is the code of an existing supplier
            boolean supplierExists = false;
            try{
                supplierExists = supplierDAO.checkSupplierExistence(key);
            } catch(SQLException ignored){}
            if(!supplierExists){
                return false;
            }

            // If the check is ok, start checking the products
            for(CartedProduct cp : cart.getCart().get(key)){
                if(!checkCartedProduct(cp, connection)){
                    return false;
                }
            }
        }

        // If all checks are passed, the cart is legal
        return true;
    }

    private static boolean checkCartedProduct(CartedProduct cp, Connection connection) {
        // If the carted product is null, the product is not valid
        if(cp == null){
            return false;
        }

        // If the quantity is 0 or negative the carted product is not legal
        if(cp.getQuantity() <= 0){
            return false;
        }

        // Create the necessary DAO
        CartCheckerDAO cartCheckerDAO = new CartCheckerDAO(connection);

        // Check product code and product name
        boolean check = false;
        try{
            check = cartCheckerDAO.checkProductCodeAndName(cp.getProductCode(), cp.getProductName());
        } catch (SQLException ignored){}
        // If one of the two is wrong, the product is not legal
        if(!check){
            return false;
        }

        // Check seller code and price of product
        check = false;
        try{
            check = cartCheckerDAO.checkSellerCodeAndPrice(cp.getSellerCode(), cp.getPrice(), cp.getProductCode());
        } catch(SQLException ignored) {}

        return check;

    }

}