/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.querymodel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QueryModel2 {

    QueryModelNode entityResultNode;
    QueryModelNode valueResultNode;        

    public QueryModelNode getEntityResultNode() {
        return entityResultNode;
    }

    public void setEntityResultNode(QueryModelNode entityResultNode) {
        this.entityResultNode = entityResultNode;
    }

    public QueryModelNode getValueResultNode() {
        return valueResultNode;
    }

    public void setValueResultNode(QueryModelNode valueResultNode) {
        this.valueResultNode = valueResultNode;
    }

}
