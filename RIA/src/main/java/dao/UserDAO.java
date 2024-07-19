package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import beans.Address;
import beans.Homepage;
import beans.User;

public class UserDAO {

    private Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public User findUser(String email, String password) throws SQLException{
        User user = null;
        String performedAction = " finding a user by email and password";
        String query = "SELECT * FROM Utente WHERE Email = ? AND Password = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                user = new User();
                user.setName(resultSet.getString("Nome"));
                user.setEmail(resultSet.getString("Email"));
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
        return user;
    }

    public User getUserByEmail(String email) throws SQLException{
        User user = null;
        String performedAction = " finding a user by email";
        String query = "SELECT * FROM Utente WHERE Email = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                user = new User();
                user.setName(resultSet.getString("Nome"));
                user.setEmail(resultSet.getString("Email"));
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
        return user;
    }

    public Homepage getFullName(Homepage currentHomepageBean) throws SQLException{

        String email = currentHomepageBean.getEmail();
        String performedAction = " finding user's full name";
        String query = "SELECT Nome, Cognome FROM Utente WHERE Email = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                String name = resultSet.getString("Nome");
                String surname = resultSet.getString("Cognome");
                String fullName = name + " " + surname;
                currentHomepageBean.setFullName(fullName);
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
        return currentHomepageBean;
    }

    public Address getUserAddress(String user) throws SQLException{

        Address result = new Address();

        String performedAction = " finding user's address";
        String query = "SELECT * FROM Utente WHERE Email = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                result.setStreetName(resultSet.getString("Via"));
                result.setStreetNumber(resultSet.getInt("NumeroCivico"));
                result.setCity(resultSet.getString("Città"));
                result.setProvince(resultSet.getString("Provincia"));
                result.setCAP(resultSet.getInt("CAP"));
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