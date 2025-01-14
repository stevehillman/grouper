package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsontype.impl;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.BeanProperty;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

/**
 * Type serializer that will embed type information in an array,
 * as the first element, and actual value as the second element.
 */
public class AsArrayTypeSerializer extends TypeSerializerBase
{
    public AsArrayTypeSerializer(TypeIdResolver idRes, BeanProperty property) {
        super(idRes, property);
    }

    @Override
    public AsArrayTypeSerializer forProperty(BeanProperty prop) {
        return (_property == prop) ? this : new AsArrayTypeSerializer(_idResolver, prop);
    }
    
    @Override
    public As getTypeInclusion() { return As.WRAPPER_ARRAY; }
}
