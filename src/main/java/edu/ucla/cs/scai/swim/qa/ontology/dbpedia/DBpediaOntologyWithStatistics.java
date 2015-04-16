/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.ucla.cs.scai.swim.qa.ontology.AttributeLookupResult;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaOntologyWithStatistics extends DBpediaOntology {

    private static boolean statisticsAdded = false;

    public static DBpediaOntology getInstance() {
        DBpediaOntology instance = DBpediaOntology.getInstance();
        if (!statisticsAdded) {
            for (DBpediaCategory c : instance.categoriesByUri.values()) {
                c.getDomainOfAttributes().clear();
                c.getRangeOfAttributes().clear();
            }
            try {
                BufferedReader in = new BufferedReader(new FileReader("/home/massimo/attributestats.json"));
                String l = in.readLine();
                String json = "";
                while (l != null) {
                    json += l;
                    l = in.readLine();
                }
                in.close();
                Gson gson = new Gson();
                Type type = new TypeToken<HashMap<String, DBpediaAttribute>>() {
                }.getType();
                HashMap<String, DBpediaAttribute> stats = gson.fromJson(json, type);
                for (String attUri : stats.keySet()) {
                    DBpediaAttribute cachedAtt = stats.get(attUri);
                    DBpediaAttribute ontoAtt = instance.attributesByUri.get(attUri);
                    ontoAtt.domainDistribution = cachedAtt.domainDistribution;
                    ontoAtt.rangeDistribution = cachedAtt.rangeDistribution;
                    ontoAtt.triplesCount = cachedAtt.triplesCount;
                }
                int totalTriples = 0;
                for (String attUri : instance.attributesByUri.keySet()) {
                    DBpediaAttribute ontoAtt = instance.attributesByUri.get(attUri);
                    System.out.println(attUri + " " + ontoAtt.triplesCount + " triples");
                    if (ontoAtt.triplesCount == 0) {
                        System.out.println("========================>> NOT FOUND <<=============================");
                    } else {
                        System.out.println("Domain: " + ontoAtt.domainUri);
                        totalTriples += ontoAtt.triplesCount;
                        for (Map.Entry<String, Double> e : ontoAtt.domainDistribution.entrySet()) {
                            System.out.println("\t" + e.getKey() + " -> " + (e.getValue() / ontoAtt.triplesCount));
                            DBpediaCategory cat = instance.categoriesByUri.get(e.getKey());
                            cat.domainOfAttributes.add(ontoAtt);
                        }
                        System.out.println("Range: " + ontoAtt.rangeUri);
                        for (Map.Entry<String, Double> e : ontoAtt.rangeDistribution.entrySet()) {
                            System.out.println("\t" + e.getKey() + " -> " + (e.getValue() / ontoAtt.triplesCount));
                            DBpediaCategory cat = instance.categoriesByUri.get(e.getKey());
                            if (cat != null) {
                                cat.rangeOfAttributes.add(ontoAtt);
                            }
                        }
                    }
                }
                System.out.println("Total triples found: " + totalTriples);
                instance.attributeLookup = new DBpediaAttributeLookupWithStatistics(instance.attributeLookup.similarityClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
            statisticsAdded = true;
        }
        return instance;
    }

    public static void main(String[] args) {
        DBpediaOntologyWithStatistics.getInstance();
    }
}
