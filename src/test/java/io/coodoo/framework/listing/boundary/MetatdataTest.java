package io.coodoo.framework.listing.boundary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class MetatdataTest {

    @Test
    public void testInitialisation() {

        Long count = 835L;
        Integer currentPage = 5;
        Integer limit = 20;
        String sort = "-sortX";

        Metadata metadata = new Metadata(count, currentPage, limit, sort);

        assertThat(metadata.getCount(), equalTo(count));
        assertThat(metadata.getCurrentPage(), equalTo(currentPage));
        assertThat(metadata.getLimit(), equalTo(limit));
        assertThat(metadata.getSort(), equalTo(sort));

        assertThat(metadata.getStartIndex(), equalTo(81));
        assertThat(metadata.getEndIndex(), equalTo(100));
        assertThat(metadata.getNumPages(), equalTo(42));

    }

    @Test
    public void testNullParameterLimit() {

        Long count = 835L;
        Integer currentPage = 5;
        Integer limit = null;

        Metadata metadata = new Metadata(count, currentPage, limit);

        assertThat(metadata.getCount(), equalTo(count));
        assertThat(metadata.getCurrentPage(), equalTo(currentPage));
        assertThat(metadata.getLimit(), equalTo(0));

        assertThat(metadata.getStartIndex(), equalTo(1));
        assertThat(metadata.getEndIndex(), equalTo(count.intValue()));
        assertThat(metadata.getNumPages(), equalTo(1));
    }

    @Test
    public void testNullParameterCount() {

        Long count = null;
        Integer currentPage = 5;
        Integer limit = 10;

        Metadata metadata = new Metadata(count, currentPage, limit);

        assertThat(metadata.getCount(), equalTo(count));
        assertThat(metadata.getCurrentPage(), equalTo(currentPage));
        assertThat(metadata.getLimit(), equalTo(limit));

        assertThat(metadata.getStartIndex(), equalTo(1));
        assertThat(metadata.getEndIndex(), equalTo(1));
        assertThat(metadata.getNumPages(), equalTo(1));
    }

    @Test
    public void testNullParameterCurrentPage() {

        Long count = 835L;
        Integer currentPage = null;
        Integer limit = 10;

        Metadata metadata = new Metadata(count, currentPage, limit);

        assertThat(metadata.getCount(), equalTo(count));
        assertThat(metadata.getCurrentPage(), equalTo(1));
        assertThat(metadata.getLimit(), equalTo(limit));

        assertThat(metadata.getStartIndex(), equalTo(1));
        assertThat(metadata.getEndIndex(), equalTo(limit));
        assertThat(metadata.getNumPages(), equalTo(84));
    }

}
