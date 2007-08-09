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
import  edu.internet2.middleware.grouper.GrouperRuntimeException;
import  edu.internet2.middleware.subject.provider.SourceManager;


/** 
 * Factory for returning a <code>SubjectResolver</code>.
 * @author  blair christensen.
 * @version $Id: SubjectResolverFactory.java,v 1.1 2007-08-09 18:55:21 blair Exp $
 * @since   @HEAD@
 */
public class SubjectResolverFactory {


  private static SubjectResolver resolver;


  /**
   * @return  <code>SubjectResolver</code> instance.
   * @throws  GrouperRuntimeException if unable to get <code>SourceManager</code>.
   * @since   @HEAD@
   */
  public static SubjectResolver getInstance() 
    throws  GrouperRuntimeException
  {
    try {
      return new ValidatingResolver(
        new CachingResolver( 
          new SourcesXmlResolver( SourceManager.getInstance() ) 
        )
      );
    }
    catch (Exception e) {
      throw new GrouperRuntimeException( "unable to get SourceManager: " + e.getMessage(), e );
    }
  }

  /**
   * @return  Singleton <code>SubjectResolver</code>.
   * @since   @HEAD@
   */
  public static SubjectResolver getResolver() {
    if (resolver == null) {
      resolver = getInstance();
    }
    return resolver;
  }

}

