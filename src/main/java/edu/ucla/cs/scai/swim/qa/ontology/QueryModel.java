/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology;

import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QueryModel {

    String entityVariableName;
    String attributeVariableName;
    String exampleEntity;

    ArrayList<QueryConstraint> constraints = new ArrayList<>();

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exampleEntity!=null && exampleEntity.trim().length()>0) {
            sb.append("Example entity: ").append(exampleEntity.trim());
        }
        for (QueryConstraint qc : constraints) {
            if (sb.length() > 0) {
                sb.append(".\n");
            }
            sb.append(qc.toString());
        }
        return sb.toString();
    }
}
