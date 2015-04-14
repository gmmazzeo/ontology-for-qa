/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology;

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
    private final HashMap<String, ArrayList<AttributeLookupResult>> cacheLookupAttribute = new HashMap<>();
    private final HashMap<String, ArrayList<CategoryLookupResult>> cacheLookupCategory = new HashMap<>();

    Ontology ontology;

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
            res = new ArrayList<QueryModel> (newRes.subList(0, Math.min(3, newRes.size())));
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
        HashMap<String, QueryConstraint> variableType = new HashMap<>();
        for (QueryConstraint qc : qm.getConstraints()) {
            if (qc.getAttrExpr().equals("rdf:type")) {
                qc.setAttrString(ontology.getTypeAttribute());
                variableType.put(qc.getSubjExpr(), qc);
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
            if (qc.getAttrExpr().startsWith("lookupAttribute(")) {
                String astring = qc.getAttrExpr().substring(16, qc.getAttrExpr().length() - 1);
                QueryConstraint subj = variableType.get(qc.getSubjExpr());
                String subjType = (subj == null) ? null : subj.getValueString();
                HashSet<String> subjTypes = new HashSet<>();
                NamedEntity domain = null, range = null;
                if (subjType == null) { //subject is already resolved as an entity
                    System.out.println("elookup: " + qc.getSubjString());
                    domain = ontology.getEntityByUri(qc.getSubjString());
                    System.out.println("elookup finished");
                    if (domain != null) {
                        for (Category c : domain.getCategories()) {
                            subjTypes.add(c.getUri());
                        }
                    }
                } else {
                    subjTypes.add(subjType);
                }
                QueryConstraint value = variableType.get(qc.getValueExpr());
                String valueType = (value == null) ? null : value.getValueString();
                HashSet<String> valueTypes = new HashSet<>();
                if (valueType == null) {
                    boolean basicType = false;
                    for (QueryConstraint qf : qm.getFilters()) {
                        if (qf.getSubjExpr().equals(qc.getValueExpr())) {
                            basicType = true;
                            break;
                        }
                    }
                    if (basicType) {
                        valueTypes.add("basicType");
                    } else if (!qc.getValueString().equals(qm.getAttributeVariableName())) {
                        range = ontology.getEntityByUri(qc.getValueString());
                        if (range != null) {
                            for (Category c : range.getCategories()) {
                                valueTypes.add(c.getUri());
                            }
                        }
                    }
                } else {
                    valueTypes.add(valueType);
                    astring += " " + value.getValueExpr().substring(15, value.getValueExpr().length() - 1);
                }

                System.out.println("lookup: \"" + astring + "\" for model: " + qm.getModelNumber());
                System.out.println("subjTypes: " + subjTypes);
                System.out.println("valueTypes: " + valueTypes);
//                System.out.println("domain: " + (domain == null ? null : domain.getUri()));
//                System.out.println("range: " + (range == null ? null : range.getUri()));
                ArrayList<AttributeLookupResult> l = (ArrayList<AttributeLookupResult>) ontology.lookupAttribute(astring, subjTypes, valueTypes, domain, range);

                if (l.isEmpty()) {
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
            res = new ArrayList<QueryModel> (newRes.subList(0, Math.min(3, newRes.size())));
        }
        for (QueryModel rqm : res) {
            for (QueryConstraint qc : rqm.getConstraints()) {
                if (qc.getAttrString().startsWith("lookupAttribute")) {
                    //throw new Exception("unresolved lookupAttribute");
                }
            }
        }
        return res;
    }

    public ArrayList<QueryModel> mapOnOntology(ArrayList<QueryModel> inputModels, Ontology ontology) throws Exception {
        this.ontology = ontology;

        //As fans of the Occam's razor principle, we prefer "simple" models, i.e., models with less constraints (may be this will penalize too much the correct models with more constraints)
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
        Collections.sort(inputModels);
        double maxWeight = inputModels.isEmpty() ? 0 : inputModels.get(0).getWeight();
        for (QueryModel qm : inputModels) {
            qm.setWeight(qm.getWeight() / maxWeight);
        }

        //TEST:Remove models that are not example models if example exists
        //TODO:Test to see if this is relevant
        boolean exampleQueryModelExists = false;
        for (QueryModel qm : inputModels) {
            if (qm.getExampleEntity() != null) {
                exampleQueryModelExists = true;
                break;
            }
        }
        if (exampleQueryModelExists) {
            for (int i = 0; i < inputModels.size(); i++) {
                if (inputModels.get(i).getExampleEntity() == null) {
                    inputModels.remove(i);
                    i--;
                }
            }
        }
        
        System.out.println("######### LOOKUP EXAMPLE PAGE #######");
        long start = System.currentTimeMillis();
        ArrayList<QueryModel> intermediateModels0 = new ArrayList<>();
        for (QueryModel inm : inputModels) {
            intermediateModels0.addAll(expandExampleEntity(inm));
        }
        long stop = System.currentTimeMillis();
        System.out.println("Example time: " + (stop - start));
        System.out.println("#####################################");
        System.out.println("######### MAPPED EXAMPLE ############");
        System.out.println("#####################################");
        Collections.sort(intermediateModels0);
        for (int i = 0; i < intermediateModels0.size(); i++) {
            if (intermediateModels0.get(i).getWeight() == 0 || intermediateModels0.get(i).getWeight() == Double.NEGATIVE_INFINITY) {
                intermediateModels0.remove(i);
                i--;
                continue;
            }
            System.out.println("Weight: " + intermediateModels0.get(i).getWeight());
            System.out.println("Number: " + intermediateModels0.get(i).getModelNumber());
            System.out.println(intermediateModels0.get(i));
            System.out.println("-------------------------");
        }
        
        System.out.println("######### LOOKUP ENTITY #############");
        start = System.currentTimeMillis();
        ArrayList<QueryModel> intermediateModels1 = new ArrayList<>();
        for (QueryModel inm : intermediateModels0) {
            intermediateModels1.addAll(expandLookupEntity(inm));
        }
        stop = System.currentTimeMillis();
        System.out.println("Entity time: " + (stop - start));
        System.out.println("#####################################");
        System.out.println("######### MAPPED ENTITY #############");
        System.out.println("#####################################");
        Collections.sort(intermediateModels1);
        for (int i = 0; i < intermediateModels1.size(); i++) {
            if (intermediateModels1.get(i).getWeight() == 0 || intermediateModels1.get(i).getWeight() == Double.NEGATIVE_INFINITY) {
                intermediateModels1.remove(i);
                i--;
                continue;
            }
            System.out.println("Weight: " + intermediateModels1.get(i).getWeight());
            System.out.println("Number: " + intermediateModels1.get(i).getModelNumber());
            System.out.println(intermediateModels1.get(i));
            System.out.println("-------------------------");
        }

        System.out.println("######### LOOKUP CATEGORY ###########");
        start = System.currentTimeMillis();
        ArrayList<QueryModel> intermediateModels2 = new ArrayList<>();
        for (QueryModel inm : intermediateModels1) {
            intermediateModels2.addAll(expandLookupCategory(inm));
        }
        stop = System.currentTimeMillis();
        System.out.println("Category time: " + (stop - start));
        System.out.println("#####################################");
        System.out.println("######### MAPPED CATEGORY ###########");
        System.out.println("#####################################");
        Collections.sort(intermediateModels2);
        for (int i = 0; i < intermediateModels2.size(); i++) {
            if (intermediateModels2.get(i).getWeight() == 0 || intermediateModels2.get(i).getWeight() == Double.NEGATIVE_INFINITY) {
                intermediateModels2.remove(i);
                i--;
                continue;
            }
            System.out.println("Weight: " + intermediateModels2.get(i).getWeight());
            System.out.println("Number: " + intermediateModels2.get(i).getModelNumber());
            System.out.println(intermediateModels2.get(i));
            System.out.println("-------------------------");
        }

        System.out.println("######### LOOKUP ATTRIBUTE ##########");
        start = System.currentTimeMillis();
        ArrayList<QueryModel> outputModels = new ArrayList<>();
        for (QueryModel inm : intermediateModels2) {
            outputModels.addAll(expandLookupAttribute(inm));
        }
        stop = System.currentTimeMillis();
        System.out.println("Attribute time: " + (stop - start));
        for (int i = 0; i < outputModels.size(); i++) {
            if (outputModels.get(i).getWeight() == 0 || outputModels.get(i).getWeight() == Double.NEGATIVE_INFINITY) {
                outputModels.remove(i);
                i--;
                continue;
            }
        }
        Collections.sort(outputModels);
        outputModels = new ArrayList<QueryModel> (outputModels.subList(0, Math.min(5, outputModels.size())));

        return outputModels;
    }

}
