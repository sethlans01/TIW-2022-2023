package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import beans.Homepage;
import beans.Product;

public class UpdateLastDao{

    private Connection connection;

    public UpdateLastDao(Connection connection){
        this.connection = connection;
    }

    public void updateLastFive(String email, String productCode) throws SQLException{

        LastFiveDAO lastFiveDao = new LastFiveDAO(connection);
        Homepage lastFiveOldBean;

        try {
            lastFiveOldBean = lastFiveDao.getHomepageBean(email);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        List<Product> lastFiveOld = lastFiveOldBean.getLastFive();

        for(int i = 0; i < 5;i++) {

            if(lastFiveOld.get(i).getCode().equals(productCode)) {

                return;

            }

        }

        List<Product> lastFiveNew = new ArrayList<>();

        Product helper = new Product();
        helper.setCode(productCode);
        lastFiveNew.add(helper);

        for(int i = 0; i < 4; i++) {
            lastFiveNew.add(lastFiveOld.get(i));
        }

        String performedAction = "Updating last 5 seen products";
        String query = "UPDATE UltimiCinque SET Prod";
        String endQuery = " = ? WHERE Utente = ?";
        PreparedStatement preparedStatement = null;

        try {

            for(int i = 0; i < 5; i++) {

                preparedStatement = connection.prepareStatement(query + Integer.toString(i+1) + endQuery);
                preparedStatement.setString(1, lastFiveNew.get(i).getCode());
                preparedStatement.setString(2, email);
                preparedStatement.executeUpdate();

            }


        }catch(SQLException e) {

            throw new SQLException("Error accessing the DB while" + performedAction);

        }finally {

            try {
                preparedStatement.close();
            }catch (Exception e) {
                throw new SQLException("Error closing the statement while" + performedAction);
            }

        }

    }

}