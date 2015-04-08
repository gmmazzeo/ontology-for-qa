/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.AttributeLookupResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaAttributeLookup {

    public static final double relSimilarityThreshold = 0.5;
    public static final double absSimilarityThreshold = 0.1;

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
            for (DBpediaAttribute a : DBpediaOntology.getInstance().attributesByUri.values()) {
                if (a.words.contains(" label")) {
                    continue;
                }
                double similarity = 0;
                try {
                    similarity = similarityClient.similarity(a.words, attributeName);
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

    public ArrayList<DBpediaAttributeLookupResult> lookup(String attributeName, Set<String> subjectTypes, Set<String> valueTypes, boolean ignoreAttributeRange) {
        ArrayList<DBpediaAttributeLookupResult> res = new ArrayList<>();
        String[] attributeNames = attributeName.toLowerCase().split(" ");

        boolean basicTypeAmongValues = false;
        for (String s : valueTypes) {
            if (s.equals("basicType")) {
                basicTypeAmongValues = true;
                break;
            }
        }
        double maxSimilarity = 0;
        for (String sts : subjectTypes) {
            DBpediaCategory subjCat = DBpediaOntology.getInstance().categoriesByUri.get(sts);
            if (subjCat == null) {
                continue;
            }
            for (DBpediaAttribute att : subjCat.domainOfAttributes) {
                boolean rangeMatch = false;
                if (att.rangeCanBeBasicType && basicTypeAmongValues) {
                    rangeMatch = true;
                } else { //check a matching between the value types and the ranges of the attribute
                    for (String valueType : valueTypes) {
                        if (valueType.equals("basicType")) {
                            continue;
                        }
                        for (String range : att.rangeUri) {
                            if (range.equals(valueType)) {
                                rangeMatch = true;
                                break;
                            } else {
                                DBpediaCategory c1 = DBpediaOntology.getInstance().categoriesByUri.get(valueType);
                                DBpediaCategory c2 = DBpediaOntology.getInstance().categoriesByUri.get(range);
                                if (c1 != null && c2 != null && c1.hasAncestor(c2)) {
                                    rangeMatch = true;
                                    break;
                                }
                            }
                        }
                        if (rangeMatch) {
                            break;
                        }
                    }
                }
                if (rangeMatch || ignoreAttributeRange) {
                    DBpediaAttributeLookupResult lr = createAttributeLookupResult(att, attributeName, attributeNames, maxSimilarity, rangeMatch ? 1 : 0.7);
                    if (lr != null) {
                        if (lr.getWeight() > maxSimilarity) {
                            maxSimilarity = lr.getWeight();
                        }
                        res.add(lr);
                    }
                }
            }
        }

        //now look for the symmetric relationships
        for (String sts : valueTypes) {
            DBpediaCategory valueCat = DBpediaOntology.getInstance().categoriesByUri.get(sts);
            if (valueCat == null) {
                continue;
            }
            for (DBpediaAttribute att : valueCat.domainOfAttributes) {
                boolean rangeMatch = false;
                for (String subjType : subjectTypes) {
                    for (String range : att.rangeUri) {
                        if (range.equals(subjType)) {
                            rangeMatch = true;
                            break;
                        } else {
                            DBpediaCategory c1 = DBpediaOntology.getInstance().categoriesByUri.get(subjType);
                            DBpediaCategory c2 = DBpediaOntology.getInstance().categoriesByUri.get(range);
                            if (c1 != null && c2 != null && c1.hasAncestor(c2)) {
                                rangeMatch = true;
                                break;
                            }
                        }
                    }
                    if (rangeMatch) {
                        break;
                    }
                }
                if (rangeMatch || ignoreAttributeRange) {
                    DBpediaAttributeLookupResult lr = createAttributeLookupResult(att, attributeName, attributeNames, maxSimilarity, rangeMatch ? 1 : 0.7);
                    if (lr != null) {
                        if (lr.getWeight() > maxSimilarity) {
                            maxSimilarity = lr.getWeight();
                        }
                        lr.invertedRelationship = true;
                        res.add(lr);
                    }
                }
            }
        }

        Collections.sort(res);
        ArrayList<DBpediaAttributeLookupResult> filteredRes = new ArrayList<>();
        double similarityThreshold = maxSimilarity * relSimilarityThreshold;
        for (DBpediaAttributeLookupResult ar : res) {
            if (ar.getWeight() >= similarityThreshold) {
                filteredRes.add(ar);
            } else {
                break;
            }
        }
        res = filteredRes;
        return res;
    }

    protected DBpediaAttributeLookupResult createAttributeLookupResult(DBpediaAttribute att, String attributeName, String[] attributeNames, double maxSimilarity, double similarityMultiplier) {
        double similarity = 0;
        try {
            similarity = similarityClient.similarity(att.words, attributeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        similarity *= similarityMultiplier;
        if (similarity >= relSimilarityThreshold * maxSimilarity && similarity > absSimilarityThreshold / attributeNames.length) {
            return new DBpediaAttributeLookupResult(att, similarity);
        }
        return null;
    }
}
