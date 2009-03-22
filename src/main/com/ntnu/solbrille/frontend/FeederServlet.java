package com.ntnu.solbrille.frontend;

import com.ntnu.solbrille.console.SearchEngineMaster;
import com.ntnu.solbrille.feeder.Struct;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class FeederServlet extends HttpServlet {

    SearchEngineMaster master;

    public FeederServlet(SearchEngineMaster master) {
        this.master = master;

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String uri = request.getParameter("uri");
        if (uri == null) {
            response.setStatus(400);
            return;
        }

        Struct struct = new Struct();
        try {
            struct.setField("uri", new URI(uri));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        master.feed(struct);
        //masterfeed(struct);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) sb.append(line);
        master.feed(sb.toString());
    }
}
