package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.type;

import java.lang.reflect.Type;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.JavaType;

/**
 * Class that defines API that can be used to modify details of
 * {@link JavaType} instances constructed using {@link TypeFactory}.
 * Registered modifiers are called in order, to let them modify (or
 * replace) basic type instance factory constructs.
 * This is typically needed to support creation of
 * {@link MapLikeType} and {@link CollectionLikeType} instances,
 * as those cannot be constructed in generic fashion.
 */
public abstract class TypeModifier
{
    /**
     * Method called to let modifier change constructed type definition.
     * Note that this is only guaranteed to be called for
     * non-container types ("simple" types not recognized as arrays,
     * <code>java.util.Collection</code> or <code>java.util.Map</code>).
     * 
     * @param type Instance to modify
     * @param jdkType JDK type that was used to construct instance to modify
     * @param context Type resolution context used for the type
     * @param typeFactory Type factory that can be used to construct parameter type; note,
     *   however, that care must be taken to avoid infinite loops -- specifically, do not
     *   construct instance of primary type itself
     * 
     * @return Actual type instance to use; usually either <code>type</code> (as is or with
     *    modifications), or a newly constructed type instance based on it. Cannot be null.
     */
    public abstract JavaType modifyType(JavaType type, Type jdkType, TypeBindings context,
            TypeFactory typeFactory);
}
