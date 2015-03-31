/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology;

import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaCategory;
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

    private ArrayList<QueryModel> expandLookupEntity(QueryModel qm) throws Exception {
        ArrayList<QueryModel> res = new ArrayList<>();
        QueryModel qm0 = new QueryModel(qm.entityVariableName, qm.attributeVariableName);
        qm0.setWeight(qm.getWeight());
        qm0.setExampleEntity(qm.exampleEntity);
        qm0.getFilters().addAll(qm.getFilters());
        res.add(qm0);
        for (QueryConstraint qc : qm.constraints) {
            ArrayList<QueryConstraint> expandedConstraints = new ArrayList<>();
            ArrayList<Double> expandedConstraintsWeight = new ArrayList<>();
            String estring = null;
            if (qc.subjExpr.startsWith("lookupEntity(")) {
                estring = qc.subjExpr.substring(13, qc.subjExpr.length() - 1);
            } else if (qc.valueExpr.startsWith("lookupEntity(")) { //it is not possibile that both the subject and the value are specific entities
                estring = qc.valueExpr.substring(13, qc.valueExpr.length() - 1);
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
                    HashSet<String> entitiesToIgnore = qm.ignoreEntitiesForLookup.get(estring);
                    if (entitiesToIgnore != null && entitiesToIgnore.contains(r.getNamedEntity().getUri())) {
                        continue;
                    }
                    QueryConstraint nqc = qc.copy();
                    if (qc.subjExpr.startsWith("lookupEntity(")) {
                        nqc.subjString = r.getNamedEntity().getUri();
                    } else {
                        nqc.valueString = r.getNamedEntity().getUri();
                    }
                    expandedConstraints.add(nqc);
                    expandedConstraintsWeight.add(r.weight);
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
                    Double weigth = expandedConstraintsWeight.get(i);
                    QueryModel nqm = new QueryModel(qmc.entityVariableName, qmc.attributeVariableName);
                    nqm.setWeight(qmc.getWeight() * weigth);
                    nqm.setExampleEntity(qmc.entityVariableName);
                    nqm.filters.addAll(qmc.filters);
                    nqm.constraints.addAll(qmc.constraints);
                    nqm.constraints.add(eqc);
                    newRes.add(nqm);
                }
            }
            res = newRes;
        }
        for (QueryModel rqm : res) {
            for (QueryConstraint qc : rqm.constraints) {
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
        QueryModel qm0 = new QueryModel(qm.entityVariableName, qm.attributeVariableName);
        qm0.setExampleEntity(qm.exampleEntity);
        qm0.setWeight(qm.getWeight());
        qm0.getFilters().addAll(qm.getFilters());
        res.add(qm0);
        for (QueryConstraint qc : qm.constraints) {
            ArrayList<QueryConstraint> expandedConstraints = new ArrayList<>();
            ArrayList<Double> expandedConstraintsWeight = new ArrayList<>();
            if (qc.valueExpr.startsWith("lookupCategory(")) {
                String cstring = qc.valueExpr.substring(15, qc.valueExpr.length() - 1);
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
                    nqc.valueString = r.getCategory().getUri();
                    expandedConstraints.add(nqc);
                    expandedConstraintsWeight.add(r.weight);
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
                    Double weigth = expandedConstraintsWeight.get(i);
                    QueryModel nqm = new QueryModel(qmc.entityVariableName, qmc.attributeVariableName);
                    nqm.setWeight(qmc.getWeight() * weigth);
                    nqm.setExampleEntity(qmc.entityVariableName);
                    nqm.filters.addAll(qmc.filters);
                    nqm.constraints.addAll(qmc.constraints);
                    nqm.constraints.add(eqc);
                    newRes.add(nqm);
                }
            }
            res = newRes;
        }
        for (QueryModel rqm : res) {
            for (QueryConstraint qc : rqm.constraints) {
                if (qc.getValueString().startsWith("lookupCategory")) {
                    throw new Exception("unresolved lookupCategory");
                }
            }
        }
        return res;
    }

    private ArrayList<QueryModel> expandLookupAttribute(QueryModel qm) throws Exception {
        HashMap<String, String> variableType = new HashMap<>();
        for (QueryConstraint qc : qm.constraints) {
            if (qc.getAttrExpr().equals("rdf:type")) {
                qc.setAttrString(ontology.getTypeAttribute());
                variableType.put(qc.getSubjExpr(), qc.getValueString());
            }
        }

        ArrayList<QueryModel> res = new ArrayList<>();
        if (false) {
            res.add(qm);
            return res;
        }

        QueryModel qm0 = new QueryModel(qm.entityVariableName, qm.attributeVariableName);
        qm0.setExampleEntity(qm.exampleEntity);
        qm0.setWeight(qm.getWeight());
        qm0.getFilters().addAll(qm.getFilters());
        res.add(qm0);

        for (QueryConstraint qc : qm.constraints) {
            ArrayList<QueryConstraint> expandedConstraints = new ArrayList<>();
            ArrayList<Double> expandedConstraintsWeight = new ArrayList<>();
            if (qc.attrExpr.startsWith("lookupAttribute(")) {
                String astring = qc.attrExpr.substring(16, qc.attrExpr.length() - 1);
                String subjType = variableType.get(qc.getSubjExpr());
                HashSet<String> subjTypes = new HashSet<>();
                if (subjType == null) { //subject is already resolved as an entity
                    NamedEntity ne = ontology.getEntityByUri(qc.getSubjString());
                    if (ne != null) {
                        for (Category c : ne.getCategories()) {
                            subjTypes.add(c.getUri());
                        }
                    }
                } else {
                    subjTypes.add(subjType);
                }
                String valueType = variableType.get(qc.getValueExpr());
                HashSet<String> valueTypes = new HashSet<>();
                if (valueType == null) {
                    boolean basicType=false;
                    for (QueryConstraint qf:qm.filters) {
                        if (qf.subjExpr.equals(qc.getValueExpr())) {
                            basicType=true;
                            break;
                        }
                    }
                    if (basicType) {
                        valueTypes.add("basicType");
                    } else if (!qc.getValueString().equals(qm.getAttributeVariableName())) {
                        NamedEntity ne = ontology.getEntityByUri(qc.getValueString());
                        if (ne != null) {
                            for (Category c : ne.getCategories()) {
                                valueTypes.add(c.getUri());
                            }
                        }
                    }
                } else {
                    valueTypes.add(valueType);
                }

                ArrayList<AttributeLookupResult> l = (ArrayList<AttributeLookupResult>) ontology.lookupAttribute(astring, subjTypes, valueTypes);

                if (l.isEmpty()) {
                    return new ArrayList<>(); //this is very inflexible - it should be better to return an approximate model, with less constraints
                }
                for (AttributeLookupResult r : l) {
                    QueryConstraint nqc = qc.copy();
                    nqc.setAttrString(r.getAttribute().getUri());
                    if (r.isInvertedRelationship()) {
                        nqc.subjExpr=qc.valueExpr;
                        nqc.subjString=qc.valueString;
                        nqc.subj=qc.valueEntity;
                        nqc.valueExpr=qc.subjExpr;
                        nqc.valueString=qc.subjString;
                        nqc.valueEntity=qc.subj;
                    }
                    expandedConstraints.add(nqc);
                    expandedConstraintsWeight.add(r.weight);
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
                    Double weigth = expandedConstraintsWeight.get(i);
                    QueryModel nqm = new QueryModel(qmc.entityVariableName, qmc.attributeVariableName);
                    nqm.setWeight(qmc.getWeight() * weigth);
                    nqm.setExampleEntity(qmc.entityVariableName);
                    nqm.filters.addAll(qmc.filters);
                    nqm.constraints.addAll(qmc.constraints);
                    nqm.constraints.add(eqc);
                    newRes.add(nqm);
                }
            }
            res = newRes;
        }
        for (QueryModel rqm : res) {
            for (QueryConstraint qc : rqm.constraints) {
                if (qc.getAttrString().startsWith("lookupAttribute")) {
                    //throw new Exception("unresolved lookupAttribute");
                }
            }
        }
        return res;
    }

    public ArrayList<QueryModel> mapOnOntology(ArrayList<QueryModel> inputModels, Ontology ontology) throws Exception {
        this.ontology = ontology;
        double minC = Double.POSITIVE_INFINITY;
        double maxC = Double.NEGATIVE_INFINITY;
        for (QueryModel qm : inputModels) {
            minC = Math.min(minC, qm.constraints.size() + qm.getFilters().size());
            maxC = Math.max(maxC, qm.constraints.size() + qm.getFilters().size());
        }
        for (QueryModel qm : inputModels) {
            qm.weight *= 1 - 0.3 * (qm.constraints.size() + qm.getFilters().size() - minC) / (maxC - minC);
        }
        Collections.sort(inputModels);

        ArrayList<QueryModel> intermediateModels1 = new ArrayList<>();
        for (QueryModel inm : inputModels) {
            intermediateModels1.addAll(expandLookupEntity(inm));
        }

        ArrayList<QueryModel> intermediateModels2 = new ArrayList<>();
        for (QueryModel inm : intermediateModels1) {
            intermediateModels2.addAll(expandLookupCategory(inm));
        }

        ArrayList<QueryModel> outputModels = new ArrayList<>();
        for (QueryModel inm : intermediateModels2) {
            outputModels.addAll(expandLookupAttribute(inm));
        }

        Collections.sort(outputModels);

        return outputModels;
    }

}
