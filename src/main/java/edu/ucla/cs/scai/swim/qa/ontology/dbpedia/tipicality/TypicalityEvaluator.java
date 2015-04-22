/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia.tipicality;

import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntology;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntologyOld;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class TypicalityEvaluator {

    private HashMap<String, Integer> categoryCount = new HashMap<>();
    private HashMap<String, Integer> attributeCount = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> categoryAttributeCount = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> attributeCategoryCount = new HashMap<>();
    private HashSet<String> attributes = new HashSet<>();
    private HashSet<String> categories = new HashSet<>();
    private int n = 0;

    public TypicalityEvaluator(String fileName) throws IOException, ClassNotFoundException {
        File f=new File(fileName);
        if (!f.exists()) {
            System.out.println("The file "+fileName+" does not exist. Please, use the correct path.");
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            categories = (HashSet<String>) ois.readObject();
            attributes = (HashSet<String>) ois.readObject();
            categoryCount = (HashMap<String, Integer>) ois.readObject();
            attributeCount = (HashMap<String, Integer>) ois.readObject();
            categoryAttributeCount = (HashMap<String, HashMap<String, Integer>>) ois.readObject();
            attributeCategoryCount = (HashMap<String, HashMap<String, Integer>>) ois.readObject();
        }

        System.out.println(categories.size() + " categories found");
        System.out.println(attributes.size() + " attributes found");

        for (Map.Entry<String, Integer> e : categoryCount.entrySet()) {
            n += e.getValue();
        }

        System.out.println(n);

        HashMap<String, ArrayList<Pair>> sortedCategoryAttributes = new HashMap<>();

        for (String category : categories) {
            //System.out.println(category);
            //System.out.println("-----------");
            ArrayList<Pair> attributesRank = new ArrayList<>();
            Integer c = categoryCount.get(category);
            if (c == null || c == 0) {
                continue;
            }
            HashMap<String, Integer> thisCategoryAttributeCount = categoryAttributeCount.get(category);
            for (Map.Entry<String, Integer> e : thisCategoryAttributeCount.entrySet()) {
                attributesRank.add(new Pair(e.getKey(), 1.0 * e.getValue() / c));
            }
            Collections.sort(attributesRank);
            for (Pair p : attributesRank) {
                //System.out.println("A:" + p.getS() + "\t" + p.getP());
            }
            //System.out.println("===============================");
            sortedCategoryAttributes.put(category, attributesRank);
        }

        for (String attribute : attributes) {
            //System.out.println(attribute);
            //System.out.println("-----------");
            ArrayList<Pair> categoriesRank = new ArrayList<>();
            Integer a = attributeCount.get(attribute);
            if (a == null || a == 0) {
                continue;
            }
            HashMap<String, Integer> thisAttributeCategoryCount = attributeCategoryCount.get(attribute);
            for (Map.Entry<String, Integer> e : thisAttributeCategoryCount.entrySet()) {
                categoriesRank.add(new Pair(e.getKey(), 1.0 * e.getValue() / a));
            }
            Collections.sort(categoriesRank);
            for (Pair p : categoriesRank) {
                //System.out.println("C:" + p.getS() + "\t" + p.getP());
            }
            //System.out.println("===============================");
        }
    }

    public ArrayList<Pair> findTopKCategories(int k, Collection<String> chosenAttributes, boolean normalizeRank) {
        long start = System.currentTimeMillis();
        ArrayList<Pair> topKcategories = new ArrayList<>();
        for (String category : categories) {
            Integer cc = categoryCount.get(category); //number of instances in this category
            if (cc == null) {
                continue;
            }
            double p = 1.0 * cc / n;
            double invcc = 1.0 / (cc + 0.5 * attributes.size());
            for (String a : chosenAttributes) {
                Integer pac = categoryAttributeCount.get(category).get(a);
                if (pac != null) {
                    p *= (pac + 0.5) * invcc;
                } else {
                    p *= 0.5 * invcc;
                }
            }
            if (topKcategories.size() < k) {
                topKcategories.add(new Pair(category, p));
                Collections.sort(topKcategories);
            } else if (p > topKcategories.get(k - 1).p) {
                topKcategories.set(k - 1, new Pair(category, p));
                Collections.sort(topKcategories);
            }
        }
        double maxProb = topKcategories.get(0).p;
        for (Pair p : topKcategories) {
            if (normalizeRank) {
                p.p /= maxProb;
            }
            //System.out.println(p.s + "\t" + p.p);
        }

        long end = System.currentTimeMillis();
        //System.out.println((end - start) + " msec");

        return topKcategories;
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        TypicalityEvaluator te = new TypicalityEvaluator(DBpediaOntologyOld.DBPEDIA_CSV_FOLDER+"counts.bin");
        ArrayList<String> a = new ArrayList<>();
        a.add("http://dbpedia.org/property/state");
        a.add("http://dbpedia.org/ontology/barPassRate");
        //a.add("http://dbpedia.org/ontology/numberOfStudents");
        te.findTopKCategories(5, a, true);
    }

}
