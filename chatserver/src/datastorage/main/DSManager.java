package datastorage.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import networking.logger.Logger;

import java.sql.*;

public class DSManager
{
	private Connection connect = null;

	private static DSManager instance;

	public DSManager()
	{
		try
		{
			String host = "jdbc:derby://localhost:3306/chatclient";
			String username = "root";
			String password = null;
			connect = DriverManager.getConnection(host, username, password);
		} catch (SQLException e)
		{
			Logger.info("DSMANAGER", "COULD NOT AQUIRE CONNECTION.");
			Logger.error(e);
		}
		if (instance != null)
		{
			throw new RuntimeException();
		} else
		{
			instance = this;
		}
	}

	public boolean validateUser(String username, String password) throws SQLException
	{
		PreparedStatement preparedStatement = connect
				.prepareStatement("select count(*) as recordcount from users.users where MYUSER = ? AND PASSWORD = ?");
		preparedStatement.setString(1, username);
		preparedStatement.setString(2, password);
		ResultSet rs = preparedStatement.executeQuery();
		rs.next();
		return rs.getInt("recordcount") != 0;
	}

	private void writeResultSet(ResultSet rs) throws SQLException
	{
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (rs.next())
		{
			for (int i = 1; i <= columnsNumber; i++)
			{
				if (i > 1)
					System.out.print(",  ");
				String columnValue = rs.getString(i);
				System.out.print(columnValue + " " + rsmd.getColumnName(i));
			}
			System.out.println("");
		}
	}

	public static DSManager getInstance()
	{
		return instance;
	}

}
