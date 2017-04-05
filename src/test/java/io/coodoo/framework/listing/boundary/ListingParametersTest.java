package io.coodoo.framework.listing.boundary;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

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
    public void testGetLimit_noLimitWanted_null() {

        classUnderTest.setLimit(0);

        Integer limit = classUnderTest.getLimit();

        assertThat(limit, is(nullValue()));
    }

    @Test
    public void testGetLimit_limitWanted_limit() {

        classUnderTest.setLimit(83);

        Integer limit = classUnderTest.getLimit();

        assertThat(limit, is(equalTo(83)));
    }

}
