/*
  Copyright (C) 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2006-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.bench;
import  edu.internet2.middleware.grouper.*; 
import  edu.internet2.middleware.subject.*;      

/**
 * Benchmark adding an effective {@link Membership}.
 * @author  blair christensen.
 * @version $Id: AddEffMember.java,v 1.6 2007-03-06 17:02:43 blair Exp $
 * @since   1.1.0
 */
public class AddEffMember extends BaseGrouperBenchmark {

  // PRIVATE INSTANCE VARIABLES
  Group   g0, g1;
  Subject subj0, subj1;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new AddEffMember();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected AddEffMember() {
    super();
  } // protected AddEffMember()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperRuntimeException 
  {
    try {
      GrouperSession  s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Stem            root  = StemFinder.findRootStem(s);
      Stem            ns    = root.addChildStem("example", "example");
      this.g0               = ns.addChildGroup("group 0", "group 0");
      this.g1               = ns.addChildGroup("group 1", "group 1");
      RegistrySubject.add(s, "subj0", "person", "subject 0");
      this.subj0            = SubjectFinder.findById("subj0");
      this.subj1            = this.g0.toSubject();
      this.g0.addMember(this.subj0);
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e.getMessage());
    }
  } // public void init()

  /**
   * @since 1.1.0
   */
  public void run() 
    throws GrouperRuntimeException 
  {
    try {
      this.g1.addMember(this.subj1);
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class AddEffMember

