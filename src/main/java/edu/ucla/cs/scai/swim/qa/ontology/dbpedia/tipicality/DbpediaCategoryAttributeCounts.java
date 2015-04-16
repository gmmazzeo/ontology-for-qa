/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia.tipicality;

import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntology;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DbpediaCategoryAttributeCounts {

    private static final HashMap<String, Integer> categoryCount = new HashMap<>();
    private static final HashMap<String, Integer> attributeCount = new HashMap<>();
    private static final HashMap<String, HashMap<String, Integer>> categoryAttributeCount = new HashMap<>();
    private static final HashMap<String, HashMap<String, Integer>> attributeCategoryCount = new HashMap<>();
    private static final HashSet<String> entities = new HashSet<>();
    private static final HashSet<String> attributes = new HashSet<>();
    private static final HashSet<String> categories = new HashSet<>();
    private static final HashSet<String> stopAttributes = new HashSet<>();

    private static void processFile(File csvData, String category) throws IOException {
        BufferedReader in=new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(csvData))));
        //CSVParser parser = CSVParser.parse(csvData, Charset.defaultCharset(), CSVFormat.RFC4180);
        CSVParser parser = CSVFormat.EXCEL.parse(in);
        int r = 0;
        ArrayList<Integer> attributePositions = new ArrayList<>();
        ArrayList<String> attributeNames = new ArrayList<>();
        HashMap<String, Integer> thisCategoryAttributeCounts = new HashMap<>();
        categoryAttributeCount.put(category, thisCategoryAttributeCounts);
        for (CSVRecord csvRecord : parser) {
            if (r == 0) {
                Iterator<String> it = csvRecord.iterator();
                it.next(); //skip URI
                if (!it.hasNext()) { //it is an empty file
                    return;
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
                categories.add(category);
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
                Iterator<String> it = csvRecord.iterator();
                String uri = it.next();
                /*if (entities.contains(uri)) {
                 System.out.println(uri + " already processed");
                 continue;
                 }*/
                entities.add(uri);
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
                                Integer ac = attributeCount.get(attribute);
                                if (ac == null) {
                                    attributeCount.put(attribute, 1);
                                } else {
                                    attributeCount.put(attribute, ac + 1);
                                }

                                Integer tcac = thisCategoryAttributeCounts.get(attribute);
                                if (tcac == null) {
                                    thisCategoryAttributeCounts.put(attribute, 1);
                                } else {
                                    thisCategoryAttributeCounts.put(attribute, tcac + 1);
                                }

                                HashMap<String, Integer> thisAttributeCategoryCounts = attributeCategoryCount.get(attribute);
                                if (thisAttributeCategoryCounts == null) {
                                    thisAttributeCategoryCounts = new HashMap<>();
                                    attributeCategoryCount.put(attribute, thisAttributeCategoryCounts);
                                }
                                Integer tacc = thisAttributeCategoryCounts.get(category);
                                if (tacc == null) {
                                    thisAttributeCategoryCounts.put(category, 1);
                                } else {
                                    thisAttributeCategoryCounts.put(category, tacc + 1);
                                }
                            }
                        }
                        i++;
                    }
                }
            }
            r++;
        }
        categoryCount.put(category, r-3);
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {

        stopAttributes.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        stopAttributes.add("http://www.w3.org/2002/07/owl#sameAs");
        stopAttributes.add("http://dbpedia.org/ontology/wikiPageRevisionID");
        stopAttributes.add("http://dbpedia.org/ontology/wikiPageID");
        stopAttributes.add("http://purl.org/dc/elements/1.1/description");
        stopAttributes.add("http://dbpedia.org/ontology/thumbnail");
        //stopAttributes.add("http://dbpedia.org/ontology/type");

        String path=DBpediaOntology.DBPEDIA_CSV_FOLDER;

        if (args != null && args.length > 0) {
            path = args[0];
            if (!path.endsWith("/")) {
                path = path + "/";
            }
        }

        File folder=new File(path);
        if (!folder.exists()) {
            System.out.println("The path with DBpedia CSV files is set as "+path);
            System.out.println("You need to change the path with the correct one on your PC");
            System.exit(0);
        }
        for (File f : new File(path).listFiles()) {
            if (f.isFile() && f.getName().endsWith(".csv.gz")) {
                System.out.println("Processing file " + f.getName() + "...");
                processFile(f, f.getName().replaceAll("\\.csv.gz", ""));
            }
        }

        System.out.println(entities.size() + " entities processed");
        System.out.println(categories.size() + " categories found");
        System.out.println(attributes.size() + " attributes found");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + "counts.bin"))) {
            oos.writeObject(categories);
            oos.writeObject(attributes);
            oos.writeObject(categoryCount);
            oos.writeObject(attributeCount);
            oos.writeObject(categoryAttributeCount);
            oos.writeObject(attributeCategoryCount);
        }
    }

}