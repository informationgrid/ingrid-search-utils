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

import java.io.Serializable;

import org.apache.lucene.util.OpenBitSet;

public class FacetClass implements Serializable {

    /**
     * The serialization ID for storing objects.
     */
    private static final long serialVersionUID = 7282020135406385812L;

    private String facetClassName;

    private OpenBitSet[] bitSets;

    public FacetClass(String facetClassname, OpenBitSet[] filterBitSet) {
        this.facetClassName = facetClassname;
        this.bitSets = filterBitSet;
    }

    public void setFacetClassName(String facetName) {
        this.facetClassName = facetName;
    }

    public String getFacetClassName() {
        return facetClassName;
    }

    public void setBitSets(OpenBitSet[] bitSet) {
        this.bitSets = bitSet;
    }

    public OpenBitSet[] getBitSets() {
        return bitSets;
    }

    public String toString() {
        String result = facetClassName + " with bitsets:";
        for (int i = 0; i < bitSets.length; i++) {
            if (bitSets[i] != null) {
                result += " " + i + ":" + bitSets[i].cardinality();
            } else {
                result += " " + i + ":" + null;
            }
        }
        return result;
    }

}
