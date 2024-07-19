package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import beans.Supplier;
import beans.Ship;


public class SupplierDAO{
	
	private Connection connection;

	public SupplierDAO(Connection connection) {
		this.connection = connection;
	}
	
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

	public boolean checkSupplierExistence(String code) throws SQLException{

		boolean result = false;

		String performedAction = " checking if a supplier exists";
		String query = "SELECT COUNT(*) FROM Fornitore WHERE Codice = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, code);
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