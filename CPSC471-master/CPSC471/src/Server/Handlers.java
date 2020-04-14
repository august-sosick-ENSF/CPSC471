package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Handlers {


	public static class RootHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange he) throws IOException {
			String response = "<h1>Server start success if you see this message</h1>" + "<h1>Port: " + Main.port + "</h1>";
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public static class EchoHeaderHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange he) throws IOException {
			Headers headers = he.getRequestHeaders();
			Set<Map.Entry<String, List<String>>> entries = headers.entrySet();
			String response = "";
			for (Map.Entry<String, List<String>> entry : entries)
				response += entry.toString() + "\n";
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
		}
	}

	public static class EchoGetHandler implements HttpHandler {
		
		
		
		public EchoGetHandler() {
		}
		
		@Override
		public void handle(HttpExchange he) throws IOException {
			boolean is_return = true;
			
			// parse request
			String SQLCommand;
			String SQLRequest;
			int resultSize;
			String response = "";
			
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			URI requestedUri = he.getRequestURI();
			String query = requestedUri.getRawQuery();
			parseQuery(query, parameters);
			
			System.out.println(query);
			System.out.println(parameters);
			
			String[] data = query.split(",");
			
			//This will add the parameters in the URL to the command sent to the SQL server
			
			
			if(query.contains("All")) {
				System.out.println("testtttt");
				String ans = "", temp = null;
				Connection myConn = null;
				try {
					myConn = DriverManager.getConnection("jdbc:mysql://127.0.0.2:3306/mydb","root", "password");
				} catch (SQLException e) {e.printStackTrace();}
				
				int i = 1;
				System.out.println();
				
				while(temp != "") {
					temp = "";
					temp = multiple(data[0], i, Integer.parseInt(data[2]), myConn);
					ans += temp + "\n";
					i++;
				}
				response = ans;
				System.out.println(ans);
			}
			else {
				if(query.contains("PostReservation")) {
					
					SQLCommand = "call " + data[0] + "(" + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + data[6] + "," + data[7] + ")";
					System.out.println(SQLCommand);
					is_return = false;
				}
				else if (query.contains("PostBill")) {
					SQLCommand = "call " + data[0] + "(" + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + data[6] + "," + data[7] + "," + data[8] 
						      + "," + data[9] + ")";
					is_return = false;
				}
				else if (query.contains("PutEmployee")) {
					
					SQLCommand = "call " + data[0] + "(" + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + data[6] + ")";
					is_return = false;
				}
				else if (query.contains("PutVehicle")) {
					SQLCommand = "call " + data[0] + "(" + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + data[6] + "," + data[7] + "," + data[8] 
							      + "," + data[9] + "," + data[10] + "," + data[11] + "," + data[12] + ")";
					is_return = false;
				}
				else if (query.contains("DeleteReservation")) {
					SQLCommand = "call " + data[0] + "(" + data[1] + ")";
					is_return = false;
					System.out.println(SQLCommand);
				}
				else if(!data[1].equals("-1")) {
					SQLCommand = "call " + data[0] + "(" + data[1] + ")";
				}
				else {
					SQLCommand = "call " + data[0] + "()";
				}
				
				
				resultSize = Integer.parseInt(data[2]) + 1;
				
				
				System.out.println(SQLCommand);
				
				
				try {
					Connection myConn = DriverManager.getConnection("jdbc:mysql://127.0.0.2:3306/mydb","root", "password");
					//insertUser();
				
					Statement myStmt = myConn.createStatement();
					ResultSet myRs = myStmt.executeQuery(SQLCommand);
					
					if(is_return) {		//only runs if return is expected
					
						int i = 1;
					
						while(myRs.next()) {
							while(i<resultSize) {
							System.out.println(myRs.getString(i));
							response += myRs.getString(i) +", ";
							i++;
							}
						}
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(!is_return) {
				response = "success";
			}
			//for (String key : parameters.keySet())
				//response += key + " = " + parameters.get(key) + "\n";
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();
			
		}
		
		//Backend for the collect all info stored procedures.
		public String multiple(String SQLCommand, int row, int resultSize, Connection myConn ) {
			String response = "";

			try {
				if(SQLCommand.contains("GetAllReservation")) {
					SQLCommand = "GR";
				}
				else {
					SQLCommand = "GV";
				}
				//insertUser();
			
				//SQLCommand = "call " + SQLCommand + "(" + myRs.getRowId(row) +")";
				SQLCommand = "call " + SQLCommand + "(" + row +")";
				System.out.println(SQLCommand);
				
				
				Statement myStmt = myConn.createStatement();
				ResultSet myRs = myStmt.executeQuery(SQLCommand);
				
		
				int i = 1;
				
				while(myRs.next()) {
					while(i<resultSize + 1) {
					System.out.println(myRs.getString(i));
					response += myRs.getString(i) +", ";
					i++;
					}
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return response;
		}

	}

	public static class EchoPostHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange he) throws IOException {
			System.out.println("Served by /echoPost handler...");
			// parse request
			Map<String, Object> parameters = new HashMap<String, Object>();
			InputStreamReader isr = new InputStreamReader(he.getRequestBody(), "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String query = br.readLine();
			parseQuery(query, parameters);
			// send response
			String response = "";
			
			for (String key : parameters.keySet())
				response += key + " = " + parameters.get(key) + "\n";
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.toString().getBytes());
			os.close();

		}
	}

	@SuppressWarnings("unchecked")
	public static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

		if (query != null) {
			String pairs[] = query.split("[&]");

			for (String pair : pairs) {
				String param[] = pair.split("[=]");

				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
				}

				if (param.length > 1) {
					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
				}

				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if (obj instanceof List<?>) {
						List<String> values = (List<String>) obj;
						values.add(value);
					} else if (obj instanceof String) {
						List<String> values = new ArrayList<String>();
						values.add((String) obj);
						values.add(value);
						parameters.put(key, values);
					}
				} else {
					parameters.put(key, value);
				}
			}
		}
	}

}
