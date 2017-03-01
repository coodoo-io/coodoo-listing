package io.coodoo.framework.listing.boundary;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MetatdataTest {

    @Test
    public void testInitialisation() {

        long count = 835L;
        int currentPage = 5;
        int limit = 20;
        String sort = "-sortX";

        Metadata metadata = new Metadata(count, currentPage, limit, sort);

        assertEquals(count, metadata.getCount());
        assertEquals(currentPage, metadata.getCurrentPage());
        assertEquals(limit, metadata.getLimit());
        assertEquals(sort, metadata.getSort());

        assertEquals(81, metadata.getStartIndex());
        assertEquals(100, metadata.getEndIndex());
        assertEquals(42, metadata.getNumPages());
    }

}
