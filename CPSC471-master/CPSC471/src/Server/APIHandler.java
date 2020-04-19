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

import org.omg.CORBA.portable.InputStream;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class APIHandler implements HttpHandler {	
	boolean noRequest;		//Whether or not there are paramaters in the URL
	
	public APIHandler() {
	}
	
	/**
	 * The purpose of this function is to handle all functions and incoming data in order to interface between the SQL server and API endpoints
	 * It can be considered the main running loop of this program
	 */
	@Override
	public void handle(HttpExchange HE) throws IOException {
		boolean is_return = true;										//Whether the will be a return variable
		String RequestMethod = HE.getRequestMethod().toLowerCase();
		String StoredFunction = null;									//The actual table in the URL for example, Vehicle. Later transformed to for example get AllVehicle
		String URL = null;												//URL in a String format
		
		// parse request
		String SQLCommand;		//The SQLCommand, generally in format "call StoreProcedureName(args)"
		int resultSize;			//number of columns you want back
		String response = "";	//response you will send back to the user using the API
		String[] data = new String[1];	//split along ',' so we can see the specific procedure and the arguments
		String[] JSONBody = null;
		
		
		//This code will parse the information given in the URL. Query will contain what comes after /APICall
		URI requestedURL = HE.getRequestURI();
		String query = requestedURL.getRawQuery();
		URL = requestedURL.toString();
		
		System.out.println(query);
		
		Connection myConn = null;
		try {
			myConn = DriverManager.getConnection("jdbc:mysql://127.0.0.2:3306/mydb","root", "password");	//creates MySQL connection at specific IP, schema, username, and password
		} catch (SQLException e) {e.printStackTrace();}
		
		//A function that will return the parameters, and other information stored in the URL. passed to the variable data
		data = parseURL(query,requestedURL.toString());
		
		
		//If there is no request, then thee are no arguments to be parsed, which is what the function contain here does
		if(!noRequest)
			StoredFunction = parseRequest(requestedURL, RequestMethod);

		//Will only search the JSON body object if it is not a Get function as get functions have all their arguments in the URL and don't need a body
		if(!RequestMethod.contentEquals("get")) { 
			 JSONBody = parseBody(HE,data[0]);			//returns the body of the request formattted correctly
		} 
		
		
		if(requestedURL.toString().contains("All")) {			//If the query contains ALL meaning it is "GetAllReservation" or "GetAllVehicle" it will run this code below
			String ans = "", temp = null;
			int i = 1;
			System.out.println("here");
			//This funciton will continuously call the function called multiple. In doing so, it will recieve information about each individual tuple via a string
			//and then will combine that information into ans to create one string will all tuple information. This will stop when multiple stops sending back actual information
			//i.e when the response from multiple = ""
			while(temp != "") {
				temp = "";
				temp = multiple(requestedURL.toString(), i, 25, myConn); 
				ans += temp + "\n";
				i++;
			}
			
			response = ans; //Once the process is complete, response (which is the data that will be sent back) is equated to the data we recieved in the answer string
			
		}
		//If All is not present in the first argument of the API call, then this code will run
		else {
			//This section of code will check what the SQL command recieved in the URL or JSON body is, and then will match the required arguments for the stored procedure with the arguments
			//given in the URL. 
			//the Data variable is simply the URL split along "," which was done above
			

			//StoredFunction contains the name of the stored function in the SQL server
			if(URL.contains("Reservation") && RequestMethod.contains("post")) {
				
				data = JSONBody;
				SQLCommand = "call " + StoredFunction + "(" + data[0] + "," + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + data[6] + ")";
				System.out.println(SQLCommand);
				is_return = false;
			
			}
			else if (URL.contains("Bill") && RequestMethod.contains("post")) {
				data = JSONBody;
				SQLCommand = "call " + StoredFunction + "(" + data[0] + "," + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + data[6] + "," + data[7] 
					      + "," + data[8] + ")";
				is_return = false;
			}
			else if (URL.contains("Employee") && RequestMethod.contains("put")) {
				data = JSONBody;
				SQLCommand = "call " + StoredFunction + "(" + data[0] + "," + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + ")";
				is_return = false;
			}
			else if (URL.contains("Vehicle") && RequestMethod.contains("put")) {
				data = JSONBody;
				SQLCommand = "call " + StoredFunction + "(" + data[0] + "," + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + data[6] + "," + data[7] 
						      + "," + data[8] + "," + data[9] + "," + data[10] + "," + data[11] + "," + data[12] + "," + data[13] + "," + data[14] + "," + data[15]+ ")";
				is_return = false;
			}
			else if (URL.contains("Reservation") && RequestMethod.contains("delete")) {
				data = JSONBody;
				SQLCommand = "call " + StoredFunction + "(" + data[0] + ")";
				System.out.println(SQLCommand);
				is_return = false;
			}
			//This last else if solely if you are searching via a unique id
			else  {	
					SQLCommand = "call " + StoredFunction + "(" + data[0] + ")";
			}

			
			SQLCommand = SQLCommand.trim();	//trimming excess spaces that sometimes arise
			
			//This section will attempt to send the query prepared above the SQL server and recieve it's response.
			try {
				Statement myStmt = myConn.createStatement();		//these two functions will send the SQLCommand created above to the DBMS to use its stored procedure
				ResultSet myRs = myStmt.executeQuery(SQLCommand);
				
				//This section below if for recieving information back from the DBMS, if none was required it is skiped via this if statement
				if(is_return) {	
					int i = 1;										//these acts as a cursor to indicate which column the while loop is currently on
					resultSize = 25;								//max columns back
					
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
		else {						//Otherwise the response data will be processed into an appropriate JSON format in the function below
			response = formatJSONResponse(response, URL, RequestMethod);
		}
	
		
		//Code responsible for formating and sending the response string back to the client who originally used the API
		HE.sendResponseHeaders(200, response.length());
		OutputStream os = HE.getResponseBody();
		os.write(response.toString().getBytes());
		os.close();
		
	}
	/**
	 * The purpose of this function is to format return data into the appropriate JSON format
	 * @param response
	 * @param URL
	 * @param RequestMethod
	 * @return	String: Contains formatted data
	 */
	public String formatJSONResponse(String response, String URL, String RequestMethod) {
		String formatted = null;
		String[] split = response.split(",");
		System.out.println(split.length);
		
		//quicly removes excess spaces
		int i = 0;
		while(i<split.length) {
			split[i] = split[i].trim();
			i++;
		}
		//Formatting GetVehicle Endpoint
		if(URL.contains("Vehicle") && RequestMethod.contains("get")) {
			
			
			formatted = "[\n" + "{\n" + "\"vehicle_ID\":\"" + split[1] + "\", \n" + "\"booking_ID\":\"" + split[2] + "\", \n"  + "\"make\":\"" + split[3] + "\", \n"  + "\"model\":\"" + split[4] + "\", \n" 
							+ "\"colour\":\"" + split[5] + "\", \n" + "\"type_ID\":\"" + split[6] + "\", \n" + "\"insurance_ID\":\"" + split[7] + "\", \n" + "\"insurer_name\":\"" + split[8] + "\", \n" 
								+ "\"cost_per_day\":\"" + split[9] + "\", \n"  + "\"coverage_type\":\"" + split[10] + "\", \n"  + "\"cur_location\":\"" + split[11] + "\", \n"  + "\"damage_history\":\"" + split[12] + "\", \n" 
										+ "\"current_condition\":\"" + split[13] + "\", \n"  + "\"maintenance_record\":\"" + split[14] + "\", \n"  + "\"rental_record\":\"" + split[15] + "\", \n"  + "\"row\":\"" + split[0] + "\", \n"
												+ "}\n" +"]\n"; 
		}
		//Formatting GetReservation Endpoint
		if(URL.contains("Reservation") && RequestMethod.contains("get") && !URL.contains("All")) {

			formatted = "[\n" + "{\n" + "\"booking_ID\":\"" + split[1] + "\", \n" + "\"drivers_license_num\":\"" + split[2] + "\", \n"  + "\"request_type_ID\":\"" + split[3] + "\", \n"  + "\"assigned_vehicle_ID\":\"" + split[4] + "\", \n" 
							+ "\"additional_drivers_license_num\":\"" + split[5] + "\", \n" + "\"bill_ID\":\"" + split[6] + "\", \n" + "\"row\":\"" + split[0] + "\", \n"
												+ "}\n" +"]\n"; 
		}
		//Formatting Get Customer Endpoint
		if(URL.contains("Customer") && RequestMethod.contains("get")) {
			
			
			formatted = "[\n" + "{\n" + "\"drivers_license_num\":\"" + split[1] + "\", \n" + "\"phone_num\":\"" + split[2] + "\", \n"  + "\"email\":\"" + split[3] + "\", \n"  + "\"row\":\"" + split[0] + "\", \n"
										+ "}\n" +"]\n"; 
		}
		//Formatting Get All Reservations endpoint
		if(URL.contains("Reservation") && RequestMethod.contains("get") && URL.contains("All")) {
			int u = 0;
		
			formatted = "[\n";
			for(int x = 0; x<7; x++) {
				int t = x;
				x = x*8;
				formatted += ("{\n" + "\"booking_ID\":\"" + split[x+1] + "\", \n" + "\"drivers_license_num\":\"" + split[x+2] + "\", \n"  + "\"request_type_ID\":\"" + split[x+3] + "\", \n"  + "\"assigned_vehicle_ID\":\"" + split[x+4] + "\", \n" 
						+ "\"additional_drivers_license_num\":\"" + split[x+5] + "\", \n" + "\"bill_ID\":\"" + split[x+6] + "\", \n" + "\"row\":\"" + split[x] + "\", \n" + "},\n"); 
				x = t;
			}
			formatted += "]\n";

		}
		//Formatting get All Vehicles endpoint
		if(URL.contains("Vehicle") && RequestMethod.contains("get") && URL.contains("All")) {
			int u = 0;
		
			formatted = "[\n";
			for(int x = 0; x<7; x++) {
				int t = x;
				x = x*8;
				formatted += ("{\n" + "\"vehicle_ID\":\"" + split[1] + "\", \n" + "\"booking_ID\":\"" + split[2] + "\", \n"  + "\"make\":\"" + split[3] + "\", \n"  + "\"model\":\"" + split[4] + "\", \n" 
						+ "\"colour\":\"" + split[5] + "\", \n" + "\"type_ID\":\"" + split[6] + "\", \n" + "\"insurance_ID\":\"" + split[7] + "\", \n" + "\"insurer_name\":\"" + split[8] + "\", \n" 
						+ "\"cost_per_day\":\"" + split[9] + "\", \n"  + "\"coverage_type\":\"" + split[10] + "\", \n"  + "\"cur_location\":\"" + split[11] + "\", \n"  + "\"damage_history\":\"" + split[12] + "\", \n" 
								+ "\"current_condition\":\"" + split[13] + "\", \n"  + "\"maintenance_record\":\"" + split[14] + "\", \n"  + "\"rental_record\":\"" + split[15] + "\", \n"  + "\"row\":\"" + split[0] + "\", \n"
										+ "},\n"); 
				x = t;
			}
			formatted += "]\n";

		}
	
		return formatted;
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
		System.out.println(SQLCommand);
		try {
			//this if, else determined which 'support' stored procedure will be used (namely GR or GV, former for getting reservations, latter for getting vehicles)
			if(SQLCommand.contains("AllReservation")) {		
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
			try {
				while(myRs.next()) {
					while(i<resultSize + 1) {
					//System.out.println(myRs.getString(i));
					response += myRs.getString(i) +", ";
					i++;
					}
				}
			}catch(Exception e) {
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
	public String [] parseURL(String query, String URL) {
		String response[] = null;
		char[] temp = URL.toCharArray();
		int count = 0;
		char[] ans = null;
		int q = 0, k =0;
		//This loop splits the url by the "=" to extract the data that will be sent to the SQL server above
		System.out.println(URL);
		
		//If the url does not contain ALL but contains ? meaning there are attributes, this if statement will format that correctly
		if(!URL.contains("All") && URL.contains("?")) {
		
			ans = new char[temp.length];
			int i, j;
			for(i = 0,j = 0; i<temp.length;i++) {
				
				if(temp[i] == '/') {
					count++;
					if(count==3)
						i++;
				}
				if(count == 3) {
					if(temp[i] == '?' || i>temp.length) {
						break;
					}
					ans[j] = temp[i];
					j++;
					
				}
			} 
			k = 1;	//Used to give extra space in the response string array, while insurring the unqiue ID is not overwriten in the loop below
		
			response = new String[query.split("&").length +k];
			for(int t = 0; t<response.length; t++) {
				if(t == 0) {
					response[0] = String.valueOf(ans).trim();	//Places in the unique ID
				}else if(query.split("&").length == 1) {
					response[1] = query.split("=")[1].trim();	//Places in one argument if there is only one
					break;
				}
				else {
					response[t] = ((query.split("&")[t]).split("="))[1].trim();	//places in all remaining arguments into the String array response
				}
			}
		}
		else if (!URL.contains("?")) {	//Used to process URL that do not contain arguments with are specified with a ?
			String[] op = URL.split("/");
			if(op.length==3) {			//Used if there is no unique ID in the request like for example in get All Vehicles
				noRequest = true;
			}
			else {
				response = new String[1];	//Used if there is a unique ID in the URL like for example Get Vehicles
				response[0] = op[3];
				System.out.println(response[0]);
			}
		}
		else {
			response = new String[1];
			response[0] = query.split("=")[1];
		}
		
		return response;
	}
	/**
	 * The purpose of this function is to interpret the data recieved in the JSON object
	 * @param HE
	 * @param Call
	 * @return String with formatted data
	 * @throws IOException
	 */
	public String[] parseBody(HttpExchange HE, String Call) throws IOException {
		java.io.InputStream in = HE.getRequestBody();
		if(in == null)
			return null;
		String[] holder;
		int q = 0;
		String body = "";
		char temp = '~';
		while(true) {
			temp = (char)in.read();
			body += temp;
			if(temp =='}') {
				break;
			}
		}
		//Removing excess characters
		body = body.replace(':', '=');
		body = body.replace("{", "");
		body = body.replace("}", "");
		body = body.replace("[", "");
		body = body.replace("]", "");
		body = body.replace("\n", "");
		body = body.replace('"', ' ');
		body = body.trim();
		body = body.replace(" ", "");

		body = Call +","+body;
		System.out.println();
		holder = body.split(",");
		//This loop splits the url by the "=" to extract the data that will be sent to the SQL server above
		for(int i = 1; i<holder.length; i++) {
			holder[i] = (holder[i].split("="))[1];
		}
		
		
		//System.out.println(holder[1]);
		return holder;
	}
	/**
	 * The purpose of this function is to determine what table and therefore function wil be used
	 * @param requestedURL
	 * @param RequestMethod
	 * @return String with table name
	 */
	@SuppressWarnings("null")
	public String parseRequest(URI requestedURL, String RequestMethod) {
		String command;
		char[] split, temp;
		int i = 0;
		//This function essentially takes out everything in the URL except for example the word Vehicle,
		//which is passed back so the program understands what field and functions are avaliable
		//to a url with the word Vehicle in it
		command = requestedURL.toString();
		command = command.replace("/APICall/", "");
		split = command.toCharArray();
		temp = new char[split.length];
		
		while(i<split.length) {
			temp[i] = split[i];
			i++;
			
			if(split[i] == '?' || split[i] == '\n' || split[i] == '/') {
				break;
			}	
		}
		return (RequestMethod + String.valueOf(temp)).trim();
	}
		
}
