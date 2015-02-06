/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia.tipicality;

import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntology;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Test {

    private static HashMap<String, Integer> categoryCount = new HashMap<>();
    private static HashMap<String, Integer> attributeCount = new HashMap<>();
    private static HashMap<String, HashMap<String, Integer>> categoryAttributeCount = new HashMap<>();
    private static HashMap<String, HashMap<String, Integer>> attributeCategoryCount = new HashMap<>();
    private static HashSet<String> attributes = new HashSet<>();
    private static HashSet<String> categories = new HashSet<>();
    private static final HashSet<String> stopAttributes = new HashSet<>();

    private static final int topCategories = 10;
    private static final int topAttributes = 20;
    private static int n;

    private static ArrayList<String> rankedCategories(HashSet<String> chosenAttributes) {
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
            if (topKcategories.size() < topCategories) {
                topKcategories.add(new Pair(category, p));
                Collections.sort(topKcategories);
            } else if (p > topKcategories.get(topCategories - 1).p) {
                topKcategories.set(topCategories - 1, new Pair(category, p));
                Collections.sort(topKcategories);
            }
        }
        ArrayList<String> rankedCategories = new ArrayList<>();
        for (Pair p : topKcategories) {
            rankedCategories.add(p.s);
        }
        return rankedCategories;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = DBpediaOntology.DBPEDIA_CSV_FOLDER;
        if (args != null && args.length > 0) {
            path = args[0];
            if (!path.endsWith("/")) {
                path = path + "/";
            }
        }

        stopAttributes.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        stopAttributes.add("http://www.w3.org/2002/07/owl#sameAs");
        stopAttributes.add("http://dbpedia.org/ontology/wikiPageRevisionID");
        stopAttributes.add("http://dbpedia.org/ontology/wikiPageID");
        stopAttributes.add("http://purl.org/dc/elements/1.1/description");
        stopAttributes.add("http://dbpedia.org/ontology/thumbnail");
        stopAttributes.add("http://dbpedia.org/ontology/type");

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path + "counts.bin"))) {
            categories = (HashSet<String>) ois.readObject();
            attributes = (HashSet<String>) ois.readObject();
            categoryCount = (HashMap<String, Integer>) ois.readObject();
            attributeCount = (HashMap<String, Integer>) ois.readObject();
            categoryAttributeCount = (HashMap<String, HashMap<String, Integer>>) ois.readObject();
            attributeCategoryCount = (HashMap<String, HashMap<String, Integer>>) ois.readObject();
        }

        System.out.println(categories.size() + " categories found");
        System.out.println(attributes.size() + " attributes found");

        n = 0;
        for (Map.Entry<String, Integer> e : categoryCount.entrySet()) {
            n += e.getValue();
        }

        System.out.println(n);

        HashMap<String, ArrayList<Pair>> sortedCategoryAttributes = new HashMap<>();

        for (String category : categories) {
            //System.out.println(category);
            //System.out.println("-----------");
            ArrayList<Pair> attributesRank = new ArrayList<Pair>();
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

        HashMap<Integer, Integer> histogram = new HashMap<>();
        histogram.put(0, 0);
        histogram.put(1, 0);
        histogram.put(2, 0);
        histogram.put(Integer.MAX_VALUE, 0);

        int nTest = 0;

        if (args != null && args.length > 0) {
            path = args[0];
            if (!path.endsWith("/")) {
                path = path + "/";
            }
        }

        for (File f : new File(path).listFiles()) {
            if (f.isFile() && f.getName().endsWith(".csv")) {
                String category = f.getName().replaceFirst("\\.csv", "");
                System.out.println("Category: " + category);
                ArrayList<HashSet<String>> entities = extractEntities(f, 2);
                for (HashSet<String> attributesOfThisEntity : entities) {
                    nTest++;
                    ArrayList<String> rankedCategories = rankedCategories(attributesOfThisEntity);
                    boolean found = false;
                    for (int i = 0; i < rankedCategories.size() && !found; i++) {
                        if (rankedCategories.get(i).equals(category)) {
                            Integer count = histogram.get(i);
                            if (count == null) {
                                histogram.put(i, 1);
                            } else {
                                histogram.put(i, count + 1);
                            }
                            found = true;
                        }
                    }
                    if (!found) {
                        histogram.put(Integer.MAX_VALUE, histogram.get(Integer.MAX_VALUE) + 1);
                    }
                }
                System.out.println("Tested entities: "+nTest);
                System.out.println("1: "+histogram.get(0));
                System.out.println("2: "+histogram.get(1));
                System.out.println("3: "+histogram.get(2));
                System.out.println("+3: "+(nTest-histogram.get(2)-histogram.get(1)-histogram.get(0)-histogram.get(Integer.MAX_VALUE)));
                System.out.println("NF: "+histogram.get(Integer.MAX_VALUE));
            }
        }
    }

    private static ArrayList<HashSet<String>> extractEntities(File csvData, int nOfAttributes) throws IOException {
        CSVParser parser = CSVParser.parse(csvData, Charset.defaultCharset(), CSVFormat.RFC4180);
        int r = 0;
        ArrayList<Integer> attributePositions = new ArrayList<>();
        ArrayList<String> attributeNames = new ArrayList<>();
        ArrayList<HashSet<String>> res = new ArrayList<>();
        for (CSVRecord csvRecord : parser) {
            if (r == 0) {
                Iterator<String> it = csvRecord.iterator();
                it.next(); //skip URI
                if (!it.hasNext()) { //it is an empty file
                    return res;
                }
                it.next(); //skip rdf-schema#label
                it.next(); //skip rdf-schema#comment
                int c = 2;
                for (; it.hasNext();) {
                    c++;
                    String attr = it.next();
                    if (!attr.endsWith("_label")) {
                        attributePositions.add(c);
                    }
                }
            } else if (r == 1) {
                Iterator<String> it = csvRecord.iterator();
                it.next(); //skip uri
                it.next(); //skip rdf-schema#label
                it.next(); //skip rdf-schema#comment
                int c = 2;
                int i = 0;
                while (i < attributePositions.size()) {
                    c++;
                    String attr = it.next();
                    if (attributePositions.get(i) == c) {
                        if (!stopAttributes.contains(attr)) {
                            attributes.add(attr);
                        }
                        attributeNames.add(attr);
                        i++;
                    }
                }
            } else if (r > 3) {
                ArrayList<String> attributesOfThisEntity = new ArrayList<>();
                Iterator<String> it = csvRecord.iterator();
                String uri = it.next();
                it.next(); //skip rdf-schema#label
                it.next(); //skip rdf-schema#comment
                int c = 2;
                int i = 0;
                while (i < attributePositions.size()) {
                    c++;
                    String val = it.next();
                    if (attributePositions.get(i) == c) {
                        if (!val.equalsIgnoreCase("null")) {
                            String attribute = attributeNames.get(i);
                            if (!stopAttributes.contains(attribute)) {
                                attributesOfThisEntity.add(attribute);
                            }
                        }
                        i++;
                    }
                }
                Collections.shuffle(attributesOfThisEntity);
                HashSet<String> s = new HashSet<>();
                for (int k = 0; k < Math.min(nOfAttributes, attributesOfThisEntity.size()); k++) {
                    s.add(attributesOfThisEntity.get(k));
                }
                res.add(s);
            }
            r++;
        }
        return res;
    }
}
