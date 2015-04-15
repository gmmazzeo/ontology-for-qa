/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia.tipicality;

import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaCategory;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntology;
import static edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntology.CLASSES_BASE_URI;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DbpediaCsvDownload {

    private static void download(Element e) throws MalformedURLException, IOException {
        for (Element c : e.children()) {
            String tagName = c.tag().getName();
            if (tagName.equals("small")) {
                for (Element c1 : c.children()) {
                    if (c1.tag().getName().equals("a") && c1.text().equalsIgnoreCase("csv")) {
                        String href = c1.attr("href");
                        System.out.println("Downloading " + href);
                        try {
                            URL remoteFile = new URL(href);
                            ReadableByteChannel rbc = Channels.newChannel(remoteFile.openStream());
                            String[] s = href.split("\\/");
                            FileOutputStream fos = new FileOutputStream(DBpediaOntology.DBPEDIA_CSV_FOLDER + s[s.length - 1]);
                            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } else if (tagName.equals("ul")) {
                for (Element c1 : c.children()) {
                    if (c1.tagName().equals("li")) {
                        download(c1);
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {
        Document doc = null;
        try {
            doc = Jsoup.connect(DBpediaOntology.DBPEDIA_CLASSES_URL).get();
        } catch (IOException ex) {
            Logger.getLogger(DBpediaOntology.class.getName()).log(Level.SEVERE, null, ex);
        }

        download(doc.body().children().get(1).children().get(1));
    }
}
