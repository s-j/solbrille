package com.ntnu.solbrille.frontend;

import com.ntnu.solbrille.query.processing.QueryProcessor;
import com.ntnu.solbrille.query.filtering.Filter;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.console.SearchEngineMaster;
import com.ntnu.solbrille.utils.Pair;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

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

        String query = request.getParameter("query");

        //Print header
        printHeader(response.getOutputStream(), query);
        printSearchForm(response.getOutputStream(), query);
        if(query == null) {
            response.setStatus(200);
            response.getOutputStream().println("Need to put something into the query");
            printFooter(response.getOutputStream());
            return;
        }

        Boolean hasClusters = false;
        String clustering = request.getParameter("cluster");
        if (clustering != null) {
            hasClusters = true;
        }

        QueryResult allResults[] = master.query(query, 0, 500);
        QueryResult[] results = new QueryResult[10];

        int start = 1;
        int end = 10;

        int offset = (request.getParameter("offset") != null) ? Integer.valueOf(request.getParameter("offset")) : 0;
        if (offset > 0 && allResults.length - offset >= 10) {
            System.arraycopy(allResults, offset, results, 0, 10);
            start = offset + 1;
            end = start + 9;
        } else if (offset > 0 && allResults.length - offset < 10) {
            results = new QueryResult[allResults.length - offset];
            System.arraycopy(allResults, offset, results, 0, allResults.length - offset);
            start = offset + 1;
            end = start + results.length - 1;
        } else if (allResults.length < 10) {
            results = allResults;
            end = results.length;
        } else {
            System.arraycopy(allResults, 0, results, 0, 10);
        }

        printDiv(response.getOutputStream(),"numresults","Number of results: " + String.valueOf(allResults.length));
        printDiv(response.getOutputStream(),"showing","Showing results " + start + " to " + end + ".");
        if (results.length > 0) {
            response.getOutputStream().println("<ol id=\"results\">");
            for(QueryResult result:results) {
                if (!hasClusters) {
                    try {
                        printResult(response.getOutputStream(),result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // For each cluster:
                    // <li class="clustername">
                    //      ... results ...
                    // </li>
                }
            }
            response.getOutputStream().println("</ol>");
        }

        // print pages
        if (allResults.length > 10) {
            response.getOutputStream().println("<ul>");
            for (int i = 0; i <= allResults.length / 10; i++) {
                response.getOutputStream().println("<a href=\"/search?query="+query+"&offset=" + (i*10) + "\">" + (i+1) + "</a>");
            }
            response.getOutputStream().println("</ul>");
        }

        printFooter(response.getOutputStream());
    }

    private void printResult(ServletOutputStream outputStream, QueryResult result) throws IOException, InterruptedException, URISyntaxException{
        outputStream.println("<li class=\"" + result.getDocumentId() +"\">");
        String uri = result.getStatisticsEntry().getURI().toString();
        Pair<Integer, Integer> teaserData = result.getBestWindow();
        String teaser = master.getSniplet(new URI(uri), teaserData.getFirst(), teaserData.getSecond());
        printDivWithA(outputStream,"uri",uri);
        printDiv(outputStream, "teaser", teaser);
        printDiv(outputStream,"score",result.getScore());
        outputStream.println("</li>");    
    }

    private void printHeader(ServletOutputStream output, String query) throws IOException {
        output.println("<html><head><title>Results for query: \"" + query + "\"</title></head><body>");
    }

    private void printFooter(ServletOutputStream output) throws IOException {
        output.println("</body></html>");
    }

    private void printDiv(ServletOutputStream outputStream,String clazz,Object value) throws IOException{
        outputStream.print("<div class=\""+clazz+"\">");
        outputStream.print(value.toString());
        outputStream.println("</div>");
    }

    private void printDivWithA(ServletOutputStream outputStream,String clazz,Object value) throws IOException {
        outputStream.print("<div class=\""+clazz+"\">");
        outputStream.print("<a href=\"" + value.toString() + "\">");
        outputStream.print(value.toString());
        outputStream.println("</a></div>");
    }

    private void printSearchForm(ServletOutputStream outputStream, String value) throws IOException {
        outputStream.println("<form method=\"get\" action=\"\">");
        outputStream.println("<input type=\"text\" value=\""+value+"\"name=\"query\" />");
        outputStream.println("<input type=\"submit\" value=\"Feelin' lucky?! Punk!\" />");
        outputStream.println("</form>");
    }


}