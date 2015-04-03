/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.swim.qa.ontology.dbpedia;

//import com.hp.hpl.jena.query.QueryExecution;
//import com.hp.hpl.jena.query.QueryExecutionFactory;
//import com.hp.hpl.jena.query.QueryFactory;
//import com.hp.hpl.jena.query.QuerySolution;
//import com.hp.hpl.jena.query.ResultSet;
//import com.hp.hpl.jena.rdf.model.RDFNode;
//import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.ucla.cs.scai.swim.qa.ontology.Attribute;
import edu.ucla.cs.scai.swim.qa.ontology.AttributeLookupResult;
import edu.ucla.cs.scai.swim.qa.ontology.Category;
import edu.ucla.cs.scai.swim.qa.ontology.CategoryLookupResult;
import edu.ucla.cs.scai.swim.qa.ontology.NamedEntity;
import edu.ucla.cs.scai.swim.qa.ontology.NamedEntityAnnotationResult;
import edu.ucla.cs.scai.swim.qa.ontology.NamedEntityLookupResult;
import edu.ucla.cs.scai.swim.qa.ontology.Ontology;
import static edu.ucla.cs.scai.swim.qa.ontology.dbpedia.DBpediaOntologyOld.TYPE_ATTRIBUTE;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.tipicality.Pair;
import edu.ucla.cs.scai.swim.qa.ontology.dbpedia.tipicality.TypicalityEvaluator;
//import edu.ucla.cs.scai.swim.qa.AttributeCondition;
//import edu.ucla.cs.scai.swim.qa.AttributeValue;
//import edu.ucla.cs.scai.swim.qa.EntityAttributeCondition;
//import edu.ucla.cs.scai.swim.qa.Query;
//import edu.ucla.cs.scai.swim.qa.interfaces.AttributeLookupResult;
//import edu.ucla.cs.scai.swim.qa.interfaces.CategoryLookupResult;
//import edu.ucla.cs.scai.swim.qa.interfaces.NamedEntityLookupResult;
//import edu.ucla.cs.scai.swim.qa.interfaces.Ontology;
//import edu.ucla.cs.scai.swim.qa.QueryResult;
//import edu.ucla.cs.scai.swim.qa.interfaces.Attribute;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaOntology implements Ontology {

    private static final DBpediaOntology instance;
    public final static String DBPEDIA_CSV_FOLDER = "/home/massimo/DBpedia csv/"; //change this with the path on your PC
    public final static String DBPEDIA_CLASSES_URL = "http://web.informatik.uni-mannheim.de/DBpediaAsTables/DBpediaClasses.htm"; //"http://mappings.dbpedia.org/server/ontology/classes/";
    public final static String CLASSES_BASE_URI = "http://dbpedia.org/ontology/";
    public final static String SPARQL_END_POINT = "http://dbpedia.org/sparql";
    public final static String SUPERPAGES_FILE = "superpages.txt";
    public final static String TYPE_ATTRIBUTE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    final HashMap<String, DBpediaCategory> categoriesByUri = new HashMap<>();
    final HashMap<String, DBpediaAttribute> attributesByUri = new HashMap<>();
    final HashMap<String, DBpediaDataType> dataTypesByUri = new HashMap<>();
    HashMap<String, DBpediaNamedEntity> entitiesByUri = new HashMap<>();

    public static final String THING_URI = "http://www.w3.org/2002/07/owl#Thing";
    public final static String ABSTRACT_ATTRIBUTE_URI = "http://www.w3.org/2000/01/rdf-schema#comment";
    private SimilarityClient similarityClient = new SwoogleSimilarityClient();//new WordNetSimilarityClient(); //

    DBpediaCategory root = new DBpediaCategory();
    DBpediaAttribute abstractAttribute;
    TypicalityEvaluator typicalityEvaluator;
    HashMap<String, DBpediaNamedEntity> namedEntities = new HashMap<>();
    //HashMap<String, DBpediaCategory> categories = new HashMap<>();

    DBpediaEntityLookup entityLookup = new DBpediaEntityLookup(similarityClient);
    DBpediaAttributeLookup attributeLookup = new DBpediaAttributeLookup(similarityClient);
    DBpediaCategoryLookup categoryLookup = new DBpediaCategoryLookup(similarityClient);

    static {
        instance = new DBpediaOntology();
    }

    public static DBpediaOntology getInstance() {
        return instance;
    }

    private void initDataTypes() {
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#nonNegativeInteger", new DBpediaDataType("", Integer.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#anyURI", new DBpediaDataType("", String.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/kilowatt", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/cubicMetrePerSecond", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/megabyte", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#float", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/second", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#positiveInteger", new DBpediaDataType("", Integer.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#gYear", new DBpediaDataType("", GregorianCalendar.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/cubicKilometre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/litre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/squareKilometre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString", new DBpediaDataType("", String.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/metre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#date", new DBpediaDataType("", Date.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/cubicCentimetre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/centimetre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#double", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/inhabitantsPerSquareKilometre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/kilometre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/gramPerKilometre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/kelvin", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#dateTime", new DBpediaDataType("", Date.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/squareMetre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/millimetre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/kilogramPerCubicMetre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#integer", new DBpediaDataType("", Integer.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/kilometrePerSecond", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/day", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/kilometrePerHour", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/cubicMetre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#boolean", new DBpediaDataType("", Boolean.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/newtonMetre", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/kilogram", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/minute", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#string", new DBpediaDataType("", String.class));
        dataTypesByUri.put("http://www.w3.org/2001/XMLSchema#gYearMonth", new DBpediaDataType("", GregorianCalendar.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/hour", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/fuelType", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/valvetrain", new DBpediaDataType("", Double.class));
        dataTypesByUri.put("http://dbpedia.org/datatype/engineConfiguration", new DBpediaDataType("", Double.class));

        for (Map.Entry<String, DBpediaDataType> e : dataTypesByUri.entrySet()) {
            e.getValue().uri = e.getKey();
        }
    }

    private JsonArray loadJsonDescriptor() throws IOException {
        StringBuilder jsonSb;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(DBpediaClassesLoader.class.getResourceAsStream("/definitions.json")))) {
            jsonSb = new StringBuilder();
            String l = in.readLine();
            while (l != null) {
                jsonSb.append(l);
                l = in.readLine();
            }
        }

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(jsonSb.toString());
        return je.getAsJsonObject().get("@graph").getAsJsonArray();
    }

    private String extractLabel(JsonObject jo, String id) {
        JsonArray ja = jo.get("http://www.w3.org/2000/01/rdf-schema#label").getAsJsonArray();
        for (JsonElement je : ja) {
            String lang = je.getAsJsonObject().get("@language").getAsString();
            if (lang != null && lang.equals("en")) {
                String label = je.getAsJsonObject().get("@value").getAsString();
                if (label != null && label.trim().length() > 0) {
                    return label;
                }
            }
        }
        //the label was not found - let's use the url
        String[] s = id.split("\\/");
        return s[s.length - 1];
    }

    private void loadEmptyCategoriesAndAttributes(JsonArray ja) throws Exception {
        for (JsonElement je : ja) {
            JsonObject jo = je.getAsJsonObject();
            String id = jo.get("@id").getAsString();
            String type = null;
            try {
                type = jo.get("@type").getAsJsonArray().get(0).getAsString();
            } catch (Exception e) {
                System.out.println(id);
                e.printStackTrace();
                continue;
            }
            if (type.endsWith("Class")) {
                DBpediaCategory c = new DBpediaCategory();
                c.setUri(id);
                c.setLabel(extractLabel(jo, id));
                categoriesByUri.put(id, c);
            } else if (type.endsWith("Property")) {
                DBpediaAttribute a = new DBpediaAttribute();
                a.setUri(id);
                a.setLabel(extractLabel(jo, id));
                attributesByUri.put(id, a);
            } else if (type.endsWith("Ontology")) {
            } else {
                throw new Exception("Unexpected type: " + id + ":" + type);
            }
        }
        System.out.println(categoriesByUri.size() + " categories");
        System.out.println(attributesByUri.size() + " attributes");
    }

    private void connectCategoriesThroughSubclassRelationship(JsonArray ja) throws Exception {
        for (JsonElement je : ja) {
            JsonObject jo = je.getAsJsonObject();
            String id = jo.get("@id").getAsString();
            String type = null;
            try {
                type = jo.get("@type").getAsJsonArray().get(0).getAsString();
            } catch (Exception e) {
                continue;
            }
            if (type.endsWith("Class")) {
                DBpediaCategory c = categoriesByUri.get(id);
                JsonArray scs = jo.get("http://www.w3.org/2000/01/rdf-schema#subClassOf").getAsJsonArray();
                for (JsonElement jesc : scs) {
                    String ps = jesc.getAsString();
                    DBpediaCategory pc = categoriesByUri.get(ps);
                    if (pc == null) { //this is an external category
                        //is it really useful to add this category?
                        //pc = new DBpediaCategory();
                        //pc.setUri(ps);
                        //categoriesByUri.put(ps, pc);
                        //System.out.println("Added an external category: " + ps);
                        //System.out.println("It was needed for " + id);
                        continue;
                    }
                    c.parents.add(pc);
                    pc.subClasses.add(c);
                }
                if (scs == null || scs.size() == 0) {
                    throw new Exception(id + " has no subclassOf");
                } else if (scs.size() > 1) {
                    //System.out.println(id + " has " + scs.size() + " subclassOf");
                }
            }
        }
    }

    private void connectCategoriesAndAttributes(JsonArray ja) throws Exception {
        for (JsonElement je : ja) {
            JsonObject jo = je.getAsJsonObject();
            String id = jo.get("@id").getAsString();
            String type;
            try {
                type = jo.get("@type").getAsJsonArray().get(0).getAsString();
            } catch (Exception e) {
                continue;
            }
            if (type.endsWith("Property")) {
                DBpediaAttribute att = attributesByUri.get(id);
                JsonElement dje = jo.get("http://www.w3.org/2000/01/rdf-schema#domain");
                if (dje != null) { //the domain is specified
                    JsonArray dja = dje.getAsJsonArray();
                    for (JsonElement j : dja) {
                        String ds = j.getAsString();
                        DBpediaCategory cat = categoriesByUri.get(ds);
                        if (cat == null) {
                            if (dataTypesByUri.containsKey(ds)) {
                                throw new Exception(id + " has domain " + dataTypesByUri.get(ds) + ", which is a basic type!");
                            }
                            continue;
                            //System.out.println(id + " has domain " + ds);
                            //cat = new DBpediaCategory();
                            //cat.setUri(ds);
                            //categoriesByUri.put(ds, cat);
                            //System.out.println("The category " + ds + " was created");
                        }
                        att.domainUri.add(ds);
                    }
                } else { //the domain is not specified, thus we assume that the domain id owl#Thing
                    att.domainUri.add(THING_URI);
                }

                JsonElement rje = jo.get("http://www.w3.org/2000/01/rdf-schema#range");
                if (rje != null) { //the range is specified
                    JsonArray rja = rje.getAsJsonArray();
                    for (JsonElement j : rja) {
                        String rs = j.getAsString();
                        DBpediaCategory cat = categoriesByUri.get(rs);
                        if (cat == null) {
                            DBpediaDataType basicType = dataTypesByUri.get(rs);
                            if (basicType != null) {
                                basicType.rangeOfAttributes.add(att);
                                att.rangeCanBeBasicType = true;
                            } else {
                                continue;
                                //System.out.println(id + " has range " + rs);
                                //cat = new DBpediaCategory();
                                //cat.setUri(rs);
                                //categoriesByUri.put(rs, cat);
                                //System.out.println("The category " + rs + " was created");
                            }
                        }
                        att.rangeUri.add(rs);
                    }
                }
            } else { //the range is not specified - so... ???

            }
        }
    }

    private void createThingAndConnectParentlessCategories() {
        DBpediaCategory thing = categoriesByUri.get(THING_URI);
        if (thing == null) {
            thing = new DBpediaCategory();
            thing.setUri(THING_URI);
            categoriesByUri.put(THING_URI, thing);
        }
        thing.setLabel("thing");
        for (DBpediaCategory c : categoriesByUri.values()) {
            if (c.parents.isEmpty()) {
                c.parents.add(thing);
                thing.subClasses.add(c);
            }
        }
        thing.parents.clear();
        thing.subClasses.remove(thing);
        //check for cycles
        HashMap<String, Integer> inDegree = new HashMap<>();
        LinkedList<String> zeroInDegree = new LinkedList<>();
        for (DBpediaCategory cat : categoriesByUri.values()) {
            if (cat.parents.isEmpty()) {
                zeroInDegree.addLast(cat.uri);
            } else {
                inDegree.put(cat.uri, cat.parents.size());
            }
        }
        while (!zeroInDegree.isEmpty()) {
            DBpediaCategory cat = categoriesByUri.get(zeroInDegree.removeFirst());
            for (DBpediaCategory cc : cat.subClasses) {
                Integer v = inDegree.get(cc.uri);
                if (v == 1) {
                    inDegree.remove(cc.uri);
                    zeroInDegree.addLast(cc.uri);
                } else {
                    inDegree.put(cc.uri, v - 1);
                }
            }
        }
        if (!inDegree.isEmpty()) {
            System.out.println("Cycle!");
        }
    }

    private void extendDomainsAndRangesToDescendants() {
        for (DBpediaAttribute att : attributesByUri.values()) {
            for (String domainUri : att.domainUri) {
                if (categoriesByUri.containsKey(domainUri)) {
                    LinkedList<DBpediaCategory> queue = new LinkedList<>();
                    queue.addLast(categoriesByUri.get(domainUri));
                    while (!queue.isEmpty()) {
                        DBpediaCategory cat = queue.removeFirst();
                        cat.domainOfAttributes.add(att);
                        for (DBpediaCategory cc : cat.getSubClasses()) {
                            if (!cc.domainOfAttributes.contains(att)) {
                                queue.addLast(cc);
                            }
                        }
                    }
                } else {
                    System.out.println(domainUri + " not found");
                }
            }
            for (String rangeUri : att.rangeUri) {
                if (dataTypesByUri.containsKey(rangeUri)) {
                    dataTypesByUri.get(rangeUri).rangeOfAttributes.add(att);
                } else if (categoriesByUri.containsKey(rangeUri)) {
                    LinkedList<DBpediaCategory> queue = new LinkedList<>();
                    queue.addLast(categoriesByUri.get(rangeUri));
                    while (!queue.isEmpty()) {
                        DBpediaCategory cat = queue.removeFirst();
                        cat.rangeOfAttributes.add(att);
                        for (DBpediaCategory cc : cat.getSubClasses()) {
                            if (!cc.rangeOfAttributes.contains(att)) {
                                queue.addLast(cc);
                            }
                        }
                    }
                } else {
                    System.out.println(rangeUri + " not found");
                }
            }
        }
    }

    private DBpediaOntology() {
        try {
            initDataTypes();
            JsonArray ja = loadJsonDescriptor();
            loadEmptyCategoriesAndAttributes(ja);
            connectCategoriesThroughSubclassRelationship(ja);
            connectCategoriesAndAttributes(ja);
            createThingAndConnectParentlessCategories();
            extendDomainsAndRangesToDescendants();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<? extends NamedEntityLookupResult> lookupEntity(String entityName) throws Exception {
        ArrayList<DBpediaEntityLookupResult> res = entityLookup.lookup(entityName, 3);
        for (DBpediaEntityLookupResult ne : res) {
            if (!namedEntities.containsKey(ne.namedEntity.label)) {
                namedEntities.put(ne.namedEntity.label, ne.namedEntity);
            }
        }
        return res;
    }

    @Override
    public ArrayList<? extends AttributeLookupResult> lookupAttribute(String attributeName) {
        return attributeLookup.lookup(attributeName);
    }

    @Override
    public ArrayList<? extends CategoryLookupResult> lookupCategory(String categoryName) {
        return categoryLookup.lookup(categoryName);
    }

    public static void main(String[] args) throws Exception {
        DBpediaOntology dbo = DBpediaOntology.getInstance();
        ArrayList<NamedEntityLookupResult> nes = (ArrayList<NamedEntityLookupResult>) dbo.lookupEntity("new york");
        for (NamedEntityLookupResult ne : nes) {
            System.out.println(ne.getNamedEntity().getName());
        }

    }

    @Override
    public Attribute getAbstractAttribute() {
        return attributesByUri.get(ABSTRACT_ATTRIBUTE_URI);
    }

    @Override
    public ArrayList<? extends CategoryLookupResult> lookupCategory(Collection<Attribute> attributes) {
        ArrayList<String> atts = new ArrayList<>();
        for (Attribute a : attributes) {
            atts.add(a.getUri());
        }
        ArrayList<DBpediaCategoryLookupResult> res = new ArrayList<>();
        for (Pair p : typicalityEvaluator.findTopKCategories(10, atts, false)) {
            res.add(new DBpediaCategoryLookupResult(categoriesByUri.get(p.getS()), p.getP()));
        }
        Collections.sort(res);
        return res;
    }

    @Override
    public ArrayList<? extends NamedEntityAnnotationResult> annotateNamedEntities(String sentence) {
        ArrayList<NamedEntityAnnotationResult> res = new ArrayList<>();
        try {
            for (DBpediaEntityAnnotationResult r : new TagMeClient().getTagMeResult(sentence)) {
                res.add(r);

            }
        } catch (Exception ex) {
            Logger.getLogger(DBpediaOntology.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    @Override
    public ArrayList<? extends AttributeLookupResult> lookupAttribute(String attributeName, Category subjectCategory, String range) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public JsonObject getEntityJsonByUri(String uri) {
        return entityLookup.getEntityJsonByUri(uri);
    }

    public DBpediaNamedEntity getEntityByJson(String uri, JsonObject json) {
        return entityLookup.getEntityByJson(uri, json);
    }

    public void updateEntity(String uri, DBpediaNamedEntity entity) {
        entitiesByUri.put(uri, entity);
    }

    @Override
    public NamedEntity getEntityByUri(String uri) {
        DBpediaNamedEntity e = entitiesByUri.get(uri);
        if (e == null) {
            e = entityLookup.getEntityByUri(uri);
            if (e != null) {
                entitiesByUri.put(e.getUri(), e);
            }
        }
        return e;
    }

    @Override
    public Category getCategoryByUri(String uri) {
        return categoriesByUri.get(uri);
    }

    @Override
    public String getTypeAttribute() {
        return TYPE_ATTRIBUTE;
    }

    @Override
    public ArrayList<? extends AttributeLookupResult> lookupAttribute(String attributeName, Set<String> subjectTypes, Set<String> valueTypes) {
        return attributeLookup.lookup(attributeName, subjectTypes, valueTypes, true);
    }

    public DBpediaAttribute getAttributeByUri(String uri) {
        return attributesByUri.get(uri);
    }

    public static DBpediaCategory thingCategory() {
        return instance.categoriesByUri.get(THING_URI);
    }

}
