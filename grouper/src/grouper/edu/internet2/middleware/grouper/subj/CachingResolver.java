/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.subj;
import  edu.internet2.middleware.grouper.cache.CacheStats;
import  edu.internet2.middleware.grouper.cache.EhcacheStats;
import  edu.internet2.middleware.subject.Source;
import  edu.internet2.middleware.subject.SourceUnavailableException;
import  edu.internet2.middleware.subject.Subject;
import  edu.internet2.middleware.subject.SubjectNotFoundException;
import  edu.internet2.middleware.subject.SubjectNotUniqueException;
import  edu.internet2.middleware.subject.provider.SourceManager;
import  edu.internet2.middleware.subject.provider.SubjectTypeEnum;
import  java.util.ArrayList;
import  java.util.LinkedHashSet;
import  java.util.List;
import  java.util.Set;
import  net.sf.ehcache.Cache;
import  net.sf.ehcache.CacheManager;
import  net.sf.ehcache.Element;
import  net.sf.ehcache.Statistics;
import  org.apache.commons.collections.keyvalue.MultiKey;


/**
 * Decorator that provides caching for {@link SubjectResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: CachingResolver.java,v 1.1 2007-08-09 18:55:21 blair Exp $
 * @since   @HEAD@
 */
public class CachingResolver extends SubjectResolverDecorator {


  public static final String        CACHE_FIND              = CachingResolver.class.getName() + ".Find";
  public static final String        CACHE_FINDALL           = CachingResolver.class.getName() + ".FindAll";
  public static final String        CACHE_FINDBYIDENTIFIER  = CachingResolver.class.getName() + ".FindByIdentifier";
  private             CacheManager  mgr;


  /**
   * @see     SubjectResolverDecorator(SubjectResolver)
   * @since   @HEAD@
   */
  public CachingResolver(SubjectResolver resolver) {
    super(resolver);
    this.initializeCaching();
  }


  /**
   * @see     SubjectResolver#find(String)
   * @since   @HEAD
   */
  public Subject find(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindCache(id, null, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().find(id);
    }
    this.putInFindCache(subj);
    return subj;
  }

  /**
   * @see     SubjectResolver#find(String, String)
   * @since   @HEAD
   */
  public Subject find(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindCache(id, type, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().find(id, type);
    }
    this.putInFindCache(subj);
    return subj;
  }

  /**
   * @see     SubjectResolver#find(String, String, String)
   * @since   @HEAD
   */
  public Subject find(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindCache(id, type, source);
    if (subj == null) {
      subj = super.getDecoratedResolver().find(id, type, source);
    }
    this.putInFindCache(subj);
    return subj;
  }

  /**
   * @see     SubjectResolver#findAll(String)
   * @since   @HEAD
   */
  public Set<Subject> findAll(String query)
    throws  IllegalArgumentException
  {
    Set<Subject> subjects = this.getFromFindAllCache(query, null);
    if (subjects == null) {
      subjects = super.getDecoratedResolver().findAll(query);
    }
    this.putInFindAllCache(query, null, subjects);
    return subjects;
  }

  /**
   * @see     SubjectResolver#findAll(String, String)
   * @since   @HEAD
   */
  public Set<Subject> findAll(String query, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    Set<Subject> subjects = this.getFromFindAllCache(query, source);
    if (subjects == null) {
      subjects = super.getDecoratedResolver().findAll(query, source);
    }
    this.putInFindAllCache(query, source, subjects);
    return subjects;
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String)
   * @since   @HEAD
   */
  public Subject findByIdentifier(String id)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindByIdentifierCache(id, null, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdentifier(id);
    }
    this.putInFindByIdentifierCache(id, subj);
    return subj;
  }            

  /**
   * @see     SubjectResolver#findByIdentifier(String, String)
   * @since   @HEAD
   */
  public Subject findByIdentifier(String id, String type)
    throws  IllegalArgumentException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindByIdentifierCache(id, type, null);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdentifier(id, type);
    }
    this.putInFindByIdentifierCache(id, subj);
    return subj;
  }

  /**
   * @see     SubjectResolver#findByIdentifier(String, String, String)
   * @since   @HEAD
   */
  public Subject findByIdentifier(String id, String type, String source)
    throws  IllegalArgumentException,
            SourceUnavailableException,
            SubjectNotFoundException,
            SubjectNotUniqueException
  {
    Subject subj = this.getFromFindByIdentifierCache(id, type, source);
    if (subj == null) {
      subj = super.getDecoratedResolver().findByIdentifier(id, type, source);
    }
    this.putInFindByIdentifierCache(id, subj);
    return subj;
  }

  /**
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   * @since   @HEAD@
   */
  private Cache getCache(String name) 
    throws  IllegalStateException
  {
    if ( this.mgr.cacheExists(name) ) {
      return this.mgr.getCache(name);
    }
    throw new IllegalStateException( "cache not found: " + name );
  }

  /**
   * Retrieve set of subjects from cache for <code>findAll(...)</code>.
   * @return  Cached set of subjects or null.
   * @since   @HEAD@
   */
  private Set<Subject> getFromFindAllCache(String query, String source) {
    Element el = this.getCache(CACHE_FINDALL).get( new MultiKey(query, source) );
    if (el != null) {
      return (Set<Subject>) el.getObjectValue();
    }
    return null;
  }

  /**
   * Retrieve subject from cache for <code>findByIdentifier(...)</code>.
   * @return  Cached subject or null.
   * @since   @HEAD@
   */
  private Subject getFromFindByIdentifierCache(String id, String type, String source) {
    // TODO 20070807 DRY w/ getFromFindCache(String, String, String)
    Element el = this.getCache(CACHE_FINDBYIDENTIFIER).get( new MultiKey(id, type, source) );
    if (el != null) {
      return (Subject) el.getObjectValue();    
    }
    return null;
  }

  /**
   * Retrieve subject from cache for <code>find(...)</code>.
   * @return  Cached subject or null.
   * @since   @HEAD@
   */
  private Subject getFromFindCache(String id, String type, String source) {
    // TODO 20070807 DRY w/ getFromFindByIdentifierCache(String, String, String)
    Element el = this.getCache(CACHE_FIND).get( new MultiKey(id, type, source) );
    if (el != null) {
      return (Subject) el.getObjectValue();    
    }
    return null;
  }

  /**
   * @see     SubjectResolver#getSource(String)
   * @since   @HEAD@
   */
  public Source getSource(String id) 
    throws  IllegalArgumentException,
            SourceUnavailableException
  {
    return super.getDecoratedResolver().getSource(id);
  }
 
  /**
   * @see     SubjectResolver#getSources()
   * @since   @HEAD@
   */
  public Set<Source> getSources() {
    return super.getDecoratedResolver().getSources();
  }

  /**
   * @see     SubjectResolver#getSources(String)
   * @since   @HEAD@
   */
  public Set<Source> getSources(String subjectType) 
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getSources(subjectType);
  }

  /**
   * @return  ehcache statistics for <i>cache</i>.
   * @since   @HEAD@
   */
  public CacheStats getStats(String cache) {
    Cache c = this.getCache(cache);
    c.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_GUARANTEED);
    return new EhcacheStats( c.getStatistics() );
  }

  /** 
   * Initialize Subject cache.
   * @since   @HEAD@
   */
  private void initializeCaching() {
    this.mgr = new CacheManager( this.getClass().getResource("/grouper.ehcache.xml") );
  }
  
  /**
   * Put set of subjects into cache for <code>findAll(...)</code>.
   * @since   @HEAD@
   */
  private void putInFindAllCache(String query, String source, Set<Subject> subjects) {
    this.getCache(CACHE_FINDALL).put( new Element( new MultiKey(query, source), subjects ) );
  }

  /**
   * Put subject into cache for <code>findByIdentifier(...)</code>.
   * @since   @HEAD@
   */
  private void putInFindByIdentifierCache(String idfr, Subject subj) {
    this.getCache(CACHE_FINDBYIDENTIFIER).put( 
      new Element( 
        new MultiKey(idfr, null, null), subj  
      )
    );
    this.getCache(CACHE_FINDBYIDENTIFIER).put( 
      new Element( 
        new MultiKey( idfr, subj.getType().getName(), null ), subj
      )
    );
    this.getCache(CACHE_FINDBYIDENTIFIER).put( 
      new Element(
        new MultiKey( idfr, subj.getType().getName(), subj.getSource().getId() ), subj
      )
    );
    this.putInFindCache(subj);
  }

  /**
   * Put subject into cache for <code>find(...)</code>.
   * @since   @HEAD@
   */
  private void putInFindCache(Subject subj) {
    this.getCache(CACHE_FIND).put( 
      new Element( 
        new MultiKey( subj.getId(), null, null ), subj  
      )
    );
    this.getCache(CACHE_FIND).put( 
      new Element( 
        new MultiKey( subj.getId(), subj.getType().getName(), null ), subj
      )
    );
    this.getCache(CACHE_FIND).put( 
      new Element(
        new MultiKey( subj.getId(), subj.getType().getName(), subj.getSource().getId() ), subj
      )
    );
    // TODO 20070807 also put in "findByIdentifier(...)" cache?
  }

}

