package Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * The purpose of this class is to further initilze the server with the necessary requirements.
 * Each URL that will be used is specified here so that an API Handler can be created for it.
 * Those API handlers are mutlithreaded.
 * @author CPSC Team
 *
 */
public class SimpleHttpServer {
	private int port;
	private HttpServer server;

	public void Start(int port) {
		try {
			this.port = port;
			server = HttpServer.create(new InetSocketAddress(port), 0);								//Create the new socket address
			System.out.println("Your server is running on localhost, port: " + port);				//Say which port the server is running on in the command line

			server.createContext("/APICall/Vehicle", new APIHandler());						//Create section of the URL that will specify we are using an API call
			server.createContext("/APICall/Reservation", new APIHandler());						//Create section of the URL that will specify we are using an API call
			server.createContext("/APICall/Customer", new APIHandler());						//Create section of the URL that will specify we are using an API call
			server.createContext("/APICall/AllReservation", new APIHandler());						//Create section of the URL that will specify we are using an API call
			server.createContext("/APICall/AllVehicle", new APIHandler());						//Create section of the URL that will specify we are using an API call
			server.createContext("/APICall/Employee", new APIHandler());						//Create section of the URL that will specify we are using an API call
			server.createContext("/APICall/Bill", new APIHandler());						//Create section of the URL that will specify we are using an API call
		
			
																	//An example here would be http://localhost/APICall/putwhateverfunctionyouwanthere
			server.setExecutor(null);																//Executor is for multi-threading
			server.start();																			//start the server
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
