package com.ntnu.solbrille.suffixtree;

import java.net.URI;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class TimeCollection {

    public String[] filenames;

    public String[] getTimeCollection(File f,int maxLength) {
        if(f.isDirectory()) {
            FilenameFilter ff = new FilenameFilter() {

                public boolean accept(File file, String s) {
                    return s.endsWith(".txt");
                }
            };
            filenames = new String[f.listFiles(ff).length];
            String retlist[] =new String[f.listFiles(ff).length];
            int pos = 0;
            Pattern pattern = Pattern.compile("[\\p{P}]+");

            int filenamepos = 0;

            for(File txt:f.listFiles(ff)) {

                StringBuilder sb = new StringBuilder();
                String line;
                try {
                    BufferedReader br = new BufferedReader(new FileReader(txt));
                    while((line = br.readLine()) != null) sb.append(line + " ");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                String output = sb.toString();
                Matcher matcher = pattern.matcher(output);
                output = matcher.replaceAll("");
                filenames[pos] = txt.getName();
                retlist[pos++] = output.substring(0,Math.min(output.length(),maxLength));

            }
            return retlist;

        } else {
            throw new RuntimeException("ERROR");
        }

    }
}
