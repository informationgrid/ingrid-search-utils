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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ingrid.search.utils.IQueryParser;
import de.ingrid.search.utils.LuceneIndexReaderWrapper;

public class FacetClassProducerTest {

    FacetClassProducer fcp;
    File indexDir;

    @BeforeEach
    public void setup() {
        IndexSearcher searcher = null;
        indexDir = DummyIndex.getTestIndex();
        try {
            searcher = new IndexSearcher(IndexReader.open(indexDir));
        } catch (CorruptIndexException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DummyQueryParsers qp = new DummyQueryParsers();
        List<IQueryParser> parsers = new ArrayList<IQueryParser>();
        parsers.add(new DummyTermQueryParser("content", null));

        qp.setQueryParsers(parsers);

        fcp = new FacetClassProducer();
        fcp.setIndexReaderWrapper(new LuceneIndexReaderWrapper(new IndexReader[] {searcher.getIndexReader()}));
        fcp.setQueryParsers(qp);
    }
    
    @AfterEach
    public void tearDown() {
        if (indexDir != null && indexDir.exists()) {
            indexDir.delete();
        }
    }

    @Test
    final void testProduceClass() {
        FacetClassDefinition fccd = new FacetClassDefinition("partner:ni", "wasser partner:ni");
        FacetClass fc = fcp.produceClass(fccd);
        assertEquals("partner:ni", fc.getFacetClassName());
        assertTrue(2 <= fc.getBitSets()[0].cardinality());
    }

    @Test
    final void testProduceClasses() {
        FacetDefinition fcd = new FacetDefinition("partner", "partner");
        List<FacetClass> fcs = fcp.produceClasses(fcd);
        FacetClass fcBund = null;
        FacetClass fcNi = null;
        for (FacetClass fc : fcs) {
            if (fc.getFacetClassName().equals("partner:bund")) {
                fcBund = fc;
            } else if (fc.getFacetClassName().equals("partner:ni")) {
                fcNi = fc;
            }
        }
        assertNotNull(fcBund);
        assertNotNull(fcNi);
        assertTrue(0 < fcBund.getBitSets()[0].cardinality());
        assertTrue(0 < fcNi.getBitSets()[0].cardinality());

        fcd = new FacetDefinition("partner_bund", "provider");
        fcd.setQueryFragment("partner:bund");
        fcs = fcp.produceClasses(fcd);
        FacetClass fcBu1 = null;
        FacetClass fcBu2 = null;
        for (FacetClass fc : fcs) {
            if (fc.getFacetClassName().equals("provider:bund_1")) {
                fcBu1 = fc;
            } else if (fc.getFacetClassName().equals("provider:bund_2")) {
                fcBu2 = fc;
            }
        }
        assertNotNull(fcBu1);
        assertNotNull(fcBu2);
        assertTrue(0 < fcBu1.getBitSets()[0].cardinality());
        assertTrue(0 < fcBu2.getBitSets()[0].cardinality());

    }

    @Test
    final void testProduceClassFromQuery() throws IOException {
        BooleanQuery bq = new BooleanQuery();
        Term luceneTerm = new Term("partner", "bund");
        TermQuery luceneTermQuery = new TermQuery(luceneTerm);
        bq.add(luceneTermQuery, Occur.MUST);

        FacetClass fc = fcp.produceClassFromQuery("partner:bund", bq);
        assertEquals("partner:bund", fc.getFacetClassName());
        assertTrue(2 <= fc.getBitSets()[0].cardinality());
    }

}
