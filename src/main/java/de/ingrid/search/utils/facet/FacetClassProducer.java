package de.ingrid.search.utils.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.PriorityQueue;

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
        String[] fields = { facetDef.getDefinition() };
        try {
            TermInfo[] tis = getHighFreqTerms(MAX_NUM, fields);
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
        IngridQuery iq = QueryStringParser.parse(definition);
        // new TermQuery(new Term(facet[0],facet[1])));
        Query q = _queryParsers.parse(iq);
        q = addSpecialFields(q, iq);
        return q;
    }

    @SuppressWarnings("unchecked")
    private BooleanQuery addSpecialFields(Query q, IngridQuery iq) {
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

    private TermInfo[] getHighFreqTerms(int numTerms, String[] fields) throws Exception {

        if (indexReaderWrapper == null || fields == null)
            return null;
        TermInfoQueue tiq = new TermInfoQueue(numTerms);

        for (IndexReader indexReader : indexReaderWrapper.getIndexReader()) {

            TermEnum terms = indexReader.terms();

            int minFreq = 0;
            while (terms.next()) {
                String field = terms.term().field();
                if (fields != null && fields.length > 0) {
                    boolean skip = true;
                    for (int i = 0; i < fields.length; i++) {
                        if (field.equals(fields[i])) {
                            skip = false;
                            break;
                        }
                    }
                    if (skip)
                        continue;
                }
                // if (junkWords != null && junkWords.get(terms.term().text())
                // !=
                // null) continue;
                if (terms.docFreq() > minFreq) {
                    tiq.put(new TermInfo(terms.term(), terms.docFreq()));
                    if (tiq.size() >= numTerms) // if tiq overfull
                    {
                        tiq.pop(); // remove lowest in tiq
                        minFreq = ((TermInfo) tiq.top()).docFreq; // reset
                        // minFreq
                    }
                }
            }
        }
        TermInfo[] res = new TermInfo[tiq.size()];
        for (int i = 0; i < res.length; i++) {
            res[res.length - i - 1] = (TermInfo) tiq.pop();
        }
        return res;
    }

    private class TermInfoQueue extends PriorityQueue {
        TermInfoQueue(int size) {
            initialize(size);
        }

        protected final boolean lessThan(Object a, Object b) {
            TermInfo termInfoA = (TermInfo) a;
            TermInfo termInfoB = (TermInfo) b;
            return termInfoA.docFreq < termInfoB.docFreq;
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
