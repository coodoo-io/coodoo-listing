package io.coodoo.framework.listing.control;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.hibernate.HibernateException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.coodoo.framework.listing.boundary.Listing;
import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.framework.listing.dbunit.AbstractDbUnitTest;
import io.coodoo.framework.listing.dbunit.model.TestDatesEntity;

public class DateTest extends AbstractDbUnitTest {

    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeClass
    public static void initDB() throws HibernateException, DatabaseUnitException, SQLException {
        datasetXml = "date-dataset.xml";
        initEntityManager();
    }

    @Before
    public void initFilterParams() {
        parameters = new ListingParameters();
        parameters.setLimit(Integer.MAX_VALUE);
    }

    @Test
    public void testDatasetLoaded() {

        TestDatesEntity result = entityManager.find(TestDatesEntity.class, 1L);

        assertEquals(1L, result.getId().longValue());
    }

    @Ignore
    @Test
    public void testLocalDateTimeDay() {

        parameters.addFilterAttributes("localDateTime1", "18.2.2014");

        Long result = Listing.countListing(entityManager, TestDatesEntity.class, parameters);

        assertEquals(1L, result.longValue());
    }

    @Ignore
    @Test
    public void testLocalDateTimeMonth() {

        parameters.addFilterAttributes("localDateTime1", "12.2015");

        Long result = Listing.countListing(entityManager, TestDatesEntity.class, parameters);

        assertEquals(31L, result.longValue());
    }

    @Ignore
    @Test
    public void testLocalDateTimeYear() {

        parameters.addFilterAttributes("localDateTime1", "2015");

        Long result = Listing.countListing(entityManager, TestDatesEntity.class, parameters);

        assertEquals(365L, result.longValue());
    }

    @Ignore
    @Test
    public void testDateDay() {

        parameters.addFilterAttributes("date1", "18.2.2014");

        Long result = Listing.countListing(entityManager, TestDatesEntity.class, parameters);

        assertEquals(1L, result.longValue());
    }

    @Ignore
    @Test
    public void testDateMonth() {

        parameters.addFilterAttributes("date1", "12.2015");

        Long result = Listing.countListing(entityManager, TestDatesEntity.class, parameters);

        assertEquals(31L, result.longValue());
    }

    @Ignore
    @Test
    public void testDateYear() {

        parameters.addFilterAttributes("date1", "2015");

        Long result = Listing.countListing(entityManager, TestDatesEntity.class, parameters);

        assertEquals(365L, result.longValue());
    }

    @Ignore
    @Test
    public void testGernerate() throws IOException {

        LocalDateTime localDateTime1 = LocalDateTime.of(2013, 12, 31, 23, 59, 59);
        LocalDateTime localDateTime2 = LocalDateTime.of(2017, 1, 1, 0, 0, 0);

        LocalDateTime date1 = LocalDateTime.of(2003, 12, 31, 23, 59, 59);
        LocalDateTime date2 = LocalDateTime.of(2007, 1, 1, 0, 0, 0);

        List<String> lines = new ArrayList<>();

        lines.add("<?xml version='1.0' encoding='UTF-8'?>");
        lines.add("<dataset>");

        for (Long id = 1L; id <= 1000; id++) {

            String line = "<TEST_DATES_ENTITY ID='" + id + "'";
            line += " LOCAL_DATE_TIME1='" + localDateTime1.format(DATE_PATTERN) + "'";
            line += " LOCAL_DATE_TIME2='" + localDateTime2.format(DATE_PATTERN) + "'";
            line += " DATE1='" + date1.format(DATE_PATTERN) + "'";
            line += " DATE2='" + date2.format(DATE_PATTERN) + "' />";
            lines.add(line);

            localDateTime1 = localDateTime1.plusDays(1L);
            localDateTime2 = localDateTime2.minusHours(6L);
            date1 = date1.plusDays(1L);
            date2 = date2.minusHours(6L);
        }
        lines.add("</dataset>");

        Path file = Paths.get("src/test/resources/date-dataset.xml");
        Files.write(file, lines, Charset.forName("UTF-8"));
    }

}
