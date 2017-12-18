package io.coodoo.framework.listing.boundary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
    public void testGetSortAttribute_nonSet() {

        String sortAttribute = classUnderTest.getSortAttribute();

        assertThat(sortAttribute, equalTo(null));
    }

    @Test
    public void testGetSortAttribute_set() {

        classUnderTest.setSortAttribute("fisch");

        String sortAttribute = classUnderTest.getSortAttribute();

        assertThat(sortAttribute, equalTo("fisch"));
    }

    @Test
    public void testGetSortAttribute_setAsc() {

        classUnderTest.setSortAttribute("+fisch");

        String sortAttribute = classUnderTest.getSortAttribute();

        assertThat(sortAttribute, equalTo("fisch"));
    }

    @Test
    public void testGetSortAttribute_setDesc() {

        classUnderTest.setSortAttribute("-fisch");

        String sortAttribute = classUnderTest.getSortAttribute();

        assertThat(sortAttribute, equalTo("fisch"));
    }

    @Test
    public void testIsSortAsc_nonSet() {

        boolean sortAsc = classUnderTest.isSortAsc();

        assertThat(sortAsc, equalTo(true));
    }

    @Test
    public void testIsSortAsc_set() {

        classUnderTest.setSortAttribute("fisch");

        boolean sortAsc = classUnderTest.isSortAsc();

        assertThat(sortAsc, equalTo(true));
    }

    @Test
    public void testIsSortAsc_setAsc() {

        classUnderTest.setSortAttribute("+fisch");

        boolean sortAsc = classUnderTest.isSortAsc();

        assertThat(sortAsc, equalTo(true));
    }

    @Test
    public void testIsSortAsc_setDesc() {

        classUnderTest.setSortAttribute("-fisch");

        boolean sortAsc = classUnderTest.isSortAsc();

        assertThat(sortAsc, equalTo(false));
    }

}
