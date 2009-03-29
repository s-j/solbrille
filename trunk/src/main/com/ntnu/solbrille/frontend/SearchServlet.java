package com.ntnu.solbrille.frontend;

import com.ntnu.solbrille.query.processing.QueryProcessor;
import com.ntnu.solbrille.query.filtering.Filter;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.clustering.ClusterList;
import com.ntnu.solbrille.query.clustering.Cluster;
import com.ntnu.solbrille.console.SearchEngineMaster;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.StemmingUtil;

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

        String clean = query.replaceAll("\"", " \" ");

        printDiv(response.getOutputStream(),"numresults","Number of results: " + String.valueOf(allResults.length));
        printDiv(response.getOutputStream(),"showing","Showing results " + start + " to " + end + ".");
        if (results.length > 0) {

            if(!hasClusters) {
                response.getOutputStream().println("<ol id=\"results\">");
                for(QueryResult result:results) {
                    try {
                        printResult(response.getOutputStream(),result, clean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                response.getOutputStream().println("</ol>");
            } else {                                                                    
                response.getOutputStream().println("<ol id=\"clusters\">");

                ClusterList list = results[0].getClusterList();
                for(Cluster cluster:list) {
                    response.getOutputStream().println("<li class=\"cluster\">");
                    response.getOutputStream().println("<span class=\"score\">" + cluster.getScore() + "</span>");

                    response.getOutputStream().println("<ol class=\"tags\">");
                    for(String tag:cluster.getTags()) {
                        response.getOutputStream().println("<li class=\"tag\">" + tag + "</li>");
                    }
                    response.getOutputStream().println("</ol>");
                    response.getOutputStream().println("<ol class=\"results\">");
                    for(QueryResult result:cluster.getResults()) {
                        try {
                            printResult(response.getOutputStream(),result,query);
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (URISyntaxException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                    response.getOutputStream().println("</ol>");







                    /*
                    <li class="cluster">
                        <ol class="tags">

                        </ol>

                        <ol class="results">
                            //printresult
                        </ol>

                    </li>
                     */

                    response.getOutputStream().println("</li>");
                }
            }

        }

        // print pages
        if (allResults.length > 10) {
            response.getOutputStream().println("<ul id=\"paginator\">");
            for (int i = 0; i <= allResults.length / 10; i++) {
                response.getOutputStream().println("<a href=\"?query="+query+"&offset=" + (i*10) + "\">" + (i+1) + "</a>");
            }
            response.getOutputStream().println("</ul>");
        }

        printFooter(response.getOutputStream());
    }

    private void printResult(ServletOutputStream outputStream, QueryResult result, String query) throws IOException, InterruptedException, URISyntaxException{
        outputStream.println("<li class=\"" + result.getDocumentId() +"\">");
        String uri = result.getStatisticsEntry().getURI().toString();
        Pair<Integer, Integer> teaserData = result.getBestWindow();
        String temp = master.getSniplet(new URI(uri), teaserData.getFirst(), teaserData.getSecond());
        int[] positions = StemmingUtil.createPositionList(query, temp);
        String teaser = master.getSniplet(new URI(uri), teaserData.getFirst(), teaserData.getSecond(), positions);
        printDivWithA(outputStream,"uri","/media/time/" + uri.subSequence(uri.lastIndexOf("/") + 1, uri.length()));
        printDiv(outputStream, "teaser", teaser);
        printDiv(outputStream,"score","Score: "+result.getScore());
        outputStream.println("</li>");    
    }

    private void printHeader(ServletOutputStream output, String query) throws IOException {
        output.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        output.println("<html><head><title>Results for query: \"" + query + "\"</title><link rel=\"stylesheet\" type=\"text/css\" href=\"http://localhost:8080/media/web/css/style.css\" />");

        output.println("<script src=\"http://localhost:8080/media/web/js/jquery-1.3.2.min.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"http://localhost:8080/media/web/js/highlight.js\" type=\"text/javascript\"></script>");
        output.println("</head><body>");
        output.println("<div id=\"wrapper\">");
        output.println("<h1 id=\"header\"><span>Solbrille</span></h1>");
    }

    private void printFooter(ServletOutputStream output) throws IOException {
        output.println("</div></body></html>");
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
        if (value == null) value = "";
        outputStream.println("<form method=\"get\" action=\"\">");
        outputStream.println("<input type=\"text\" value=\""+value+"\" name=\"query\" />");
        outputStream.println("<input type=\"submit\" value=\"Feelin' lucky?! Punk!\" />");
        outputStream.println("</form>");
    }


}