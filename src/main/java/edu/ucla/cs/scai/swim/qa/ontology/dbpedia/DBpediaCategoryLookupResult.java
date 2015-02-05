/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

import edu.ucla.cs.scai.swim.qa.ontology.CategoryLookupResult;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaCategoryLookupResult extends CategoryLookupResult {

    DBpediaCategory category;

    public DBpediaCategoryLookupResult(DBpediaCategory category, double weight) {
        this.category = category;
        this.weight = weight;
    }

    @Override
    public DBpediaCategory getCategory() {
        return category;
    }

    public void setCategory(DBpediaCategory category) {
        this.category = category;
    }

}
