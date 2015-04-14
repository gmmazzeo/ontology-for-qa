/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
    public class QueryModel implements Comparable<QueryModel> {

    String entityVariableName;
    String attributeVariableName;
    String exampleEntity;
    int modelNumber;
    double weight = 1; //this value taken alone has no meaning - it is meaningful just for a collection of models, generated together, in order to rank them
    HashMap<String, HashSet<String>> ignoreEntitiesForLookup = new HashMap<>();

    ArrayList<QueryConstraint> constraints = new ArrayList<>();
    ArrayList<QueryConstraint> filters = new ArrayList<>();

    public QueryModel(String entityVariableName, String attributeVariableName) {
        this.entityVariableName = entityVariableName;
        this.attributeVariableName = attributeVariableName;
    }

    public ArrayList<QueryConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(ArrayList<QueryConstraint> constraints) {
        this.constraints = constraints;
    }

    public ArrayList<QueryConstraint> getFilters() {
        return filters;
    }

    public void setFilters(ArrayList<QueryConstraint> filters) {
        this.filters = filters;
    }

    public String getEntityVariableName() {
        return entityVariableName;
    }

    public void setEntityVariableName(String entityVariableName) {
        this.entityVariableName = entityVariableName;
    }

    public String getAttributeVariableName() {
        return attributeVariableName;
    }

    public void setAttributeVariableName(String attributeVariableName) {
        this.attributeVariableName = attributeVariableName;
    }

    public String getExampleEntity() {
        return exampleEntity;
    }

    public void setExampleEntity(String exampleEntity) {
        this.exampleEntity = exampleEntity;
    }

    public int getModelNumber() {
        return modelNumber;
    }
    
    public void setModelNumber(int modelNumber) {
        this.modelNumber = modelNumber;
    }
    
    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public HashMap<String, HashSet<String>> getIgnoreEntitiesForLookup() {
        return ignoreEntitiesForLookup;
    }

    public void setIgnoreEntitiesForLookup(HashMap<String, HashSet<String>> ignoreEntitiesForLookup) {
        this.ignoreEntitiesForLookup = ignoreEntitiesForLookup;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exampleEntity != null && exampleEntity.trim().length() > 0) {
            sb.append("Example entity: ").append(exampleEntity.trim()).append("\n");
        }
        int length = sb.length();
        for (QueryConstraint qc : constraints) {
            if (sb.length() > length) {
                sb.append(".\n");
            }
            sb.append(qc.toString());
        }
        if (!filters.isEmpty()) {
            sb.append("\nFilters:");
            for (QueryConstraint qc : filters) {
                sb.append("\n");
                sb.append(qc.toString());
            }
        }
        return sb.toString();
    }

    @Override
    public int compareTo(QueryModel o) {
        return Double.compare(o.weight, weight);
    }
}
