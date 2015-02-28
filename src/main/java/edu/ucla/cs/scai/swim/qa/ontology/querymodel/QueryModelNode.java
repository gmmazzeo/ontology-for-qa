/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.querymodel;

import java.util.ArrayList;
import java.util.Objects;
import javax.management.Query;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QueryModelNode {

    String variableName;
    String lookupEntity;
    String lookupCategory;
    ArrayList<QueryModelEdge> outEdges = new ArrayList<>();
    //ArrayList<QueryModelEdge> inEdges = new ArrayList<>();   
    //the following define a tree-representation - probably, we will need to extend the model so that it graphs can be represented
    QueryModelNode inNode;

    public QueryModelNode(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getLookupEntity() {
        return lookupEntity;
    }

    public void setLookupEntity(String lookupEntity) {
        this.lookupEntity = lookupEntity;
    }

    public String getLookupCategory() {
        return lookupCategory;
    }

    public void setLookupCategory(String lookupCategory) {
        this.lookupCategory = lookupCategory;
    }

    public ArrayList<QueryModelEdge> getOutEdges() {
        return outEdges;
    }

    public void setOutEdges(ArrayList<QueryModelEdge> outEdges) {
        this.outEdges = outEdges;
    }

    public QueryModelNode getInNode() {
        return inNode;
    }

    public void setInNode(QueryModelNode inNode) {
        this.inNode = inNode;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.variableName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryModelNode other = (QueryModelNode) obj;
        if (!Objects.equals(this.variableName, other.variableName)) {
            return false;
        }
        return true;
    }

    public QueryModelNode getCopy(QueryModelNode inNode) {
        QueryModelNode res = new QueryModelNode(variableName);
        res.inNode = inNode;
        res.lookupCategory = lookupCategory;
        res.lookupEntity = lookupEntity;
        res.outEdges = new ArrayList<>();
        for (QueryModelEdge edge : outEdges) {
            res.outEdges.add(edge.getCopy(res));
        }
        res.variableName = variableName;
        return res;
    }

}
