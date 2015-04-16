/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class SwoogleSimilarityClient implements SimilarityClient {

    //public final static String serviceUrl = "http://swoogle.umbc.edu/StsService/GetStsSim?operation=api&";
    public final static String serviceUrl = "http://swoogle.umbc.edu/SimService/GetSimilarity?operation=api&";

    private final static String CACHE_FILE = "/home/massimo/swoogle.cache";

    static final HashMap<String, HashMap<String, Double>> cache = new HashMap<>();

    static {
        int pairs = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(CACHE_FILE));) {
            //load cache from disk            
            String l = in.readLine();
            int i = 1;
            while (l != null) {
                StringTokenizer st = new StringTokenizer(l, "|");
                String p1;
                String p2;
                double s;
                try {
                    p1 = st.nextToken();
                    p2 = st.nextToken();
                    s = Double.parseDouble(st.nextToken());
                    if (s == Double.NEGATIVE_INFINITY) {
                        s = 0;
                    }
                } catch (NoSuchElementException e) {
                    System.out.println("Wrong line " + i + ": " + l);
                    l = in.readLine();
                    i++;
                    continue;
                }
                String p1n = normalize(p1);
                String p2n = normalize(p2);
                if (p1n.compareTo(p2n) > 0) {
                    p1 = p2n;
                    p2 = p1n;
                } else {
                    p1 = p1n;
                    p2 = p2n;
                }
                HashMap<String, Double> h = cache.get(p1);
                if (h == null) {
                    h = new HashMap<>();
                    cache.put(p1, h);
                }
                h.put(p2, s);
                l = in.readLine();
                i++;
            }
        } catch (Exception ex) {
            Logger.getLogger(SwoogleSimilarityClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        //rewrite the clean cache        
        try (PrintWriter out = new PrintWriter(new FileWriter(CACHE_FILE, false), true)) {
            for (Map.Entry<String, HashMap<String, Double>> e1 : cache.entrySet()) {
                pairs += e1.getValue().size();
                for (Map.Entry<String, Double> e2 : e1.getValue().entrySet()) {
                    out.println(e1.getKey() + "|" + e2.getKey() + "|" + e2.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Pairs in cache: " + pairs);
    }

    private static String normalize(String p) {
        String[] a = p.toLowerCase().split(" ");
        Arrays.sort(a);
        String res = "";
        for (String s : a) {
            if (s == null || s.length() == 0) {
                continue;
            }
            if (res.length() > 0) {
                res += " ";
            }
            res += s;
        }
        return res;
    }

    private static synchronized Double getSimilarity(String p1, String p2) {
        String p1n = normalize(p1);
        String p2n = normalize(p2);
        if (p1n.compareTo(p2n) > 0) {
            p1 = p2n;
            p2 = p1n;
        } else {
            p1 = p1n;
            p2 = p2n;
        }
        HashMap<String, Double> h = cache.get(p1);
        if (h == null) {
            return null;
        }
        return h.get(p2);
    }

    private static synchronized void updateCache(String p1, String p2, double s) {
        String p1n = normalize(p1);
        String p2n = normalize(p2);
        if (p1n.compareTo(p2n) > 0) {
            p1 = p2n;
            p2 = p1n;
        } else {
            p1 = p1n;
            p2 = p2n;
        }
        HashMap<String, Double> h = cache.get(p1);
        if (h == null) {
            h = new HashMap<>();
            cache.put(p1, h);
        }
        h.put(p2, s);
        try (PrintWriter out = new PrintWriter(new FileOutputStream(CACHE_FILE, true), true)) {
            out.println(p1 + "|" + p2 + "|" + s);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SwoogleSimilarityClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public double similarity(String phrase1, String phrase2) throws Exception {
        Double s = getSimilarity(phrase1, phrase2);
        if (s == null) {
            String p1 = URLEncoder.encode(phrase1, "UTF-8");
            String p2 = URLEncoder.encode(phrase2, "UTF-8");
            String url = serviceUrl + "phrase1=" + p1 + "&phrase2=" + p2;
            InputStream fileStream = new URL(url).openStream();
            Reader decoder = new InputStreamReader(fileStream, "UTF-8");
            BufferedReader buffered = new BufferedReader(decoder);
            String l = buffered.readLine();
            s = Double.parseDouble(l);
            if (s == Double.NEGATIVE_INFINITY) {
                s = 0d;
            }
            updateCache(phrase1, phrase2, s);
        }
        return s;
    }

    public double similarityWithoutCache(String phrase1, String phrase2) throws Exception {
        String p1 = URLEncoder.encode(phrase1, "UTF-8");
        String p2 = URLEncoder.encode(phrase2, "UTF-8");
        String url = serviceUrl + "phrase1=" + p1 + "&phrase2=" + p2;
        InputStream fileStream = new URL(url).openStream();
        Reader decoder = new InputStreamReader(fileStream, "UTF-8");
        BufferedReader buffered = new BufferedReader(decoder);
        String l = buffered.readLine();
        double s = Double.parseDouble(l);
        return s;
    }

    public static void main(String[] args) throws Exception {
        SwoogleSimilarityClient qa = new SwoogleSimilarityClient();
        double res = qa.similarity("by direct", "effects special");
        System.out.println(res);
        res = qa.similarityWithoutCache("by direct", "effects special");
        System.out.println(res);

    }
}
