package com.ntnu.solbrille.frontend;

import com.ntnu.solbrille.query.processing.QueryProcessor;
import com.ntnu.solbrille.query.filtering.Filter;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.console.SearchEngineMaster;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.velocity.tools.view.servlet.VelocityViewServlet;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */

class SearchServlet extends HttpServlet {

    SearchEngineMaster master;

    public SearchServlet(SearchEngineMaster master) {
        this.master = master;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        

        //Print header
        printHeader(response.getOutputStream());



        String query = request.getParameter("query");
        if(query == null) {
            response.setStatus(200);
            response.getOutputStream().println("Need to put something into the query");
            printFooter(response.getOutputStream());
            return;
        }

        QueryResult results[] = master.query(query);

        
        printDiv(response.getOutputStream(),"numresults",results.length);
        for(QueryResult result:results) {
            printResult(response.getOutputStream(),result);
        }
        printFooter(response.getOutputStream());
    }

    private void printResult(ServletOutputStream outputStream, QueryResult result) throws IOException{
        outputStream.println("<div class=\"result\">");
        String uri = result.getStatisticsEntry().getURI().toString();
        printDiv(outputStream,"uri",uri);
        printDiv(outputStream,"score",result.getScore());
        outputStream.println("</div>");

        
    }

    private void printHeader(ServletOutputStream output) throws IOException {
        output.println("<html><head><title>Query result</title></head><body>");
    }

    private void printFooter(ServletOutputStream output) throws IOException {
        output.println("</body></html>");
    }

    private void printDiv(ServletOutputStream outputStream,String clazz,Object value) throws IOException{
        outputStream.print("<div class=\"" + clazz + "\">");
        outputStream.print(value.toString());
        outputStream.println("</div>");

    }


}