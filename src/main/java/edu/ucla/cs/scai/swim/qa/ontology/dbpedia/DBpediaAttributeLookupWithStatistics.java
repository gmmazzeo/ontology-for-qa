/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.AttributeLookupResult;
import edu.ucla.cs.scai.swim.qa.ontology.NamedEntity;
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
public class DBpediaAttributeLookupWithStatistics extends DBpediaAttributeLookup {

    public DBpediaAttributeLookupWithStatistics(SimilarityClient similarityClient) {
        super(similarityClient);
    }

    @Override
    public ArrayList<DBpediaAttributeLookupResult> lookup(String attributeName, Set<String> subjectTypes, Set<String> valueTypes, NamedEntity domainEntity, NamedEntity rangeEntity, boolean ignoreAttributeRange) {
        ignoreAttributeRange=false;
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
                        for (String range : att.rangeDistribution.keySet()) {
                            if (range.equals(valueType)) {
                                rangeMatch = true;
                                break;
                            } else {
                                //DBpediaCategory c1 = DBpediaOntology.getInstance().categoriesByUri.get(valueType);
                                //DBpediaCategory c2 = DBpediaOntology.getInstance().categoriesByUri.get(range);
                                //if (c1 != null && c2 != null && c1.hasAncestor(c2)) {
                                //    rangeMatch = true;
                                //    break;
                                //}
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
            DBpediaCategory subjCat = DBpediaOntology.getInstance().categoriesByUri.get(sts);
            if (subjCat == null) {
                continue;
            }
            for (DBpediaAttribute att : subjCat.domainOfAttributes) {
                boolean rangeMatch = false;
                for (String valueType : subjectTypes) {
                    for (String range : att.rangeDistribution.keySet()) {
                        if (range.equals(valueType)) {
                            rangeMatch = true;
                            break;
                        } else {
                            //DBpediaCategory c1 = DBpediaOntology.getInstance().categoriesByUri.get(valueType);
                            //DBpediaCategory c2 = DBpediaOntology.getInstance().categoriesByUri.get(range);
                            //if (c1 != null && c2 != null && c1.hasAncestor(c2)) {
                            //    rangeMatch = true;
                            //    break;
                            //}
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
}
