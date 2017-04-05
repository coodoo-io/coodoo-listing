package io.coodoo.framework.listing.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listing configuration
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@ApplicationScoped
public class ListingConfig {

    private static Logger log = LoggerFactory.getLogger(ListingConfig.class);

    /**
     * Default index for pagination
     */
    public static int DEFAULT_INDEX = 0;

    /**
     * Default current page number for pagination
     */
    public static int DEFAULT_PAGE = 1;

    /**
     * Default limit of results per page for pagination
     */
    public static int DEFAULT_LIMIT = 10;

    /**
     * If this key is present in filterAttributes map, the attributes gets disjuncted (default is conjunction)
     */
    public static String FILTER_TYPE_DISJUNCTION = "Filter-Type-Disjunction";

    /**
     * Limit on OR operator separated predicated to handle it in an IN statement
     */
    public static int OR_TO_IN_LIMIT = 10;

    /**
     * NOT operator
     */
    public static String OPERATOR_NOT = "!";

    /**
     * NOT operator as word
     */
    private static String OPERATOR_NOT_WORD_BLANK = "NOT";
    public static String OPERATOR_NOT_WORD = OPERATOR_NOT_WORD_BLANK + " ";

    /**
     * OR operator
     */
    public static String OPERATOR_OR = "|";

    /**
     * OR operator as word
     */
    private static String OPERATOR_OR_WORD_BLANK = "OR";
    public static String OPERATOR_OR_WORD = " " + OPERATOR_OR_WORD_BLANK + " ";

    /**
     * NULL operator
     */
    public static String OPERATOR_NULL = "NULL";

    /**
     * Name of the (optional) listing property file
     */
    private static final String listingPropertiesFilename = "coodoo.listing.properties";

    static Properties properties = new Properties();

    public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
        loadProperties();
    }

    private static void loadProperties() {
        InputStream inputStream = null;
        try {
            inputStream = ListingConfig.class.getClassLoader().getResourceAsStream(listingPropertiesFilename);

            if (inputStream != null) {

                properties.load(inputStream);
                log.info("Reading {}", listingPropertiesFilename);

                DEFAULT_INDEX = loadProperty(DEFAULT_INDEX, "coodoo.listing.default.index");
                DEFAULT_PAGE = loadProperty(DEFAULT_PAGE, "coodoo.listing.default.page");
                DEFAULT_LIMIT = loadProperty(DEFAULT_LIMIT, "coodoo.listing.default.limit");
                FILTER_TYPE_DISJUNCTION = loadProperty(FILTER_TYPE_DISJUNCTION, "coodoo.listing.filter.type.disjunction");
                OR_TO_IN_LIMIT = loadProperty(OR_TO_IN_LIMIT, "coodoo.listing.or.to.in.imit");
                OPERATOR_NOT = loadProperty(OPERATOR_NOT, "coodoo.listing.operator.not");
                OPERATOR_NOT_WORD_BLANK = loadProperty(OPERATOR_NOT_WORD_BLANK, "coodoo.listing.operator.not.word");
                OPERATOR_OR = loadProperty(OPERATOR_OR, "coodoo.listing.operator_or");
                OPERATOR_OR_WORD_BLANK = loadProperty(OPERATOR_OR_WORD_BLANK, "coodoo.listing.operator.or.word");
                OPERATOR_NULL = loadProperty(OPERATOR_NULL, "coodoo.listing.operator.null");
            }
        } catch (IOException e) {
            log.info("Couldn't read {}!", listingPropertiesFilename, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.warn("Couldn't close {}!", listingPropertiesFilename, e);
            }
        }
    }

    private static String loadProperty(String value, String key) {

        String property = properties.getProperty(key);
        if (property == null) {
            return value;
        }
        log.info("Listing Property {} loaded: {}", key, property);
        return property;
    }

    private static int loadProperty(int value, String key) {
        String property = properties.getProperty(key);
        if (property != null) {
            try {
                log.info("Listing Property {} loaded: {}", key, property);
                return Integer.valueOf(property).intValue();
            } catch (NumberFormatException e) {
                log.warn("Listing Property {} value invalid: {}", key, property);
            }
        }
        return value;
    }

}
