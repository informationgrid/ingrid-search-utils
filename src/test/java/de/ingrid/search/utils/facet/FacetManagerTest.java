package de.ingrid.search.utils.facet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.OpenBitSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.ingrid.search.utils.IQueryParser;
import de.ingrid.search.utils.LuceneIndexReaderWrapper;
import de.ingrid.search.utils.facet.counter.IFacetCounter;
import de.ingrid.search.utils.facet.counter.IndexFacetCounter;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

public class FacetManagerTest {

    private FacetManager fm;
    private IndexReader indexReader;
    private DummyQueryParsers qps;
    private File indexDir;

    @Before
    public void setUp() throws Exception {
        IndexSearcher searcher = null;
        indexDir = DummyIndex.getTestIndex();
        try {
            searcher = new IndexSearcher(IndexReader.open(indexDir));
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        qps = new DummyQueryParsers();
        List<IQueryParser> parsers = new ArrayList<IQueryParser>();
        parsers.add(new DummyTermQueryParser("content", null));

        qps.setQueryParsers(parsers);
        indexReader = searcher.getIndexReader();

        FacetClassProducer fp = new FacetClassProducer();
        fp.setIndexReaderWrapper(new LuceneIndexReaderWrapper(new IndexReader[] { indexReader }));
        fp.setQueryParsers(qps);

        FacetClassRegistry fr = new FacetClassRegistry();
        fr.setFacetClassProducer(fp);

        IndexFacetCounter fc = new IndexFacetCounter();
        fc.setFacetClassRegistry(fr);

        fm = new FacetManager();
        fm.setIndexReaderWrapper(new LuceneIndexReaderWrapper(new IndexReader[] { indexReader }));
        fm.setQueryParsers(qps);
        fm.setFacetCounters(Arrays.asList(new IFacetCounter[] { fc }));
    }

    @After
    public void tearDown() {
        if (indexDir != null && indexDir.exists()) {
            indexDir.delete();
        }
    }

    @Test
    public final void testGetFacetCounts() {

        IngridQuery ingridQuery = null;
        try {
            ingridQuery = QueryStringParser.parse("wasser");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        addFacets(ingridQuery);

        OpenBitSet[] queryBitSets = FacetUtils.getBitSetsFromQuery(qps.parse(ingridQuery),
                new LuceneIndexReaderWrapper(new IndexReader[] { indexReader }));
        IngridDocument counts = fm.getFacetClassCounts(ingridQuery, queryBitSets);

        Assert.assertTrue("Cardinality of facet class must not be bigger than the cardinality of query.",
                sumBitsetCardinalities(queryBitSets) >= counts.getLong("partner:bund"));

        Assert.assertEquals(2, counts.getLong("partner:bund"));
    }

    @Test
    public void cacheTest() {
        long start = System.currentTimeMillis();
        testGetFacetCounts();
        long duration = System.currentTimeMillis() - start;
        System.out.println("initial search took: " + duration + "ms");

        start = System.currentTimeMillis();
        testGetFacetCounts();
        duration = System.currentTimeMillis() - start;
        System.out.println("second same search took: " + duration + "ms");

        IngridQuery ingridQuery = null;
        try {
            ingridQuery = QueryStringParser.parse("umwelt");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        start = System.currentTimeMillis();
        addFacets(ingridQuery);

        fm.getFacetClassCounts(ingridQuery, FacetUtils.getBitSetsFromQuery(qps.parse(ingridQuery),
                new LuceneIndexReaderWrapper(new IndexReader[] { indexReader })));
        duration = System.currentTimeMillis() - start;
        System.out.println("third different search took: " + duration + "ms");
    }

    @Test
    public void cacheOverflowTest() {
        IngridQuery ingridQuery = null;
        try {
            ingridQuery = QueryStringParser.parse("wasser");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        addLotsOfFacets(ingridQuery);
        long start = System.currentTimeMillis();
        fm.getFacetClassCounts(ingridQuery, FacetUtils.getBitSetsFromQuery(qps.parse(ingridQuery),
                new LuceneIndexReaderWrapper(new IndexReader[] { indexReader })));
        long duration = System.currentTimeMillis() - start;
        System.out.println("many facets search took: " + duration + "ms");

        start = System.currentTimeMillis();
        fm.getFacetClassCounts(ingridQuery, FacetUtils.getBitSetsFromQuery(qps.parse(ingridQuery),
                new LuceneIndexReaderWrapper(new IndexReader[] { indexReader })));
        duration = System.currentTimeMillis() - start;
        System.out.println("many facets 2nd search took: " + duration + "ms");

        try {
            ingridQuery = QueryStringParser.parse("umwelt");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        addLotsOfFacets(ingridQuery);
        start = System.currentTimeMillis();
        fm.getFacetClassCounts(ingridQuery, FacetUtils.getBitSetsFromQuery(qps.parse(ingridQuery),
                new LuceneIndexReaderWrapper(new IndexReader[] { indexReader })));
        duration = System.currentTimeMillis() - start;
        System.out.println("many facets 3nd different search took: " + duration + "ms");
    }

    @SuppressWarnings("unchecked")
    private void addFacets(IngridQuery ingridQuery) {
        Map f1 = new HashMap();
        f1.put("id", "partner");

        Map f2 = new HashMap();
        f2.put("id", "datatype");
        Map classes = new HashMap();
        classes.put("id", "iso");
        classes.put("fragment", "datatype:csw AND metaclass:1 OR metaclass:3");
        f2.put("classes", Arrays.asList(new Object[] { classes }));

        Map f3 = new HashMap();
        f3.put("id", "datatype");
        Map classes2 = new HashMap();
        classes2.put("id", "myBundWaldbrand");
        classes2.put("fragment", "partner:bund AND (Waldbrand OR Auto)");
        f3.put("classes", Arrays.asList(new Object[] { classes2 }));

        ingridQuery.put("FACETS", Arrays.asList(new Object[] { f1, f2, f3 }));
    }

    @SuppressWarnings("unchecked")
    private void addLotsOfFacets(IngridQuery ingridQuery) {
        Map f1 = new HashMap();
        f1.put("id", "partner");

        Map f2 = new HashMap();
        f2.put("id", "provider");

        Map f3 = new HashMap();
        f2.put("id", "host");

        Map f4 = new HashMap();
        f4.put("id", "site");

        ingridQuery.put("FACETS", Arrays.asList(new Object[] { f1, f2, f3, f4 }));
    }

    private long sumBitsetCardinalities(OpenBitSet[] bitSets) {
        long result = 0;
        for (OpenBitSet bitSet : bitSets) {
            result += bitSet.cardinality();
        }
        return result;
    }

}
