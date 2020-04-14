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

public class SimpleHttpServer {
	private int port;
	private HttpServer server;

	public void Start(int port) {
		try {
			this.port = port;
			server = HttpServer.create(new InetSocketAddress(port), 0);
			System.out.println("server started at " + port);
			server.createContext("/", new Handlers.RootHandler());
			server.createContext("/echoHeader", new Handlers.EchoHeaderHandler());
			
			//server.createContext("/testing", new Handlers.EchoGetHandler("Select * from Customer", "email"));
			/*
			server.createContext("/DeleteReservation", new Handlers.EchoGetHandler("{call DeleteReservation(", "something"));
			server.createContext("/GetAllReservation", new Handlers.EchoGetHandler("{call GetAllReservation(", "something"));
			
			server.createContext("/GetAllVehicle", new Handlers.EchoGetHandler("call GetAllVehicle(", "something",4));
			
			server.createContext("/GetCustomer", new Handlers.EchoGetHandler("call GetCustomer(", "something",3));
			
			server.createContext("/GetReservation", new Handlers.EchoGetHandler("call GetReservation(", "something",4));
			*/
			
			server.createContext("/APICall", new Handlers.EchoGetHandler("call GetVehicle(", "vehicle_ID, booking_ID",4));
			/*
			server.createContext("/PostBill", new Handlers.EchoGetHandler("{call PostBill(", "something"));
			server.createContext("/PostReservation", new Handlers.EchoGetHandler("{call PostReservation(", "something"));
			server.createContext("/PutEmployee", new Handlers.EchoGetHandler("{call PutEmployee(", "something"));
			server.createContext("/PutVehicle", new Handlers.EchoGetHandler("{call PutVehicle(", "something"));
			*/
			server.createContext("/echoPost", new Handlers.EchoPostHandler());
			server.setExecutor(null);
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void Stop() {
		server.stop(0);
		System.out.println("server stopped");
	}
}
