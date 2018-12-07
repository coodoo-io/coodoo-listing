package io.coodoo.framework.listing.boundary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.coodoo.framework.listing.control.ListingConfig;

public class ListingParametersTest {

    private ListingParameters classUnderTest;

    @Before
    public void init() {
        classUnderTest = new ListingParameters();
    }

    @Test
    public void testGetLimit_noLimitSet_defaultLimit() {

        Integer limit = classUnderTest.getLimit();

        assertThat(limit, is(equalTo(ListingConfig.DEFAULT_LIMIT)));
    }

    @Test
    public void testGetLimit_limitWanted_limit() {

        classUnderTest.setLimit(83);

        Integer limit = classUnderTest.getLimit();

        assertThat(limit, equalTo(83));
    }

    @Test
    public void testGetLimit_noLimit() {

        classUnderTest.setLimit(0);

        Integer page = classUnderTest.getPage();

        Integer limit = classUnderTest.getLimit();

        assertThat(page, equalTo(1));
        assertThat(limit, equalTo(0));
    }

    @Test
    public void testGetSortAttribute_set() {

        classUnderTest.setSortAttribute("fisch");

        String sortAttribute = classUnderTest.getSortAttribute();

        assertThat(sortAttribute, equalTo("fisch"));
    }

    @Test
    public void testGetDecodedFilterAttributes_get() {
        classUnderTest.addFilterAttributes("test", "%2B");
        Map<String, String> result = classUnderTest.getFilterAttributes();
        assertThat(result.get("test"), equalTo("+"));
    }

    @Test
    public void testGetPage() throws Exception {

        classUnderTest.setLimit(0);
        classUnderTest.setPage(null);
        classUnderTest.setIndex(1);

        Integer page = classUnderTest.getPage();

        assertThat(page, equalTo(1));
    }

}
