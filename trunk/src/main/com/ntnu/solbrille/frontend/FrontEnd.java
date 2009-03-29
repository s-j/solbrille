package com.ntnu.solbrille.frontend;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.*;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.resource.URLResource;
import org.mortbay.resource.FileResource;
import org.mortbay.resource.Resource;
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
 * @author <a href="mailto:janmaxim@gmail.com">Jan Maximilian Winther Kristiansen</a>
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

        // Creating ContextHandlers
        ContextHandler servlets = new ContextHandler();
        ContextHandler media = new ContextHandler();

        servlets.setContextPath("/servlets");
        media.setContextPath("/media");

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
                Iterator<DictionaryTerm> iter = master.getDict().iterator();
                while(iter.hasNext()) {
                    response.setContentType("text");
                    response.getOutputStream().println(iter.next().getTerm());
                }
            }
        }),"/dumpdict");

        servlets.setHandler(servletHandler);

        ServletHandler mediaHandler = new ServletHandler();
        ServletHolder sh = new ServletHolder(DefaultServlet.class);
        sh.setInitParameter("dirAllowed", String.valueOf(true));
        sh.setInitParameter("resourceBase", System.getProperty("user.dir"));
        mediaHandler.addServletWithMapping(sh, "/*");
        
        media.setHandler(mediaHandler);

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] {servlets, media});

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler()});

        server.setHandler(handlers);
        
        try {
            master.start();
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println(System.getProperty("jetty.home"));

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
