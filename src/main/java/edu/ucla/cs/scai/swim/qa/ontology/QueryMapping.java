/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QueryMapping {

    private final HashMap<String, ArrayList<NamedEntityLookupResult>> cacheLookupEntity = new HashMap<>();
    private final HashMap<String, ArrayList<CategoryLookupResult>> cacheLookupCategory = new HashMap<>();

    Ontology ontology;
    boolean printIntermediateModels = true;
    double THRESHOLD = 0.8;
    double OUTPUT_THRESHOLD = 0.7;
    PrintStream out = printIntermediateModels ? System.out : new PrintStream(new OutputStream() {@Override public void write(int b) {}}); //send System.out to not print if printIntermediateModels is false

    private ArrayList<QueryModel> expandExampleEntity(QueryModel qm) throws Exception {
        ArrayList<QueryModel> res = new ArrayList<>();
        String exampleEntity = qm.getExampleEntity();
        if (exampleEntity != null && exampleEntity.startsWith("lookupEntity")) {
            String estring = exampleEntity.substring(13, exampleEntity.length() - 1);
            ArrayList<NamedEntityLookupResult> l = cacheLookupEntity.get(estring);
            if (l == null) {
                l = (ArrayList<NamedEntityLookupResult>) ontology.lookupEntity(estring);
                cacheLookupEntity.put(estring, l);
            }
            if (l.isEmpty()) {
                return new ArrayList<>();
            }
            for (NamedEntityLookupResult r : l) {
                HashSet<String> entitiesToIgnore = qm.getIgnoreEntitiesForLookup().get(estring);
                if (entitiesToIgnore != null && entitiesToIgnore.contains(r.getNamedEntity().getUri())) {
                    continue;
                }
                QueryModel qm0 = new QueryModel(qm.getEntityVariableName(), qm.getAttributeVariableName());
                qm0.setModelNumber(qm.getModelNumber());
                qm0.setWeight(qm.getWeight() * r.getWeight());
                qm0.setExampleEntity(r.getNamedEntity().getUri());
                qm0.getFilters().addAll(qm.getFilters());
                res.add(qm0);
                for (QueryConstraint qc : qm.getConstraints()) {
                    QueryConstraint nqc = qc.copy();
                    if (qc.getSubjString().equals(exampleEntity)) {
                        nqc.setSubjString(r.getNamedEntity().getUri());
                    } else if (qc.getValueString().equals(exampleEntity)) {
                        nqc.setValueString(r.getNamedEntity().getUri());
                    }
                    qm0.getConstraints().add(nqc);
                }
            }
        } else {
            res.add(qm);
        }
        return res;
    }

    private ArrayList<QueryModel> expandLookupEntity(QueryModel qm) throws Exception {
        ArrayList<QueryModel> res = new ArrayList<>();
        QueryModel qm0 = new QueryModel(qm.getEntityVariableName(), qm.getAttributeVariableName());
        qm0.setModelNumber(qm.getModelNumber());
        qm0.setWeight(qm.getWeight());
        qm0.setExampleEntity(qm.getExampleEntity());
        qm0.getFilters().addAll(qm.getFilters());
        res.add(qm0);
        for (QueryConstraint qc : qm.getConstraints()) {
            ArrayList<QueryConstraint> expandedConstraints = new ArrayList<>();
            ArrayList<Double> expandedConstraintsWeight = new ArrayList<>();
            String estring = null;
            if (qc.getSubjString().startsWith("lookupEntity(")) {
                estring = qc.getSubjString().substring(13, qc.getSubjString().length() - 1);
            } else if (qc.getValueString().startsWith("lookupEntity(")) { //it is not possibile that both the subject and the value are specific entities
                estring = qc.getValueString().substring(13, qc.getValueString().length() - 1);
            }
            if (estring != null) {
                ArrayList<NamedEntityLookupResult> l = cacheLookupEntity.get(estring);
                if (l == null) {
                    l = (ArrayList<NamedEntityLookupResult>) ontology.lookupEntity(estring);
                    cacheLookupEntity.put(estring, l);
                }
                if (l.isEmpty()) {
                    return new ArrayList<>(); //this is very inflexible - it should be better to return an approximate model
                }
                for (NamedEntityLookupResult r : l) {
                    HashSet<String> entitiesToIgnore = qm.getIgnoreEntitiesForLookup().get(estring);
                    if (entitiesToIgnore != null && entitiesToIgnore.contains(r.getNamedEntity().getUri())) {
                        continue;
                    }
                    QueryConstraint nqc = qc.copy();
                    if (qc.getSubjExpr().startsWith("lookupEntity(")) {
                        nqc.setSubjString(r.getNamedEntity().getUri());
                    } else {
                        nqc.setValueString(r.getNamedEntity().getUri());
                    }
                    expandedConstraints.add(nqc);
                    expandedConstraintsWeight.add(r.getWeight());
                }
            } else {
                QueryConstraint nqc = qc.copy();
                expandedConstraints.add(nqc);
                expandedConstraintsWeight.add(1d);
            }
            ArrayList<QueryModel> newRes = new ArrayList<>();
            for (QueryModel qmc : res) {
                for (int i = 0; i < expandedConstraints.size(); i++) {
                    QueryConstraint eqc = expandedConstraints.get(i);
                    Double weight = expandedConstraintsWeight.get(i);
                    QueryModel nqm = new QueryModel(qmc.getEntityVariableName(), qmc.getAttributeVariableName());
                    nqm.setModelNumber(qmc.getModelNumber());
                    nqm.setWeight(qmc.getWeight() * weight);
                    nqm.setExampleEntity(qmc.getExampleEntity());
                    nqm.getFilters().addAll(qmc.getFilters());
                    nqm.getConstraints().addAll(qmc.getConstraints());
                    nqm.getConstraints().add(eqc);
                    newRes.add(nqm);
                }
            }
            res = newRes;
        }
        for (QueryModel rqm : res) {
            for (QueryConstraint qc : rqm.getConstraints()) {
                if (qc.getSubjString().startsWith("lookupEntity")) {
                    throw new Exception("unresolved lookupEntity");
                }
                if (qc.getValueString().startsWith("lookupEntity")) {
                    throw new Exception("unresolved lookupEntity");
                }
            }
        }
        return res;
    }

    private ArrayList<QueryModel> expandLookupCategory(QueryModel qm) throws Exception {
        ArrayList<QueryModel> res = new ArrayList<>();
        QueryModel qm0 = new QueryModel(qm.getEntityVariableName(), qm.getAttributeVariableName());
        qm0.setModelNumber(qm.getModelNumber());
        qm0.setExampleEntity(qm.getExampleEntity());
        qm0.setWeight(qm.getWeight());
        qm0.getFilters().addAll(qm.getFilters());
        res.add(qm0);
        for (QueryConstraint qc : qm.getConstraints()) {
            ArrayList<QueryConstraint> expandedConstraints = new ArrayList<>();
            ArrayList<Double> expandedConstraintsWeight = new ArrayList<>();
            if (qc.getValueString().startsWith("lookupCategory(")) {
                String cstring = qc.getValueString().substring(15, qc.getValueString().length() - 1);
                if (cstring.length() == 0) {
                    return new ArrayList<>(); //some error occurred before - the argument of the lookupCategory function was missing
                }
                ArrayList<CategoryLookupResult> l = cacheLookupCategory.get(cstring);
                if (l == null) {
                    l = (ArrayList<CategoryLookupResult>) ontology.lookupCategory(cstring);
                    cacheLookupCategory.put(cstring, l);
                }
                if (l.isEmpty()) {
                    return new ArrayList<>(); //this is very inflexible - it should be better to return an approximate model, with less constraints
                }
                for (CategoryLookupResult r : l) {
                    QueryConstraint nqc = qc.copy();
                    nqc.setValueString(r.getCategory().getUri());
                    expandedConstraints.add(nqc);
                    expandedConstraintsWeight.add(r.getWeight());
                }
            } else {
                QueryConstraint nqc = qc.copy();
                expandedConstraints.add(nqc);
                expandedConstraintsWeight.add(1d);
            }
            ArrayList<QueryModel> newRes = new ArrayList<>();
            for (QueryModel qmc : res) {
                for (int i = 0; i < expandedConstraints.size(); i++) {
                    QueryConstraint eqc = expandedConstraints.get(i);
                    Double weight = expandedConstraintsWeight.get(i);
                    QueryModel nqm = new QueryModel(qmc.getEntityVariableName(), qmc.getAttributeVariableName());
                    nqm.setModelNumber(qmc.getModelNumber());
                    nqm.setWeight(qmc.getWeight() * weight);
                    nqm.setExampleEntity(qmc.getExampleEntity());
                    nqm.getFilters().addAll(qmc.getFilters());
                    nqm.getConstraints().addAll(qmc.getConstraints());
                    nqm.getConstraints().add(eqc);
                    newRes.add(nqm);
                }
            }
            Collections.sort(newRes);
            double highestWeight = (!newRes.isEmpty()) ? newRes.get(0).getWeight() : 0;
            for (int i = 0 ; i < newRes.size(); i++) {
                if (newRes.get(i).getWeight() < highestWeight * THRESHOLD) {
                    newRes.remove(i);
                    i--;
                }
            }
            res = newRes;
        }
        for (QueryModel rqm : res) {
            for (QueryConstraint qc : rqm.getConstraints()) {
                if (qc.getValueString().startsWith("lookupCategory")) {
                    throw new Exception("unresolved lookupCategory");
                }
            }
        }
        return res;
    }

    private ArrayList<QueryModel> expandLookupAttribute(QueryModel qm) throws Exception {
        ArrayList<String> basicTypes = new ArrayList<>();
        HashMap<String, QueryConstraint> variableTypes = new HashMap<>();
        HashMap<String, String> domainsOfResolvedAttributes = new HashMap<>();
        HashMap<String, String> rangesOfResolvedAttributes = new HashMap<>();
        HashSet<String> resolvedAttributes = new HashSet<>();
        HashSet<String> unresolvedVariables = new HashSet<>();
        for (QueryConstraint qc : qm.getConstraints()) {
            if (qc.getAttrExpr().equals("rdf:type")) {
                qc.setAttrString(ontology.getTypeAttribute());
                variableTypes.put(qc.getSubjExpr(), qc);
            }
        }
        for (QueryConstraint qf : qm.getFilters()) {
            basicTypes.add(qf.getSubjExpr());
        }
        for (QueryConstraint qc : qm.getConstraints()) {
            if (qc.getAttrString().startsWith("http://dbpedia.org/ontology")) {
                if (qc.getSubjString().startsWith("http://dbpedia.org/resource") || variableTypes.containsKey(qc.getSubjString())) {
                    rangesOfResolvedAttributes.put(qc.getValueString(), qc.getAttrString());
                    resolvedAttributes.add(qc.getValueString());
                } else if (qc.getValueString().startsWith("http://dbpedia.org/resource") || variableTypes.containsKey(qc.getValueString())) {
                    domainsOfResolvedAttributes.put(qc.getSubjString(), qc.getAttrString());
                    resolvedAttributes.add(qc.getSubjString());
                }
            } else if (qc.getAttrString().startsWith("lookupAttribute(")) {
                unresolvedVariables.add(qc.getValueString());
            }
        }

        ArrayList<QueryModel> res = new ArrayList<>();
        QueryModel qm0 = new QueryModel(qm.getEntityVariableName(), qm.getAttributeVariableName());
        qm0.setModelNumber(qm.getModelNumber());
        qm0.setExampleEntity(qm.getExampleEntity());
        qm0.setWeight(qm.getWeight());
        qm0.getFilters().addAll(qm.getFilters());
        res.add(qm0);

        for (QueryConstraint qc : qm.getConstraints()) {
            ArrayList<QueryConstraint> expandedConstraints = new ArrayList<>();
            ArrayList<Double> expandedConstraintsWeight = new ArrayList<>();
            String attr = qc.getAttrString();
            boolean resolvedConstraint = qc.getSubjString().startsWith("http://dbpedia.org/resource") || qc.getValueString().startsWith("http://dbpedia.org/resource");
            boolean variableTypeContainsSubj = variableTypes.containsKey(qc.getSubjString());
            boolean resolvedAttributesContainsSubj = resolvedAttributes.contains(qc.getSubjString());
            boolean subjIsAttrVarName = qc.getSubjString().equals(qm.getAttributeVariableName()) && !unresolvedVariables.contains(qc.getSubjString()) && (variableTypes.containsKey(qc.getValueString()) || basicTypes.contains(qc.getValueString()) || resolvedAttributes.contains(qc.getValueString()));
            if (attr.startsWith("lookupAttribute(") && (resolvedConstraint || variableTypeContainsSubj || resolvedAttributesContainsSubj || subjIsAttrVarName)) { //resolved or typed subj or value, or free variable is resolved
                HashSet<String> subjTypes = new HashSet<>();
                HashSet<String> valueTypes = new HashSet<>();
                NamedEntity domain = null, range = null;
                String astring = attr.substring(16, attr.length() - 1);
                if (resolvedConstraint) { //not free constraint
                    QueryConstraint subj = variableTypes.get(qc.getSubjExpr());
                    QueryConstraint value = variableTypes.get(qc.getValueExpr());
                    if (subj == null) { //subject is already resolved as an entity
                        out.println("elookup: " + qc.getSubjString());
                        domain = ontology.getEntityByUri(qc.getSubjString());
                        out.println("elookup finished");
                        if (domain != null) {
                            for (Category c : domain.getCategories()) {
                                subjTypes.add(c.getUri());
                            }
                        }
                    } else { //subject has a category type
                        subjTypes.add(subj.getValueString());
                        astring += " " + subj.getValueExpr().substring(15, subj.getValueExpr().length() - 1);
                    }
                    if (value == null) { //value is entity or basic type
                        boolean basicType = basicTypes.contains(qc.getValueExpr());
                        if (basicType) {
                            valueTypes.add("basicType");
                        } else if (!qc.getValueString().equals(qm.getAttributeVariableName())) {
                            out.println("elookup: " + qc.getValueString());
                            range = ontology.getEntityByUri(qc.getValueString());
                            out.println("elookup finished");
                            if (range != null) {
                                for (Category c : range.getCategories()) {
                                    valueTypes.add(c.getUri());
                                }
                            }
                        }
                    } else { //value has a category type
                        valueTypes.add(value.getValueString());
                        astring += " " + value.getValueExpr().substring(15, value.getValueExpr().length() - 1);
                    }
                } else { //free constraint is typed or has resolved attribute
                    QueryConstraint subj = variableTypes.get(qc.getSubjExpr());
                    QueryConstraint value = variableTypes.get(qc.getValueExpr());
                    if (subj == null) {
                        if (rangesOfResolvedAttributes.get(qc.getSubjString()) != null) {
                            subjTypes.addAll(ontology.getAttributeByUri(rangesOfResolvedAttributes.get(qc.getSubjString())).getRangeUri());
                        } else if (domainsOfResolvedAttributes.get(qc.getSubjString()) != null) {
                            subjTypes.addAll(ontology.getAttributeByUri(domainsOfResolvedAttributes.get(qc.getSubjString())).getDomainUri());
                        } else {
                            System.out.println(qc.getSubjString());
                            for (QueryConstraint qc2 : qm.getConstraints()) {
                                System.out.println(" " + qc2.toString());
                            }
                            System.out.println(variableTypes);
                            throw new Exception("resolveAttribute error"); //can we even get here?
                        }
                    } else { //subj has a category type
                        subjTypes.add(subj.getValueString());
                        astring += " " + subj.getValueExpr().substring(15, subj.getValueExpr().length() - 1);
                    }
                    if (value == null) { //value is basic type or resolved attribute
                        boolean basicType = basicTypes.contains(qc.getValueExpr());
                        if (basicType) {
                            valueTypes.add("basicType");
                        } else if (resolvedAttributes.contains(qc.getValueString())) {
                            if (rangesOfResolvedAttributes.get(qc.getValueString()) != null) {
                                valueTypes.addAll(ontology.getAttributeByUri(rangesOfResolvedAttributes.get(qc.getValueString())).getRangeUri());
                            } else if (domainsOfResolvedAttributes.get(qc.getValueString()) != null) {
                                valueTypes.addAll(ontology.getAttributeByUri(domainsOfResolvedAttributes.get(qc.getValueString())).getDomainUri());
                            } else {
                                System.out.println(qc.getValueString());
                                for (QueryConstraint qc2 : qm.getConstraints()) {
                                    System.out.println(" " + qc2.toString());
                                }
                                System.out.println(variableTypes);
                                throw new Exception("resolveAttribute error"); //can we even get here?
                            }
                        }
                    } else { //value has a category type
                        valueTypes.add(value.getValueString());
                        astring += " " + value.getValueExpr().substring(15, value.getValueExpr().length() - 1);
                    }
                }
                out.println("lookup: \"" + astring + "\" for model: " + qm.getModelNumber());
                out.println("subjTypes: " + subjTypes);
                out.println("valueTypes: " + valueTypes);
                out.println("domain: " + (domain == null ? null : domain.getUri()));
                out.println("range: " + (range == null ? null : range.getUri()));
                ArrayList<AttributeLookupResult> l = (ArrayList<AttributeLookupResult>) ontology.lookupAttribute(astring, subjTypes, valueTypes, domain, range);

                if (l.isEmpty()) {
                    out.println("Empty AttributeLookupResult");
                    return new ArrayList<>(); //this is very inflexible - it should be better to return an approximate model, with less constraints
                }
                for (AttributeLookupResult r : l) {
                    QueryConstraint nqc = qc.copy();
                    nqc.setAttrString(r.getAttribute().getUri());
                    if (r.isInvertedRelationship()) {
                        nqc.subjExpr = qc.valueExpr;
                        nqc.subjString = qc.valueString;
                        nqc.subj = qc.valueEntity;
                        nqc.valueExpr = qc.subjExpr;
                        nqc.valueString = qc.subjString;
                        nqc.valueEntity = qc.subj;
                    }
                    expandedConstraints.add(nqc);
                    expandedConstraintsWeight.add(r.getWeight());
                }
            } else {
                QueryConstraint nqc = qc.copy();
                expandedConstraints.add(nqc);
                expandedConstraintsWeight.add(1d);
            }
            ArrayList<QueryModel> newRes = new ArrayList<>();
            for (QueryModel qmc : res) {
                for (int i = 0; i < expandedConstraints.size(); i++) {
                    QueryConstraint eqc = expandedConstraints.get(i);
                    Double weight = expandedConstraintsWeight.get(i);
                    QueryModel nqm = new QueryModel(qmc.getEntityVariableName(), qmc.getAttributeVariableName());
                    nqm.setModelNumber(qmc.getModelNumber());
                    nqm.setWeight(qmc.getWeight() * weight);
                    nqm.setExampleEntity(qmc.getExampleEntity());
                    nqm.getFilters().addAll(qmc.getFilters());
                    nqm.getConstraints().addAll(qmc.getConstraints());
                    nqm.getConstraints().add(eqc);
                    newRes.add(nqm);
                }
            }
            Collections.sort(newRes);
            double highestWeight = (!newRes.isEmpty()) ? newRes.get(0).getWeight() : 0;
            for (int i = 0 ; i < newRes.size(); i++) {
                if (newRes.get(i).getWeight() < highestWeight * THRESHOLD) {
                    newRes.remove(i);
                    i--;
                }
            }
            res = newRes;
        }
        for (int i = 0 ; i < res.size(); i++) {
            QueryModel rqm = res.get(i);
            boolean recurse = false;
            for (QueryConstraint qc : rqm.getConstraints()) {
                if (qc.getAttrString().startsWith("lookupAttribute")) {
                    out.println("recurse");
                    recurse = true;
                    break;
                }
            }
            if (recurse) {
                res.remove(i);
                i--;
                ArrayList<QueryModel> newRes = expandLookupAttribute(rqm);
                if (newRes.isEmpty()) {
                    out.println("recursive lookup empty");
                } else {
                    res.addAll(newRes);
                }
            }
        }
        return res;
    }

    public ArrayList<QueryModel> mapOnOntology(ArrayList<QueryModel> inputModels, Ontology ontology) throws Exception {
        this.ontology = ontology;

        //TEST:Remove models that are not example models if example exists
        //TODO:Test to see if this is relevant
        //(Max) I insist that dropping some initial models according to the example page of other models is conceptually wrong.
        //Different models (with or without example page) derive from different interpretations, which are created because we can not know which are the correct ones before using the ontology.
        //This should be enough to see that dropping some initial models is not a good idea.
        //However, practical examples are those questions asking for the attribute of a class of enties, such as: give me the capitals of american countries
        //When we resolve the NP node "american countries", we try both the entity and the category interpretations for this node. Only for the entity interpretation we have the example page.
        //Then, the correct model, with "american countries" interpreted as a category, is dropped.
        /*
        System.out.println("#####################################");
        System.out.println("######### REDUCED MODELS ############");
        System.out.println("#####################################");
        boolean exampleQueryModelExists = false;
        for (QueryModel qm : inputModels) {
            if (qm.getExampleEntity() != null) {
                exampleQueryModelExists = true;
                break;
            }
        }
        for (int i = 0; i < inputModels.size(); i++) {
            if (exampleQueryModelExists) {
                boolean containsEntity = false;
                for (QueryConstraint qc : inputModels.get(i).getConstraints()) {
                    boolean hasLookupEntity = qc.getSubjExpr().contains("lookupEntity") || qc.getValueExpr().contains("lookupEntity");
                    boolean hasResource = qc.getSubjExpr().contains("http://dbpedia.org/resource") || qc.getValueExpr().contains("http://dbpedia.org/resource");
                    if (hasLookupEntity || hasResource) {
                        containsEntity = true;
                        break;
                    }
                }
                if (!containsEntity && inputModels.get(i).getExampleEntity() == null) {
                    inputModels.remove(i);
                    i--;
                } else {
                    System.out.println("Weight: " + inputModels.get(i).getWeight());
                    System.out.println("Number: " + inputModels.get(i).getModelNumber());
                    System.out.println(inputModels.get(i));
                    System.out.println("-------------------------");
                }
            } else {
                System.out.println("Weight: " + inputModels.get(i).getWeight());
                System.out.println("Number: " + inputModels.get(i).getModelNumber());
                System.out.println(inputModels.get(i));
                System.out.println("-------------------------");
            }
        }
        */
        /*
        //This is too penalizing for models with "many" constraints, which will be later penalized for the larger number of factors (<1) by which their weight will be multiplied
        double minC = Double.POSITIVE_INFINITY;
        double maxC = Double.NEGATIVE_INFINITY;
        for (QueryModel qm : inputModels) {
            minC = Math.min(minC, qm.getConstraints().size() + qm.getFilters().size());
            maxC = Math.max(maxC, qm.getConstraints().size() + qm.getFilters().size());
        }
        if (maxC != minC) {
            for (QueryModel qm : inputModels) {
                qm.weight *= 1 - 0.3 * (qm.getConstraints().size() + qm.getFilters().size() - minC) / (maxC - minC);
            }
        }
        */
        Collections.sort(inputModels);
        double maxWeight = inputModels.isEmpty() ? 0 : inputModels.get(0).getWeight();
        for (QueryModel qm : inputModels) {
            qm.setWeight(qm.getWeight() / maxWeight);
        }

        out.println("######### LOOKUP EXAMPLE PAGE #######");
        long start = System.currentTimeMillis();
        ArrayList<QueryModel> intermediateModels0 = new ArrayList<>();
        for (QueryModel inm : inputModels) {
            intermediateModels0.addAll(expandExampleEntity(inm));
        }
        long stop = System.currentTimeMillis();
        out.println("Example time: " + (stop - start));
        out.println("#####################################");
        out.println("######### MAPPED EXAMPLE ############");
        out.println("#####################################");
        Collections.sort(intermediateModels0);
        for (int i = 0; i < intermediateModels0.size(); i++) {
            if (intermediateModels0.get(i).getWeight() == 0 || intermediateModels0.get(i).getWeight() == Double.NEGATIVE_INFINITY) {
                intermediateModels0.remove(i);
                i--;
                continue;
            }
            out.println("Weight: " + intermediateModels0.get(i).getWeight());
            out.println("Number: " + intermediateModels0.get(i).getModelNumber());
            out.println(intermediateModels0.get(i));
            out.println("-------------------------");
        }

        out.println("######### LOOKUP ENTITY #############");
        start = System.currentTimeMillis();
        ArrayList<QueryModel> intermediateModels1 = new ArrayList<>();
        for (QueryModel inm : intermediateModels0) {
            intermediateModels1.addAll(expandLookupEntity(inm));
        }
        stop = System.currentTimeMillis();
        out.println("Entity time: " + (stop - start));
        out.println("#####################################");
        out.println("######### MAPPED ENTITY #############");
        out.println("#####################################");
        Collections.sort(intermediateModels1);
        for (int i = 0; i < intermediateModels1.size(); i++) {
            if (intermediateModels1.get(i).getWeight() == 0 || intermediateModels1.get(i).getWeight() == Double.NEGATIVE_INFINITY) {
                intermediateModels1.remove(i);
                i--;
                continue;
            }
            out.println("Weight: " + intermediateModels1.get(i).getWeight());
            out.println("Number: " + intermediateModels1.get(i).getModelNumber());
            out.println(intermediateModels1.get(i));
            out.println("-------------------------");
        }

        out.println("######### LOOKUP CATEGORY ###########");
        start = System.currentTimeMillis();
        ArrayList<QueryModel> intermediateModels2 = new ArrayList<>();
        for (QueryModel inm : intermediateModels1) {
            intermediateModels2.addAll(expandLookupCategory(inm));
        }
        stop = System.currentTimeMillis();
        out.println("Category time: " + (stop - start));
        out.println("#####################################");
        out.println("######### MAPPED CATEGORY ###########");
        out.println("#####################################");
        Collections.sort(intermediateModels2);
        for (int i = 0; i < intermediateModels2.size(); i++) {
            if (intermediateModels2.get(i).getWeight() == 0 || intermediateModels2.get(i).getWeight() == Double.NEGATIVE_INFINITY) {
                intermediateModels2.remove(i);
                i--;
                continue;
            }
            out.println("Weight: " + intermediateModels2.get(i).getWeight());
            out.println("Number: " + intermediateModels2.get(i).getModelNumber());
            out.println(intermediateModels2.get(i));
            out.println("-------------------------");
        }

        out.println("######### LOOKUP ATTRIBUTE ##########");
        start = System.currentTimeMillis();
        ArrayList<QueryModel> outputModels = new ArrayList<>();
        for (QueryModel inm : intermediateModels2) {
            out.println("===========================================");
            outputModels.addAll(expandLookupAttribute(inm));
        }
        stop = System.currentTimeMillis();
        out.println("Attribute time: " + (stop - start));
        for (int i = 0; i < outputModels.size(); i++) {
            if (outputModels.get(i).getWeight() == 0 || outputModels.get(i).getWeight() == Double.NEGATIVE_INFINITY) {
                outputModels.remove(i);
                i--;
            }
        }
        Collections.sort(outputModels);
        double highestWeight = (!outputModels.isEmpty()) ? outputModels.get(0).getWeight() : 0;
        for (int i = 0 ; i < outputModels.size(); i++) {
            if (outputModels.get(i).getWeight() < highestWeight * OUTPUT_THRESHOLD) {
                outputModels.remove(i);
                i--;
            }
        }

        return outputModels;
    }

}
