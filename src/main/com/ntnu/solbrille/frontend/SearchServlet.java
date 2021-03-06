package com.ntnu.solbrille.frontend;

import com.ntnu.solbrille.console.SearchEngineMaster;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.clustering.Cluster;
import com.ntnu.solbrille.query.clustering.ClusterList;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.iterators.IteratorMerger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @author @author <a href="mailto:janmaxim@gmail.coms">Jan Maximilian W. Kristiansen</a>
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

        if (query == null) {
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

        if (!hasClusters) {
            printDiv(response.getOutputStream(), "numresults", "Number of results: " + String.valueOf(allResults.length));
            printDiv(response.getOutputStream(), "showing", "Showing results " + start + " to " + end + ".");
        }

        if (results.length > 0) {

            if (!hasClusters) {
                response.getOutputStream().println("<ol class=\"results\">");
                for (QueryResult result : results) {
                    try {
                        printResult(response.getOutputStream(), result, clean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                response.getOutputStream().println("</ol>");
            } else {
                ClusterList list = results[0].getClusterList();
                response.getOutputStream().println("<div id=\"clusterwrap\"><h2>Clusters</h2><ol id=\"clusterlist\">");
                for (Cluster cluster : list) {
                    StringBuilder sb = new StringBuilder();
                    StringBuilder commaSb = new StringBuilder();
                    for(String tag:cluster.getTags()) {
                        sb.append(tag);
                        commaSb.append("\"" + tag.trim() + "\" ");
                    }

                    response.getOutputStream().println("<li class=\""+sb.toString()+"\"><a href=\"#\">" + commaSb + " ("+ cluster.getSize() +")</a></li>");
                }
                response.getOutputStream().println("</ol></div>");
                response.getOutputStream().println("<div id=\"resultwrap\"><ol id=\"clusters\">");


                for (Cluster cluster : list) {
                    StringBuilder id = new StringBuilder();
                    for (String tag : cluster.getTags()) {
                        id.append(tag);
                    }
                    response.getOutputStream().println("<li class=\"cluster " + id.toString() + "\">");
                    response.getOutputStream().println("");

                    response.getOutputStream().println("<h2>Showing cluster with tags: ");

                    response.getOutputStream().println("<span class=\"tag\">" + id.toString() + "</span>  ");

                    response.getOutputStream().println("</h2><h3><span class=\"score\">Score: " + cluster.getScore() + "</span></h3>");
                    response.getOutputStream().println("<ol class=\"results\">");
                    for (QueryResult result : cluster.getResults()) {
                        try {
                            printResult(response.getOutputStream(), result, query);
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (URISyntaxException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                    response.getOutputStream().println("</ol>");
                    response.getOutputStream().println("</li>");
                }

                response.getOutputStream().println("</div>");
            }

        }

        // print pages
        if (allResults.length > 10 && !hasClusters) {
            response.getOutputStream().println("<ul id=\"paginator\">");
            for (int i = 0; i <= allResults.length / 10; i++) {
                response.getOutputStream().println("<a href=\"?query=" + query + "&offset=" + (i * 10) + "\">" + (i + 1) + "</a>");
            }
            response.getOutputStream().println("</ul>");
        }

        printFooter(response.getOutputStream());
    }

    private void printResult(ServletOutputStream outputStream, QueryResult result, String query) throws IOException, InterruptedException, URISyntaxException {
        outputStream.println("<li class=\"" + result.getDocumentId() + "\">");
        String uri = result.getStatisticsEntry().getURI().toString();
        Pair<Integer, Integer> teaserData = result.getBestWindow();
        String temp = master.getSniplet(new URI(uri), teaserData.getFirst(), teaserData.getSecond());
        int numPositions = 0;
        int i = 0;
        Iterator<Integer>[] positionIterators = new Iterator[result.getTerms().size()];
        for (DictionaryTerm term : result.getTerms()) {
            List<Integer> positions = result.getOccurences(term).getPositionList();
            positionIterators[i++] = positions.iterator();
            numPositions += positions.size();

        }
        Iterator<Integer> merged = new IteratorMerger(positionIterators);
        int[] positions = new int[numPositions];
        int j = 0;
        while (merged.hasNext()) {
            int pos = merged.next();
            if (pos >= teaserData.getFirst()) positions[j++] = pos;
        }

        String teaser = master.getSniplet(new URI(uri), teaserData.getFirst(), teaserData.getSecond(), positions);
        printDivWithA(outputStream, "uri", "/media/time/" + uri.subSequence(uri.lastIndexOf("/") + 1, uri.length()));
        printDiv(outputStream, "teaser", teaser);
        printDiv(outputStream, "score", "Score: " + result.getScore());
        outputStream.println("</li>");
    }

    private void printHeader(ServletOutputStream output, String query) throws IOException {
        output.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        output.println("<html><head><title>Results for query: \"" + query + "\"</title><link rel=\"stylesheet\" type=\"text/css\" href=\"/media/web/css/style.css\" />");

        output.println("<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.3/jquery.min.js\" type=\"text/javascript\"></script>");
        output.println("<script src=\"/media/web/js/highlight.js\" type=\"text/javascript\"></script>");
        output.println("</head><body>");
        if (query == null) {
            output.println("<div id=\"wrapper\">");
            output.println("<h1 id=\"header\"><span>Solbrille</span></h1>");
            printSearchForm(output, query);
        } else {
            output.println("<div id=\"wrapheader\">");
            output.println("<img width=\"70\" height=\"50\" src=\"/media/web/img/logo.png\" />");
            output.println("<div id=\"form\">");
            printSearchForm(output, query);
            output.println("</div></div>");
            output.println("<div id=\"wrapper\">");
        }

    }

    private void printFooter(ServletOutputStream output) throws IOException {
        output.println("</div></body></html>");
    }

    private void printDiv(ServletOutputStream outputStream, String clazz, Object value) throws IOException {
        outputStream.print("<div class=\"" + clazz + "\">");
        outputStream.print(value.toString());
        outputStream.println("</div>");
    }

    private void printDivWithA(ServletOutputStream outputStream, String clazz, Object value) throws IOException {
        outputStream.print("<div class=\"" + clazz + "\">");
        outputStream.print("<a href=\"" + value.toString() + "\">");
        outputStream.print(value.toString());
        outputStream.println("</a></div>");
    }

    private void printSearchForm(ServletOutputStream outputStream, String value) throws IOException {
        if (value == null) value = "";
        outputStream.println("<form method=\"get\" action=\"\">");
        outputStream.println("<input type=\"text\" value=\"" + value + "\" name=\"query\" />");
        outputStream.println("Clusters? <input type=\"checkbox\" name=\"cluster\" value=\"clusters?\" />");
        outputStream.println("<input type=\"submit\" value=\"Feelin' lucky?! Punk!\" />");
        outputStream.println("</form>");
    }


}