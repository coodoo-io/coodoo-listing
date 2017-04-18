package io.coodoo.framework.listing.control;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.DatabaseUnitException;
import org.hibernate.HibernateException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.framework.listing.dbunit.AbstractDbUnitTest;
import io.coodoo.framework.listing.dbunit.model.TestNumbersEntity;

public class NumbersTest extends AbstractDbUnitTest {

    @BeforeClass
    public static void initDB() throws HibernateException, DatabaseUnitException, SQLException {
        datasetXml = "numbers-dataset.xml";
        initEntityManager();
    }

    @Before
    public void initFilterParams() {
        parameters = new ListingParameters();
        parameters.setLimit(Integer.MAX_VALUE);
    }

    @Test
    public void testDatasetLoaded() {

        TestNumbersEntity result = entityManager.find(TestNumbersEntity.class, 1L);

        assertEquals(1L, result.getId().longValue());
    }

    @Ignore
    @Test
    public void testGernerate() throws IOException {

        List<String> lines = new ArrayList<>();

        lines.add("<?xml version='1.0' encoding='UTF-8'?>");
        lines.add("<dataset>");

        for (Long id = 1L; id <= 1000; id++) {

            String line = "<TEST_NUMBERS_ENTITY ID='" + id + "'";
            line += " LONG_CLASS='" + id + "'";
            line += " LONG_PRIMITIVE='" + id + "'";
            line += " LONG_LIKE='" + id + "'";
            line += " INT_CLASS='" + id + "'";
            line += " INT_PRIMITIVE='" + id + "'";
            line += " INT_LIKE='" + id + "'";
            line += " SHORT_CLASS='" + id + "'";
            line += " SHORT_PRIMITIVE='" + id + "'";
            line += " SHORT_LIKE='" + id + "' />";
            lines.add(line);
        }
        lines.add("</dataset>");

        Path file = Paths.get("src/test/resources/numbers-dataset.xml");
        Files.write(file, lines, Charset.forName("UTF-8"));
    }

}
