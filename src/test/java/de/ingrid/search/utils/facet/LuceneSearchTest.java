/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.PriorityQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LuceneSearchTest {

    IndexSearcher searcher;
    Map<String, BitSet> bitsets;
    private List<String> partnerValues;
    private List<String> providerValues;
    File indexDir = null;

    @BeforeEach
    public void init() {
        try {
            indexDir = DummyIndex.getTestIndex();
            searcher = new IndexSearcher(IndexReader.open(indexDir));
            bitsets = new HashMap<String, BitSet>();
            partnerValues = new ArrayList<String>();
            providerValues = new ArrayList<String>();
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
    void bitSetGenerationSpeedTest() {
        try {
            MyCollector facetCollector = new MyCollector();
            long start = System.currentTimeMillis();
            searcher.search(getQuery("partner", "ni"), facetCollector);
            facetCollector.getBitSet().size();
            long duration = System.currentTimeMillis() - start;
            System.out.println("The BitSet-Generation took " + duration + "ms.");

        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findAllLocationValues() {
        // searcher.getIndexReader().getFieldNames(FieldOption.INDEXED);
        long start = System.currentTimeMillis();
        List<String> locationValues = new ArrayList<String>();
        try {
            int n = 0;
            Document doc = null;
            int max = searcher.getIndexReader().maxDoc();
            while (max > n) {
                doc = searcher.getIndexReader().document(n++);
                String valueLocation = doc.get("location");
                if (!locationValues.contains(valueLocation)) {
                    locationValues.add(valueLocation);
                }
            }
            long duration = System.currentTimeMillis() - start;
            System.out.println("The location-value search took " + duration + "ms.");
            System.out.println("found locations: " + locationValues.size() + " : " + locationValues.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findAllValuesOfPartnerAndProvider() {
        // searcher.getIndexReader().getFieldNames(FieldOption.INDEXED);
        long start = System.currentTimeMillis();
        try {
            int n = 0;
            Document doc = null;
            int max = searcher.getIndexReader().maxDoc();
            while (max > n) {
                doc = searcher.getIndexReader().document(n++);
                String valuePartner = doc.get("partner");
                String valueProvider = doc.get("provider");
                if (!partnerValues.contains(valuePartner)) {
                    partnerValues.add(valuePartner);
                }
                if (!providerValues.contains(valueProvider)) {
                    providerValues.add(valueProvider);
                }
            }
            long duration = System.currentTimeMillis() - start;
            System.out.println("The partner-provider-value search took " + duration + "ms.");
            System.out.println("found partners: " + partnerValues.size() + " : " + partnerValues.toString());
            System.out.println("found provider: " + providerValues.size() + " : " + providerValues.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getAllPartnerAndProviderFacets() {
        findAllValuesOfPartnerAndProvider();

        long start = System.currentTimeMillis();
        for (String value : partnerValues) {
            if (value != null)
                searchAndCollectFacets("partner", value);
        }
        for (String value : providerValues) {
            if (value != null /* && value.startsWith("bu_") */)
                searchAndCollectFacets("provider", value);
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("The BitSet-Generation of all partners took " + duration + "ms.");
    }

    @Test
    void querySearchForFacets() {
        System.out.println("\nSEARCH FOR FACETS WHEN QUERY IS 'water'\n");
        getAllPartnerAndProviderFacets();
        FacetCollector facetCollector = new FacetCollector(bitsets);
        long start = System.currentTimeMillis();
        try {
            searcher.search(getQuery("content", "http"), facetCollector);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("The search for all partner facets when querying 'water' took " + duration + "ms.");
        // facetCollector.getValues()
    }

    private void searchAndCollectFacets(String field, String value) {
        MyCollector facetCollector = new MyCollector();
        try {
            searcher.search(getQuery(field, value), facetCollector);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        bitsets.put(field + ":" + value, facetCollector.getBitSet());
    }

    private Query getQuery(String field, String value) {
        BooleanQuery bQuery = new BooleanQuery();
        Query query = null;
        if (value == null)
            query = new TermQuery(new Term(field));
        else
            query = new TermQuery(new Term(field, value));
        bQuery.add(query, Occur.MUST);
        return query;
    }

    private class MyCollector extends HitCollector {

        // private Map<String, Integer> results = new HashMap<String,
        // Integer>();

        final BitSet bitSet = new BitSet();

        // public Map<String, Integer> getResults() {
        // return results;
        // }

        public BitSet getBitSet() {
            return bitSet;
        }

        @Override
        public void collect(int doc, float arg1) {
            bitSet.set(doc);
            
        }

    }

    private class FacetCollector extends HitCollector {
        Map<String, Integer> values = new HashMap<String, Integer>();
        Map<String, BitSet> bitsets;

        public FacetCollector(Map<String, BitSet> bitsets) {
            this.bitsets = bitsets;
        }

        @Override
        public void collect(int doc, float arg1) {
            Integer count = null;
            for (String facet : bitsets.keySet()) {
                count = values.get(facet);
                if (bitsets.get(facet).get(doc))
                    values.put(facet, count == null ? 1 : count + 1);
            }
        }

    }

    @Test
    void calculateHighFreqTerms() {
        long start = System.currentTimeMillis();
        String[] fields = {"provider"};
        try {
            TermInfo[] tis = getHighFreqTerms(searcher.getIndexReader(), 10, fields);
            long duration = System.currentTimeMillis() - start;
            System.out.println("The highest frequency calculation of field location took " + duration + "ms.");
            System.out.println("Result of high freq locations: " + tis[0].term + "," + tis[1].term + "," + tis[2].term);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    void calculateHighFreqTermsOfPartnerAndProvider() {
        long start = System.currentTimeMillis();
        String[] fields = {"partner", "provider"};
        try {
            TermInfo[] tis = getHighFreqTerms(searcher.getIndexReader(), 200, fields);
            long duration = System.currentTimeMillis() - start;
            System.out.println("The highest frequency calculation of field location took " + duration + "ms.");
            System.out.println("Result of high freq locations: " + tis.length);
            for (TermInfo termInfo : tis) {
                // System.out.print(termInfo.term + ", ");
                searchAndCollectFacets(termInfo.term.field(), termInfo.term.text());
            }
            start = System.currentTimeMillis();

            FacetCollector facetCollector = new FacetCollector(bitsets);
            try {
                searcher.search(getQuery("content", "http"), facetCollector);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            duration = System.currentTimeMillis() - start;
            System.out.println("The search took " + duration + "ms.");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
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

