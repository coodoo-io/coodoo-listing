package io.coodoo.framework.listing.boundary.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fields that shall not be part in a type wide listing filter
 * 
 * @author coodoo GmbH (coodoo.io)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ListingFilterIgnoreFields {

    String[] value();

}
