/*
 * @author mchyzer
 * $Id: GuiSettings.java,v 1.4 2009-08-12 05:20:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;


/**
 *
 */
public class GuiSettings implements Serializable {

  /**
   * retrieveFromRequest, cant be null
   * @return the app state in request scope
   */
  public static GuiSettings retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    GuiSettings guiSettings = (GuiSettings)httpServletRequest
      .getAttribute("guiSettings");
    if (guiSettings == null) {
      throw new RuntimeException("GuiSettings is null");
    }
    return guiSettings;
  }

  /**
   * store to request scope
   */
  public void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("guiSettings", this);
  }

  /** need to send this key back with each request in authnKey param */
  private String authnKey = null;
  
  /** logged in subject */
  private GuiSubject loggedInSubject = null;
  
  /**
   * logged in subject
   * @return logged in subject
   */
  public GuiSubject getLoggedInSubject() {
    return this.loggedInSubject;
  }

  /**
   * logged in subject
   * @param loggedInSubject1
   */
  public void setLoggedInSubject(GuiSubject loggedInSubject1) {
    this.loggedInSubject = loggedInSubject1;
  }

  /**
   * need to send this key back with each request in authnKey param
   * @return the authn key
   */
  public String getAuthnKey() {
    return this.authnKey;
  }

  /**
   * need to send this key back with each request in authnKey param
   * @param authnKey1
   */
  public void setAuthnKey(String authnKey1) {
    this.authnKey = authnKey1;
  }

}
