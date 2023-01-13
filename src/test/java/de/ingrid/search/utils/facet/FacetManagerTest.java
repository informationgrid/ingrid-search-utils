/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.search.utils.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
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

    @AfterEach
    public void tearDown() {
        if (indexDir != null && indexDir.exists()) {
            indexDir.delete();
        }
    }

    @Test
    final void testGetFacetCounts() {

        IngridQuery ingridQuery = null;
        try {
            ingridQuery = QueryStringParser.parse("wasser");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        addFacets(ingridQuery);

        OpenBitSet[] queryBitSets = FacetUtils.getBitSetsFromQuery(qps.parse(ingridQuery),
                new LuceneIndexReaderWrapper(new IndexReader[]{indexReader}));
        IngridDocument counts = fm.getFacetClassCounts(ingridQuery, queryBitSets);

        assertTrue(sumBitsetCardinalities(queryBitSets) >= counts.getLong("partner:bund"),
                "Cardinality of facet class must not be bigger than the cardinality of query.");

        assertEquals(2, counts.getLong("partner:bund"));

        // check for odd class cast exception in
        // FacetUtils.getBitSetsFromQuery()
        // providing a certain query was leading to an class cast exception
        // this was fixed, checking the instance of the returned bitset class
        fm.initialize();
        ingridQuery = null;
        try {
            ingridQuery = QueryStringParser.parse("-antike");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        addFacets(ingridQuery);

        queryBitSets = FacetUtils.getBitSetsFromQuery(qps.parse(ingridQuery), new LuceneIndexReaderWrapper(
                new IndexReader[]{indexReader}));
        counts = fm.getFacetClassCounts(ingridQuery, queryBitSets);

        assertTrue(sumBitsetCardinalities(queryBitSets) >= counts.getLong("partner:bund"),
                "Cardinality of facet class must not be bigger than the cardinality of query.");

        assertEquals(0, counts.getLong("partner:bund"));

    }

    @Test
    void cacheTest() {
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
                new LuceneIndexReaderWrapper(new IndexReader[]{indexReader})));
        duration = System.currentTimeMillis() - start;
        System.out.println("third different search took: " + duration + "ms");
    }

    @Test
    void cacheOverflowTest() {
        IngridQuery ingridQuery = null;
        try {
            ingridQuery = QueryStringParser.parse("wasser");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        addLotsOfFacets(ingridQuery);
        long start = System.currentTimeMillis();
        fm.getFacetClassCounts(ingridQuery, FacetUtils.getBitSetsFromQuery(qps.parse(ingridQuery),
                new LuceneIndexReaderWrapper(new IndexReader[]{indexReader})));
        long duration = System.currentTimeMillis() - start;
        System.out.println("many facets search took: " + duration + "ms");

        start = System.currentTimeMillis();
        fm.getFacetClassCounts(ingridQuery, FacetUtils.getBitSetsFromQuery(qps.parse(ingridQuery),
                new LuceneIndexReaderWrapper(new IndexReader[]{indexReader})));
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
                new LuceneIndexReaderWrapper(new IndexReader[]{indexReader})));
        duration = System.currentTimeMillis() - start;
        System.out.println("many facets 3nd different search took: " + duration + "ms");
    }

    @Test
    void filterFacetDefinitionTest() {
        IngridQuery ingridQuery = null;
        try {
            ingridQuery = QueryStringParser.parse("wasser");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        addFacets(ingridQuery);

        ConfigurableFacetClassProcessor facetClassProcessor = new ConfigurableFacetClassProcessor();
        Map<String, Map<String, String>> config = new HashMap<String, Map<String, String>>();
        Map<String, String> configValue = new HashMap<String, String>();
        configValue.put("datatype:csw AND metaclass:1 OR metaclass:3", "changed");
        config.put("datatype:iso", configValue);
        configValue = new HashMap<String, String>();
        configValue.put("partner:bund AND (Waldbrand OR Auto)", "changed2");
        config.put("datatype:myBundWaldbrand", configValue);
        facetClassProcessor.setFacetFilterDefinitions(config);
        List<FacetDefinition> defs = FacetUtils.getFacetDefinitions(ingridQuery);
        facetClassProcessor.process(defs);
        assertEquals("changed", defs.get(1).getClasses().get(0).getFragment());
        assertEquals("changed2", defs.get(2).getClasses().get(0).getFragment());
    }

    @SuppressWarnings("unchecked")
    private void addFacets(IngridQuery ingridQuery) {
        Map f1 = new HashMap();
        f1.put("id", "partner");

        Map f2 = new HashMap();
        f2.put("id", "datatype");
        Map classes = new HashMap();
        classes.put("id", "iso");
        classes.put("query", "datatype:csw AND metaclass:1 OR metaclass:3");
        f2.put("classes", Arrays.asList(new Object[] { classes }));

        Map f3 = new HashMap();
        f3.put("id", "datatype");
        Map classes2 = new HashMap();
        classes2.put("id", "myBundWaldbrand");
        classes2.put("query", "partner:bund AND (Waldbrand OR Auto)");
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
