package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.visitors.TextExtractingVisitor;
import org.htmlparser.util.ParserException;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */

class RemoveInvisibleTextVisitor extends TextExtractingVisitor {

    public final static Set<String> invisibleTags = new HashSet(Arrays.asList(new String[]{
            "SCRIPT",
            "STYLE",
            "EMBED",
            "OBJECT",
            "APPLET",
            "NOFRAMES"
    }));

    private int invisible = 0;

    @Override
    public void visitTag(Tag tag) {
        if(invisibleTags.contains(tag.getTagName())) {
            invisible++;
        }


    }
    @Override
    public void visitStringNode(Text text) {
        if(invisible == 0) {
            super.visitStringNode(text);
        }
    }

    @Override
    public void visitEndTag(Tag tag) {
        if(invisibleTags.contains(tag.getTagName())) {
            if(invisible > 0)
                invisible--;
        }

    }
}

public class HtmlToText extends AbstractDocumentProcessor{
    public HtmlToText(String inputField, String outputField) {
        super(inputField, outputField);
    }

    public void process(Struct document) {
        String content = document.getField(getInputField()).getValue().toString();
        try {
            Parser parser = new Parser(new Lexer(content));
            
            TextExtractingVisitor tev = new RemoveInvisibleTextVisitor();
            parser.visitAllNodesWith(tev);
            String textInPage = tev.getExtractedText();
            document.setField(getOutputField(),textInPage);
        } catch (ParserException e) {
            e.printStackTrace();
        }

    }
}
