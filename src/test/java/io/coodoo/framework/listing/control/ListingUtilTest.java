package io.coodoo.framework.listing.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;

import org.junit.Test;

public class ListingUtilTest {

    @Test
    public void testParseDateTime() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("04.10.1983", false);
        assertEquals("1983-10-04T00:00", result.toString());
    }

    @Test
    public void testParseDateTime_separator_dash() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("04-10-1983", false);
        assertEquals("1983-10-04T00:00", result.toString());
    }

    @Test
    public void testParseDateTime_separator_blank() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("04 10 1983", false);
        assertEquals("1983-10-04T00:00", result.toString());
    }

    @Test
    public void testParseDateTime_separator_slash() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("04/10/1983", false);
        assertEquals("1983-10-04T00:00", result.toString());
    }

    @Test
    public void testParseDateTime_invalid() throws Exception {

        assertNull(ListingUtil.parseDateTime("Bullshit", false));
    }

    @Test
    public void testParseDateTime_null() throws Exception {

        assertNull(ListingUtil.parseDateTime(null, false));
    }

    @Test
    public void testParseDateTime_year_start() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("2000", false);
        assertEquals("2000-01-01T00:00", result.toString());
    }

    @Test
    public void testParseDateTime_year_end() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("2000", true);
        assertEquals("2000-12-31T23:59:59", result.toString());
    }

    @Test
    public void testParseDateTime_month_start() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("09.1999", false);
        assertEquals("1999-09-01T00:00", result.toString());
    }

    @Test
    public void testParseDateTime_month_end() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("09.1999", true);
        assertEquals("1999-09-30T23:59:59", result.toString());
    }

    @Test
    public void testParseDateTime_day_start() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("29.02.2020", false);
        assertEquals("2020-02-29T00:00", result.toString());
    }

    @Test
    public void testParseDateTime_day_end() throws Exception {

        LocalDateTime result = ListingUtil.parseDateTime("29.02.2020", true);
        assertEquals("2020-02-29T23:59:59", result.toString());
    }

}
