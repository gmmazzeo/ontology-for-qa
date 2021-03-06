/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology;

import java.util.HashSet;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QueryConstraint {

    String entityVariableName;
    String valueName;

    String subjString;
    String attrString;
    String valueString;

    String subjExpr;
    String attrExpr;
    String valueExpr;

    String type;

    NamedEntity subj;
    Attribute attr;
    NamedEntity valueEntity;

    boolean optional;

    public QueryConstraint(boolean optional) {
        this.optional = optional;
    }

    public QueryConstraint(String subjExpr, String attrExpr, String valueExpr, boolean optional) {
        this.subjExpr = subjExpr;
        this.subjString = subjExpr;
        this.attrExpr = attrExpr;
        this.attrString = attrExpr;
        this.valueExpr = valueExpr;
        this.valueString = valueExpr;
        this.optional = optional;
    }

    public QueryConstraint(String subjExpr, String attrExpr, String valueExpr, String type, boolean optional) {
        this(subjExpr, attrExpr, valueExpr, optional);
        if (type != null && type.trim().length() == 0) {
            type = null;
        }
        if (type != null) {
            this.type = type.trim();
        }
    }

    public QueryConstraint copy() {
        QueryConstraint res = new QueryConstraint(subjExpr, attrExpr, valueExpr, type, optional);
        res.subjString = subjString;
        res.valueString = valueString;
        res.attrString = attrString;
        res.subj = subj;
        res.attr = attr;
        res.valueEntity = valueEntity;
        return res;
    }

    public String getSubjExpr() {
        return subjExpr;
    }

    public void setSubjExpr(String subjExpr) {
        this.subjExpr = subjExpr;
        this.subjString = subjExpr;
    }

    public String getAttrExpr() {
        return attrExpr;
    }

    public void setAttrExpr(String attrExpr) {
        this.attrExpr = attrExpr;
        this.attrString = attrExpr;
    }

    public String getValueExpr() {
        return valueExpr;
    }

    public void setValueExpr(String valueExpr) {
        this.valueExpr = valueExpr;
        this.valueString = valueExpr;
    }

    public String getSubjString() {
        return subjString;
    }

    public void setSubjString(String subjString) {
        this.subjString = subjString;
    }

    public String getAttrString() {
        return attrString;
    }

    public void setAttrString(String attrString) {
        this.attrString = attrString;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public NamedEntity getSubj() {
        return subj;
    }

    public void setSubj(NamedEntity subj) {
        this.subj = subj;
    }

    public Attribute getAttr() {
        return attr;
    }

    public void setAttr(Attribute attr) {
        this.attr = attr;
    }

    public NamedEntity getValueEntity() {
        return valueEntity;
    }

    public void setValueEntity(NamedEntity valueEntity) {
        this.valueEntity = valueEntity;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getEntityVariableName() {
        return entityVariableName;
    }

    public void setEntityVariableName(String entityVariableName) {
        this.entityVariableName = entityVariableName;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return (optional ? "OPTIONAL " : "") + "<" + subjString + "> <" + attrString + "> <" + valueString + ">" + (type != null && type.trim().length() > 0 ? "[" + type.trim() + "]" : "");
    }

}
