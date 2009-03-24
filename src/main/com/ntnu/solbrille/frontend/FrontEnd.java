package com.ntnu.solbrille.frontend;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.resource.URLResource;
import org.mortbay.resource.FileResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;
import java.util.Iterator;
import java.net.URL;
import java.net.URISyntaxException;

import com.ntnu.solbrille.console.SearchEngineMaster;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.sun.corba.se.spi.activation._ActivatorImplBase;

/**
 * //The web frontend, based on Jetty
 *
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class FrontEnd {

    public static Log LOG = LogFactory.getLog(FrontEnd.class);

    public static void main(String args[]) throws IOException{
        Server server = new Server();
        final SearchEngineMaster master = SearchEngineMaster.createMaster();
        

        SocketConnector connector = new SocketConnector();
        connector.setPort(8080);
        server.addConnector(connector);
        //Now we have to decide how to handle stuff

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(new ServletHolder(new SearchServlet(master)),"/search");
        servletHandler.addServletWithMapping(new ServletHolder(new FeederServlet(master)),"/feed");
        servletHandler.addServletWithMapping(new ServletHolder(new HttpServlet() {
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                master.flush();
            }
        }),"/flush");

        servletHandler.addServletWithMapping(new ServletHolder(new HttpServlet() {
            protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                StringBuilder sb = new StringBuilder();
                Iterator<DictionaryTerm> iter = master.getDict().iterator();
                while(iter.hasNext()) {
                    response.setContentType("text");
                    response.getOutputStream().println(iter.next().getTerm());
                }

            }
        }),"/dumpdict");

        server.addHandler(servletHandler);

        // Handles static files
        // ResourceHandler resourceHandler = new ResourceHandler();
        // resourceHandler.setResourceBase(".");
        // server.addHandler(resourceHandler);

        try {
            master.start();
            server.start();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }



        Thread stopper = new Thread() {
            @Override
            public void run() {
                System.out.println("Running shutdown hook");
                System.out.flush();
                master.stop();
            }
        };

        Runtime.getRuntime().addShutdownHook(stopper);

    }

}
