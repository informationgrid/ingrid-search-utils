package de.ingrid.search.utils.facet.counter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.lucene.util.OpenBitSet;
import org.junit.Test;

import de.ingrid.search.utils.ConfigurablePlugDescriptionWrapper;
import de.ingrid.search.utils.facet.FacetDefinition;
import de.ingrid.search.utils.facet.FacetUtils;
import de.ingrid.search.utils.facet.counter.DscPlugdescriptionFacetCounter;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

public class DscPlugdescriptionFacetCounterTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testCount() {

        DscPlugdescriptionFacetCounter fc = new DscPlugdescriptionFacetCounter();

        String[] facetClassDefinitions = { "datatype:csw", "datatype:dsc" };

        HashMap<String, List<String>> facetDefinitions = new HashMap<String, List<String>>();
        facetDefinitions.put("location", Arrays.asList(new String[] { "location:ffm", "location:b" }));

        PlugDescription pd = new PlugDescription();
        pd.addPartner("he");
        pd.addPartner("ni");
        pd.addProvider("he_p1");
        pd.addProvider("ni_p2");

        fc.setFacetClassDefinitions(Arrays.asList(facetClassDefinitions));
        fc.setFacetDefinitions(facetDefinitions);
        fc.setPlugDescriptionWrapper(new ConfigurablePlugDescriptionWrapper(pd));

        OpenBitSet bs = new OpenBitSet();
        bs.set(10L);
        bs.set(12L);
        bs.set(22L);
        IngridDocument result = new IngridDocument();
        IngridQuery query = null;
        try {
            query = QueryStringParser.parse("wasser");
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map f1 = new HashMap();
        f1.put("id", "partner");

        Map f2 = new HashMap();
        f2.put("id", "datatype");
        Map class1 = new HashMap();
        class1.put("id", "csw");
        Map class2 = new HashMap();
        class2.put("id", "dsc");
        Map class3 = new HashMap();
        class3.put("id", "iso");
        f2.put("classes", Arrays.asList(new Object[] { class1, class2, class3 }));

        Map f3 = new HashMap();
        f3.put("id", "location");

        Map f4 = new HashMap();
        f4.put("id", "provider_ni");

        Map f5 = new HashMap();
        f5.put("id", "provider");
        Map class4 = new HashMap();
        class4.put("id", "he_p1");
        f5.put("classes", Arrays.asList(new Object[] { class4 }));
        
        query.put("FACETS", Arrays.asList(new Object[] { f1, f2, f3, f4, f5 }));

        List<FacetDefinition> fDefs = FacetUtils.getFacetDefinitions(query);
        fc.count(result, null, new OpenBitSet[] {bs}, fDefs);

        Assert.assertEquals(true, result.getLong("datatype:csw") == bs.cardinality());
        Assert.assertEquals(true, result.getLong("datatype:dsc") == bs.cardinality());
        Assert.assertEquals(true, result.get("datatype:iso") == null);
        Assert.assertEquals(true, result.getLong("location:ffm") == bs.cardinality());
        Assert.assertEquals(true, result.getLong("location:b") == bs.cardinality());

        Assert.assertEquals(true, result.getLong("partner:he") == bs.cardinality());
        Assert.assertEquals(true, result.getLong("partner:ni") == bs.cardinality());

        Assert.assertEquals(true, result.getLong("provider_ni:he_p1") == bs.cardinality());
        Assert.assertEquals(true, result.getLong("provider_ni:ni_p2") == bs.cardinality());
        
        Assert.assertEquals(true, result.getLong("provider:he_p1") == bs.cardinality());

    }
    
}
