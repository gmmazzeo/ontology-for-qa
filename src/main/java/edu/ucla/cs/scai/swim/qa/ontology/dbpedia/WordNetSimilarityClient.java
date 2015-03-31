/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class WordNetSimilarityClient implements SimilarityClient {

    static final String wordnetServiceUrl = "http://localhost:8080/wordnetws/similarity/";

    @Override
    public double similarity(String s1, String s2) {
        try {
            String url = wordnetServiceUrl + URLEncoder.encode(s1.replace(" ", ","), "UTF-8") + "/" + URLEncoder.encode(s2.replace(" ", ","), "UTF-8");
            Document doc = Jsoup.connect(url).get();
            return Double.parseDouble(doc.text());
        } catch (Exception ex) {
            Logger.getLogger(DBpediaAttributeLookup.class.getName()).log(Level.SEVERE, "Error evaluating similarity of {0} <and> {1}", new Object[]{s1, s2});
            Logger.getLogger(DBpediaAttributeLookup.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }

    public static void main(String args[]) {
    }

}
