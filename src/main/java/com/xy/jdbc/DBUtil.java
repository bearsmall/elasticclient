package com.xy.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DBUtil数据库工具类
 * @author xiong
 * 
 */
public class DBUtil {
	private static String url = "jdbc:mysql://127.0.0.1:3306/snykdb?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT"; // 数据库连接字符串
	private static String user = "root";// 数据库账号
	private static String password = "12345678";// 数据库密码
//
//	static {
//		try {
//			Class.forName("com.mysql.jdbc.Driver");// 加载Driver驱动类，放在静态代码块中,随类的加载而加载，原因是只用加载一次就行
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * 获得数据库连接对象：Connection
	 * @return 数据库连细节对象
	 */
	public static Connection getConnection() {
		Connection conn = null; // 声明放在try catch外部
		try {
			conn = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 关闭数据库连接对象
	 * @param con 数据库连接对象
	 */
	public static void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭PreparedStatement
	 * @param ps PreparedStatement对象
	 */
	public static void closePreparedStatement(PreparedStatement ps) {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭ResultSet
	 * @param rs ResutlSet对象
	 */
	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**逐一关闭数据库连接资源
	 * @param con Connection对象
	 * @param ps PreparedStatement对象
	 * @param rs ResultSet对象
	 */
	public static void closeAll(Connection con, PreparedStatement ps,
			ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static boolean exist(String url){
		boolean flag = false;
		String sql = "select * from maven where url=?";
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1,url);
			rs = ps.executeQuery();
			while (rs.next()){
				flag = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeAll(connection,ps,null);
		return flag;
	}

	public static int insert(String url,String name){
		int i = 0;
		String sql = "insert into maven(url,name) values(?,?);";
		Connection connection = getConnection();
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1,url);
			ps.setString(2,name);
			i = ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		closeAll(connection,ps,null);
		return i;
	}
}