package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.LimitInvalidException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMember;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPermissionEntryActionsContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboLogic;
import edu.internet2.middleware.grouper.grouperUi.beans.dojo.DojoComboQueryLogicBase;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.permissionUpdate.PermissionUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.PermissionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.permissions.PermissionRoleDelegate;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBean;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitDocumentation;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author vsachdeva
 */
public class UiV2Permission {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2Permission.class);
  
  /**
   * @param request
   * @param response
   */
  public void groupPermission(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    Group group = null;
  
    try {
  
      grouperSession = GrouperSession.start(loggedInSubject);
  
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      final Group GROUP = group;
      final boolean[] canGroupAttrUpdate = new boolean[1];
      
      //these need to be looked up as root
      GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
        /**
         * @see edu.internet2.middleware.grouper.misc.GrouperSessionHandler#callback(edu.internet2.middleware.grouper.GrouperSession)
         */
        public Object callback(GrouperSession rootSession) throws GrouperSessionException {
          canGroupAttrUpdate[0] = PrivilegeHelper.canGroupAttrUpdate(rootSession, GROUP, loggedInSubject);
          return null;
        }
      });
      
      final GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      PermissionContainer permissionContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getPermissionContainer();
      permissionContainer.setCanAssignPermission(canGroupAttrUpdate[0]);
      
      permissionContainer.setGuiGroup(new GuiGroup(group));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/permission/groupPermission.jsp"));
      
      if (group.getTypeOfGroup() == TypeOfGroup.role) {
        groupViewPermissionsHelper(request, response, group);
      }
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * show form to assign permission to a role
   * @param request
   * @param response
   */
  public void groupAssignPermission(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (group.getTypeOfGroup() != TypeOfGroup.role) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("groupPermissionErrorNotRole")));
        return;
      }
      
      PermissionContainer permissionContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getPermissionContainer();
      
      permissionContainer.setGuiGroup(new GuiGroup(group));
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPermission", 
          "/WEB-INF/grouperUi2/permission/groupAssignPermission.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * save permission assignment in the database.
   * @param request
   * @param response
   */
  public void assignGroupPermissionSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (group.getTypeOfGroup() != TypeOfGroup.role) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("groupPermissionErrorNotRole")));
        return;
      }
      
      String permissionDefId = request.getParameter("permissionDefComboName");
      String permissionDefNameId = request.getParameter("permissionResourceNameComboName");
      String actionId = request.getParameter("permissionActionComboName");
      String permissionAllowed = request.getParameter("permissionAddAllowed[]");
      
      AttributeDef attributeDef = null;
      if (!StringUtils.isBlank(permissionDefId)) {
        attributeDef = AttributeDefFinder.findById(permissionDefId, false);
      }
      
      if (attributeDef == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#permissionDefComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupAssignPermissionInvalidPermissionDefError")));
        return;
      }
      
      AttributeDefName attributeDefName = null;
      if (!StringUtils.isBlank(permissionDefNameId)) {
        attributeDefName = AttributeDefNameFinder.findById(permissionDefNameId, false);
      }
  
      if (attributeDefName == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#permissionResourceNameComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupAssignPermissionInvalidPermissionResourceNameError")));
        return;
      }
      
      if (StringUtils.isBlank(actionId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#permissionActionComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupAssignPermissionBlankActionError")));
        return;
      }
      
      PermissionAllowed permAllowed = null;
      try {
        permAllowed = PermissionAllowed.valueOf(permissionAllowed);
      } catch(Exception e) {
        throw new RuntimeException("Permission allowed value is not valid.");
      }
      
      Set<AttributeAssignAction> allowedActions = attributeDefName.getAttributeDef().getAttributeDefActionDelegate().allowedActions();
      
      AttributeAssignAction attributeAssignAction = null;
      
      for (AttributeAssignAction assignAction: allowedActions) {
        if (assignAction.getId().equals(actionId)) {
          attributeAssignAction = assignAction;
          break;
        }
      }
        
      if (attributeAssignAction == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#permissionActionComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("groupAssignPermissionInvalidActionError")));
        return;
      }
      
      PermissionRoleDelegate permissionRoleDelegate = group.getPermissionRoleDelegate();

      permissionRoleDelegate.assignRolePermission(attributeAssignAction.getName(), attributeDefName, permAllowed);
      
      PermissionContainer permissionContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getPermissionContainer();
      
      permissionContainer.setGuiGroup(new GuiGroup(group));
      
      guiResponseJs.addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2Permission.groupPermission&groupId=" + group.getId() + "')"));

      //lets show a success message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("groupAssignPermissionSuccess")));
            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * assign limit
   * @param request
   * @param response
   */
  public void assignLimitSubmit(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String permissionAssignTypeString = request.getParameter("permissionAssignType");
      
      if (StringUtils.isBlank(permissionAssignTypeString)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.requiredOwnerType")));
        return;
      }
      
      PermissionType permissionAssignType = PermissionType.valueOfIgnoreCase(permissionAssignTypeString, false);
      
      if (permissionAssignType ==  null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.requiredOwnerType")));
        return;
      }
      
      PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      permissionUpdateRequestContainer.setPermissionType(permissionAssignType);
      
      String permissionAssignmentId = request.getParameter("permissionAssignmentId");
  
      AttributeAssign permissionAssign = AttributeAssignFinder.findById(permissionAssignmentId, true);
      
      //get current state
      Role role = null;
      {
        String roleId = permissionAssign.getOwnerGroupId();
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          return;
        }
      }
      
      AttributeDef attributeDef = null;
      AttributeDefName attributeDefName = null;
      {
        String attributeDefNameId = permissionAssign.getAttributeDefNameId();
        attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
        attributeDef = attributeDefName.getAttributeDef();
        if (!PrivilegeHelper.canAttrUpdate(GrouperSession.staticGrouperSession(), attributeDef, loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditAttributeDef")));
          return;
        }
        
      }

      String limitId = request.getParameter("limitResourceNameComboName");
      
      if (StringUtils.isBlank(limitId)) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#limitResourceNameComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorLimitNameIsRequired")));
        return;
      }
      
      AttributeDefName limitName = AttributeDefNameFinder.findById(limitId, false);

      if (limitName == null) {
        guiResponseJs.addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#limitResourceNameComboErrorId",
            TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorLimitNameIsRequired")));
        return;
      }

      AttributeDef limitDef = limitName.getAttributeDef();
      if (!PrivilegeHelper.canAttrUpdate(GrouperSession.staticGrouperSession(), limitDef, loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditLimit")));
        return;
      }

      String screenMessage = TagUtils.navResourceString("simplePermissionUpdate.addLimitSuccess");
      
      AttributeAssignResult attributeAssignResult = null;
      
      if (limitDef.isMultiAssignable()) {
        attributeAssignResult = permissionAssign.getAttributeDelegate().addAttribute(limitName);
      } else {
        attributeAssignResult = permissionAssign.getAttributeDelegate().assignAttribute(limitName);
        if (!attributeAssignResult.isChanged()) {
          screenMessage = TagUtils.navResourceString("simplePermissionUpdate.addLimitAlreadyAssigned");
        }
      }
      
      String addLimitValue = request.getParameter("addLimitValue");
      if (!StringUtils.isBlank(addLimitValue)) {
        AttributeAssign limitAssign = attributeAssignResult.getAttributeAssign();
        try {
          
          AttributeAssignValue attributeAssignValue = new AttributeAssignValue();
          attributeAssignValue.setAttributeAssignId(limitAssign.getId());
          
          if (!assignLimitValueAndValidate(guiResponseJs, limitDef, null, attributeAssignValue, addLimitValue)) {
            //had trouble with value, dont do the limit
            limitAssign.delete();
            return;
          }
          screenMessage = screenMessage + "<br />" + TagUtils.navResourceString("simplePermissionUpdate.addLimitValueSuccess");

        } catch (Exception e) {
          LOG.info("Error assigning value: " + addLimitValue + ", to assignment: " + limitAssign.getId(), e);

          try {
            //had trouble with value, dont do the limit
            limitAssign.delete();
          } catch (Exception e2) {
            LOG.error("Cant clean up assignment", e2);
          }
          
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.addLimitValueError") + 
              "  " + e.getClass().getSimpleName() + ": " + GrouperUiUtils.escapeHtml(e.getMessage(), true)));
          return;
        }
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/permission/groupPermission.jsp"));
      
      groupViewPermissionsHelper(request, response, role);
      //lets show a message on the new screen
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, screenMessage));
            
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * assign a limit value to the limit value object, and validate that ok
   * @param guiResponseJs
   * @param limitAssign
   * @param limitDef
   * @param limitAssignValueId for logging, could be null on insert
   * @param limitAssignValues
   * @param attributeAssignValue
   * @param valueToEdit
   * @return true if ok, false if not ok
   */
  private boolean assignLimitValueAndValidate(GuiResponseJs guiResponseJs,AttributeDef limitDef, String limitAssignValueId,
      AttributeAssignValue attributeAssignValue, String valueToEdit) {
    try {
      attributeAssignValue.assignValue(valueToEdit);
      attributeAssignValue.saveOrUpdate();
    } catch (LimitInvalidException lie) {
      
      PermissionLimitDocumentation error = lie.getPermissionLimitDocumentation();
      if (error != null) {
        
        String documentation = TagUtils.navResourceString(error.getDocumentationKey());
        
        for (int i=0; i<GrouperUtil.length(error.getArgs()); i++) {
          documentation = StringUtils.replace(documentation, "{" + 0 + "}", error.getArgs().get(i));
        }
        guiResponseJs.addAction(GuiScreenAction.newAlert(documentation));
        return false;
      } else {
        throw lie;
      }

    } catch (RuntimeException re) {
      
      String error = null;
      
      //if type is not a string, it is probably a type problem:
      if (limitDef.getValueType() == AttributeDefValueType.integer) {
        error = TagUtils.navResourceString("simplePermissionUpdate.limitTypeProblemInt") + ", " + re.getClass().getSimpleName() + ": " + re.getMessage();
      } else if (limitDef.getValueType() == AttributeDefValueType.floating) {
        error = TagUtils.navResourceString("simplePermissionUpdate.limitTypeProblemDecimal") + ", " + re.getClass().getSimpleName() + ": " + re.getMessage();
      } else if (limitDef.getValueType() == AttributeDefValueType.timestamp) {
        error = TagUtils.navResourceString("simplePermissionUpdate.limitTypeProblemDate") + ", " + re.getClass().getSimpleName() + ": " + re.getMessage();
      } else {
        error = re.getClass().getSimpleName() + ": " + re.getMessage();
      }          
      guiResponseJs.addAction(GuiScreenAction.newAlert(error));
      return false;
    }

    return true;
  }
  
  /**
   * view all permissions assigned to a group.
   * @param request
   * @param response
   */
  public void groupViewPermissions(final HttpServletRequest request, final HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
    
    Group group = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      if (group.getTypeOfGroup() != TypeOfGroup.role) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("groupPermissionErrorNotRole")));
        return;
      }
      
      groupViewPermissionsHelper(request, response, group);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
  }
  
  /**
   * show permissions for the given role
   * @param request
   * @param response
   * @param group
   */
  private void groupViewPermissionsHelper(HttpServletRequest request, HttpServletResponse response, Role role) {
    
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getPermissionUpdateRequestContainer();
    
    permissionUpdateRequestContainer.setPermissionType(PermissionType.role);
    permissionUpdateRequestContainer.setEnabledDisabled(true);
    permissionUpdateRequestContainer.setSimulateLimits(false);
    
    PermissionFinder permissionFinder = new PermissionFinder();
    permissionFinder.addRole(role);
    permissionFinder.assignPermissionType(PermissionType.role);
    permissionFinder.assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS);
    
    Map<PermissionEntry, Set<PermissionLimitBean>> permissionEntryLimitBeanMap = permissionFinder.findPermissionsAndLimits();
    
    //lets keep track of all limits for documentation
    {
      for (Set<PermissionLimitBean> permissionLimitBeanSet : GrouperUtil.nonNull(permissionEntryLimitBeanMap).values()) {
        
        for (PermissionLimitBean permissionLimitBean : GrouperUtil.nonNull(permissionLimitBeanSet)) {
          
          permissionUpdateRequestContainer.getAllLimitsOnScreen().add(permissionLimitBean.getLimitAssign().getAttributeDefName());
          
        }
        
      }
      
      //lets sort the limits on screen
      List<AttributeDefName> attributeDefNameList = new ArrayList<AttributeDefName>(permissionUpdateRequestContainer.getAllLimitsOnScreen());
      Collections.sort(attributeDefNameList, new Comparator<AttributeDefName>() {

        @Override
        public int compare(AttributeDefName o1, AttributeDefName o2) {
          return StringUtils.defaultString(o1.getDisplayExtension()).compareTo(StringUtils.defaultString(o2.getDisplayExtension()));
        }
      });
      permissionUpdateRequestContainer.getAllLimitsOnScreen().clear();
      
      permissionUpdateRequestContainer.getAllLimitsOnScreen().addAll(attributeDefNameList);
    }
    
   Set<PermissionEntry> permissionEntriesFromDb = permissionEntryLimitBeanMap.keySet();
   
   List<GuiPermissionEntryActionsContainer> guiPermissionEntryActionsContainers = new ArrayList<GuiPermissionEntryActionsContainer>();
   
   //lets link the set of actions (alphabetized), to a GuiPermissionEntryActionsContainer
   Map<MultiKey, GuiPermissionEntryActionsContainer> actionsToPermissionsEntryActionsContainer 
     = new HashMap<MultiKey, GuiPermissionEntryActionsContainer>();
   
   //lets link the attribute def id to a GuiPermissionEntryActionsContainer to save time since the attributeDefId has the same set of actions
   Map<String, GuiPermissionEntryActionsContainer> attributeDefIdToPermissionsEntryActionsContainer 
     = new HashMap<String, GuiPermissionEntryActionsContainer>();

   //we need all actions so the screen knows how many columns etc
   Set<String> allActionsSet = new TreeSet<String>();
   
 //process the permissions to group up the GuiPermissionEntryActionsContainers
   for (PermissionEntry permissionEntry : permissionEntriesFromDb) {
     String attributeDefId = permissionEntry.getAttributeDefId();
     
     //see if we are all set
     GuiPermissionEntryActionsContainer guiPermissionEntryActionsContainer 
       = attributeDefIdToPermissionsEntryActionsContainer.get(attributeDefId);
     
     if (guiPermissionEntryActionsContainer == null) {
       //if we havent done this id yet
       //see if we have the actions taken care of
       AttributeDef currentAttributeDef = permissionEntry.getAttributeDef();
       
       List<String> actions = null;
       
       actions = new ArrayList<String>(currentAttributeDef.getAttributeDefActionDelegate().allowedActionStrings());

         Collections.sort(actions);
         
         allActionsSet.addAll(actions);
       
       Object[] actionsArray = actions.toArray();
       
       MultiKey actionsKey = new MultiKey(actionsArray);
       
       //lets see if these actions are there
       guiPermissionEntryActionsContainer = actionsToPermissionsEntryActionsContainer.get(actionsKey);
       
       if (guiPermissionEntryActionsContainer == null) {
         
         guiPermissionEntryActionsContainer = new GuiPermissionEntryActionsContainer();
         guiPermissionEntryActionsContainer.setPermissionType(PermissionType.role);
         guiPermissionEntryActionsContainer.setRawPermissionEntries(new ArrayList<PermissionEntry>());
         
         guiPermissionEntryActionsContainer.setActions(actions);
         
         guiPermissionEntryActionsContainers.add(guiPermissionEntryActionsContainer);
         
         actionsToPermissionsEntryActionsContainer.put(actionsKey, guiPermissionEntryActionsContainer);
         
       }
       attributeDefIdToPermissionsEntryActionsContainer.put(attributeDefId, guiPermissionEntryActionsContainer);
     }
     guiPermissionEntryActionsContainer.getRawPermissionEntries().add(permissionEntry);
   }
   
   List<String> allActionsList = new ArrayList<String>(allActionsSet);
   permissionUpdateRequestContainer.setAllActions(allActionsList);
   
   for (GuiPermissionEntryActionsContainer guiPermissionEntryActionsContainer : guiPermissionEntryActionsContainers) {
     
     guiPermissionEntryActionsContainer.processRawEntries(permissionEntryLimitBeanMap);
     
   }
   
   permissionUpdateRequestContainer.setGuiPermissionEntryActionsContainers(guiPermissionEntryActionsContainers);
    
//   guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
//       "/WEB-INF/grouperUi2/permission/groupPermission.jsp"));
   
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#viewPermissions",
        "/WEB-INF/grouperUi2/permission/groupViewPermissions.jsp"));
    
  }
  
  /**
   * save updated permission assignment
   * @param request
   * @param response
   */
  public void permissionEditSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String permissionAssignTypeString = request.getParameter("permissionAssignType");
      
      if (StringUtils.isBlank(permissionAssignTypeString)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionAssign.requiredOwnerType")));
        return;
      }
      PermissionType permissionAssignType = PermissionType.valueOfIgnoreCase(permissionAssignTypeString, false);
      
      if (permissionAssignType == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionAssign.requiredOwnerType")));
        return;
      }
      
      permissionUpdateRequestContainer.setPermissionType(permissionAssignType);
      
      String permissionAssignmentId = request.getParameter("permissionAssignmentId");

      AttributeAssign attributeAssign = AttributeAssignFinder.findById(permissionAssignmentId, true);

      //get current state
      Role role = null;
      {
        String roleId = attributeAssign.getOwnerGroupId();
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          return;
        }
      }
      
      AttributeDef attributeDef = null;
      AttributeDefName attributeDefName = null;
      {
        String attributeDefNameId = attributeAssign.getAttributeDefNameId();
        attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
        attributeDef = attributeDefName.getAttributeDef();
        if (!PrivilegeHelper.canAttrUpdate(GrouperSession.staticGrouperSession(), attributeDef, loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditAttributeDef")));
          return;
        }
        
      }
      
      {
        String enabledDate = request.getParameter("enabledDate");
        
        if (StringUtils.isBlank(enabledDate) ) {
          attributeAssign.setEnabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp enabledTimestamp = GrouperUtil.toTimestamp(enabledDate);
          attributeAssign.setEnabledTime(enabledTimestamp);
        }
      }
      
      {
        String disabledDate = request.getParameter("disabledDate");
  
        if (StringUtils.isBlank(disabledDate) ) {
          attributeAssign.setDisabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp disabledTimestamp = GrouperUtil.toTimestamp(disabledDate);
          attributeAssign.setDisabledTime(disabledTimestamp);
        }
      }
      
      attributeAssign.saveOrUpdate();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/permission/groupPermission.jsp"));
      //go to the view permissions screen
      groupViewPermissionsHelper(request, response, role);

      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.assignEditSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }
  
  /**
   * submit the add value limit screen
   * @param request
   * @param response
   */
  public void limitAddValueSubmit(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {
      //PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String limitAssignId = request.getParameter("limitAssignId");
      
      if (StringUtils.isBlank(limitAssignId)) {
        throw new RuntimeException("Why is limitAssignId blank???");
      }

      AttributeAssign limitAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(limitAssignId, true, false);
      
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, limitAssign.getAttributeDef(), loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionAssign.editLimitNotAllowed")));
        return;
      }

      Role role = null;
      {
        String roleId = limitAssign.getOwnerAttributeAssign().getOwnerGroupId();
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          return;
        }
      }
      
      {
        String valueToAdd = request.getParameter("valueToAdd");
        
        if (StringUtils.isBlank(valueToAdd) ) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.addLimitValueRequired")));
          return;
          
        }
        
        limitAssign.getValueDelegate().addValue(valueToAdd);
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/permission/groupPermission.jsp"));
      groupViewPermissionsHelper(request, response, role);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.limitAddValueSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
  
  /**
   * edit assigned limit to permission
   * @param request
   * @param response
   */
  public void permissionLimitEditSubmit(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    GrouperSession grouperSession = null;
  
    try {
      PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String limitAssignId = request.getParameter("limitAssignId");
      
      if (StringUtils.isBlank(limitAssignId)) {
        throw new RuntimeException("Why is limitAssignId blank???");
      }
  
      AttributeAssign limitAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(limitAssignId, true, false);
      
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, limitAssign.getAttributeDef(), loggedInSubject)) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionAssign.editLimitNotAllowed")));
        return;
      }

      Role role = null;
      {
        String roleId = limitAssign.getOwnerAttributeAssign().getOwnerGroupId();
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          return;
        }
      }

      {
        String enabledDate = request.getParameter("enabledDate");
        
        if (StringUtils.isBlank(enabledDate) ) {
          limitAssign.setEnabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp enabledTimestamp = GrouperUtil.toTimestamp(enabledDate);
          limitAssign.setEnabledTime(enabledTimestamp);
        }
      }
      
      {
        String disabledDate = request.getParameter("disabledDate");
  
        if (StringUtils.isBlank(disabledDate) ) {
          limitAssign.setDisabledTime(null);
        } else {
          //must be yyyy/mm/dd
          Timestamp disabledTimestamp = GrouperUtil.toTimestamp(disabledDate);
          limitAssign.setDisabledTime(disabledTimestamp);
        }
      }
      
      limitAssign.saveOrUpdate();
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/permission/groupPermission.jsp"));
      
      groupViewPermissionsHelper(request, response, role);

      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.assignEditSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
    
  }
  
  
  /**
   * delete a limit assigned to permission
   * @param request
   * @param response
   */
  public void limitDelete(HttpServletRequest request, HttpServletResponse response) {
    
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String limitAssignId = request.getParameter("limitAssignId");
      
      Object limitAssignIdAttribute = request.getAttribute("limitAssignId");
      
      if (StringUtils.isBlank(limitAssignId) && limitAssignIdAttribute == null) {
        throw new RuntimeException("Why is limitAssignId blank???");
      }
      
      limitAssignId = StringUtils.defaultIfBlank(limitAssignId, (String)limitAssignIdAttribute);
      
      AttributeAssign limitAssign = AttributeAssignFinder.findById(limitAssignId, true);
      
      limitAssign.delete();
      
      AttributeDef limitAttributeDef = null;
      AttributeDefName limitAttributeDefName = null;
      {
        String attributeDefNameId = limitAssign.getAttributeDefNameId();
        limitAttributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
        limitAttributeDef = limitAttributeDefName.getAttributeDef();
        if (!PrivilegeHelper.canAttrUpdate(GrouperSession.staticGrouperSession(), limitAttributeDef, loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditAttributeDef")));
          return;
        }
      }
      
      AttributeAssign permissionAssign = limitAssign.getOwnerAttributeAssign();
      
      //get current state
      Role role = null;
      {
        String roleId = permissionAssign.getOwnerGroupId();
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          return;
        }
      }
      
      AttributeDef permissionDef = permissionAssign.getAttributeDef();
      if (!permissionDef.getPrivilegeDelegate().canAttrUpdate(loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditLimit")));
        return;
      }

      groupViewPermissionsHelper(request, response, role);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.deleteLimitSuccessMessage")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }

  }
  
  /**
   * delete a limit assignment value
   * @param request
   * @param response
   */
  public void limitValueDelete(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
      PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String[] limitAssignValues = retrieveLimitAssignValueId(request);
  
      String limitAssignId = limitAssignValues[0];
      
      if (StringUtils.isBlank(limitAssignId)) {
        throw new RuntimeException("Why is limitAssignId blank???");
      }
      
      AttributeAssign limitAssign = AttributeAssignFinder.findById(limitAssignId, true);
  
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, limitAssign.getAttributeDef(), loggedInSubject)) {
        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionAssign.editLimitNotAllowed")));
        return;        
      }
      
      Role role = null;
      {
        String roleId = limitAssign.getOwnerAttributeAssign().getOwnerGroupId();
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          return;
        }
      }

      String limitAssignValueId = limitAssignValues[1];
      
      if (StringUtils.isBlank(limitAssignValueId)) {
        throw new RuntimeException("Why is limitAssignValueId blank???");
      }
  
      AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(limitAssignValueId, true);
      
      limitAssign.getValueDelegate().deleteValue(attributeAssignValue);
      
      groupViewPermissionsHelper(request, response, role);
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.limitValueSuccessDelete")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  
  }
  
  /**
   * edit a limit value
   * @param request
   * @param response
   */
  public void limitValueEdit(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String[] limitAssignValues = retrieveLimitAssignValueId(request);
  
      String limitAssignId = limitAssignValues[0];
      
      if (StringUtils.isBlank(limitAssignId)) {
        throw new RuntimeException("Why is limitAssignId blank???");
      }
  
      AttributeAssign limitAssign = AttributeAssignFinder.findById(limitAssignId, true);
  
      //now we need to check security
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, limitAssign.getAttributeDef(), loggedInSubject)) {        
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionAssign.editLimitNotAllowed")));
        return;
      }
      
      {
        Role role = null;
        String roleId = limitAssign.getOwnerAttributeAssign().getOwnerGroupId();
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          return;
        }
      }

      String limitAssignValueId = limitAssignValues[1];
      
      if (StringUtils.isBlank(limitAssignValueId)) {
        throw new RuntimeException("Why is limitAssignValueId blank???");
      }
  
      AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(limitAssignValueId, true);
      
      PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      permissionUpdateRequestContainer.setAttributeAssignValue(attributeAssignValue);
      AttributeAssignType attributeAssignType = limitAssign.getAttributeAssignType();
  
      AttributeAssign underlyingAssignment = limitAssign.getOwnerAttributeAssign();
      AttributeAssignType underlyingAttributeAssignType = underlyingAssignment.getAttributeAssignType();
      
      //set the type to underlying, so that the labels are correct
      GuiAttributeAssign guiUnderlyingAttributeAssign = new GuiAttributeAssign();
      guiUnderlyingAttributeAssign.setAttributeAssign(underlyingAssignment);

      permissionUpdateRequestContainer.setGuiAttributeAssign(guiUnderlyingAttributeAssign);
      
      GuiAttributeAssign guiAttributeAssignAssign = new GuiAttributeAssign();
      guiAttributeAssignAssign.setAttributeAssign(limitAssign);

      permissionUpdateRequestContainer.setGuiAttributeAssignAssign(guiAttributeAssignAssign);
      permissionUpdateRequestContainer.setAttributeAssignType(underlyingAttributeAssignType);
      permissionUpdateRequestContainer.setAttributeAssignAssignType(attributeAssignType);
      
       guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#groupPermission", 
           "/WEB-INF/grouperUi2/permission/permissionLimitValueEdit.jsp"));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }
  
  /**
   * submit the edit value screen
   * @param request
   * @param response
   */
  public void limitValueEditSubmit(HttpServletRequest request, HttpServletResponse response) {
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    GrouperSession grouperSession = null;
  
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String limitAssignId = request.getParameter("limitAssignId");
      
      if (StringUtils.isBlank(limitAssignId)) {
        throw new RuntimeException("Why is limitAssignId blank???");
      }
  
      AttributeAssign limitAssign = AttributeAssignFinder.findById(limitAssignId, true);
  
      //now we need to check security
      AttributeDef limitDef = limitAssign.getAttributeDef();
      if (!PrivilegeHelper.canAttrUpdate(grouperSession, limitDef, loggedInSubject)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionAssign.editLimitNotAllowed")));
        return;
      }
      
      Role role = null;
      {
        String roleId = limitAssign.getOwnerAttributeAssign().getOwnerGroupId();
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          return;
        }
      }

      String limitAssignValueId = request.getParameter("limitAssignValueId");
      
      if (StringUtils.isBlank(limitAssignValueId)) {
        throw new RuntimeException("Why is limitAssignValueId blank???");
      }
  
      Set<AttributeAssignValue> limitAssignValues = limitAssign.getValueDelegate().retrieveValues();
      
      //AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(limitAssignValueId, true);
      
      AttributeAssignValue attributeAssignValue = null;
      
      for (AttributeAssignValue current : limitAssignValues) {
        if (StringUtils.equals(limitAssignValueId, current.getId())) {
          attributeAssignValue = current;
          break;
        }
      }
      
      //should never happen
      if (attributeAssignValue == null) {
        throw new RuntimeException("Why can value not be found? " + limitAssignValueId);
      }

      guiResponseJs.addAction(GuiScreenAction.newCloseModal());

      PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
      
      {
        String valueToEdit = request.getParameter("valueToEdit");
        
        if (StringUtils.isBlank(valueToEdit) ) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.editLimitValueRequired")));
          return;
          
        }
        
        if (!assignLimitValueAndValidate(guiResponseJs, limitDef,
            limitAssignValueId, attributeAssignValue, valueToEdit)) {
          return;
        }
        
      }
      
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId", 
          "/WEB-INF/grouperUi2/permission/groupPermission.jsp"));
      
      groupViewPermissionsHelper(request, response, role);
      
      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, 
          TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.limitEditValueSuccess")));
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
  
  }
  
  /**
   * submit permissions button was pressed on the view permissions screen
   * @param request
   * @param response
   */
  public void saveMultiplePermissionSubmit(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();
  
    GrouperSession grouperSession = null;
  
    AttributeDef attributeDef = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      
      String permissionAssignTypeString = request.getParameter("permissionAssignType");
      
      if (StringUtils.isBlank(permissionAssignTypeString)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionAssign.requiredOwnerType")));
        return;
      }
      PermissionType permissionAssignType = PermissionType.valueOfIgnoreCase(permissionAssignTypeString, false);

      if (permissionAssignType ==  null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.requiredOwnerType")));
        return;
      }
      
      StringBuilder message = new StringBuilder();
      
      Pattern pattern = Pattern.compile("^previousState__(.*)__(.*)__(.*)__(.*)$");
      Enumeration<?> enumeration = request.getParameterNames();
      
      Role role = null;

      //process all params submitted
      while (enumeration != null && enumeration.hasMoreElements()) {
        String paramName = (String)enumeration.nextElement();
        Matcher matcher = pattern.matcher(paramName);
        if (matcher.matches()) {
          
          //lets get the previous state
          boolean previousChecked = GrouperUtil.booleanValue(request.getParameter(paramName));
          
          //get current state
          String roleId = matcher.group(1);
          String memberId = matcher.group(2);
          String attributeDefNameId = matcher.group(3);
          String action = matcher.group(4);

          String currentStateString = request.getParameter("permissionCheckbox__" + roleId + "__" + memberId + "__" + attributeDefNameId + "__" + action);
          boolean currentChecked = GrouperUtil.booleanValue(currentStateString, false);
          
          //if they dont match, do something about it
          if (previousChecked != currentChecked) {

            if (message.length() > 0) {
              message.append("<br />");
            }

            if (role == null) {
              role = GroupFinder.findByUuid(grouperSession, roleId, true);
              if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
                guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                    TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
                return;
              }
            }
           
            Member member = null;
            { 
              if (permissionAssignType == PermissionType.role_subject) {
                member = MemberFinder.findByUuid(grouperSession, memberId, true);
              }
            }
            AttributeDefName attributeDefName = null;
            {
              attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
              attributeDef = attributeDefName.getAttributeDef();
              if (!PrivilegeHelper.canAttrUpdate(GrouperSession.staticGrouperSession(), attributeDef, loggedInSubject)) {
                guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
                    TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditAttributeDef")));
                return;
              }
              
            }
            
            GuiMember guiMember = permissionAssignType == PermissionType.role_subject ? new GuiMember(member) : null;
            String subjectScreenLabel = permissionAssignType == PermissionType.role_subject ? guiMember.getGuiSubject().getScreenLabel() : null;

            if (currentChecked) {
              
              if (permissionAssignType == PermissionType.role) {
                AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(action, attributeDefName, PermissionAllowed.ALLOWED);
                if (attributeAssignResult.isChanged()) {
                  //simplePermissionUpdate.permissionAllowRole = Success: Role: {0} can now perform action: {1} on permission resource: {2}
                  message.append(
                      GrouperUiUtils.message("simplePermissionUpdate.permissionAllowRole", false, true, 
                          new Object[]{role.getDisplayExtension(), action, attributeDefName.getDisplayExtension()}));
                } else {
                  throw new RuntimeException("Why was this not changed????");
                }
              } else if (permissionAssignType == PermissionType.role_subject) {
                AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignSubjectRolePermission(action, attributeDefName, member, PermissionAllowed.ALLOWED);
                if (attributeAssignResult.isChanged()) {
                  //simplePermissionUpdate.permissionAllowRoleSubject = Success: Subject: {0} can now perform action: {1} on permission resource: {2} in the context of role: {3}
                  message.append(
                      GrouperUiUtils.message("simplePermissionUpdate.permissionAllowRoleSubject", false, true, 
                          new Object[]{subjectScreenLabel, action, attributeDefName.getDisplayExtension(), role.getDisplayExtension()}));
                } else {
                  throw new RuntimeException("Why was this not changed????");
                }
                
              } else {
                throw new RuntimeException("Not expecting permission type: " + permissionAssignType);
              }
            } else {

              if (permissionAssignType == PermissionType.role) {
                AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().removeRolePermission(action, attributeDefName);
                if (attributeAssignResult.isChanged()) {
                  //simplePermissionUpdate.permissionRevokeRole = Success: Role: {0} can no longer perform action: {1} on permission resource: {2}
                  message.append(
                      GrouperUiUtils.message("simplePermissionUpdate.permissionRevokeRole", false, true, 
                          new Object[]{role.getDisplayExtension(), action, attributeDefName.getDisplayExtension()}));
                } else {
                  throw new RuntimeException("Why was this not changed????");
                }
                
              } else if (permissionAssignType == PermissionType.role_subject) {
                AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().removeSubjectRolePermission(action, attributeDefName, member);
                if (attributeAssignResult.isChanged()) {
                  //simplePermissionUpdate.permissionAllowRoleSubject = Success: Subject: {0} can now perform action: {1} on permission resource: {2} in the context of role: {3}
                  message.append(
                      GrouperUiUtils.message("simplePermissionUpdate.permissionRevokeRoleSubject", false, true, 
                          new Object[]{subjectScreenLabel, action, attributeDefName.getDisplayExtension(), role.getDisplayExtension()}));
                } else {
                  throw new RuntimeException("Why was this not changed????");
                }
                
              } else {
                throw new RuntimeException("Not expecting permission type: " + permissionAssignType);
              }

            }
            
          }
        }
      }
      
      groupViewPermissionsHelper(request, response, group);
      
      if (message.length() > 0) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, message.toString()));
      } else {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.info,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.noPermissionChangesDetected")));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    } 
    
  }
  
  /**
   * privilege image button was pressed on the privilege edit panel
   * @param request
   * @param response
   */
  public void permissionPanelImageClick(HttpServletRequest request, HttpServletResponse response) {
  
    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
  
    PermissionUpdateRequestContainer permissionUpdateRequestContainer = PermissionUpdateRequestContainer.retrieveFromRequestOrCreate();

    GrouperSession grouperSession = null;

    AttributeDef attributeDef = null;
    
    try {
      grouperSession = GrouperSession.start(loggedInSubject);
      
      Group group = UiV2Group.retrieveGroupHelper(request, AccessPrivilege.READ).getGroup();
      
      if (group == null) {
        return;
      }
      
      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
  
      String guiPermissionId = request.getParameter("guiPermissionId");
      
      if (StringUtils.isBlank(guiPermissionId)) {
        throw new RuntimeException("Why is guiPermissionId blank????");
      }

      String permissionAssignTypeString = request.getParameter("permissionAssignType");
      
      if (StringUtils.isBlank(permissionAssignTypeString)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionAssign.requiredOwnerType")));
        return;
      }
      PermissionType permissionType = PermissionType.valueOfIgnoreCase(permissionAssignTypeString, false);

      if (permissionType ==  null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.requiredOwnerType")));
        return;
      }
      
      //<c:set var="guiPermissionId" value="${firstPermissionEntry.roleId}__${firstPermissionEntry.memberId}__${firstPermissionEntry.attributeDefNameId}__${firstPermissionEntry.action}" />
      Pattern pattern = Pattern.compile("^(.*)__(.*)__(.*)__(.*)$");
      Matcher matcher = pattern.matcher(guiPermissionId);
      if (!matcher.matches()) {
        throw new RuntimeException("Why does guiPermissionId not match? " + guiPermissionId);
      }

      //get current state
      Role role = null;
      {
        String roleId = matcher.group(1);
        role = GroupFinder.findByUuid(grouperSession, roleId, true);
        if (!((Group)role).hasGroupAttrUpdate(loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantManageRole")));
          return;
        }
      }
      Member member = null;
      { 
        if (permissionType == PermissionType.role_subject) {
          String memberId = matcher.group(2);
          member = MemberFinder.findByUuid(grouperSession, memberId, true);
        }
      }
      AttributeDefName attributeDefName = null;
      {
        String attributeDefNameId = matcher.group(3);
        attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, true);
        attributeDef = attributeDefName.getAttributeDef();
        if (!PrivilegeHelper.canAttrUpdate(GrouperSession.staticGrouperSession(), attributeDef, loggedInSubject)) {
          guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
              TextContainer.retrieveFromRequest().getText().get("simplePermissionUpdate.errorCantEditAttributeDef")));
          return;
        }
        
      }
      
      String action = matcher.group(4);
      
      String allowString = request.getParameter("allow");

      if (StringUtils.isBlank(allowString)) {
        throw new RuntimeException("Why is allow blank????");
      }
      boolean allow = GrouperUtil.booleanValue(allowString);
      
      String subjectScreenLabel = null;
      
      if (permissionType == PermissionType.role_subject) {
        GuiMember guiMember = new GuiMember(member);
        subjectScreenLabel = guiMember.getGuiSubject().getScreenLabel();
      }
      if (allow) {
        
        if (permissionType == PermissionType.role) {
          AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignRolePermission(action, attributeDefName, PermissionAllowed.ALLOWED);
          if (attributeAssignResult.isChanged()) {
            //simplePermissionUpdate.permissionAllowRole = Success: Role: {0} can now perform action: {1} on permission resource: {2}
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, GrouperUiUtils.message("simplePermissionUpdate.permissionAllowRole", false, true,
                new Object[]{role.getDisplayExtension(), action, attributeDefName.getDisplayExtension()})));
          } else {
            throw new RuntimeException("Why was this not changed????");
          }
        } else if (permissionType == PermissionType.role_subject) {
          AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().assignSubjectRolePermission(action, attributeDefName, member, PermissionAllowed.ALLOWED);
          if (attributeAssignResult.isChanged()) {
            //simplePermissionUpdate.permissionAllowRoleSubject = Success: Subject: {0} can now perform action: {1} on permission resource: {2} in the context of role: {3}
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, GrouperUiUtils.message("simplePermissionUpdate.permissionAllowRoleSubject", false, true, 
                new Object[]{subjectScreenLabel, action, attributeDefName.getDisplayExtension(), role.getDisplayExtension()})));
          } else {
            throw new RuntimeException("Why was this not changed????");
          }
          
        } else {
          throw new RuntimeException("Not expecting permission type: " + permissionType);
        }
      } else {

        if (permissionType == PermissionType.role) {
          AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().removeRolePermission(action, attributeDefName);
          if (attributeAssignResult.isChanged()) {
            //simplePermissionUpdate.permissionRevokeRole = Success: Role: {0} can no longer perform action: {1} on permission resource: {2}
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, GrouperUiUtils.message("simplePermissionUpdate.permissionRevokeRole", false, true, 
                new Object[]{role.getDisplayExtension(), action, attributeDefName.getDisplayExtension()})));
          } else {
            throw new RuntimeException("Why was this not changed????");
          }
          
        } else if (permissionType == PermissionType.role_subject) {
          AttributeAssignResult attributeAssignResult = role.getPermissionRoleDelegate().removeSubjectRolePermission(action, attributeDefName, member);
          if (attributeAssignResult.isChanged()) {
            //simplePermissionUpdate.permissionAllowRoleSubject = Success: Subject: {0} can now perform action: {1} on permission resource: {2} in the context of role: {3}
            guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success, GrouperUiUtils.message("simplePermissionUpdate.permissionRevokeRoleSubject", false, true, 
                new Object[]{subjectScreenLabel, action, attributeDefName.getDisplayExtension(), role.getDisplayExtension()})));
          } else {
            throw new RuntimeException("Why was this not changed????");
          }
          
        } else {
          throw new RuntimeException("Not expecting permission type: " + permissionType);
        }

      }
      groupViewPermissionsHelper(request, response, role);
      
    } finally {
      GrouperSession.stopQuietly(grouperSession); 
    }
        
  }

  private String[] retrieveLimitAssignValueId(HttpServletRequest httpServletRequest) {
    String menuIdOfMenuTarget = httpServletRequest.getParameter("menuIdOfMenuTarget");
    
    if (StringUtils.isBlank(menuIdOfMenuTarget)) {
      throw new RuntimeException("Missing id of menu target");
    }
    if (!menuIdOfMenuTarget.startsWith("limitAssignValueButton_")) {
      throw new RuntimeException("Invalid id of menu target: '" + menuIdOfMenuTarget + "'");
    }
    
    String[] values = menuIdOfMenuTarget.split("_");
    
    if (values.length != 3) {
      throw new RuntimeException("Invalid id of menu target");
    }
    
    return new String[] {values[1], values[2]};
    
  }
  
  public void permissionActionNameFilter(final HttpServletRequest request, final HttpServletResponse response) {
    
    //run the combo logic
    DojoComboLogic.logic(request, response, new DojoComboQueryLogicBase<AttributeAssignAction>() {
  
      /**
       */
      @Override
      public AttributeAssignAction lookup(HttpServletRequest request, GrouperSession grouperSession, String query) {
        
        String permissionDefId = request.getParameter("permissionDefComboName");
        
        String permissionDefNameId = request.getParameter("permissionResourceNameComboName");
        
        if (StringUtils.isBlank(permissionDefId) && StringUtils.isBlank(permissionDefNameId)) {
          return null;
        }
                
        AttributeDef attributeDef = null;
        
        if (StringUtils.isNotBlank(permissionDefId)) {
          attributeDef = AttributeDefFinder.findById(permissionDefId, false);
          if (attributeDef == null) {
            throw new RuntimeException("given attribute def id "+permissionDefId+" is not valid.");
          }
        }
        
        if (attributeDef == null && StringUtils.isNotBlank(permissionDefNameId)) {
          AttributeDefName attributeDefName = AttributeDefNameFinder.findById(permissionDefNameId, false);
          if (attributeDefName == null) {
            throw new RuntimeException("given attribute def name id "+permissionDefNameId+" is not valid.");
          }
          attributeDef = attributeDefName.getAttributeDef();
        }
        
        return attributeDef.getAttributeDefActionDelegate().findAction(query, false);
      }
  
      /**
       * 
       */
      @Override
      public Collection<AttributeAssignAction> search(HttpServletRequest request, GrouperSession grouperSession, String query) {
        
        String permissionDefId = request.getParameter("permissionDefComboName");
        
        String permissionDefNameId = request.getParameter("permissionResourceNameComboName");
        
        if (StringUtils.isBlank(permissionDefId) && StringUtils.isBlank(permissionDefNameId)) {
          return new ArrayList<AttributeAssignAction>();
        }
                
        AttributeDef attributeDef = null;
        
        if (StringUtils.isNotBlank(permissionDefId)) {
          attributeDef = AttributeDefFinder.findById(permissionDefId, false);
          if (attributeDef == null) {
            throw new RuntimeException("given attribute def id "+permissionDefId+" is not valid.");
          }
        }
        
        if (attributeDef == null && StringUtils.isNotBlank(permissionDefNameId)) {
          AttributeDefName attributeDefName = AttributeDefNameFinder.findById(permissionDefNameId, false);
          if (attributeDefName == null) {
            throw new RuntimeException("given attribute def name id "+permissionDefNameId+" is not valid.");
          }
          attributeDef = attributeDefName.getAttributeDef();
        }
        
        List<AttributeAssignAction> actions = new ArrayList<AttributeAssignAction>();
        
        Set<AttributeAssignAction> availableActions = attributeDef.getAttributeDefActionDelegate().allowedActions();
        
        if (!StringUtils.isBlank(query)) {
        
          String searchTerm = query.toLowerCase();
          
          for (AttributeAssignAction action : availableActions) {
            if (action.getName().toLowerCase().contains(searchTerm)) {
              actions.add(action);
            }
          }

        }
        
        return actions;
        
      }
  
      /**
       * 
       * @param t
       * @return
       */
      @Override
      public String retrieveId(GrouperSession grouperSession, AttributeAssignAction action) {
        return action.getId();
      }
      
      /**
       * 
       */
      @Override
      public String retrieveLabel(GrouperSession grouperSession, AttributeAssignAction action) {
        return action.getName();
      }
  
      /**
       * 
       */
      @Override
      public String retrieveHtmlLabel(GrouperSession grouperSession, AttributeAssignAction action) {
        //description could be null?
        String label = GrouperUiUtils.escapeHtml(action.getName(), true);
        String htmlLabel = "<img src=\"../../grouperExternal/public/assets/images/folder.gif\" /> " + label;
        return htmlLabel;
      }
      
      @Override
      public String initialValidationError(HttpServletRequest localRequest, GrouperSession grouperSession) {

        String permissionDefId = request.getParameter("permissionDefComboName");
        
        String permissionDefNameId = request.getParameter("permissionResourceNameComboName");
        
        if (StringUtils.isBlank(permissionDefId) && StringUtils.isBlank(permissionDefNameId)) {
          return TextContainer.retrieveFromRequest().getText().get("groupAssignPermissionErrorNoPermDefOrResource");
        }
        
        return null;
      }
  
    });
  }
  
}
