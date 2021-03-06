/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.NamedEntity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
            for (String attr : DBpediaOntology.getInstance().attributes) {
                DBpediaAttribute a = DBpediaOntology.getInstance().attributesByUri.get(attr);
                if (a == null) {
                    System.out.println("attribute error: " + attr);
                    continue;
                }
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

    public ArrayList<DBpediaAttributeLookupResult> lookup(String attributeName, Set<String> subjectTypes, Set<String> valueTypes, NamedEntity domain, NamedEntity range, boolean ignoreAttributeRange) {
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
        HashSet<String> visitedAttr = new HashSet<>();

        if (domain != null) {
            for (String attr : ((DBpediaNamedEntity) domain).getDomainOfAttributes()) {
                DBpediaAttribute da = DBpediaOntology.getInstance().attributesByUri.get(attr);
                if (da != null) {
                    subjectTypes.addAll(da.getDomainUri());
                }
            }
        }

        if (range != null) {
            for (String attr : ((DBpediaNamedEntity) range).getRangeOfAttributes()) {
                DBpediaAttribute da = DBpediaOntology.getInstance().attributesByUri.get(attr);
                if (da != null) {
                    subjectTypes.addAll(da.getDomainUri());
                }
            }
        }

        for (String sts : subjectTypes) {
            DBpediaCategory subjCat = DBpediaOntology.getInstance().categoriesByUri.get(sts);
            if (subjCat == null) {
                continue;
            }
            for (DBpediaAttribute att : subjCat.domainOfAttributes) { //Idea: in order to reduce the number of attributes tested we could handle the attributes of the classes of the hierarchy separately - for instance (first we scan the attributes of MusicalArtist, then those of Artist, then those of Person, until reaching Thing. For each level we assign a weight. We keep the current maximum weight. If at a certain level, its weight is less than the maximum weight multiplied by a threshold level, then we stop the computation
                boolean rangeMatch = false;
                if (domain != null) {
                    if (!((DBpediaNamedEntity) domain).getDomainOfAttributes().contains(att.getUri())) {
                        continue; //We are excluding the models that cannot provide an answer, but, since the ontology can be incomplete, semantically correct models can be excluded (e.g., instruments played by Katy Parry)
                    }
                }
                if (range != null) {
                    if (!((DBpediaNamedEntity) range).getRangeOfAttributes().contains(att.getUri())) {
                        continue;
                    } else {
                        rangeMatch = true;
                    }
                }
                if (domain == null && range == null && !att.getUri().contains("http://dbpedia.org/ontology/")) {
                    continue;
                }
                if (att.rangeCanBeBasicType && basicTypeAmongValues) {
                    rangeMatch = true;
                } else if (domain != null && valueTypes.isEmpty()) { //domain matched
                    rangeMatch = true;
                }
                if (!rangeMatch && att.getUri().contains("http://dbpedia.org/ontology/")) { //check a matching between the value types and the ranges of the attribute
                    if (valueTypes.isEmpty() || att.rangeUri.isEmpty()) {
                        rangeMatch = true;
                    }
                    for (String valueType : valueTypes) {
                        if (valueType.equals("basicType")) {
                            continue;
                        }
                        for (String rangeUri : att.rangeUri) {
                            if (rangeUri.equals(valueType)) {
                                rangeMatch = true;
                                break;
                            } else {
                                DBpediaCategory c1 = DBpediaOntology.getInstance().categoriesByUri.get(valueType);
                                DBpediaCategory c2 = DBpediaOntology.getInstance().categoriesByUri.get(rangeUri);
                                if (c1 != null && c2 != null && (c1.hasAncestor(c2) || (c2.hasAncestor(c1) && !c1.equals(DBpediaOntology.thingCategory())))) {
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
                    if (visitedAttr.contains(att.getUri())) {
                        continue;
                    }
                    visitedAttr.add(att.getUri());
                    DBpediaAttributeLookupResult lr = createAttributeLookupResult(att, attributeName, attributeNames, maxSimilarity, rangeMatch ? 1 : 0.7);
                    if (lr != null) {
                        if (lr.getWeight() > maxSimilarity) {
                            maxSimilarity = lr.getWeight();
                        }
                        if (att.getUri().contains("http://dbpedia.org/property/")) {
                            lr.setWeight(0.999 * lr.getWeight());
                        }
                        res.add(lr);
                    }
                }
            }
        }

        if (valueTypes.isEmpty() && domain != null) {
            for (String attr : ((DBpediaNamedEntity) domain).getRangeOfAttributes()) {
                DBpediaAttribute da = DBpediaOntology.getInstance().attributesByUri.get(attr);
                if (da != null) {
                    valueTypes.addAll(da.getDomainUri());
                }
            }
        }

        if (range != null) {
            for (String attr : ((DBpediaNamedEntity) range).getRangeOfAttributes()) {
                DBpediaAttribute da = DBpediaOntology.getInstance().attributesByUri.get(attr);
                if (da != null) {
                    valueTypes.addAll(da.getDomainUri());
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
                if (domain != null) {
                    if (!((DBpediaNamedEntity) domain).getRangeOfAttributes().contains(att.getUri())) {
                        continue;
                    } else {
                        rangeMatch = true;
                    }
                }
                if (range != null) {
                    if (!((DBpediaNamedEntity) range).getDomainOfAttributes().contains(att.getUri())) {
                        continue;
                    }
                }
                if (!rangeMatch && att.getUri().contains("http://dbpedia.org/ontology/")) {
                    for (String subjType : subjectTypes) {
                        for (String rangeUri : att.rangeUri) {
                            if (rangeUri.equals(subjType)) {
                                rangeMatch = true;
                                break;
                            } else {
                                DBpediaCategory c1 = DBpediaOntology.getInstance().categoriesByUri.get(subjType);
                                DBpediaCategory c2 = DBpediaOntology.getInstance().categoriesByUri.get(rangeUri);
                                if (c1 != null && c2 != null && (c1.hasAncestor(c2) || (c2.hasAncestor(c1) && !c1.equals(DBpediaOntology.thingCategory())))) { // || c2.hasAncestor(c1))) {
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
                    if (visitedAttr.contains(att.getUri())) {
                        continue;
                    }
                    visitedAttr.add(att.getUri());
                    DBpediaAttributeLookupResult lr = createAttributeLookupResult(att, attributeName, attributeNames, maxSimilarity, rangeMatch ? 1 : 0.7);
                    if (lr != null) {
                        if (lr.getWeight() > maxSimilarity) {
                            maxSimilarity = lr.getWeight();
                        }
                        if (att.getUri().contains("http://dbpedia.org/property/")) {
                            lr.setWeight(0.999 * lr.getWeight());
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
