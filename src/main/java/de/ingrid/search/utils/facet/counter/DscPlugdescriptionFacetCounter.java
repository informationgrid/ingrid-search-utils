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

import org.apache.log4j.Logger;
import org.apache.lucene.util.OpenBitSet;

import de.ingrid.search.utils.ConfigurablePlugDescriptionWrapper;
import de.ingrid.search.utils.facet.FacetClassDefinition;
import de.ingrid.search.utils.facet.FacetDefinition;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.IngridQuery;

/**
 * Extends the ConfigurableFacetCounter to add counts based on the
 * plugdescription values.
 * <p/>
 * The following facets are supported:
 * <p/>
 * <b>partner</b> Add all partner of the plugdescription with the cardinality of
 * the search result. If facet classes like partner:&lt;id&gt; are defined, use
 * only the matching ones.
 * <p/>
 * <b>provider</b> Add all provider of the plugdescription with the cardinality
 * of the search result. If facet classes like provider:&lt;id&gt; are defined,
 * use only the matching ones.
 * <p/>
 * <b>provider_&lt;partner id&gt;</b> If the partner id matches a partner from
 * the plugdescription, add all provider of the plugdescription with the
 * cardinality of the search result. If facet classes like partner:&lt;id&gt;
 * are defined, use only the matching ones.
 * <p/>
 * The results obtained from the extended ConfigurableFacetCounter are NOT
 * overwritten.
 * 
 * @author joachim@wemove.com
 * 
 */
public class DscPlugdescriptionFacetCounter extends ConfigurableFacetCounter {

    private ConfigurablePlugDescriptionWrapper plugDescriptionWrapper = null;

    private static Logger LOG = Logger.getLogger(DscPlugdescriptionFacetCounter.class);
    
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * de.ingrid.search.utils.facet.counter.IFacetCounter#count(de.ingrid.utils.query
     * .IngridQuery, org.apache.lucene.util.OpenBitSetDISI, java.util.List)
     */
    @Override
    public IngridDocument count(IngridDocument result, IngridQuery query, OpenBitSet[] bitsets,
            List<FacetDefinition> facetDefs) {
        super.count(result, query, bitsets, facetDefs);
        for (FacetDefinition fd : facetDefs) {
                // add partner facet classes according to the plugdescription
                if (fd.getName().equals("partner")) {
                    for (String partner : plugDescriptionWrapper.getPlugDescription().getPartners()) {
                        String fcdString = fd.getName() + ":" + partner;
                        if (fd.getClasses() != null) {
                            for (FacetClassDefinition fcd : fd.getClasses()) {
                                if (fcd.getName().equals(fcdString)) {
                                    long sum = sumBitsetCardinalities(bitsets);
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Add facet '" + fcdString + "' with cardinality " + sum + ".");
                                    }
                                    addResult(result, fcdString, sum);
                                }
                            }
                        } else {
                            long sum = sumBitsetCardinalities(bitsets);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Add facet '" + fcdString + "' with cardinality " + sum + ".");
                            }
                            addResult(result, fcdString, sum);
                        }
                    }
                }
                // add provider facet classes according to the plugdescription
                if (fd.getName().equals("provider")) {
                    for (String provider : plugDescriptionWrapper.getPlugDescription().getProviders()) {
                        String fcdString = fd.getName() + ":" + provider;
                        if (fd.getClasses() != null) {
                            for (FacetClassDefinition fcd : fd.getClasses()) {
                                if (fcd.getName().equals(fcdString)) {
                                    long sum = sumBitsetCardinalities(bitsets);
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Add facet '" + fcdString + "' with cardinality " + sum + ".");
                                    }
                                    addResult(result, fcdString, sum);
                                }
                            }
                        } else {
                            long sum = sumBitsetCardinalities(bitsets);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Add facet '" + fcdString + "' with cardinality " + sum + ".");
                            }
                            addResult(result, fcdString, sum);
                        }
                    }
                }
                // add partner_provider facet classes according to the
                // plugdescription
                if (fd.getName().startsWith("provider_")) {
                    for (String partner : plugDescriptionWrapper.getPlugDescription().getPartners()) {
                        if (fd.getName().equals("provider_" + partner)) {
                            for (String provider : plugDescriptionWrapper.getPlugDescription().getProviders()) {
                                String fcdString = fd.getName() + ":" + provider;
                                if (fd.getClasses() != null) {
                                    for (FacetClassDefinition fcd : fd.getClasses()) {
                                        if (fcd.getName().equals(fcdString)) {
                                            long sum = sumBitsetCardinalities(bitsets);
                                            if (LOG.isDebugEnabled()) {
                                                LOG.debug("Add facet '" + fcdString + "' with cardinality " + sum + ".");
                                            }
                                            addResult(result, fcdString, sum);
                                        }
                                    }
                                } else {
                                    long sum = sumBitsetCardinalities(bitsets);
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Add facet '" + fcdString + "' with cardinality " + sum + ".");
                                    }
                                    addResult(result, fcdString, sum);
                                }
                            }
                        }
                    }
                }
        }
        return result;
    }

    public ConfigurablePlugDescriptionWrapper getPlugDescriptionWrapper() {
        return plugDescriptionWrapper;
    }

    public void setPlugDescriptionWrapper(ConfigurablePlugDescriptionWrapper plugDescriptionWrapper) {
        this.plugDescriptionWrapper = plugDescriptionWrapper;
    }

}
