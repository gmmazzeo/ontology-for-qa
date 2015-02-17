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
public class SpecificCategoryConstraint extends QueryConstraint {

    String searchString;
    ArrayList<? extends CategoryLookupResult> lookupResult = new ArrayList<>();

    public SpecificCategoryConstraint(String entityVariableName, String searchString) {
        super(false);
        this.entityVariableName = entityVariableName;
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public ArrayList<? extends CategoryLookupResult> getLookupResult() {
        return lookupResult;
    }

    public void resolve(Ontology ontology) throws Exception {
        lookupResult = ontology.lookupCategory(searchString);
    }

}
