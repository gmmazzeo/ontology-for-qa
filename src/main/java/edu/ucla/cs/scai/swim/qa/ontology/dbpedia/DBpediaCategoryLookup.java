/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaCategoryLookup {

    private static final double relSimilarityThreshold = 0.5;
    private static final double absSimilarityThreshold = 0.3;

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
        if (categoryName.length() == 0) {
            return new ArrayList<>();
        }
        String[] categoryNames = categoryName.toLowerCase().split(" ");
        ArrayList<DBpediaCategoryLookupResult> res = readCache(categoryName);
        if (res == null) {
            res = new ArrayList<>();
            double maxSimilarity = 0;
            double similarityThreshold = 0;
            for (String cat : DBpediaOntology.getInstance().categories) {
                DBpediaCategory c = DBpediaOntology.getInstance().categoriesByUri.get(cat);
                if (c == null) {
                    System.out.println("category error: " + cat);
                    continue;
                }
                if (c.words == null || c.words.length() == 0) {
                    continue;
                }
                double similarity = 0;
                try {
                    similarity = similarityClient.conceptSimilarity(c.words, categoryName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (similarity >= relSimilarityThreshold * maxSimilarity && similarity > absSimilarityThreshold / categoryNames.length) {
                    if (similarity > maxSimilarity) {
                        maxSimilarity = similarity;
                        similarityThreshold = maxSimilarity * relSimilarityThreshold;
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
