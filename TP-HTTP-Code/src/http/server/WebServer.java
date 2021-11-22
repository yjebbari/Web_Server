///A Simple Web Server (WebServer.java)

package http.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

	String request = "";
	String ressource = "";
	String httpVersion = "";
	String requestType = "";

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
				BufferedOutputStream dataOutStream = new BufferedOutputStream(remote.getOutputStream());

				// read the data sent. We basically ignore it,
				// stop reading once a blank line is hit. This
				// blank line signals the end of the client HTTP
				// headers.
				request = "";
				requestType = "";
				String str = ".";
				while (str != null && !str.equals("")) {
					str = in.readLine();
					if(str!=null) { 
						request += str + "\n"; //y avait pas de if avant 
					}
				}
				
				if (request!="" && request!=null) { 
					System.out.println("request is :"+request);
					requestType = request.substring(0, request.indexOf(" "));
					System.out.println("requestType: "+requestType);
				}
				if (requestType.equals("GET")) {
					requestGet(dataOutStream,remote);
				}else if(requestType.equals("HEAD")){
					requestHead(dataOutStream);
				}else if(requestType.equals("PUT")){
					requestPut(dataOutStream,in);
				}else if(requestType.equals("DELETE")){
					requestDelete(dataOutStream);
				}else if(requestType.equals("POST")){
					requestPost(dataOutStream,in);
				}
				dataOutStream.flush();
				remote.close();
			} catch (Exception e) {
				System.out.println("Error: " + e);
			}
		}

	}

	public String ressourceContentType(String ressource) {
		if (ressource.endsWith(".html"))
			return "text/html";
		else if (ressource.endsWith(".png"))
			return "image/png";
		else if (ressource.endsWith(".jpg") || ressource.endsWith(".jpeg"))
			return "image/jpeg";
		else if (ressource.endsWith(".mp4"))
			return "video/mp4";
		else if (ressource.endsWith(".gif"))
			return "image/gif";
		else if (ressource.endsWith(".mp3"))
			return "audio/mpeg";
		else
			return "text/html";
	}

	public void requestGet(BufferedOutputStream dataOutStream, Socket remote) {
		String aString = request.substring(0,request.indexOf("HTTP/"));
		ressource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));//request.substring(request.indexOf("HTTP/"), request.indexOf("Host"));
		try {
			if (!ressource.equals("/")) {
				String unString = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString().replace(System.getProperty("file.separator"), "/")+"/ressources"+ressource;
				File file = new File(unString);
				if (file.exists()) {
					byte[] content = Files.readAllBytes(file.toPath());
					dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
					String typeRessource = ressourceContentType(ressource);
					dataOutStream.write(("Content-Type: " + typeRessource + "\n").getBytes());
					dataOutStream.write(("Content-Length: " + (int) content.length + "\n").getBytes());
					dataOutStream.write(("Server: Bot\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					//new
					dataOutStream.flush();
					BufferedOutputStream contentOutPut=new BufferedOutputStream(remote.getOutputStream());//new
					contentOutPut.write(content, 0, (int) content.length);
					contentOutPut.flush();
				} else {
					dataOutStream.write((httpVersion + " 404" + "\n").getBytes());
					dataOutStream.write(("Content-Type: " + ressourceContentType(ressource) + "\n").getBytes());
					dataOutStream.write(("Server: Bot" + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					// Send the HTML page
					dataOutStream.write(("<H1>Error 404 : Page Not Found</H1>").getBytes());
				}
			} else {
				// Send the response
				// Send the headers
				dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
				dataOutStream.write(("Content-Type: text/html" + "\n").getBytes());
				dataOutStream.write(("Server: Bot" + "\n").getBytes());
				// this blank line signals the end of the headers
				dataOutStream.write(("\n").getBytes());
				// Send the HTML page
				dataOutStream.write(("<H1>Welcome to the Ultra Mini-WebServer</H1>").getBytes());
			}
			dataOutStream.flush();
		} catch (Exception e) {
			try {
				System.out.println("Error: " + e);
				dataOutStream.write((httpVersion + " 500" + "\n").getBytes());
				dataOutStream.write(("\n").getBytes());
				dataOutStream.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
		}
	}
	
	public void requestPut(BufferedOutputStream dataOutStream, BufferedReader in) {
		String aString = request.substring(0,request.indexOf("HTTP/"));
		ressource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));//request.substring(request.indexOf("HTTP/"), request.indexOf("Host"));
		boolean fileExists=true;
		try {
			if (!ressource.equals("/")) {
				String unString = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString().replace(System.getProperty("file.separator"), "/")+"/ressources"+ressource;
				File file = new File(unString);
				if (!file.exists()) {
					fileExists=false;
				}
					String body="";
					String string =".";
					while (string != null && !string.equals("") && in.ready()) {
						string = in.readLine();
						if(string!=null) body += string;
					}
					FileWriter fileWriter = new FileWriter(file);
					fileWriter.write(body);
					fileWriter.close();
				if(!fileExists) {	
					dataOutStream.write((httpVersion + " 201 Created" + "\n").getBytes());
					dataOutStream.write(("Content-Location: " + ressource + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
				} else {
					dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
					dataOutStream.write(("Content-Location: " + ressource + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					// Send the HTML page
				}
			} else {
				// Send the response
				// Send the headers
				dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
				dataOutStream.write(("Content-Type: text/html" + "\n").getBytes());
				dataOutStream.write(("Server: Bot" + "\n").getBytes());
				// this blank line signals the end of the headers
				dataOutStream.write(("\n").getBytes());
				// Send the HTML page
				dataOutStream.write(("<H1>Welcome to the Ultra Mini-WebServer</H1>").getBytes());
			}
			dataOutStream.flush();
		} catch (Exception e) {
			try {
				System.out.println("Error: " + e);
				dataOutStream.write((httpVersion + " 500" + "\n").getBytes());
				dataOutStream.write(("\n").getBytes());
				dataOutStream.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
		}
	}
	
	public void requestPost(BufferedOutputStream dataOutStream, BufferedReader in) {
		String aString = request.substring(0,request.indexOf("HTTP/"));
		ressource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));//request.substring(request.indexOf("HTTP/"), request.indexOf("Host"));
		boolean fileExists=true;
		try {
			if (!ressource.equals("/")) {
				String unString = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString().replace(System.getProperty("file.separator"), "/")+"/ressources"+ressource;
				File file = new File(unString);
				if (!file.exists()) {
					fileExists=false;
				}
					String body="";
					String string =".";
					while (string != null && !string.equals("") && in.ready()) {
						string = in.readLine();
						if(string!=null) body += string;
					}
					FileWriter fileWriter = new FileWriter(file,true);
					fileWriter.write(body);
					fileWriter.close();
				if(!fileExists) {	
					dataOutStream.write((httpVersion + " 201 Created" + "\n").getBytes());
					dataOutStream.write(("Content-Location: " + ressource + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
				} else {
					dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
					dataOutStream.write(("Content-Location: " + ressource + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					// Send the HTML page
				}
			} else {
				// Send the response
				// Send the headers
				dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
				dataOutStream.write(("Content-Type: text/html" + "\n").getBytes());
				dataOutStream.write(("Server: Bot" + "\n").getBytes());
				// this blank line signals the end of the headers
				dataOutStream.write(("\n").getBytes());
				// Send the HTML page
				dataOutStream.write(("<H1>Welcome to the Ultra Mini-WebServer</H1>").getBytes());
			}
			dataOutStream.flush();
		} catch (Exception e) {
			try {
				System.out.println("Error: " + e);
				dataOutStream.write((httpVersion + " 500" + "\n").getBytes());
				dataOutStream.write(("\n").getBytes());
				dataOutStream.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
		}
	}
	
	public void requestDelete(BufferedOutputStream dataOutStream) {
		String aString = request.substring(0,request.indexOf("HTTP/"));
		ressource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));
		try {
			if (!ressource.equals("/")) {
				String unString = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString().replace(System.getProperty("file.separator"), "/")+"/ressources"+ressource;
				File file = new File(unString);
				if (file.exists()) {
					if(file.delete())
			        {
						dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
						dataOutStream.write(("\n").getBytes());
						dataOutStream.write(("<H2>FILE DELETED</H2>").getBytes());
			        }
			        else
			        {
			        	dataOutStream.write((httpVersion + " 403" + "\n").getBytes());
						dataOutStream.write(("\n").getBytes());
			        }
				} else {
					dataOutStream.write((httpVersion + " 404" + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					// Send the HTML page
				}
			} else {
				// Send the response
				// Send the headers
				dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
				dataOutStream.write(("Content-Type: text/html" + "\n").getBytes());
				dataOutStream.write(("Server: Bot" + "\n").getBytes());
				// this blank line signals the end of the headers
				dataOutStream.write(("\n").getBytes());
				// Send the HTML page
			}
			dataOutStream.flush();
		} catch (Exception e) {
			try {
				System.out.println("Error: " + e);
				dataOutStream.write((httpVersion + " 500" + "\n").getBytes());
				dataOutStream.write(("\n").getBytes());
				dataOutStream.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
		}
	}
	
	public void requestHead(BufferedOutputStream dataOutStream) {
		String aString = request.substring(0,request.indexOf("HTTP/"));
		ressource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));
		try {
			if (!ressource.equals("/")) {
				String unString = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString().replace(System.getProperty("file.separator"), "/")+"/ressources"+ressource;
				File file = new File(unString);
				if (file.exists()) {
					dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
					String typeRessource = ressourceContentType(ressource);
					dataOutStream.write(("Content-Type: " + typeRessource + "\n").getBytes());
					dataOutStream.write(("Server: Bot\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
				} else {
					dataOutStream.write((httpVersion + " 404" + "\n").getBytes());
					dataOutStream.write(("Content-Type: " + ressourceContentType(ressource) + "\n").getBytes());
					dataOutStream.write(("Server: Bot" + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					// Send the HTML page
				}
			} else {
				// Send the response
				// Send the headers
				dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
				dataOutStream.write(("Content-Type: text/html" + "\n").getBytes());
				dataOutStream.write(("Server: Bot" + "\n").getBytes());
				// this blank line signals the end of the headers
				dataOutStream.write(("\n").getBytes());
				// Send the HTML page
			}
			dataOutStream.flush();
		} catch (Exception e) {
			try {
				System.out.println("Error: " + e);
				dataOutStream.write((httpVersion + " 500" + "\n").getBytes());
				dataOutStream.write(("\n").getBytes());
				dataOutStream.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
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
