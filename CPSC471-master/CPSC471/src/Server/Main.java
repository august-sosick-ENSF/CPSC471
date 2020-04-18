package Server;
/***
 * The purpose of this class is to initalize an HTTP server at a specific port (will be running on your localhost).
 * The library for the Java HTTP server is present in the lib file with the name http-20070405.jar. 
 * A summary of that package is avaliable here : https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html
 * @author CPSC Team
 *
 */
public class Main {
	public static int port = 9023;
	public static void main(String[] args) {
		SimpleHttpServer httpServer = new SimpleHttpServer();	//Create HTTP server
		httpServer.Start(port);									//Start at specific port
	}
}

