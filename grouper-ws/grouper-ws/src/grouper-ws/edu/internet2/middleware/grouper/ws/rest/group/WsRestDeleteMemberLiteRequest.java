/*
 * @author mchyzer
 * $Id: WsRestDeleteMemberLiteRequest.java,v 1.1 2008-03-26 07:39:10 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.group;

import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;



/**
 * lite bean that will be the data from rest request
 * @see GrouperServiceLogic#addMemberLite(edu.internet2.middleware.grouper.ws.GrouperWsVersion, String, String, String, String, String, String, String, String, edu.internet2.middleware.grouper.Field, boolean, boolean, String, String, String, String, String)
 * for lite method
 */
public class WsRestDeleteMemberLiteRequest implements WsRequestBean {
  /** 
   * field
   */
  private String clientVersion;
  
  /** field */
  private String groupName;
  /** field */
  private String groupUuid;
  /** field */
  private String subjectId;
  /** field */
  private String subjectSource;
  
  /** field */
  private String subjectIdentifier;
  /** field */
  private String actAsSubjectId;
  /** field */
  private String actAsSubjectSource;
  
  /** field */
  private String actAsSubjectIdentifier;
  /** field */
  private String fieldName;
  /** field */
  private String includeGroupDetail;
  /** field */
  private String includeSubjectDetail;
  /** field */
  private String subjectAttributeNames;
  /** field */
  private String paramName0;
  
  /** field */
  private String paramValue0;
  /** field */
  private String paramName1;
  /** field */
  private String paramValue1;
  
  /**
   * 
   * @return field
   */
  public String getClientVersion() {
    return this.clientVersion;
  }
  /**
   * 
   * @param clientVersion1
   */
  public void setClientVersion(String clientVersion1) {
    this.clientVersion = clientVersion1;
  }
  /**
   * 
   * @return field
   */
  public String getGroupName() {
    return this.groupName;
  }
  
  /**
   * 
   * @param groupName1
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }
  
  /**
   * 
   * @return field
   */
  public String getGroupUuid() {
    return this.groupUuid;
  }
  
  /**
   * 
   * @param groupUuid1
   */
  public void setGroupUuid(String groupUuid1) {
    this.groupUuid = groupUuid1;
  }
  
  /**
   * 
   * @return field
   */
  public String getSubjectId() {
    return this.subjectId;
  }
  
  /**
   * 
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }
  
  /**
   * 
   * @return field
   */
  public String getSubjectSource() {
    return this.subjectSource;
  }
  
  /**
   * 
   * @param subjectSource1
   */
  public void setSubjectSource(String subjectSource1) {
    this.subjectSource = subjectSource1;
  }
  
  /**
   * 
   * @return field
   */
  public String getSubjectIdentifier() {
    return this.subjectIdentifier;
  }
  
  /**
   * 
   * @param subjectIdentifier1
   */
  public void setSubjectIdentifier(String subjectIdentifier1) {
    this.subjectIdentifier = subjectIdentifier1;
  }
  
  /**
   * 
   * @return field
   */
  public String getActAsSubjectId() {
    return this.actAsSubjectId;
  }
  
  /**
   * 
   * @param actAsSubjectId1
   */
  public void setActAsSubjectId(String actAsSubjectId1) {
    this.actAsSubjectId = actAsSubjectId1;
  }
  
  /**
   * 
   * @return field
   */
  public String getActAsSubjectSource() {
    return this.actAsSubjectSource;
  }
  
  /**
   * 
   * @param actAsSubjectSource1
   */
  public void setActAsSubjectSource(String actAsSubjectSource1) {
    this.actAsSubjectSource = actAsSubjectSource1;
  }
  
  /**
   * 
   * @return field
   */
  public String getActAsSubjectIdentifier() {
    return this.actAsSubjectIdentifier;
  }
  
  /**
   * 
   * @param actAsSubjectIdentifier1
   */
  public void setActAsSubjectIdentifier(String actAsSubjectIdentifier1) {
    this.actAsSubjectIdentifier = actAsSubjectIdentifier1;
  }
  
  /**
   * 
   * @return field
   */
  public String getFieldName() {
    return this.fieldName;
  }
  /**
   * 
   * @param fieldName1
   */
  public void setFieldName(String fieldName1) {
    this.fieldName = fieldName1;
  }
  
  /**
   * 
   * @return field
   */
  public String getIncludeGroupDetail() {
    return this.includeGroupDetail;
  }
  
  /**
   * 
   * @param includeGroupDetail1
   */
  public void setIncludeGroupDetail(String includeGroupDetail1) {
    this.includeGroupDetail = includeGroupDetail1;
  }
  
  /**
   * 
   * @return field
   */
  public String getIncludeSubjectDetail() {
    return this.includeSubjectDetail;
  }
  
  /**
   * 
   * @param includeSubjectDetail1
   */
  public void setIncludeSubjectDetail(String includeSubjectDetail1) {
    this.includeSubjectDetail = includeSubjectDetail1;
  }
  /**
   * 
   * @return field
   */
  public String getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }
  /**
   * 
   * @param subjectAttributeNames1
   */
  public void setSubjectAttributeNames(String subjectAttributeNames1) {
    this.subjectAttributeNames = subjectAttributeNames1;
  }
  /**
   * 
   * @return field
   */
  public String getParamName0() {
    return this.paramName0;
  }
  /**
   * 
   * @param _paramName0
   */
  public void setParamName0(String _paramName0) {
    this.paramName0 = _paramName0;
  }
  /**
   * 
   * @return field
   */
  public String getParamValue0() {
    return this.paramValue0;
  }
  /**
   * 
   * @param _paramValue0
   */
  public void setParamValue0(String _paramValue0) {
    this.paramValue0 = _paramValue0;
  }
  /**
   * 
   * @return field
   */
  public String getParamName1() {
    return this.paramName1;
  }
  /**
   * 
   * @param _paramName1
   */
  public void setParamName1(String _paramName1) {
    this.paramName1 = _paramName1;
  }
  
  /**
   * 
   * @return field
   */
  public String getParamValue1() {
    return this.paramValue1;
  }
  
  /**
   * 
   * @param _paramValue1
   */
  public void setParamValue1(String _paramValue1) {
    this.paramValue1 = _paramValue1;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.ws.rest.WsRequestBean#retrieveRestHttpMethod()
   */
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.DELETE;
  }

}
