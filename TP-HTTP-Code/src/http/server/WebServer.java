///A Simple Web Server (WebServer.java)

package http.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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

				// read the data sent. We basically ignore it,
				// stop reading once a blank line is hit. This
				// blank line signals the end of the client HTTP
				// headers.
				String str = ".";
				String request = "";
				while (str != null && !str.equals("")) {
					str = in.readLine();
//					if(str.contains("GET")) {
//						ressource = 
//					}

					request += str + "\n";
				}

				String ressource = "";
				String httpVersion = "";
				if (request.contains("GET")) {
					request = request.replace("GET /", "/");
					ressource = request.substring(request.indexOf("/"), request.indexOf("H"));
					request = request.replace(ressource + "H", "H");
					httpVersion = request.substring(0, request.indexOf("Host"));

					System.out.println(ressource);
					System.out.println();
					if (!ressource.equals("/ ")) {

						File file = new File("D:/documents/insa_lyon/4A/S1//Programmation_reseau/Web_Server/TP-HTTP-Code/Ressources" + ressource);
						if (!file.exists()) {
							out.write(httpVersion + " 404"); // the file does not exists
						} else {
							out.println(httpVersion + "200 OK");
							out.println("Content-Type: text/html");
							out.println("Server: Bot");
							// this blank line signals the end of the headers
							out.println("");

							FileReader fr = new FileReader(file);
							BufferedReader bfr = new BufferedReader(fr);
							String line;
							while ((line = bfr.readLine()) != null) {
								out.write(line);
							}
						}
					}else {
						// Send the response
						// Send the headers
						out.println(httpVersion + " 200 OK");
						out.println("Content-Type: text/html");
						out.println("Server: Bot");
						// this blank line signals the end of the headers
						out.println("");
						// Send the HTML page
						out.println("<H1>Welcome to the Ultra Mini-WebServer</H2>");
					}
				}


				out.flush();
				remote.close();
			} catch (Exception e) {
				System.out.println("Error: " + e);
			}
		}
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
