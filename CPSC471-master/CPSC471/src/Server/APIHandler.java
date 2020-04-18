package Server;
/***
 * The purpose of this class to interpret the information provided by each API endpoint and translate that into the store procedures that the MySQL server can understand
 */
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


public class APIHandler implements HttpHandler {	
	public APIHandler() {
	}
	
	@Override
	public void handle(HttpExchange HE) throws IOException {
		boolean is_return = true;
		
		// parse request
		String SQLCommand;		//The SQLCommand, generally in format "call StoreProcedureName(args)"
		int resultSize;			//number of columns you want back
		String response = "";	//response you will send back to the user using the API
		
		//This code will parse the information given in the URL. Query will contain what comes after /APICall
		URI requestedUri = HE.getRequestURI();
		String query = requestedUri.getRawQuery();
		
		
		Connection myConn = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://127.0.0.2:3306/mydb","root", "password");	//creates MySQL connection at specific IP, schema, username, and password
		} catch (SQLException e) {e.printStackTrace();}
		

		String[] data;	//split along ',' so we can see the specific procedure and the arguments
		data = parseURL(query);
		
		System.out.println(data[1]);
		
		if(query.contains("All")) {			//If the query contains ALL meaning it is "GetAllReservation" or "GetAllVehicle" it will run this code below
			String ans = "", temp = null;
			int i = 1;
			
			//This funciton will continuously call the function called multiple. In doing so, it will recieve information about each individual tuple via a string
			//and then will combine that information into ans to create one string will all tuple information. This will stop when multiple stops sending back actual information
			//i.e when the response from multiple = ""
			while(temp != "") {
				temp = "";
				temp = multiple(data[0], i, Integer.parseInt(data[1]), myConn); //data[0] = name of procedure, arg2 = number of columns desired, myConn is connection object for database
				ans += temp + "\n";
				i++;
			}
			response = ans; //Once the process is complete, response (which is the data that will be sent back) is equated to the data we recieved in the answer string
			
		}
		//If All is not present in the first argument of the API call, then this code will run
		else {
			//This section of code will check what the SQL command recieved in the URL is, and then will match the required arguments for the stored procedure with the arguments
			//given in the URL. 
			//the Data variable is simply the URL split along "," which was done above
			//The is_return variable simply denotes if there is an expected return from the SQL procedure or not. This is handled below.
			if(query.contains("PostReservation")) {
				
				SQLCommand = "call " + data[0] + "(" + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + data[6] + "," + data[7] + ")";
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
						      + "," + data[9] + "," + data[10] + "," + data[11] + "," + data[12] + "," + data[13] + "," + data[14] + "," + data[15] + "," + data[16]+ ")";
				is_return = false;
			}
			else if (query.contains("DeleteReservation")) {
				SQLCommand = "call " + data[0] + "(" + data[1] + ")";
				System.out.println(SQLCommand);
				is_return = false;
			}
			//These two last if statements allow a user to input -1 as the second argument in their URL to specify if there will be no arguments passed to the SQL stored procedure
			//A person could do that by providing a -1 in the second argument spot of their URL
			else if(!data[1].equals("-1")) {						
				SQLCommand = "call " + data[0] + "(" + data[1] + ")";
			}
			else {
				SQLCommand = "call " + data[0] + "()";
			}
			
			
			
			
			try {
				Statement myStmt = myConn.createStatement();		//these two functions will send the SQLCommand created above to the DBMS to use its stored procedure
				ResultSet myRs = myStmt.executeQuery(SQLCommand);
				
				//This section below if for recieving information back from the DBMS, if none was required it is skiped via this if statement
				if(is_return) {	
					int i = 1;										//these acts as a cursor to indicate which column the while loop is currently on
					resultSize = Integer.parseInt(data[2]) + 1;		//result size is the number of columns you want back. In testing it was determined you needed to add one to recieve
																	//back your actual desired amount
					
					//This statement will run through the response from the query sent from above to capture the data in each column (column is tracked by var 'i') and then add that to the response string  
					while(myRs.next()) {								
						while(i<resultSize) {
							response += myRs.getString(i) +", ";
							i++;
						}
					}
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(!is_return) {			//If no response was expected, a response of 200 OK will be sent back 
			response = "200 OK";
		}
		//Code responsible for formating and sending the response string back to the client who originally used the API
		HE.sendResponseHeaders(200, response.length());
		OutputStream os = HE.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();
		
	}
	
	/**
	 * This function is used solely for procedures that request all the data in a table. This function is run many times while completing that task
	 * @param SQLCommand - what is the storedprocedure?
	 * @param row - current row
	 * @param resultSize- Number of columns to be returned
	 * @param myConn- MySQL connection object
	 * @return
	 */
	public String multiple(String SQLCommand, int row, int resultSize, Connection myConn ) {
		String response = "";

		try {
			//this if, else determined which 'support' stored procedure will be used (namely GR or GV, former for getting reservations, latter for getting vehicles)
			if(SQLCommand.contains("GetAllReservation")) {			
				SQLCommand = "GR";
			}
			else {
				SQLCommand = "GV";
			}
		
			SQLCommand = "call " + SQLCommand + "(" + row +")";		//Formats the SQL query to be sent to the database
			
			//Send SQL command to the server
			Statement myStmt = myConn.createStatement();
			ResultSet myRs = myStmt.executeQuery(SQLCommand);
			
	
			int i = 1; //i is a column tracker
			
			//This statement will run through the response from the query sent from above to capture the data in each column (column is tracked by var 'i') and then add that to the response string 
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
	
	/**
	 * This function will take the relevant information out of the URL and format it into the response array
	 * @param query
	 * @return String[]: response
	 */
	public String [] parseURL(String query) {
		String response[];
		response = query.split("&");
		//This loop splits the url by the "=" to extract the data that will be sent to the SQL server above
		for(int i = 1; i<response.length; i++) {
			response[i] = (response[i].split("="))[1];
		}
		return response;
	}
}
