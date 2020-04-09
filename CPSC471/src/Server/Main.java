package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
	public static int port = 9002;
	public static void main(String[] args) {
//		// start http server
//		SimpleHttpServer httpServer = new SimpleHttpServer();
//		httpServer.Start(port);
		
		// start https server
		SimpleHttpServer httpsServer = new SimpleHttpServer();
		httpsServer.Start(port);
		
//		System.out.println(System.getProperty("user.dir"));
//		System.out.println(Main.class.getClassLoader().getResource("").getPath());
		
	}
}

/*		try {
Connection myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila","root", "password");
//insertUser();

Statement myStmt = myConn.createStatement();
ResultSet myRs = myStmt.executeQuery("select * from customer");

while(myRs.next()) {
	System.out.println(myRs.getString("last_name"));
}

} catch (SQLException e) {
e.printStackTrace();
}

}*/