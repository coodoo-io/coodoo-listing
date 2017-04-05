package io.coodoo.framework.listing.dbunit;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.HibernateException;
import org.hibernate.internal.SessionImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import io.coodoo.framework.listing.boundary.ListingParameters;
import io.coodoo.framework.listing.control.ListingQuery;

public abstract class AbstractDbUnitTest {

    private static EntityManagerFactory entityManagerFactory;
    private static IDatabaseConnection connection;
    private static IDataSet dataset;
    public static EntityManager entityManager;

    public static String datasetXml = "test-dataset.xml";

    public ListingParameters params;

    /**
     * Set up memory database and insert data from test-dataset.xml
     * 
     * @throws DatabaseUnitException
     * @throws HibernateException
     * @throws SQLException
     */
    @BeforeClass
    public static void initEntityManager() throws HibernateException, DatabaseUnitException, SQLException {
        entityManagerFactory = Persistence.createEntityManagerFactory("listing-test-db");
        entityManager = entityManagerFactory.createEntityManager();
        connection = new DatabaseConnection(((SessionImpl) (entityManager.getDelegate())).connection());
        connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(datasetXml);
        if (inputStream != null) {
            FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
            flatXmlDataSetBuilder.setColumnSensing(true);
            dataset = flatXmlDataSetBuilder.build(inputStream);
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataset);
        }
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void closeEntityManager() {
        entityManager.close();
        entityManagerFactory.close();
    }

    public <T> List<T> getListing(Class<T> entityClass, ListingParameters queryParams) {
        return new ListingQuery<>(entityManager, entityClass).sort(queryParams.getSortAttribute(), queryParams.isSortAsc())
                        .filterAllAttributes(queryParams.getFilter()).filterByAttributes(queryParams.getFilterAttributes())
                        .list(queryParams.getIndex(), queryParams.getLimit());
    }

    public <T> Long countListing(Class<T> entityClass, ListingParameters queryParams) {
        return new ListingQuery<>(entityManager, entityClass).sort(queryParams.getSortAttribute(), queryParams.isSortAsc())
                        .filterAllAttributes(queryParams.getFilter()).filterByAttributes(queryParams.getFilterAttributes()).count();
    }

}
