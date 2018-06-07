package org.terrier.rest;

import org.apache.commons.cli.CommandLine;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.terrier.applications.CLITool.CLIParsedCLITool;

import java.io.IOException;
import java.net.URI;

/**
 * Loads the default index and exports via a REST service at http://localhost:8080/
 */
public class SingleIndexRestServer extends CLIParsedCLITool {
    @Override
	public String commandname() {
		return "rest-singleindex";
	}

	@Override
	public String helpsummary() {
		return "starts a HTTP REST server to serve a single index";
	}

	// Base URI the Grizzly HTTP server will listen on
    public static final int DEFAULT_PORT = 8080;
    
    @Override
	public int run(CommandLine line) throws Exception {
    	int port = DEFAULT_PORT;
    	if (line.getArgs().length > 0)
    		port = Integer.parseInt(line.getArgs()[0]);
    	String uri = "http://0.0.0.0:"+port+"/";
    	final HttpServer server = startServer(uri);
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", uri));
        System.in.read();
        server.shutdown();
        return 0;
	}

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(String uri) {
        // create a resource config that scans for JAX-RS resources and providers
        // in org.terrier.terrier_rest package
        final ResourceConfig rc = new ResourceConfig().packages("org.terrier.terrier_rest");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        new SingleIndexRestServer().run(args);
    }

	
}
