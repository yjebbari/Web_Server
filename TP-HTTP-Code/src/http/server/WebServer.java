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
 * The class WebServer is a HTTP server that implements the GET, POST, HEAD, PUT
 * and DELETE methods.
 * 
 * @author JEBBARI Yousra and LEBON Nathalie
 */
public class WebServer {

	/**
	 * The request sent to the Web Server.
	 */
	String request = "";

	/**
	 * The resource managed by the Web Server. It could be a text/HTML, an image, an
	 * audio or a video.
	 */
	String resource = "";

	/**
	 * The used version of HTTP protocol.
	 */
	String httpVersion = "";

	/**
	 * The type of the request sent to the Web Server : GET, POST, HEAD, PUT, DELETE
	 * etc.
	 */
	String requestType = "";

	/**
	 * This method creates the main server socket and waits for a client's
	 * connection. When the client is connected and sends a request to the Web
	 * Server, this method processes it.
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
				BufferedOutputStream dataOutStream = new BufferedOutputStream(remote.getOutputStream());

				// the following BufferedOutputStream is used to write the body of the response
				// of a GET
				// request, i.e the content of the specified resource
				BufferedOutputStream contentOutPut = new BufferedOutputStream(remote.getOutputStream());

				request = "";
				requestType = "";
				String str = ".";
				// read the data sent. We basically ignore it,
				// stop reading once a blank line is hit. This
				// blank line signals the end of the client HTTP
				// headers.
				while (str != null && !str.equals("")) {
					str = in.readLine();
					if (str != null) {
						request += str + "\n";
					}
				}

				if (request != "" && request != null) {
					requestType = request.substring(0, request.indexOf(" "));
				}

				if (requestType.equals("GET")) {
					requestGet(dataOutStream, contentOutPut);
				} else if (requestType.equals("HEAD")) {
					requestHead(dataOutStream);
				} else if (requestType.equals("PUT")) {
					requestPut(dataOutStream, in);
				} else if (requestType.equals("DELETE")) {
					requestDelete(dataOutStream);
				} else if (requestType.equals("POST")) {
					requestPost(dataOutStream, in);
				}
				dataOutStream.flush();
				contentOutPut.flush();
				remote.close();
			} catch (Exception e) {
				System.out.println("Error: " + e);
			}
		}

	}

	/**
	 * This method takes as a parameter a resource that the Web Server will send to
	 * the client following a GET request, and returns the right Content-Type
	 * necessary in the response header depending on the extension of the resource.
	 * 
	 * @param resource The name of the file (with the extension) that the Web Server
	 *                 will send to the client following a GET request
	 * @return the Content-Type corresponding to the type "resource", and is
	 *         necessary in the response header
	 */
	public String resourceContentType(String resource) {
		if (resource.endsWith(".html"))
			return "text/html";
		else if (resource.endsWith(".png"))
			return "image/png";
		else if (resource.endsWith(".jpg") || resource.endsWith(".jpeg"))
			return "image/jpeg";
		else if (resource.endsWith(".mp4"))
			return "video/mp4";
		else if (resource.endsWith(".gif"))
			return "image/gif";
		else if (resource.endsWith(".mp3"))
			return "audio/mpeg";
		else
			return "text/html";
	}

	/**
	 * Implements the GET method.
	 * 
	 * If the resource is specified in the request and exists, a 200 OK return code
	 * is sent, along with the asked resource.
	 * 
	 * If the resource is specified in the request but doesn't exists, a 404 NOT
	 * FOUND return code is sent.
	 * 
	 * If no resource is specified in the request, a 200 OK return code is sent,
	 * along with a simple HTML default content.
	 * 
	 * If the method catches an exception it sends a 500 Internal Server Error
	 * return code.
	 * 
	 * @param dataOutStream The Buffered Output Stream in which the Web Server
	 *                      writes its response
	 * @param contentOutPut The Buffered Output Stream in which the Web Server
	 *                      writes the body of the response of the GET request, i.e
	 *                      the content of the specified resource
	 */
	public void requestGet(BufferedOutputStream dataOutStream, BufferedOutputStream contentOutPut) {
		// aString contains the first part of a request until the HTTP version (not
		// included)
		String aString = request.substring(0, request.indexOf("HTTP/"));
		resource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));

		try {
			if (!resource.equals("/")) {
				String resourcePath = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString()
						.replace(System.getProperty("file.separator"), "/") + "/ressources" + resource;
				System.out.println(resourcePath);
				File file = new File(resourcePath);
				if (file.exists()) {
					byte[] content = Files.readAllBytes(file.toPath());
					dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
					String typeResource = resourceContentType(resource);
					dataOutStream.write(("Content-Type: " + typeResource + "\n").getBytes());
					dataOutStream.write(("Content-Length: " + (int) content.length + "\n").getBytes());
					dataOutStream.write(("Server: Bot\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					dataOutStream.flush();
					contentOutPut.write(content, 0, (int) content.length);
					contentOutPut.flush();
				} else {
					dataOutStream.write((httpVersion + " 404" + "\n").getBytes());
					dataOutStream.write(("Content-Type: " + resourceContentType(resource) + "\n").getBytes());
					dataOutStream.write(("Server: Bot" + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					// Send the HTML page
					dataOutStream.write(("<H1>Error 404 : Page Not Found</H1>").getBytes());
				}
			} else {
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
			contentOutPut.flush();
		} catch (Exception e) {
			try {
				System.out.println("Error: " + e);
				dataOutStream.write((httpVersion + " 500" + "\n").getBytes());
				dataOutStream.write(("\n").getBytes());
				dataOutStream.flush();
				contentOutPut.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Implements the PUT method.
	 * 
	 * If the resource is specified in the request and exists, the content of the
	 * resource is overwritten by the content of the PUT's request's body. Thus, a
	 * 200 OK return code is sent. NB: The PUT's request's body must end with an
	 * '\n'.
	 * 
	 * If the resource is specified in the request but doesn't exists, a new
	 * resource with the same name is created in the resources directory and its
	 * content will be the PUT's request's body. Thus, a 201 Created return code is
	 * sent.
	 * 
	 * If no resource is specified in the request, a 200 OK return code is sent,
	 * along with a simple HTML default content.
	 * 
	 * If the method catches an exception it sends a 500 Internal Server Error
	 * return code.
	 * 
	 * @param dataOutStream The Buffered Output Stream in which the Web Server
	 *                      writes its response
	 * @param in            The buffered Reader used to read the request sent by the
	 *                      client to the Web Server.
	 */
	public void requestPut(BufferedOutputStream dataOutStream, BufferedReader in) {
		// aString contains the first part of a request until the HTTP version (not
		// included)
		String aString = request.substring(0, request.indexOf("HTTP/"));
		resource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));

		boolean fileExists = true;
		try {
			if (!resource.equals("/")) {
				String resourcePath = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString()
						.replace(System.getProperty("file.separator"), "/") + "/ressources" + resource;
				File file = new File(resourcePath);
				if (!file.exists()) {
					fileExists = false;
				}
				String body = "";
				String string = ".";
				while (string != null && !string.equals("") && in.ready()) {
					string = in.readLine();
					if (string != null)
						body += string;
				}
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write(body);
				fileWriter.close();
				if (!fileExists) {
					dataOutStream.write((httpVersion + " 201 Created" + "\n").getBytes());
					dataOutStream.write(("Content-Location: " + resource + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
				} else {
					dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
					dataOutStream.write(("Content-Location: " + resource + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					// Send the HTML page
					dataOutStream.write(("<H2>FILE MODIFIED</H2>").getBytes());
				}
			} else {
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

	/**
	 * Implements the POST method.
	 * 
	 * If the resource is specified in the request and exists, the POST's request's
	 * body is appended to the already existing content of the resource. Thus, a 200
	 * OK return code is sent.
	 * 
	 * If the resource is specified in the request but doesn't exists, a new
	 * resource with the same name is created in the resources directory and its
	 * content will be the POST's request's body. Thus, a 201 Created return code is
	 * sent.
	 * 
	 * If no resource is specified in the request, a 200 OK return code is sent,
	 * along with a simple HTML default content.
	 * 
	 * If the method catches an exception it sends a 500 Internal Server Error
	 * return code.
	 * 
	 * @param dataOutStream The Buffered Output Stream in which the Web Server
	 *                      writes its response
	 * @param in            The buffered Reader used to read the request sent by the
	 *                      client to the Web Server.
	 */
	public void requestPost(BufferedOutputStream dataOutStream, BufferedReader in) {
		// aString contains the first part of a request until the HTTP version (not
		// included)
		String aString = request.substring(0, request.indexOf("HTTP/"));
		resource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));

		boolean fileExists = true;
		try {
			if (!resource.equals("/")) {
				String resourcePath = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString()
						.replace(System.getProperty("file.separator"), "/") + "/ressources" + resource;
				File file = new File(resourcePath);
				if (!file.exists()) {
					fileExists = false;
				}
				String body = "";
				String string = ".";
				while (string != null && !string.equals("") && in.ready()) {
					string = in.readLine();
					if (string != null)
						body += string;
				}
				FileWriter fileWriter = new FileWriter(file, true);
				fileWriter.write(body);
				fileWriter.close();
				if (!fileExists) {
					dataOutStream.write((httpVersion + " 201 Created" + "\n").getBytes());
					dataOutStream.write(("Content-Location: " + resource + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
				} else {
					dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
					dataOutStream.write(("Content-Location: " + resource + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
					dataOutStream.write(("<H2>FILE MODIFIED</H2>").getBytes());

				}
			} else {
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

	/**
	 * Implements the DELETE method.
	 * 
	 * If the resource is specified in the request and exists, the resource is
	 * deleted. Therefore, a 200 OK return code is sent if the resource was
	 * successfully deleted or a 403 Forbidden return code if it wasn't.
	 * 
	 * If the resource is specified in the request but doesn't exists, a 404 NOT
	 * FOUND return code is sent.
	 * 
	 * If no resource is specified in the request, a 200 OK return code is sent,
	 * along with a simple HTML default content.
	 * 
	 * If the method catches an exception it sends a 500 Internal Server Error
	 * return code.
	 * 
	 * @param dataOutStream The Buffered Output Stream in which the Web Server
	 *                      writes its response
	 */
	public void requestDelete(BufferedOutputStream dataOutStream) {
		// aString contains the first part of a request until the HTTP version (not
		// included)
		String aString = request.substring(0, request.indexOf("HTTP/"));
		resource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));
		try {
			if (!resource.equals("/")) {
				String resourcePath = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString()
						.replace(System.getProperty("file.separator"), "/") + "/ressources" + resource;
				File file = new File(resourcePath);
				if (file.exists()) {
					if (file.delete()) {
						dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
						dataOutStream.write(("\n").getBytes());
						dataOutStream.write(("<H2>FILE DELETED</H2>").getBytes());
					} else {
						dataOutStream.write((httpVersion + " 403" + "\n").getBytes());
						dataOutStream.write(("\n").getBytes());
					}
				} else {
					dataOutStream.write((httpVersion + " 404" + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
				}
			} else {
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

	/**
	 * Implements the HEAD method.
	 * 
	 * If the resource is specified in the request and exists, a 200 OK return code
	 * is sent, along with the same response headers that would be returned if the
	 * request was a GET request(so the content of the resource is not sent).
	 * 
	 * If the resource is specified in the request but doesn't exists, a 404 NOT
	 * FOUND return code is sent.
	 * 
	 * If no resource is specified in the request, a 200 OK return code is sent,
	 * along with a simple HTML default content.
	 * 
	 * If the method catches an exception it sends a 500 Internal Server Error
	 * return code.
	 * 
	 * @param dataOutStream The Buffered Output Stream in which the Web Server
	 *                      writes its response
	 */
	public void requestHead(BufferedOutputStream dataOutStream) {
		// aString contains the first part of a request until the HTTP version (not
		// included)
		String aString = request.substring(0, request.indexOf("HTTP/"));
		resource = aString.substring(aString.lastIndexOf("/"), aString.lastIndexOf(" "));
		httpVersion = request.substring(request.indexOf("HTTP/"), request.indexOf("\n"));
		try {
			if (!resource.equals("/")) {
				String resourcePath = Paths.get("").toAbsolutePath().getParent().getParent().getParent().toString()
						.replace(System.getProperty("file.separator"), "/") + "/ressources" + resource;
				File file = new File(resourcePath);
				if (file.exists()) {
					dataOutStream.write((httpVersion + " 200 OK" + "\n").getBytes());
					String typeResource = resourceContentType(resource);
					dataOutStream.write(("Content-Type: " + typeResource + "\n").getBytes());
					dataOutStream.write(("Server: Bot\n").getBytes());
					dataOutStream.write(("Content-Length: " + (int) file.length() + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
				} else {
					dataOutStream.write((httpVersion + " 404" + "\n").getBytes());
					dataOutStream.write(("Content-Type: " + resourceContentType(resource) + "\n").getBytes());
					dataOutStream.write(("Server: Bot" + "\n").getBytes());
					// this blank line signals the end of the headers
					dataOutStream.write(("\n").getBytes());
				}
			} else {
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
