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
package de.ingrid.search.utils.facet.counter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.OpenBitSet;
import org.junit.jupiter.api.Test;

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
    void testCount() {

        DscPlugdescriptionFacetCounter fc = new DscPlugdescriptionFacetCounter();

        String[] facetClassDefinitions = {"datatype:csw", "datatype:dsc"};

        HashMap<String, List<String>> facetDefinitions = new HashMap<String, List<String>>();
        facetDefinitions.put("location", Arrays.asList(new String[]{"location:ffm", "location:b"}));

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
        f2.put("classes", Arrays.asList(new Object[]{class1, class2, class3}));

        Map f3 = new HashMap();
        f3.put("id", "location");

        Map f4 = new HashMap();
        f4.put("id", "provider_ni");

        Map f5 = new HashMap();
        f5.put("id", "provider");
        Map class4 = new HashMap();
        class4.put("id", "he_p1");
        f5.put("classes", Arrays.asList(new Object[]{class4}));

        query.put("FACETS", Arrays.asList(new Object[]{f1, f2, f3, f4, f5}));

        List<FacetDefinition> fDefs = FacetUtils.getFacetDefinitions(query);
        fc.count(result, null, new OpenBitSet[]{bs}, fDefs);

        assertEquals(true, result.getLong("datatype:csw") == bs.cardinality());
        assertEquals(true, result.getLong("datatype:dsc") == bs.cardinality());
        assertEquals(true, result.get("datatype:iso") == null);
        assertEquals(true, result.getLong("location:ffm") == bs.cardinality());
        assertEquals(true, result.getLong("location:b") == bs.cardinality());

        assertEquals(true, result.getLong("partner:he") == bs.cardinality());
        assertEquals(true, result.getLong("partner:ni") == bs.cardinality());

        assertEquals(true, result.getLong("provider_ni:he_p1") == bs.cardinality());
        assertEquals(true, result.getLong("provider_ni:ni_p2") == bs.cardinality());

        assertEquals(true, result.getLong("provider:he_p1") == bs.cardinality());

    }
    
}
