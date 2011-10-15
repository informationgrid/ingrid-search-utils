package de.ingrid.search.utils.facet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.ingrid.search.utils.IQueryParser;
import de.ingrid.search.utils.LuceneIndexReaderWrapper;
import de.ingrid.search.utils.facet.FacetClass;
import de.ingrid.search.utils.facet.FacetClassDefinition;
import de.ingrid.search.utils.facet.FacetClassProducer;
import de.ingrid.search.utils.facet.FacetDefinition;

public class FacetClassProducerTest {

    FacetClassProducer fcp;
    File indexDir;

    @Before
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
    
    @After
    public void tearDown() {
        if (indexDir != null && indexDir.exists()) {
            indexDir.delete();
        }
    }

    @Test
    public final void testProduceClass() {
        FacetClassDefinition fccd = new FacetClassDefinition("partner:ni", "wasser partner:ni");
        FacetClass fc = fcp.produceClass(fccd);
        Assert.assertEquals("partner:ni", fc.getFacetClassName());
        Assert.assertTrue(2 <= fc.getBitSets()[0].cardinality());
    }

    @Test
    public final void testProduceClasses() {
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
        Assert.assertNotNull(fcBund);
        Assert.assertNotNull(fcNi);
        Assert.assertTrue(0 < fcBund.getBitSets()[0].cardinality());
        Assert.assertTrue(0 < fcNi.getBitSets()[0].cardinality());
    }

    @Test
    public final void testProduceClassFromQuery() throws IOException {
        BooleanQuery bq = new BooleanQuery();
        Term luceneTerm = new Term("partner", "bund");
        TermQuery luceneTermQuery = new TermQuery(luceneTerm);
        bq.add(luceneTermQuery, Occur.MUST);

        FacetClass fc = fcp.produceClassFromQuery("partner:bund", bq);
        Assert.assertEquals("partner:bund", fc.getFacetClassName());
        Assert.assertTrue(2 <= fc.getBitSets()[0].cardinality());
    }

}
