/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class SwoogleSimilarityClient implements SimilarityClient {

    public final static String serviceUrl = "http://swoogle.umbc.edu/StsService/GetStsSim?operation=api&";

    @Override
    public double similarity(String phrase1, String phrase2) throws Exception {
        String p1 = URLEncoder.encode(phrase1, "UTF-8");
        String p2 = URLEncoder.encode(phrase2, "UTF-8");
        String url = serviceUrl + "phrase1=" + p1 + "&phrase2=" + p2;
        InputStream fileStream = new URL(url).openStream();
        Reader decoder = new InputStreamReader(fileStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        String l = buffered.readLine();
        return Double.parseDouble(l);
    }

    public static void main(String[] args) throws Exception {
        SwoogleSimilarityClient qa = new SwoogleSimilarityClient();
        double res = qa.similarity("creator", "creature");
        System.out.println(res);
    }
}
