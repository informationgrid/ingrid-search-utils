/*
 * **************************************************-
 * ingrid-search-utils
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
/**
 * 
 */
package de.ingrid.search.utils.facet.counter;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.facet.FacetClassDefinition;
import de.ingrid.search.utils.facet.FacetDefinition;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.IngridQuery;

/**
 * Add facet class counts based on a injected configuration.
 * <p/>
 * The configuration can be a mapping for
 * <p/>
 * 1.) facetDefinitions Map
 * <p/>
 * facet:[facetClass1, facetClass2, ..]
 * <p/>
 * If facet is requested, all facetClasses will be added with the cardinality of
 * the search results.
 * <p/>
 * 2.) facetClassDefinitions as List
 * <p/>
 * [facetClass1, facetClass2, ..]
 * <p/>
 * If one of the facet classes is requested, it will be added with the
 * cardinality of the search results.
 * <p/>
 * <p/>
 * This way the responsibility of the iplug can be flexible defined.
 * 
 * @author joachim@wemove.com
 * 
 */
public class ConfigurableFacetCounter implements IFacetCounter {

    private Map<String, List<String>> facetDefinitions = null;

    private List<String> facetClassDefinitions = null;

    private static Logger LOG = Logger.getLogger(ConfigurableFacetCounter.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ingrid.search.utils.facet.counter.IFacetCounter#count(de.ingrid.utils
     * .query .IngridQuery, org.apache.lucene.util.OpenBitSetDISI,
     * java.util.List)
     */
    @Override
    public IngridDocument count(IngridDocument result, IngridQuery query, OpenBitSet[] bitsets,
            List<FacetDefinition> facetDefs) {
        for (FacetDefinition fd : facetDefs) {
            if (fd.getClasses() == null) {
                if (facetDefinitions != null && facetDefinitions.containsKey(fd.getName())) {
                    for (String fcd : facetDefinitions.get(fd.getName())) {
                        long sum = sumBitsetCardinalities(bitsets);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Add facet '" + fcd + "' with cardinality " + sum + ".");
                        }
                        addResult(result, fcd, sum);
                    }
                }
            } else {
                if (facetClassDefinitions != null) {
                    for (FacetClassDefinition fcd : fd.getClasses()) {
                        if (facetClassDefinitions.contains(fcd.getName())) {
                            long sum = sumBitsetCardinalities(bitsets);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Add facet '" + fcd.getName() + "' with cardinality " + sum + ".");
                            }
                            addResult(result, fcd.getName(), sum);
                        }
                    }
                }
            }
        }
        return result;
    }

    public Map<String, List<String>> getFacetDefinitions() {
        return facetDefinitions;
    }

    public void setFacetDefinitions(Map<String, List<String>> facetDefinitions) {
        this.facetDefinitions = facetDefinitions;
    }

    public List<String> getFacetClassDefinitions() {
        return facetClassDefinitions;
    }

    public void setFacetClassDefinitions(List<String> facetClassDefinitions) {
        this.facetClassDefinitions = facetClassDefinitions;
    }

    protected void addResult(IngridDocument result, String facetClassName, long cnt) {
        if (!result.containsKey(facetClassName)) {
            result.put(facetClassName, cnt);
        }
    }

    protected long sumBitsetCardinalities(OpenBitSet[] bitSets) {
        long result = 0;
        for (OpenBitSet bitSet : bitSets) {
            result += bitSet.cardinality();
        }
        return result;
    }

    @Override
    public void initialize() {
        // nothing to be done
    }

}
