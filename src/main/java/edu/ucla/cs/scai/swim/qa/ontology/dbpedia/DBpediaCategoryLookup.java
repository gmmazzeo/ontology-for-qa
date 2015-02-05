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
public class DBpediaCategoryLookup {

    private static final double similarityCutOff = 0.5;

    private static HashMap<String, ArrayList<DBpediaCategoryLookupResult>> cache = new HashMap<>();

    SimilarityClient similarityClient;

    private static synchronized void updateCache(String categoryName, ArrayList<DBpediaCategoryLookupResult> result) {
        cache.put(categoryName, result);
    }

    private static synchronized ArrayList<DBpediaCategoryLookupResult> readCache(String categoryName) {
        return cache.get(categoryName);
    }

    public DBpediaCategoryLookup(SimilarityClient similarityClient) {
        this.similarityClient = similarityClient;
    }

    public ArrayList<DBpediaCategoryLookupResult> lookup(String categoryName) {
        String[] categoryNames = categoryName.toLowerCase().split(" ");
        /*
        Arrays.sort(categoryNames);
        categoryName = categoryNames[0];
        for (int i = 1; i < categoryNames.length; i++) {
            categoryName += " " + categoryNames[i];
        }
        */
        ArrayList<DBpediaCategoryLookupResult> res = readCache(categoryName);
        if (res == null) {
            res = new ArrayList<>();
            double maxSimilarity = 0;
            double similarityThreshold = 0;
            for (DBpediaCategory c : DBpediaOntology.getInstance().categoryMap.values()) {
                double similarity = 0;
                try {
                    similarity=similarityClient.similarity(c.words, categoryName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (similarity >= similarityCutOff * maxSimilarity && similarity > 0) {
                    if (similarity > maxSimilarity) {
                        maxSimilarity = similarity;
                        similarityThreshold = maxSimilarity * similarityCutOff;
                    }
                    res.add(new DBpediaCategoryLookupResult(c, similarity));
                }
            }
            Collections.sort(res);
            for (Iterator<DBpediaCategoryLookupResult> it = res.iterator(); it.hasNext();) {
                if (it.next().getWeight() < similarityThreshold) {
                    it.remove();
                }
            }
            updateCache(categoryName, res);
        }

        return res;
    }

}
