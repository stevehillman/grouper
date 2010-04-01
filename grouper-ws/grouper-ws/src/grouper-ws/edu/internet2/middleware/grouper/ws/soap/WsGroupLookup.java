/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.soap.WsGetMembersResult.WsGetMembersResultCode;
import edu.internet2.middleware.grouper.ws.soap.WsGroupDeleteResult.WsGroupDeleteResultCode;

/**
 * <pre>
 * Class to lookup a group via web service
 * 
 * developers make sure each setter calls this.clearGroup();
 * </pre>
 * @author mchyzer
 */
public class WsGroupLookup {

  /**
   * convert group lookups to group ids
   * @param grouperSession
   * @param wsGroupLookups
   * @param errorMessage
   * @param lookupCount is an array of size one int where 1 will be added if there are records, and no change if not
   * @return the group ids
   */
  public static Set<String> convertToGroupIds(GrouperSession grouperSession, WsGroupLookup[] wsGroupLookups, StringBuilder errorMessage, int[] lookupCount) {
    //get all the groups
    //we could probably batch these to get better performance.
    Set<String> groupIds = null;
    if (GrouperUtil.length(wsGroupLookups) > 0) {
      
      groupIds = new LinkedHashSet<String>();
      int i=0;
      
      boolean foundRecords = false;
      
      for (WsGroupLookup wsGroupLookup : wsGroupLookups) {
        
        if (wsGroupLookup == null || !wsGroupLookup.hasData()) {
          continue;
        }
        
        if (!foundRecords) {
          lookupCount[0]++;
        }
        
        wsGroupLookup.retrieveGroupIfNeeded(grouperSession);
        Group group = wsGroupLookup.retrieveGroup();
        if (group != null) {
          groupIds.add(group.getUuid());
        } else {
          
          if (errorMessage.length() > 0) {
            errorMessage.append(", ");
          }
          
          errorMessage.append("Error on group index: " + i + ", " + wsGroupLookup.retrieveGroupFindResult() + ", " + wsGroupLookup.toStringCompact());
        }
        
        i++;
      }
      
    }
    return groupIds;
  }
  
  /**
   * 
   * @param wsGroup
   */
  public WsGroupLookup(WsGroup wsGroup) {
    this.groupName = wsGroup.getName();
    this.uuid = wsGroup.getUuid();
  }
  
  /**
   * see if blank
   * @return true if blank
   */
  public boolean blank() {
    return StringUtils.isBlank(this.groupName) && StringUtils.isBlank(this.uuid)
      && this.group == null && this.groupFindResult == null;
  }

  
  /**
   * see if this group lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.groupName) || !StringUtils.isBlank(this.uuid);
  }
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(WsGroupLookup.class);

  /** find the group */
  @XStreamOmitField
  private Group group = null;

  /** result of group find */
  public static enum GroupFindResult {

    /** found the group */
    SUCCESS {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsGroupDeleteResultCode convertToGroupDeleteResultCode() {
        return WsGroupDeleteResultCode.SUCCESS;
      }

      /**
       * convert this code to a members code
       * @return the code
       */
      @Override
      public WsGetMembersResultCode convertToGetMembersResultCode() {
        return WsGetMembersResultCode.SUCCESS;
      }
    },

    /** uuid doesnt match name */
    GROUP_UUID_DOESNT_MATCH_NAME {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsGroupDeleteResultCode convertToGroupDeleteResultCode() {
        return WsGroupDeleteResultCode.GROUP_UUID_DOESNT_MATCH_NAME;
      }

      /**
       * convert this code to a members code
       * @return the code
       */
      @Override
      public WsGetMembersResultCode convertToGetMembersResultCode() {
        return WsGetMembersResultCode.GROUP_UUID_DOESNT_MATCH_NAME;
      }
    },

    /** cant find the group */
    GROUP_NOT_FOUND {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsGroupDeleteResultCode convertToGroupDeleteResultCode() {
        return WsGroupDeleteResultCode.SUCCESS_GROUP_NOT_FOUND;
      }

      /**
       * convert this code to a members code
       * @return the code
       */
      @Override
      public WsGetMembersResultCode convertToGetMembersResultCode() {
        return WsGetMembersResultCode.GROUP_NOT_FOUND;
      }
    },

    /** incvalid query (e.g. if everything blank) */
    INVALID_QUERY {

      /**
       * convert this code to a delete code
       * @return the code
       */
      @Override
      public WsGroupDeleteResultCode convertToGroupDeleteResultCode() {
        return WsGroupDeleteResultCode.INVALID_QUERY;
      }

      /**
       * convert this code to a members code
       * @return the code
       */
      @Override
      public WsGetMembersResultCode convertToGetMembersResultCode() {
        return WsGetMembersResultCode.INVALID_QUERY;
      }

    };

    /**
     * if this is a successful result
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }

    /**
     * convert this code to a delete code
     * @return the code
     */
    public abstract WsGroupDeleteResultCode convertToGroupDeleteResultCode();

    /**
     * convert this code to a get members code
     * @return the code
     */
    public abstract WsGetMembersResultCode convertToGetMembersResultCode();

    /**
     * null safe equivalent to convertToDeleteCode
     * @param groupFindResult to convert
     * @return the code
     */
    public static WsGroupDeleteResultCode convertToGroupDeleteCodeStatic(
        GroupFindResult groupFindResult) {
      return groupFindResult == null ? WsGroupDeleteResultCode.EXCEPTION
          : groupFindResult.convertToGroupDeleteResultCode();
    }
    
    /**
     * null safe equivalent to convertToGetMembersResultCode
     * @param groupFindResult to convert
     * @return the code
     */
    public static WsGetMembersResultCode convertToGetMembersCodeStatic(
        GroupFindResult groupFindResult) {
      return groupFindResult == null ? WsGetMembersResultCode.EXCEPTION
          : groupFindResult.convertToGetMembersResultCode();
    }
  }

  /**
   * uuid of the group to find
   */
  private String uuid;

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the group
   */
  public Group retrieveGroup() {
    return this.group;
  }

  /**
   * <pre>
   * 
   * Note: this is not a javabean property because we dont want it in the web service
   * </pre>
   * @return the groupFindResult, this is never null
   */
  public GroupFindResult retrieveGroupFindResult() {
    return this.groupFindResult;
  }

  /**
   * make sure this is an explicit toString
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * make sure this is an explicit toString
   * @return return a compact to string
   */
  public String toStringCompact() {
    if (!StringUtils.isBlank(this.groupName)) {
      return "name: " + this.groupName;
    }
    if (!StringUtils.isBlank(this.uuid)) {
      return "id: " + this.uuid;
    }
    return "blank";
  }

  /**
   * retrieve the group for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   */
  public void retrieveGroupIfNeeded(GrouperSession grouperSession) {
    this.retrieveGroupIfNeeded(grouperSession, null);
  }

  /**
   * retrieve the group for this lookup if not looked up yet.  pass in a grouper session
   * @param grouperSession 
   * @param invalidQueryReason is the text to go in the WsInvalidQueryException
   * @return the group
   * @throws WsInvalidQueryException if there is a problem, and if the invalidQueryReason is set
   */
  public Group retrieveGroupIfNeeded(GrouperSession grouperSession,
      String invalidQueryReason) throws WsInvalidQueryException {

    //see if we already retrieved
    if (this.groupFindResult != null) {
      return this.group;
    }

    //assume success (set otherwise if there is a problem)
    this.groupFindResult = GroupFindResult.SUCCESS;
    
    try {
      boolean hasUuid = !StringUtils.isBlank(this.uuid);

      boolean hasName = !StringUtils.isBlank(this.groupName);

      //must have a name or uuid
      if (!hasUuid && !hasName) {
        this.groupFindResult = GroupFindResult.INVALID_QUERY;
        if (!StringUtils.isEmpty(invalidQueryReason)) {
          throw new WsInvalidQueryException("Invalid group query for '"
              + invalidQueryReason + "', " + this);
        }
        String logMessage = "Invalid query: " + this;
        LOG.warn(logMessage);
      }

      if (hasName) {
        
        //TODO make this more efficient
        Group theGroup = GroupFinder.findByName(grouperSession, this.groupName, 
            true, new QueryOptions().secondLevelCache(false));

        //make sure uuid matches 
        if (hasUuid && !StringUtils.equals(this.uuid, theGroup.getUuid())) {
          this.groupFindResult = GroupFindResult.GROUP_UUID_DOESNT_MATCH_NAME;
          String error = "Group name '" + this.groupName + "' and uuid '" + this.uuid
              + "' do not match";
          if (!StringUtils.isEmpty(invalidQueryReason)) {
            throw new WsInvalidQueryException(error + " for '" + invalidQueryReason
                + "', " + this);
          }
          String logMessage = "Invalid query: " + this;
          LOG.warn(logMessage);
        }

        //success
        this.group = theGroup;

      } else if (hasUuid) {
        this.group = GroupFinder.findByUuid(grouperSession, this.uuid, true, new QueryOptions().secondLevelCache(false));
      }

    } catch (GroupNotFoundException gnf) {
      this.groupFindResult = GroupFindResult.GROUP_NOT_FOUND;
      if (!StringUtils.isBlank(invalidQueryReason)) {
        throw new WsInvalidQueryException("Invalid group for '" + invalidQueryReason
            + "', " + this, gnf);
      }
    }
    return this.group;
  }

  /**
   * clear the group if a setter is called
   */
  private void clearGroup() {
    this.group = null;
    this.groupFindResult = null;
  }

  /** name of the group to find (includes stems, e.g. stem1:stem2:groupName */
  private String groupName;

  /** result of group find */
  @XStreamOmitField
  private GroupFindResult groupFindResult = null;

  /**
   * uuid of the group to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the group to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
    this.clearGroup();
  }

  /**
   * name of the group to find (includes stems, e.g. stem1:stem2:groupName
   * @return the theName
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * name of the group to find (includes stems, e.g. stem1:stem2:groupName
   * @param theName the theName to set
   */
  public void setGroupName(String theName) {
    this.groupName = theName;
    this.clearGroup();
  }

  /**
   * 
   */
  public WsGroupLookup() {
    //blank
  }

  /**
   * @param groupName1 
   * @param uuid1
   */
  public WsGroupLookup(String groupName1, String uuid1) {
    this.uuid = uuid1;
    this.setGroupName(groupName1);
  }

}
