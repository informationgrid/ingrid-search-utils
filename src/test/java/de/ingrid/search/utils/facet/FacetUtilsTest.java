package de.ingrid.search.utils.facet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FacetUtilsTest {

    @Test
    public void testCreateOpenBitsetWithCardinality() {

        assertEquals(1000, FacetUtils.createOpenBitsetWithCardinality(1000).cardinality());

    }

}
