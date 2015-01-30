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
package de.ingrid.search.utils.facet.counter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.facet.FacetClass;
import de.ingrid.search.utils.facet.FacetClassRegistry;
import de.ingrid.search.utils.facet.FacetDefinition;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.IngridQuery;

public class IndexFacetCounter implements IFacetCounter {

    private FacetClassRegistry _facetClassRegistry;

    private static Logger LOG = Logger.getLogger(IndexFacetCounter.class);

    public IndexFacetCounter() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ingrid.search.utils.facet.counter.IFacetCounter#count(java.util.Map,
     * de.ingrid.utils.query.IngridQuery, org.apache.lucene.util.OpenBitSetDISI,
     * java.util.List)
     */
    @Override
    public IngridDocument count(IngridDocument result, IngridQuery query, OpenBitSet[] bitsets,
            List<FacetDefinition> facetDefs) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Count facets with '" + this.getClass().getName() + "'");
        }

        if (result == null) {
            return result;
        }
        List<FacetClass> facetClasses = new ArrayList<FacetClass>();

        if (facetDefs == null)
            return null;

        // get all FacetClasses from the definitions, which contain a BitSet
        // of all the documents containing each FacetClass
        for (FacetDefinition def : facetDefs) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Get facet '" + def.getName() + "': [field: " + def.getField() + ", query: "
                        + def.getQueryFragment() + ", classes: " + def.getClasses() + "]");
            }
            facetClasses.addAll(_facetClassRegistry.getFacetClasses(def));
        }

        for (FacetClass fc : facetClasses) {
            // if the facet class has already been set by another facet counter,
            // ignore it
            if (!result.containsKey(fc.getFacetClassName())) {
                long start = 0;
                if (LOG.isDebugEnabled()) {
                    start = System.currentTimeMillis();
                }
                result.put(fc.getFacetClassName(), getFacetHitCount(bitsets, fc.getBitSets()));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Create facet class '" + fc.getFacetClassName() + "', set to: "
                            + result.getLong(fc.getFacetClassName()) + " in " + (System.currentTimeMillis() - start)
                            + " ms.");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skip creating facet class '" + fc.getFacetClassName()
                            + "', because is was already set to: " + result.getLong(fc.getFacetClassName()));
                }
            }
        }
        return result;
    }

    private long getFacetHitCount(OpenBitSet[] baseBitSets, OpenBitSet[] filterBitSets) {
        if (baseBitSets.length != filterBitSets.length) {
            LOG.warn("Different bitset array sizes detected. Results may be inaccurate.");
        }

        int minArraySize = (baseBitSets.length > filterBitSets.length) ? filterBitSets.length : baseBitSets.length;
        long result = 0;

        for (int i = 0; i < minArraySize; i++) {
            if (filterBitSets[i] != null) {
                OpenBitSet clone = (OpenBitSet) filterBitSets[i].clone();
                clone.and(baseBitSets[i]);
                result += clone.cardinality();
            }
        }
        return result;
    }

    public FacetClassRegistry getFacetClassRegistry() {
        return _facetClassRegistry;
    }

    public void setFacetClassRegistry(FacetClassRegistry facetClassRegistry) {
        _facetClassRegistry = facetClassRegistry;
    }

    @Override
    public void initialize() {
        _facetClassRegistry.clear();
    }

}
