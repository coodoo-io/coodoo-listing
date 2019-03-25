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
    public void testGetDecodedFilterAttributes_true() {

        ListingConfig.URI_DECODE = true;

        classUnderTest.addFilterAttributes("test", "%2B");
        Map<String, String> result = classUnderTest.getFilterAttributes();
        assertThat(result.get("test"), equalTo("+"));
    }

    @Test
    public void testGetDecodedFilterAttributes_false() {

        ListingConfig.URI_DECODE = false;

        classUnderTest.addFilterAttributes("test", "%2B");
        Map<String, String> result = classUnderTest.getFilterAttributes();
        assertThat(result.get("test"), equalTo("%2B"));
    }

    @Test
    public void testGetIndex() throws Exception {

        Integer expected = 77;
        classUnderTest.setIndex(expected);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetIndex_DefaultValue() throws Exception {

        Integer expected = ListingConfig.DEFAULT_INDEX;

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetIndex_zero() throws Exception {

        Integer expected = ListingConfig.DEFAULT_INDEX;

        classUnderTest.setLimit(0);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetIndex_negative() throws Exception {

        Integer expected = ListingConfig.DEFAULT_INDEX;

        classUnderTest.setLimit(-1);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetIndex_withPage() throws Exception {

        Integer expected = 820;

        classUnderTest.setPage(83);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetIndex_withPage1() throws Exception {

        Integer expected = 0;

        classUnderTest.setPage(1);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetIndex_withPageAndLimit() throws Exception {

        Integer expected = 3;

        classUnderTest.setPage(2);
        classUnderTest.setLimit(3);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetIndex_withPage1AndLimit1() throws Exception {

        Integer expected = 0;

        classUnderTest.setPage(1);
        classUnderTest.setLimit(1);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testSetIndex() throws Exception {

        Integer expected = 77;

        classUnderTest.setIndex(expected);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testSetIndex_zero() throws Exception {

        Integer expected = 0;

        classUnderTest.setIndex(expected);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testSetIndex_null() throws Exception {

        Integer expected = ListingConfig.DEFAULT_INDEX;

        classUnderTest.setIndex(null);

        Integer result = classUnderTest.getIndex();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetPage() throws Exception {

        Integer expected = ListingConfig.DEFAULT_PAGE;

        classUnderTest.setPage(expected);

        Integer result = classUnderTest.getPage();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetPage_neagtive() throws Exception {

        Integer expected = ListingConfig.DEFAULT_PAGE;

        classUnderTest.setPage(-3);

        Integer result = classUnderTest.getPage();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetPage_withLimitAndIndex() throws Exception {

        Integer expected = 4;

        classUnderTest.setLimit(7);
        classUnderTest.setIndex(23);

        Integer result = classUnderTest.getPage();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetPage_withLimitAndIndexNewPage() throws Exception {

        Integer expected = 4;

        classUnderTest.setLimit(7);
        classUnderTest.setIndex(28);

        Integer result = classUnderTest.getPage();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetPage_withLimitZeroAndIndex() throws Exception {

        Integer expected = ListingConfig.DEFAULT_PAGE;

        classUnderTest.setLimit(0);
        classUnderTest.setIndex(23);

        Integer result = classUnderTest.getPage();

        assertThat(result, equalTo(expected));
    }

    @Test
    public void testGetPage_withLimitAndIndexZero() throws Exception {

        Integer expected = ListingConfig.DEFAULT_PAGE;

        classUnderTest.setLimit(7);
        classUnderTest.setIndex(0);

        Integer result = classUnderTest.getPage();

        assertThat(result, equalTo(expected));
    }

}
