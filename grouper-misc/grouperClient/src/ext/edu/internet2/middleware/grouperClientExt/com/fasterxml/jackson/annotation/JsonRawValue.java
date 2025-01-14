package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that indicates that the annotated method
 * or field should be serialized by including literal String value
 * of the property as is, without quoting of characters.
 * This can be useful for injecting values already serialized in JSON or 
 * passing javascript function definitions from server to a javascript client.
 *<p>
 * Warning: the resulting JSON stream may be invalid depending on your input value.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonRawValue
{
    /**
     * Optional argument that defines whether this annotation is active
     * or not. The only use for value 'false' if for overriding purposes
     * (which is not needed often); most likely it is needed for use
     * with "mix-in annotations" (aka "annotation overrides").
     * For most cases, however, default value of "true" is just fine
     * and should be omitted.
     */
    boolean value() default true;
}
