/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia.tipicality;

import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntology;
import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Shell {

    private static HashMap<String, Integer> categoryCount = new HashMap<>();
    private static HashMap<String, Integer> attributeCount = new HashMap<>();
    private static HashMap<String, HashMap<String, Integer>> categoryAttributeCount = new HashMap<>();
    private static HashMap<String, HashMap<String, Integer>> attributeCategoryCount = new HashMap<>();
    private static HashSet<String> attributes = new HashSet<>();
    private static HashSet<String> categories = new HashSet<>();

    private static final int topCategories = 10;
    private static final int topAttributes = 20;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = DBpediaOntology.DBPEDIA_CSV_FOLDER;
        if (args != null && args.length > 0) {
            path = args[0];
            if (!path.endsWith("/")) {
                path = path + "/";
            }
        }

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

        int n = 0;
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

        Scanner in = new Scanner(System.in);
        String l = in.nextLine().toLowerCase();
        ArrayList<String> attributesA = new ArrayList<>(attributes);

        while (true) {
            if (l.equals("exit")) {
                break;
            }
            if (l.startsWith("f ")) {
                StringTokenizer st = new StringTokenizer(l, " ");
                st.nextToken();
                HashSet<String> chosenAttributes = new HashSet<>();
                while (st.hasMoreTokens()) {
                    int i = Integer.parseInt(st.nextToken()) % attributesA.size();
                    chosenAttributes.add(attributesA.get(i));
                    System.out.println(attributesA.get(i));
                }
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
                    if (topKcategories.size() < topCategories) {
                        topKcategories.add(new Pair(category, p));
                        Collections.sort(topKcategories);
                    } else if (p > topKcategories.get(topCategories - 1).p) {
                        topKcategories.set(topCategories - 1, new Pair(category, p));
                        Collections.sort(topKcategories);
                    }
                }
                double maxProb = topKcategories.get(0).p;
                for (Pair p : topKcategories) {
                    p.p /= maxProb;
                    System.out.println(p.s + "\t" + p.p);
                }                

                HashMap<String, Double> rankedAttributes = new HashMap<>();

                for (Pair p : topKcategories) {
                    String category = p.getS();
                    double weight = p.getP();
                    System.out.println(category+" "+weight);
                    ArrayList<Pair> attributeRank = sortedCategoryAttributes.get(category);
                    int processedAttributes = 0;
                    for (Pair ap : attributeRank) {
                        if (chosenAttributes.contains(ap.s)) {
                            continue;
                        }
                        if (processedAttributes == 20) {
                            break;
                        }
                        processedAttributes++;
                        double r = ap.p * weight;
                        Double v = rankedAttributes.get(ap.s);
                        if (v == null) {
                            rankedAttributes.put(ap.s, r);
                        } else {
                            rankedAttributes.put(ap.s, r + v);
                        }
                    }
                }

                ArrayList<Pair> topKattributes = new ArrayList<>();
                
                for (Map.Entry<String, Double> e : rankedAttributes.entrySet()) {
                    topKattributes.add(new Pair(e.getKey(), e.getValue()));
                }
                
                Collections.sort(topKattributes);

                /*
                 for (String attribute : attributes) {
                 Integer ac = attributeCount.get(attribute); //number of instances in this category
                 if (ac == null) {
                 continue;
                 }
                 double p = 1.0 * ac / n;
                 double invac = 1.0 / (ac + 0.5 * categories.size());
                 for (Pair pair : topKcategories) {
                 String c = pair.s;
                 Integer pca = attributeCategoryCount.get(attribute).get(c);
                 if (pca != null) {
                 p *= (pca + 0.5) * invac;
                 } else {
                 p *= 0.5 * invac;
                 }
                 //p *= pair.p;
                 }
                 if (topKattributes.size() < topAttributes) {
                 topKattributes.add(new Pair(attribute, p));
                 Collections.sort(topKcategories);
                 } else if (p > topKattributes.get(topAttributes-1).p) {
                 topKattributes.set(topAttributes-1, new Pair(attribute, p));
                 Collections.sort(topKattributes);
                 }

                 }
                 */
                long end = System.currentTimeMillis();
                for (int i=0; i<topAttributes; i++) {
                    Pair p=topKattributes.get(i);
                    System.out.println(p.s + "\t" + p.p);
                }
                System.out.println((end - start) + " msec");
            } else if (l.startsWith("s ")) {
                StringTokenizer st = new StringTokenizer(l, " ");
                st.nextToken();
                String a = st.nextToken().toLowerCase();
                for (int i = 0; i < attributesA.size(); i++) {
                    String s = attributesA.get(i);
                    if (s.toLowerCase().contains(a)) {
                        System.out.println(i + ": " + s);
                    }
                }
            }
            l = in.nextLine().toLowerCase();
        }
    }
}