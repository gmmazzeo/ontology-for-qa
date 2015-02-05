/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaAttributeLookup {

    private static final double relSimilarityThreshold = 0.5;
    private static final double absSimilarityThreshold = 0.5;

    SimilarityClient similarityClient;

    private static HashMap<String, ArrayList<DBpediaAttributeLookupResult>> cache = new HashMap<>();

    private static synchronized void updateCache(String attributeName, ArrayList<DBpediaAttributeLookupResult> result) {
        cache.put(attributeName, result);
    }

    private static synchronized ArrayList<DBpediaAttributeLookupResult> readCache(String attributeName) {
        return cache.get(attributeName);
    }

    public DBpediaAttributeLookup(SimilarityClient similarityClient) {
        this.similarityClient = similarityClient;
    }

    public ArrayList<DBpediaAttributeLookupResult> lookup(String attributeName) {        
        String[] attributeNames = attributeName.toLowerCase().split(" ");
        /*
        Arrays.sort(attributeNames);
        attributeName = attributeNames[0];
        for (int i = 1; i < attributeNames.length; i++) {
            attributeName += "," + attributeNames[i];
        }
        */
        ArrayList<DBpediaAttributeLookupResult> res = readCache(attributeName);
        if (res == null) {
            res = new ArrayList<>();
            double maxSimilarity = 0;
            double similarityThreshold = 0;
            for (HashSet<DBpediaAttribute> s : DBpediaOntology.getInstance().attributeMap.values()) {
                for (DBpediaAttribute a : s) {
                    if (a.words.contains(" label")) {
                        continue;
                    }
                    double similarity = 0;
                    try {
                        similarity=similarityClient.similarity(a.words, attributeName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (similarity >= relSimilarityThreshold * maxSimilarity && similarity > absSimilarityThreshold / attributeNames.length) {
                        if (similarity > maxSimilarity) {
                            maxSimilarity = similarity;
                            similarityThreshold = maxSimilarity * relSimilarityThreshold;
                        }
                        res.add(new DBpediaAttributeLookupResult(a, similarity));
                    }
                }
            }
            Collections.sort(res);
            ArrayList<DBpediaAttributeLookupResult> filteredRes = new ArrayList<>();
            for (DBpediaAttributeLookupResult ar : res) {
                if (ar.getWeight() >= similarityThreshold) {
                    filteredRes.add(ar);
                } else {
                    break;
                }
            }
            res = filteredRes;
            updateCache(attributeName, res);
        }

        return res;
    }

    public static void main(String[] args) {
        DBpediaAttributeLookup l = new DBpediaAttributeLookup(new SwoogleSimilarityClient());
        for (DBpediaAttributeLookupResult r : l.lookup("pass rate")) {
            System.out.println(r.getAttribute().label);
        }
    }

}
