package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.visitors.NodeVisitor;
import org.htmlparser.util.ParserException;

import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;


class LinkExtractorVisitor extends NodeVisitor {

    public class Link {
        String linkText;
        String href;
        public Link(String linkText,String href) {
            this.linkText = linkText;
            this.href = href;
        }

    }

    List<Link> links= new ArrayList<Link>();

    public List<Link> getLinks() {
        return links;
    }

    public void visitTag(Tag tag) {
        if(tag instanceof LinkTag) {
            LinkTag linkTag = (LinkTag) tag;
            String link = linkTag.getLinkText().trim();
            if(link.length() == 0 || link.startsWith("#"))
                return;
            links.add(new Link(linkTag.getLinkText(),linkTag.extractLink()));
        }
    }

}

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class LinkExtractor extends AbstractDocumentProcessor{
    public LinkExtractor(String inputField, String outputField) {
        super(inputField, outputField);
    }

    public void process(Struct document) {
        try {

            Parser parser = new Parser();
            parser.setInputHTML(document.getField(getInputField()).getValue());
            URI baseUri;
            try {
                baseUri = new URI(document.getField("uri").getValue());
            } catch (URISyntaxException e) {
                return;
            }
            LinkExtractorVisitor lfv = new LinkExtractorVisitor();
            parser.visitAllNodesWith(lfv);
            for(LinkExtractorVisitor.Link link:lfv.getLinks()) {
                Struct s = new Struct();
                s.addField("title",link.linkText);

                //Create absolute from relative
                try {
                    URI uri = new URI(link.href);
                    String uriStr;
                    if(!uri.isAbsolute()) {
                        uriStr = baseUri.getScheme() + "://" + baseUri.getAuthority() + "/" + uri.getPath();
                    } else {
                        uriStr = link.href;
                    }
                    s.addField("src",uriStr);
                } catch (URISyntaxException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                document.addField(getOutputField(),s);
            }

        } catch (ParserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
