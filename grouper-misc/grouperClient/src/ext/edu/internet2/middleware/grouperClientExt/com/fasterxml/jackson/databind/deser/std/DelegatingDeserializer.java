package edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.util.Collection;

import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.core.JsonParser;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.*;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.deser.*;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.type.LogicalType;
import edu.internet2.middleware.grouperClientExt.com.fasterxml.jackson.databind.util.AccessPattern;

/**
 * Base class that simplifies implementations of {@link JsonDeserializer}s
 * that mostly delegate functionality to another deserializer implementation
 * (possibly forming a chaing of deserializers delegating functionality
 * in some cases)
 * 
 * @since 2.1
 */
public abstract class DelegatingDeserializer
    extends StdDeserializer<Object>
    implements ContextualDeserializer, ResolvableDeserializer
{
    private static final long serialVersionUID = 1L;

    protected final JsonDeserializer<?> _delegatee;
    
    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    public DelegatingDeserializer(JsonDeserializer<?> d)
    {
        super(d.handledType());
        _delegatee = d;
    }

    /*
    /**********************************************************************
    /* Abstract methods to implement
    /**********************************************************************
     */
    
    protected abstract JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee);
    
    /*
    /**********************************************************************
    /* Overridden methods for contextualization, resolving
    /**********************************************************************
     */

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        if (_delegatee instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer) _delegatee).resolve(ctxt);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property)
        throws JsonMappingException
    {
        JavaType vt = ctxt.constructType(_delegatee.handledType());
        JsonDeserializer<?> del = ctxt.handleSecondaryContextualization(_delegatee,
                property, vt);
        if (del == _delegatee) {
            return this;
        }
        return newDelegatingInstance(del);
    }

    @Override
    public JsonDeserializer<?> replaceDelegatee(JsonDeserializer<?> delegatee)
    {
        if (delegatee == _delegatee) {
            return this;
        }
        return newDelegatingInstance(delegatee);
    }

    /*
    /**********************************************************************
    /* Overridden deserialization methods
    /**********************************************************************
     */

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException
    {
        return _delegatee.deserialize(p,  ctxt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt,
            Object intoValue)
        throws IOException
    {
        return ((JsonDeserializer<Object>)_delegatee).deserialize(p, ctxt, intoValue);
    }

    @Override
    public Object deserializeWithType(JsonParser p, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException
    {
        return _delegatee.deserializeWithType(p, ctxt, typeDeserializer);
    }

    /*
    /**********************************************************************
    /* Overridden other methods
    /**********************************************************************
     */

    @Override
    public boolean isCachable() { return _delegatee.isCachable(); }

    @Override // since 2.9
    public Boolean supportsUpdate(DeserializationConfig config) {
        return _delegatee.supportsUpdate(config);
    }

    @Override
    public JsonDeserializer<?> getDelegatee() {
        return _delegatee;
    }

    @Override
    public SettableBeanProperty findBackReference(String logicalName) {
        // [databind#253]: Hope this works....
        return _delegatee.findBackReference(logicalName);
    }

    @Override
    public AccessPattern getNullAccessPattern() {
        return _delegatee.getNullAccessPattern();
    }

    @Override
    public Object getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return _delegatee.getNullValue(ctxt);
    }

    @Override
    public Object getEmptyValue(DeserializationContext ctxt) throws JsonMappingException {
        return _delegatee.getEmptyValue(ctxt);
    }

    @Override // since 2.12
    public LogicalType logicalType() {
        return _delegatee.logicalType();
    }

    @Override
    public Collection<Object> getKnownPropertyNames() { return _delegatee.getKnownPropertyNames(); }

    @Override
    public ObjectIdReader getObjectIdReader() { return _delegatee.getObjectIdReader(); }
}
