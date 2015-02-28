/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.querymodel;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QueryModelEdge {

    String lookupAttribute;
    QueryModelNode valNode;

    public QueryModelEdge(String lookupAttribute, QueryModelNode valNode) {
        this.lookupAttribute = lookupAttribute;
        this.valNode = valNode;
    }

    public String getLookupAttribute() {
        return lookupAttribute;
    }

    public void setLookupAttribute(String lookupAttribute) {
        this.lookupAttribute = lookupAttribute;
    }

    public QueryModelNode getValNode() {
        return valNode;
    }

    public void setValNode(QueryModelNode valNode) {
        this.valNode = valNode;
    }

    public QueryModelEdge getCopy(QueryModelNode inNode) {
        QueryModelEdge res=new QueryModelEdge(lookupAttribute, valNode.getCopy(inNode));
        return res;
    }
}
