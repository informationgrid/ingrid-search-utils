package de.ingrid.search.utils.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.IQueryParsers;
import de.ingrid.search.utils.LuceneIndexReaderWrapper;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * This class produces a FacetClass according to a FacetDefinition. It will
 * create a query to search the index for creating a bitset, which contains all
 * document ids for a FacetClass.
 */
public class FacetClassProducer {

    // the maximum number of values for an index field
    private static final int MAX_NUM = 300;

    private static Logger LOG = Logger.getLogger(FacetClassProducer.class);

    private LuceneIndexReaderWrapper indexReaderWrapper;

    private IQueryParsers _queryParsers;

    public FacetClassProducer() {
    }

    public FacetClass produceClass(FacetClassDefinition facetClassDef) {
        FacetClass fc = null;
        try {
            long start = 0;
            if (LOG.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }
            if (facetClassDef.getDefinition() != null) {
                fc = produceClassFromQuery(facetClassDef.getName(), getLuceneQuery(facetClassDef.getDefinition()));
            } else {
                fc = new FacetClass(facetClassDef.getName(), new OpenBitSet[indexReaderWrapper.getIndexReader().length]);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Create facet class template for '" + fc + " in " + (System.currentTimeMillis() - start)
                        + " ms.");
            }
        } catch (IOException e) {
            LOG.error("Error producing facet class '" + facetClassDef.getName() + "'.", e);
        } catch (ParseException e) {
            LOG.error("Error producing facet class '" + facetClassDef.getName() + "'.", e);
        }

        return fc;
    }

    public List<FacetClass> produceClasses(FacetDefinition facetDef) {
        List<FacetClass> fClasses = new ArrayList<FacetClass>();
        try {
            if (facetDef.getQueryFragment() == null) {
                // presume we have a single field definition
                TermInfo[] tis = getHighFreqTerms(MAX_NUM, facetDef.getDefinition());
                for (TermInfo ti : tis) {
                    long start = 0;
                    if (LOG.isInfoEnabled()) {
                        start = System.currentTimeMillis();
                    }
                    fClasses.add(produceClassFromQuery(ti.term.field() + ":" + ti.term.text(), getLuceneQuery(ti.term
                            .field()
                            + ":" + ti.term.text())));
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Create facet class: " + fClasses.get(fClasses.size() - 1) + " in "
                                + (System.currentTimeMillis() - start) + " ms.");
                    }
                }
            } else {

                // we have a query fragment
                Query query = getLuceneQuery(facetDef.getQueryFragment());
                OpenBitSet[] bitSets = FacetUtils.getBitSetsFromQuery(query, indexReaderWrapper);
                Map<Term, Integer> tiq = new HashMap<Term, Integer>();
                for (int i = 0; i < bitSets.length; i++) {
                    IndexReader indexReader = indexReaderWrapper.getIndexReader()[i];
                    TermEnum termEnum = indexReader.terms(new Term(facetDef.getDefinition(), ""));
                    // iterate through all the values of this facet and see look
                    // at number of hits per term
                    try {
                        TermDocs termDocs = indexReader.termDocs();
                        // open termDocs only once, and use seek: this is more
                        // efficient
                        try {
                            do {
                                Term term = termEnum.term();
                                int count = 0;
                                int minFreq = 0;
                                if (term != null && term.field() == facetDef.getDefinition()) { // interned
                                    // comparison
                                    termDocs.seek(term);
                                    while (termDocs.next()) {
                                        if (bitSets[i].get(termDocs.doc())) {
                                            count++;
                                        }
                                    }
                                    if (count > 0) {
                                        if (!"".equals(term.text())) {
                                            if (count > minFreq) {
                                                tiq.put(term, count);
                                                if (tiq.size() > MAX_NUM) // if
                                                // tiq
                                                // overfull
                                                {
                                                    // find and remove minimal
                                                    // term to ensure capacity
                                                    // of
                                                    Term minTerm = null;
                                                    for (Term t : tiq.keySet()) {
                                                        if (minTerm == null) {
                                                            minFreq = tiq.get(t);
                                                            minTerm = t;
                                                        }
                                                        if (minFreq > tiq.get(t)) {
                                                            minFreq = tiq.get(t);
                                                            minTerm = t;
                                                        }
                                                    }
                                                    tiq.remove(minTerm);
                                                }
                                            }
                                        }
                                    }

                                } else {
                                    break;
                                }
                            } while (termEnum.next());
                        } finally {
                            termDocs.close();
                        }
                    } finally {
                        termEnum.close();
                    }

                    TermInfo[] res = new TermInfo[tiq.size()];
                    int cnt = 0;
                    for (Term t : tiq.keySet()) {
                        res[cnt] = new TermInfo(t, tiq.get(t));
                        cnt++;
                    }
                    Arrays.sort(res, new TermInfoComparator());

                    for (TermInfo ti : res) {
                        long start = 0;
                        if (LOG.isInfoEnabled()) {
                            start = System.currentTimeMillis();
                        }
                        fClasses.add(produceClassFromQuery(ti.term.field() + ":" + ti.term.text(),
                                getLuceneQuery(facetDef.getQueryFragment() + " " + ti.term.field() + ":" + ti.term.text())));
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Create facet class: " + fClasses.get(fClasses.size() - 1) + " in "
                                    + (System.currentTimeMillis() - start) + " ms.");
                        }
                    }

                }

            }

        } catch (ParseException e) {
            LOG.error("Error producing facet classes from facet '" + facetDef.getName() + "'.", e);
        } catch (Exception e) {
            LOG.error("Error producing facet classes from facet '" + facetDef.getName() + "'.", e);
        }
        return fClasses;
    }

    public FacetClass produceClassFromQuery(String name, Query query) throws IOException {
        return new FacetClass(name, FacetUtils.getBitSetsFromQuery(query, indexReaderWrapper));
    }

    public OpenBitSet[] getBitSetFromQuery(IngridQuery ingridQuery) {
        return FacetUtils.getBitSetsFromQuery(_queryParsers.parse(ingridQuery), indexReaderWrapper);
    }

    public LuceneIndexReaderWrapper getIndexReaderWrapper() {
        return indexReaderWrapper;
    }

    public void setIndexReaderWrapper(LuceneIndexReaderWrapper indexReaderWrapper) {
        this.indexReaderWrapper = indexReaderWrapper;
    }

    public IQueryParsers get_queryParsers() {
        return _queryParsers;
    }

    public void setQueryParsers(IQueryParsers queryParsers) {
        _queryParsers = queryParsers;
    }

    private Query getLuceneQuery(String definition) throws ParseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Start getting LuceneQuery from IngridQuery String: " + definition);
        }
        IngridQuery iq = QueryStringParser.parse(definition);
        // new TermQuery(new Term(facet[0],facet[1])));
        Query q = _queryParsers.parse(iq);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resulting lucene query after parsing: " + q);
        }
        q = addSpecialFields(q, iq);
        return q;
    }

    @SuppressWarnings("unchecked")
    private BooleanQuery addSpecialFields(Query q, IngridQuery iq) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Add special fields (partner, provider, datatype) from IngridQuery: " + iq);
        }

        BooleanQuery bq = new BooleanQuery();
        List<FieldQuery> partners = iq.getArrayList("partner");
        if (partners != null) {
            for (FieldQuery partner : partners) {
                Term luceneTerm = new Term(partner.getFieldName(), partner.getFieldValue());
                TermQuery luceneTermQuery = new TermQuery(luceneTerm);
                bq.add(luceneTermQuery, Occur.MUST);
            }
        }
        List<FieldQuery> providers = iq.getArrayList("provider");
        if (providers != null) {
            for (FieldQuery provider : providers) {
                Term luceneTerm = new Term(provider.getFieldName(), provider.getFieldValue());
                TermQuery luceneTermQuery = new TermQuery(luceneTerm);
                bq.add(luceneTermQuery, Occur.MUST);
            }
        }
        List<FieldQuery> datatypes = iq.getArrayList("datatype");
        if (datatypes != null) {
            for (FieldQuery datatype : datatypes) {
                Term luceneTerm = new Term(datatype.getFieldName(), datatype.getFieldValue());
                TermQuery luceneTermQuery = new TermQuery(luceneTerm);
                bq.add(luceneTermQuery, Occur.MUST);
            }
        }
        if (q.toString().length() != 0)
            bq.add(q, Occur.MUST);
        return bq;

    }

    private TermInfo[] getHighFreqTerms(int numTerms, String field) throws Exception {

        if (indexReaderWrapper == null || field == null)
            return null;
        Map<Term, Integer> tiq = new HashMap<Term, Integer>();

        for (IndexReader indexReader : indexReaderWrapper.getIndexReader()) {

            TermEnum terms = indexReader.terms(new Term(field, ""));

            if (terms == null || terms.term() == null || !terms.term().field().equals(field)) {
                continue;
            }

            int minFreq = 0;
            boolean skip = false;
            while (skip == false) {
                int docFreq = terms.docFreq();
                if (tiq.containsKey(terms.term())) {
                    docFreq += tiq.get(terms.term());
                }
                if (docFreq > minFreq) {
                    tiq.put(terms.term(), docFreq);
                    if (tiq.size() > numTerms) // if tiq overfull
                    {
                        // find and remove minimal term to ensure capacity of
                        Term minTerm = null;
                        for (Term t : tiq.keySet()) {
                            if (minTerm == null) {
                                minFreq = tiq.get(t);
                                minTerm = t;
                            }
                            if (minFreq > tiq.get(t)) {
                                minFreq = tiq.get(t);
                                minTerm = t;
                            }
                        }
                        tiq.remove(minTerm);
                    }
                }
                if (terms.next()) {
                    String fld = terms.term().field();
                    // skip if another field is reached, we only want the terms
                    // of one field
                    if (!fld.equals(field)) {
                        skip = true;
                    }
                } else {
                    skip = true;
                }
            }
        }
        TermInfo[] res = new TermInfo[tiq.size()];
        int cnt = 0;
        for (Term t : tiq.keySet()) {
            res[cnt] = new TermInfo(t, tiq.get(t));
            cnt++;
        }
        Arrays.sort(res, new TermInfoComparator());
        return res;
    }

    private class TermInfoComparator implements Comparator<TermInfo> {

        @Override
        public int compare(TermInfo ti1, TermInfo ti2) {
            if (ti1.docFreq < ti2.docFreq)
                return -1;
            if (ti1.docFreq > ti2.docFreq)
                return 1;
            return 0;
        }

    }

    private class TermInfo {
        public Term term;
        public int docFreq;

        public TermInfo(Term t, int df) {
            this.term = t;
            this.docFreq = df;
        }
    }

}
