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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.PriorityQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LuceneBitSetSearchTest {

    IndexSearcher searcher;
    Map<String, Query> subQueries;
    private Query baseQuery;
    File indexDir = null;

    @BeforeEach
    public void init() {
        try {
            indexDir = DummyIndex.getTestIndex();
            searcher = new IndexSearcher(IndexReader.open(indexDir));
        } catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @AfterEach
    public void tearDown() {
        if (indexDir != null && indexDir.exists()) {
            indexDir.delete();
        }
    }

    @Test
    void facetSearch() throws Exception {
        subQueries = new HashMap<String, Query>();

        List<String[]> facets = getAllPartnerProvider();
        for (String[] facet : facets) {
            subQueries.put(facet[0] + ":" + facet[1], new TermQuery(new Term(facet[0], facet[1])));
        }

        Map<String, Integer> facetCounts = new HashMap<String, Integer>();
        IndexReader reader = searcher.getIndexReader();
        baseQuery = getBaseQuery();
        QueryWrapperFilter baseQueryFilter = new QueryWrapperFilter(baseQuery);
        BitSet baseBitSet = baseQueryFilter.bits(reader);

        long start = System.currentTimeMillis();

        for (String attribute : subQueries.keySet()) {
            QueryWrapperFilter filter = new QueryWrapperFilter(subQueries.get(attribute));
            BitSet filterBitSet = filter.bits(reader);
            facetCounts.put(attribute, getFacetHitCount(baseBitSet, filterBitSet));
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("pure search took: " + duration + "ms");

    }

    public List<String[]> getAllPartnerProvider() {
        long start = System.currentTimeMillis();
        String[] fields = { "partner", "provider" };
        List<String[]> facets = null;
        try {
            TermInfo[] tis = getHighFreqTerms(searcher.getIndexReader(), 1000, fields);
            long duration = System.currentTimeMillis() - start;
            System.out.println("The highest frequency calculation of field location took " + duration + "ms.");
            System.out.println("Result of high freq locations: " + tis.length);
            facets = new ArrayList<String[]>();
            for (TermInfo termInfo : tis) {
                facets.add(new String[] { termInfo.term.field(), termInfo.term.text() });
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return facets;
    }

    public TermInfo[] getHighFreqTerms(IndexReader reader, int numTerms, String[] fields) throws Exception {
        if (reader == null || fields == null)
            return null;
        TermInfoQueue tiq = new TermInfoQueue(numTerms);
        TermEnum terms = reader.terms();

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
            // if (junkWords != null && junkWords.get(terms.term().text()) !=
            // null) continue;
            if (terms.docFreq() > minFreq) {
                tiq.put(new TermInfo(terms.term(), terms.docFreq()));
                if (tiq.size() >= numTerms) // if tiq overfull
                {
                    tiq.pop(); // remove lowest in tiq
                    minFreq = ((TermInfo) tiq.top()).docFreq; // reset minFreq
                }
            }
        }
        TermInfo[] res = new TermInfo[tiq.size()];
        for (int i = 0; i < res.length; i++) {
            res[res.length - i - 1] = (TermInfo) tiq.pop();
        }
        return res;
    }

    private Query getBaseQuery() {
        BooleanQuery bQuery = new BooleanQuery();
        Query query = null;
        query = new TermQuery(new Term("content", "waldbrand "));
        bQuery.add(query, Occur.MUST);
        return query;
    }

    private int getFacetHitCount(BitSet baseBitSet, BitSet filterBitSet) {
        filterBitSet.and(baseBitSet);
        return filterBitSet.cardinality();
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
