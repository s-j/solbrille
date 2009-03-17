package com.ntnu.solbrille;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


class SearchHandler extends AbstractHandler {

    public void handle(String path, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, int i) throws IOException, ServletException {
        //Yes, this is the way to do it
        //http://docs.codehaus.org/display/JETTY/Writing+a+Jetty+Handler
        if (!path.startsWith("/search"))
            return;
        Request request = httpServletRequest instanceof Request ? (Request) httpServletRequest : HttpConnection.getCurrentConnection().getRequest();
        Response response = httpServletResponse instanceof Response ? (Response) httpServletResponse : HttpConnection.getCurrentConnection().getResponse();

        response.setContentType("text/plain");
        response.getOutputStream().println(request.getPathInfo());
        response.complete();


    }
}

/**
 * //The web frontend, based on Jetty
 *
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class FrontEnd {

    private Server server;
    private boolean running = true;

    public FrontEnd() {
        server = new Server();

        SocketConnector connector = new SocketConnector();
        connector.setPort(8080);
        server.addConnector(connector);


        //Now we have to decide how to handle stuff

        server.addHandler(new SearchHandler());


        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public static void main(String args[]) {
        FrontEnd frontend = new FrontEnd();
        while (frontend.isRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
