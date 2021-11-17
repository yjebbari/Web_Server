///A Simple Web Server (WebServer.java)

package http.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * 
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 * 
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {

	/**
	 * WebServer constructor.
	 */
	protected void start() {
		ServerSocket s;

		System.out.println("Webserver starting up on port 80");
		System.out.println("(press ctrl-c to exit)");
		try {
			// create the main server socket
			s = new ServerSocket(3000);
		} catch (Exception e) {
			System.out.println("Error: " + e);
			return;
		}

		System.out.println("Waiting for connection");
		for (;;) {
			try {
				// wait for a connection
				Socket remote = s.accept();
				// remote is now the connected socket
				System.out.println("Connection, sending data.");
				BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
				PrintWriter out = new PrintWriter(remote.getOutputStream());
				BufferedOutputStream dataOutStream= new BufferedOutputStream(remote.getOutputStream());
				
				// read the data sent. We basically ignore it,
				// stop reading once a blank line is hit. This
				// blank line signals the end of the client HTTP
				// headers.
				String str = ".";
				String request = "";
				while (str != null && !str.equals("")) {
					str = in.readLine();
					request += str + "\n";
				}

				String ressource = "";
				String httpVersion = "";
				if (request.contains("GET")) {
					request = request.replace("GET /", "/");
					ressource = request.substring(request.indexOf("/"), request.indexOf("H")-1);
					request = request.replace(ressource + " H", "H");
					httpVersion = request.substring(0, request.indexOf("Host"));

					System.out.println(ressource);
					System.out.println();
					if (!ressource.equals("/ ")) {
						String unString = "C:/Users/yousr/Documents/GitHub/Web_Server/TP-HTTP-Code/Ressources"
								+ ressource;
//						unString = unString.substring(0, unString.indexOf(" "));
						File file=new File(unString);
						if(file.exists()) {
							byte[] content = Files.readAllBytes(file.toPath());
							System.out.println(file.toPath());
							
							out.println(httpVersion + "200 OK");
							String typeRessource=ressourceContentType(ressource);
							out.println("Content-Type: "+typeRessource);
							out.println("Content-Length: "+(int)content.length);
							System.out.println(typeRessource);
							out.println("Server: Bot");
							// this blank line signals the end of the headers
							out.println("");
							dataOutStream.write(content, 0, (int)content.length);
							
							//out.println(new String(content));
							//dataOutStream.();
						}else{
							out.println(httpVersion + " 404"); // the file does not exists
							out.println("Content-Type: "+ressourceContentType(ressource));
							
							out.println("Server: Bot");
							// this blank line signals the end of the headers
							out.println("");
							// Send the HTML page
							out.println("<H1>Error 404 : Page Not Found</H1>");
						}
					} else {
						// Send the response
						// Send the headers
						out.println(httpVersion + " 200 OK");
						out.println("Content-Type: text/html");
						out.println("Server: Bot");
						// this blank line signals the end of the headers
						out.println("");
						// Send the HTML page
						out.println("<H1>Welcome to the Ultra Mini-WebServer</H1>");
					}
				}
				
				out.flush();
				dataOutStream.flush();
				remote.close();
			} catch (Exception e) {
				System.out.println("Error: " + e);
			}
		}

	}

	public String ressourceContentType(String ressource) {
		System.out.println('.'+ressource+'.');
		if (ressource.endsWith(".html"))
			return "text/html";
		else if (ressource.endsWith(".png"))
			return "image/png";
		else if (ressource.endsWith(".jpg")||ressource.endsWith(".jpeg"))
			return "image/jpeg";
		else return "text/html";
	}

	/**
	 * Start the application.
	 * 
	 * @param args Command line parameters are not used.
	 */
	public static void main(String args[]) {
		WebServer ws = new WebServer();
		ws.start();
	}
}
