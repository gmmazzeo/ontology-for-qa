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
import edu.ucla.cs.scai.swim.qa.ontology.Attribute;
import edu.ucla.cs.scai.swim.qa.ontology.AttributeLookupResult;
import edu.ucla.cs.scai.swim.qa.ontology.Category;
import edu.ucla.cs.scai.swim.qa.ontology.CategoryLookupResult;
import edu.ucla.cs.scai.swim.qa.ontology.NamedEntity;
import edu.ucla.cs.scai.swim.qa.ontology.NamedEntityAnnotationResult;
import edu.ucla.cs.scai.swim.qa.ontology.NamedEntityLookupResult;
import edu.ucla.cs.scai.swim.qa.ontology.Ontology;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class DBpediaOntologyOld implements Ontology {

    public final static String DBPEDIA_CSV_FOLDER = "/home/massimo/DBpedia csv/"; //change this with the path on your PC

    private static final DBpediaOntologyOld instance;
    public final static String DBPEDIA_CLASSES_URL = "http://web.informatik.uni-mannheim.de/DBpediaAsTables/DBpediaClasses.htm"; //"http://mappings.dbpedia.org/server/ontology/classes/";
    public final static String CLASSES_BASE_URI = "http://dbpedia.org/ontology/";
    public final static String SPARQL_END_POINT = "http://dbpedia.org/sparql";
    public final static String SUPERPAGES_FILE = DBPEDIA_CSV_FOLDER + "superpages.txt";
    public final static String TYPE_ATTRIBUTE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private SimilarityClient similarityClient = new WordNetSimilarityClient(); //new SwoogleSimilarityClient();//
    private TagMeClient tagMeClient = new TagMeClient();

    DBpediaCategory root = new DBpediaCategory();
    HashMap<String, DBpediaCategory> categoryMap = new HashMap<>();
    HashMap<String, DBpediaCategory> categoriesByUri = new HashMap<>();
    HashMap<String, HashSet<DBpediaAttribute>> attributeMap = new HashMap<>();
    HashMap<String, DBpediaAttribute> attributesByUri = new HashMap<>();
    HashMap<String, DBpediaNamedEntity> entitiesByUri = new HashMap<>();
    DBpediaAttribute abstractAttribute;
    TypicalityEvaluator typicalityEvaluator;

    static {
        instance = new DBpediaOntologyOld();
    }

    public static DBpediaOntologyOld getInstance() {
        return instance;
    }
    /*
     private void setNameThumbAndUrl(DBpediaNamedEntity ne) {
     if (entityMap.containsKey(ne.uri)) {
     return;
     }
     entityMap.put(ne.uri, ne);
     ne.setNameFromUri();
     String qs = "PREFIX dbpedia-owl:<http://dbpedia.org/ontology/>\n"
     + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
     + "PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n"
     + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
     qs += "SELECT DISTINCT ?u ?t\n";
     qs += "WHERE { \n";
     qs += "\tOPTIONAL {<" + ne.uri + "> dbpedia-owl:thumbnail ?t.}\n";
     qs += "\tOPTIONAL {?u foaf:primaryTopic <" + ne.uri + ">.}\n";
     qs += "} ";
     qs += "LIMIT 1\n";
     System.out.println(qs);
     com.hp.hpl.jena.query.Query query = QueryFactory.create(qs);
     QueryExecution qexec = QueryExecutionFactory.sparqlService(SPARQL_END_POINT, query);
     try {
     ResultSet rs = qexec.execSelect();
     for (; rs.hasNext();) {
     QuerySolution qSol = rs.next();
     RDFNode eu = qSol.get("u");
     if (eu != null) {
     ne.setPageUrl(eu.toString());
     }
     RDFNode et = qSol.get("t");
     if (et != null) {
     ne.setThumbUrl(et.toString());
     }
     }
     //System.out.println("===========================");
     } catch (QueryExceptionHTTP ex) {
     System.out.println(SPARQL_END_POINT + " is DOWN");
     } finally {
     qexec.close();
     } // end try/catch/finally
     }
     */

    private void traverseHierarchy(Element e, DBpediaCategory category, HashMap<String, DBpediaCategory> map) {
        for (Element c : e.children()) {
            String tagName = c.tag().getName();
            if (tagName.equals("a")) {
                String href = c.attr("href");
                if (href != null && href.length() > 0) {
                    category.setLabel(c.text());
                    category.setUri(CLASSES_BASE_URI + c.text());
                    map.put(category.getLabel(), category);
                    System.out.println(c.text() + "\t" + CLASSES_BASE_URI + c.text());
                }
            } else if (tagName.equals("ul")) {
                for (Element c1 : c.children()) {
                    if (c1.tagName().equals("li")) {
                        DBpediaCategory cc = new DBpediaCategory();
                        traverseHierarchy(c1, cc, map);
                        cc.parents = new HashSet<>();
                        cc.parents.add(category);
                        category.getSubClasses().add(cc);
                    }
                }
            }
        }
    }

    private void processFile(BufferedReader in, DBpediaCategory category, HashMap<String, HashSet<DBpediaAttribute>> map) throws IOException {

        //The first header contains the properties labels.
        //The second header contains the properties URIs.
        //The third header contains the properties range labels.
        //The fourth header contains the properties range URIs.
        String l1 = in.readLine();
        String l2 = in.readLine();
        String l3 = in.readLine();
        String l4 = in.readLine();

        Iterator<CSVRecord> it = CSVParser.parse(l1 + "\n" + l2 + "\n" + l3 + "\n" + l4, CSVFormat.RFC4180).iterator();
        Iterator<String> r1 = it.next().iterator();
        Iterator<String> r2 = it.next().iterator();
        Iterator<String> r3 = it.next().iterator();
        Iterator<String> r4 = it.next().iterator();

        while (r1.hasNext() && r2.hasNext() && r3.hasNext() && r4.hasNext()) {

            String name = r1.next();
            String uri = r2.next();
            String range = r3.next();
            String rangeUri = r4.next();

            HashSet<DBpediaAttribute> as = map.get(name);
            if (as == null) {
                as = new HashSet<>();
                map.put(name, as);
            }

            DBpediaAttribute a = attributesByUri.get(uri);

            if (a == null) {
                a = new DBpediaAttribute();
                a.setLabel(name);
                a.setRange(range);
                a.rangeUri.add(rangeUri);
                a.setUri(uri);
                attributesByUri.put(a.getUri(), a);
            }
            as.add(a);

            if (abstractAttribute == null && uri.equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                abstractAttribute = a;
                System.out.println("Abstract attribute found");
            }

            category.domainOfAttributes.add(a);
        }

        if (r1.hasNext() || r2.hasNext() || r3.hasNext() || r4.hasNext()) {
            System.out.println("Error: number of columns not matching in first rows of " + category.getLabel() + " csv file");
        }
    }

    private DBpediaOntologyOld() {

        Document doc = null;
        try {
            doc = Jsoup.connect(DBPEDIA_CLASSES_URL).get();
        } catch (IOException ex) {
            Logger.getLogger(DBpediaOntologyOld.class.getName()).log(Level.SEVERE, null, ex);
        }

        traverseHierarchy(doc.body().children().get(1).children().get(1), root, categoryMap);

        for (DBpediaCategory c : categoryMap.values()) {
            categoriesByUri.put(c.uri, c);
        }

        File dir = new File(DBPEDIA_CSV_FOLDER);
        for (File f : dir.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".csv.gz")) {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
                } catch (IOException ex) {
                    Logger.getLogger(DBpediaOntologyOld.class.getName()).log(Level.SEVERE, null, ex);
                }
                String label = f.getName().replace(".csv.gz", "");
                DBpediaCategory category = categoryMap.get(label);
                System.out.println("Processing category " + label);
                if (category == null) {
                    System.out.println("Category " + label + " not found");
                    continue;
                }
                try {
                    processFile(in, category, attributeMap);
                } catch (Exception ex) {
                    Logger.getLogger(DBpediaOntologyOld.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println(category.domainOfAttributes.size() + " attributes found");
                for (DBpediaAttribute a : category.domainOfAttributes) {
                    System.out.println(a.getName());
                }
            }
        }
        try {
            typicalityEvaluator = new TypicalityEvaluator(DBPEDIA_CSV_FOLDER + "counts.bin");
        } catch (IOException ex) {
            Logger.getLogger(DBpediaOntologyOld.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBpediaOntologyOld.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            BufferedReader in = new BufferedReader(new FileReader(SUPERPAGES_FILE));
            String l = in.readLine();
            while (l != null) {
                StringTokenizer st = new StringTokenizer(l, "\t ");
                String category = st.nextToken();
                String superpage = st.nextToken();
                Integer count = Integer.parseInt(st.nextToken());
                DBpediaCategory c = categoryMap.get(category);
                if (c != null) {
                    c.updateMostPopularEntity(superpage, count);
                }
                l = in.readLine();
            }
            in.close();
            for (DBpediaCategory c : categoryMap.values()) {
                if (c.getMostPopularEntity() != null) {
                    c.updateAncestorsPopularity();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    HashMap<String, DBpediaNamedEntity> namedEntities = new HashMap<>();
    //HashMap<String, DBpediaCategory> categories = new HashMap<>();

    DBpediaEntityLookup entityLookup = new DBpediaEntityLookup(similarityClient);
    DBpediaAttributeLookup attributeLookup = new DBpediaAttributeLookup(similarityClient);
    DBpediaCategoryLookup categoryLookup = new DBpediaCategoryLookup(similarityClient);

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
        DBpediaOntologyOld dbo = DBpediaOntologyOld.getInstance();
        ArrayList<NamedEntityLookupResult> nes = (ArrayList<NamedEntityLookupResult>) dbo.lookupEntity("new york");
        for (NamedEntityLookupResult ne : nes) {
            System.out.println(ne.getNamedEntity().getName());
        }

    }
    /*
     @Override
     public ArrayList<? extends QueryResult> executeQuery(Query q) {
     ArrayList<QueryResult> res = new ArrayList<>();

     if (q.getTypeOfQuestion() == Query.ATTRIBUTE_OF_ENTITY || q.getTypeOfQuestion() == Query.DEFINITION) {

     for (DBpediaNamedEntity ne : ((ArrayList<DBpediaNamedEntity>) q.getSelectedEntities())) {
     setNameThumbAndUrl(ne);
     QueryResult qr = new QueryResult();
     res.add(qr);
     qr.setNamedEntity(ne);
     if (!q.getSelectedAttributes().isEmpty()) {
     String qs = "PREFIX dbpedia-owl:<http://dbpedia.org/ontology/>\n"
     + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
     + "PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n"
     + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
     qs += "SELECT DISTINCT ?a ?v ?vu ?vt";
     qs += " \nWHERE { \n";
     boolean first = true;
     HashMap<String, DBpediaAttribute> attributeUriMap = new HashMap<>();
     for (DBpediaAttribute sa : (ArrayList<DBpediaAttribute>) q.getSelectedAttributes()) {
     attributeUriMap.put(sa.getUri(), sa);
     if (first) {
     first = false;
     } else {
     qs += "UNION \n";
     }
     qs += "\t{ \n";
     qs += "\t<" + ne.getUri() + "> ?a ?v \n";
     qs += "\tOPTIONAL {?vu foaf:primaryTopic ?v.}\n";
     qs += "\tOPTIONAL {?v dbpedia-owl:thumbnail ?vt.}\n";
     qs += "\tFILTER (str(?a) = \"" + sa.getUri() + "\")\n";
     qs += "\t} \n";
     }
     qs += "} LIMIT 10\n";
     System.out.println(qs);
     com.hp.hpl.jena.query.Query query = QueryFactory.create(qs);
     QueryExecution qexec = QueryExecutionFactory.sparqlService(SPARQL_END_POINT, query);
     try {
     ResultSet rs = qexec.execSelect();
     for (; rs.hasNext();) {
     QuerySolution qSol = rs.next();
     AttributeValue ac = new AttributeValue();
     qr.getProperties().add(ac);
     DBpediaAttribute a = attributeUriMap.get(qSol.get("a").toString());
     ac.setAttribute(a);
     DBpediaNamedEntity eValue = new DBpediaNamedEntity();
     String value = qSol.get("v").toString().split("\\^\\^")[0];
     boolean basicType = setBasicAttributeValue(ac, value);
     if (!basicType) {
     eValue.setUri(value);
     eValue.setNameFromUri();
     ac.setEntityValue(eValue);
     ac.setStringValue(eValue.getName());
     }

     RDFNode vu = qSol.get("vu");
     if (vu != null) {
     eValue.setPageUrl(vu.toString());
     }
     RDFNode vt = qSol.get("vt");
     if (vt != null) {
     eValue.setThumbUrl(vt.toString());
     }
     }
     //System.out.println("===========================");
     } catch (QueryExceptionHTTP e) {
     System.out.println(SPARQL_END_POINT + " is DOWN");
     } finally {
     qexec.close();
     } // end try/catch/finally
     }
     if (q.getTypeOfQuestion() == Query.DEFINITION) {
     qr.getProperties().add(new AttributeValue(getAbstractAttribute(), qr.getNamedEntity().getDescription()));
     }
     }
     } else if (q.getTypeOfQuestion() == Query.ENTITY_OF_CLASS) { //query on categories
     for (DBpediaCategory c : ((ArrayList<DBpediaCategory>) q.getSelectedCategories())) {
     String qs = "PREFIX dbpedia-owl:<http://dbpedia.org/ontology/>\n"
     + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
     + "PREFIX foaf:<http://xmlns.com/foaf/0.1/>\n"
     + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n";
     qs += "SELECT DISTINCT ?e ?a ?v \n";
     qs += "WHERE { \n";
     boolean first = true;
     for (AttributeCondition ac : (ArrayList<AttributeCondition>) q.getAttributeConditions()) {
     if (first) {
     first = false;
     } else {
     qs += "UNION \n";
     }
     qs += "\t{ \n";
     qs += "\t?e <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + c.uri + "> .\n";
     qs += "\t?e ?a ?v .\n";
     if (ac instanceof EntityAttributeCondition) {
     qs += "\tFILTER (str(?v) = \"" + ((EntityAttributeCondition) ac).getEntity().getUri() + "\")\n";
     }
     qs += "\t} \n";
     }
     qs += "} LIMIT 10\n";
     System.out.println(qs);
     com.hp.hpl.jena.query.Query query = QueryFactory.create(qs);
     QueryExecution qexec = QueryExecutionFactory.sparqlService(SPARQL_END_POINT, query);
     try {
     ResultSet rs = qexec.execSelect();
     for (; rs.hasNext();) {
     QuerySolution qSol = rs.next();
     DBpediaNamedEntity eValue = new DBpediaNamedEntity();
     String uri = qSol.get("e").toString();
     eValue.setUri(uri);
     eValue.setNameFromUri();
     QueryResult qr = new QueryResult();
     qr.setNamedEntity(eValue);
     //TODO: aggiungere gli attributi

     //RDFNode vu = qSol.get("vu");
     //if (vu != null) {
     //eValue.setPageUrl(vu.toString());
     //}
     //RDFNode vt = qSol.get("vt");
     //if (vt != null) {
     //eValue.setThumbUrl(vt.toString());
     //}
     res.add(qr);
     }
     //System.out.println("===========================");
     } catch (QueryExceptionHTTP e) {
     System.out.println(SPARQL_END_POINT + " is DOWN");
     } finally {
     qexec.close();
     } // end try/catch/finally

     }
     }

     return res;
     }
     */
    /*
     private boolean setBasicAttributeValue(AttributeValue av, String s) {
     DBpediaAttribute a = (DBpediaAttribute) av.getAttribute();
     if (a.getRange().equalsIgnoreCase("XMLSchema#byte")) {
     av.setIntegerValue(new BigInteger("" + DatatypeConverter.parseByte(s)));
     av.setStringValue(av.getIntegerValue().toString());
     return true;
     }
     if (a.getRange().equalsIgnoreCase("XMLSchema#decimal")) {
     av.setDecimalValue(DatatypeConverter.parseDecimal(s));
     av.setStringValue(av.getIntegerValue().toString());
     return true;
     }
     if (a.getRange().equalsIgnoreCase("XMLSchema#int")) {
     av.setIntegerValue(new BigInteger("" + DatatypeConverter.parseInt(s)));
     av.setStringValue(av.getIntegerValue().toString());
     return true;
     }
     if (a.getRange().equalsIgnoreCase("XMLSchema#long")) {
     av.setIntegerValue(new BigInteger("" + DatatypeConverter.parseLong(s)));
     av.setStringValue(av.getIntegerValue().toString());
     return true;
     }
     if (a.getRange().equalsIgnoreCase("XMLSchema#nonNegativeInteger")) {
     av.setIntegerValue(DatatypeConverter.parseInteger(s));
     av.setStringValue(av.getIntegerValue().toString());
     return true;
     }
     if (a.getRange().equalsIgnoreCase("XMLSchema#date")) {
     av.setDateValue(DatatypeConverter.parseDate(s));
     av.setStringValue(new SimpleDateFormat("yyyy-MM-dd").format(av.getDateValue().getTime()));
     return true;
     }
     if (a.getRange().equalsIgnoreCase("XMLSchema#dateTime")) {
     av.setDateValue(DatatypeConverter.parseDate(s));
     av.setStringValue(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(av.getDateValue().getTime()));
     return true;
     }
     if (a.getRange().equalsIgnoreCase("XMLSchema#gYear")) {
     av.setDateValue(DatatypeConverter.parseDate(s));
     av.setIntegerValue(new BigInteger("" + av.getDateValue().get(Calendar.YEAR)));
     av.setStringValue(av.getIntegerValue().toString());
     return true;
     }
     if (a.getRange().equalsIgnoreCase("XMLSchema#string")) {
     av.setStringValue(DatatypeConverter.parseString(s));
     return true;
     }
     if (a.getRange().equalsIgnoreCase("XMLSchema#token")) {
     av.setStringValue(DatatypeConverter.parseString(s));
     return true;
     }
     if (a.getRange().equalsIgnoreCase("rdf-schema#Literal")) {
     av.setStringValue(DatatypeConverter.parseString(s));
     return true;
     }

     //if (a.getRange().equalsIgnoreCase("XMLSchema#gMonth")) {
     //av.setDateValue(DatatypeConverter.parseDate(s));
     //av.setIntegerValue(new BigInteger("" + av.getDateValue().get(Calendar.MONTH)));
     //av.setStringValue(new SimpleDateFormat("MMMMMMMMMM").format(av.getDateValue().getTime()));
     //return true;
     //}
     return false;
     }
     */

    @Override
    public Attribute getAbstractAttribute() {
        return abstractAttribute;
    }

    @Override
    public ArrayList<? extends CategoryLookupResult> lookupCategory(Collection<Attribute> attributes) {
        ArrayList<String> atts = new ArrayList<>();
        for (Attribute a : attributes) {
            atts.add(a.getUri());
        }
        ArrayList<DBpediaCategoryLookupResult> res = new ArrayList<>();
        for (Pair p : typicalityEvaluator.findTopKCategories(10, atts, false)) {
            if (categoryMap.get(p.getS()) == null) {
                System.out.println();
                continue;
            }
            res.add(new DBpediaCategoryLookupResult(categoryMap.get(p.getS()), p.getP()));
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
            Logger.getLogger(DBpediaOntologyOld.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    @Override
    public ArrayList<? extends AttributeLookupResult> lookupAttribute(String attributeName, Category subjectCategory, String range) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public NamedEntity getEntityByUri(String uri) {
        DBpediaNamedEntity e = entitiesByUri.get(uri);
        if (e == null) {
            //e=...
            entitiesByUri.put(e.getUri(), e);
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
    public ArrayList<? extends AttributeLookupResult> lookupAttribute(String attributeName, Set<String> subjectType, Set<String> valueType) {
        return lookupAttribute(attributeName);
    }

    @Override
    public ArrayList<? extends AttributeLookupResult> lookupAttribute(String attributeName, Set<String> subjectType, Set<String> valueType, NamedEntity domain, NamedEntity range) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Attribute getAttributeByUri(String uri) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
