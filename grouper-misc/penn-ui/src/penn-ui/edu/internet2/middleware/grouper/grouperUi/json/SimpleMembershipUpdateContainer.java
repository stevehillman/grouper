/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.4 2009-08-12 05:20:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;
import edu.internet2.middleware.subject.Source;



/**
 * bean for simple membership update
 */
public class SimpleMembershipUpdateContainer implements Serializable {

  /**
   * available sourceIds for the upload form
   * @return the source ids
   */
  public String getSourceIds() {
    Set<Source> sources = SubjectFinder.getSources();
    
    StringBuilder result = new StringBuilder();
    
    List<String> sourceIds = new ArrayList<String>();
    
    for (Source source : sources) {
      sourceIds.add(source.getId());
    }
    
    Collections.sort(sourceIds);
    
    int i=0;
    for (String sourceId: sourceIds) {
      result.append(sourceId);
      if (i != sourceIds.size()-1) {
        result.append(", ");
      }
      i++;
    }
    
    return result.toString();
  }
  
  /**
   * store to session scope
   */
  public void storeToSession() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    httpServletRequest.getSession().setAttribute("simpleMembershipUpdateContainer", this);
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static SimpleMembershipUpdateContainer retrieveFromSession() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    HttpSession httpSession = httpServletRequest.getSession();
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = (SimpleMembershipUpdateContainer)httpSession
      .getAttribute("simpleMembershipUpdateContainer");
    if (simpleMembershipUpdateContainer == null) {
      throw new RuntimeException("simpleMembershipUpdateContainer is null, start the application over and try again.  Contact the help desk if you have repeated problems.");
    }
    return simpleMembershipUpdateContainer;
  }

  /** if can read group */
  private boolean canReadGroup;
  
  /** if can update group */
  private boolean canUpdateGroup;

  /**
   * group object
   */
  private GuiGroup guiGroup;

  /**
   * members in result
   */
  private GuiMember[] guiMembers;

  /**
   * 
   * @return the group
   */
  public GuiGroup getGuiGroup() {
    return this.guiGroup;
  }

  /**
   * group object
   * @param group1
   */
  public void setGuiGroup(GuiGroup group1) {
    this.guiGroup = group1;
  }

  /**
   * 
   * @return if can read group
   */
  public boolean isCanReadGroup() {
    return this.canReadGroup;
  }

  /**
   * if can read group
   * @param canReadGroup1
   */
  public void setCanReadGroup(boolean canReadGroup1) {
    this.canReadGroup = canReadGroup1;
  }

  /**
   * if can update group
   * @return if can update group
   */
  public boolean isCanUpdateGroup() {
    return this.canUpdateGroup;
  }

  /**
   * if can update group
   * @param canUpdateGroup1
   */
  public void setCanUpdateGroup(boolean canUpdateGroup1) {
    this.canUpdateGroup = canUpdateGroup1;
  }

  /**
   * members in result
   * @return members
   */
  public GuiMember[] getGuiMembers() {
    return this.guiMembers;
  }

  /**
   * members in result
   * @param members1
   */
  public void setGuiMembers(GuiMember[] members1) {
    this.guiMembers = members1;
  }
  
}
