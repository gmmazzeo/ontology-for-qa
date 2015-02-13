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

    ArrayList<QueryConstraint> constraints = new ArrayList<>();

    public ArrayList<QueryConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(ArrayList<QueryConstraint> constraints) {
        this.constraints = constraints;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        for (QueryConstraint qc:constraints) {
            if (sb.length()>0) {
                sb.append(".\n");
            }
            sb.append(qc.toString());
        }
        return sb.toString();
    }
}
