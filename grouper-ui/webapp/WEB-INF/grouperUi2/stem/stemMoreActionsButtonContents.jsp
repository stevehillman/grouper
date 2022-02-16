<!-- ./webapp/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp -->

<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start stem/stemMoreActionsButtonContents.jsp -->

                    <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">
                      <%-- on the privs tab, show the add member button --%>
                      <c:choose>
                        <c:when test="${grouperRequestContainer.stemContainer.showAddMember}">
                          <a id="show-add-block" href="#" onclick="$('#add-block-container').toggle('slow'); return false;" class="btn btn-medium btn-primary btn-block"><i class="fa fa-plus"></i> ${textContainer.text['stemViewMoreActionsAddMembers'] }</a>
                        </c:when>
                        <c:otherwise>
                          <a href="#" onclick="return guiV2link('operation=UiV2Stem.stemEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;" class="btn btn-medium btn-block btn-primary" role="button">${textContainer.text['stemViewEditStemButton'] }</a>
                        </c:otherwise>
                      </c:choose>
                    </c:if>
                    <div class="btn-group btn-block">
                    	<a data-toggle="dropdown" href="#" class="btn btn-medium btn-block dropdown-toggle" 
                    		aria-haspopup="true" aria-label="${textContainer.text['ariaLabelGuiMoreStemActions']}" aria-expanded="false" role="menu" 
                    		onclick="$('#stem-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#stem-more-options li').first().focus();return true;});">
                    		${textContainer.text['stemViewMoreActionsButton'] } <span class="caret"></span></a>
                      <ul class="dropdown-menu dropdown-menu-right" id="stem-more-options">
                        <li class="dropdown-item disabled grouper-menu-subheader">${textContainer.text['stemViewMoreActionsQuickLinks'] }</li>
                        <%-- add or remove to/from my favorites, this causes a success message --%>
                        <c:choose>
                          <c:when test="${grouperRequestContainer.stemContainer.favorite}">
                            <li><a href="#"
                            onclick="ajax('../app/UiV2Stem.removeFromMyFavorites?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                            >${textContainer.text['stemViewMoreActionsRemoveFromMyFavorites']}</a></li>
                          </c:when>
                          <c:otherwise>
                            <li><a href="#"
                            onclick="ajax('../app/UiV2Stem.addToMyFavorites?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                            >${textContainer.text['stemViewMoreActionsAddToMyFavorites']}</a></li>
                          </c:otherwise>
                        </c:choose>

                        <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Visualization.stemView&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                          >${textContainer.text['visualization.title'] }</a></li>
                          
                        <c:if test="${ (grouperRequestContainer.stemContainer.canCreateGroups && grouperRequestContainer.stemContainer.canCreateStems) || (grouperRequestContainer.groupStemTemplateContainer.templatesToShowInMoreActions.size() > 0 || grouperRequestContainer.groupStemTemplateContainer.customGshTemplates.size() > 0)}">

                          <li class="dropdown-item disabled grouper-menu-subheader">${textContainer.text['stemViewMoreActionsTemplates']}</li>


                          <li><a href="#" onclick="return guiV2link('operation=UiV2Template.newTemplate&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['createNewTemplateMenuButton'] }</a></li>
                              
                          <c:forEach items="${grouperRequestContainer.groupStemTemplateContainer.templatesToShowInMoreActions}" var="gshTemplate">
                            <li><a href="#" onclick="return guiV2link('operation=UiV2Template.newTemplate&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&templateType=${gshTemplate.key}'); return false;"
                                >${gshTemplate.value} </a></li>
                          </c:forEach>
                         
                        </c:if>
                        
                        
                        <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">

                          <li class="dropdown-item disabled grouper-menu-subheader">${textContainer.text['stemViewMoreActionsManage']}</li>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemCopy&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['stemViewCopyStemButton'] }</a></li>

                          <c:if test="${grouperRequestContainer.stemContainer.canCreateStems}">
                            <li><a href="#" 
                               onclick="return guiV2link('operation=UiV2AttributeDef.newAttributeDef', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['attributeDefNewCreateNewAttributeDefMenuButton'] }</a></li>
                            <li><a href="#" 
                               onclick="return guiV2link('operation=UiV2AttributeDefName.newAttributeDefName', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['attributeDefNameNewCreateNewAttributeDefNameMenuButton'] }</a></li>
                          </c:if>

                          <c:if test="${grouperRequestContainer.stemContainer.canCreateStems }">
                            <li><a href="#"
                            onclick="return guiV2link('operation=UiV2Stem.newStem', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['stemNewCreateNewStemMenuButton'] }</a></li>
  
                          </c:if>
                          <c:if test="${grouperRequestContainer.stemContainer.canCreateGroups }">
  
                            <li><a href="#"
                              onclick="return guiV2link('operation=UiV2Group.newGroup', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['groupNewCreateNewGroupMenuButton'] }</a></li>
  
                          </c:if>
                          <c:if test="${grouperRequestContainer.stemContainer.canCreateGroups }">
  
                            <li><a href="#"
                              onclick="return guiV2link('operation=UiV2LocalEntity.newLocalEntity', {optionalFormElementNamesToSend: 'objectStemId'});">${textContainer.text['groupNewCreateNewLocalEntityMenuButton'] }</a></li>
  
                          </c:if>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemEdit&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['stemViewEditStemButton'] }</a></li>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemMove&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['stemViewMoveStemButton'] }</a></li>

                          <c:if test="${isWheelGroupMember || grouperRequestContainer.stemContainer.canAdminPrivileges}">
                            <li class="dropdown-item disabled grouper-menu-subheader">${textContainer.text['stemViewMoreActionsAdministration']}</li>
                          </c:if>
                          
                          <c:if test="${isWheelGroupMember || grouperRequestContainer.stemContainer.canAdminPrivileges || grouperRequestContainer.stemContainer.canReadAttributes
                               || grouperRequestContainer.deprovisioningContainer.canReadDeprovisioning || grouperRequestContainer.provisioningContainer.canReadProvisioning
                               || grouperRequestContainer.grouperReportContainer.reportingEnabled || grouperRequestContainer.objectTypeContainer.canReadObjectType }">
                            <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Attestation.stemAttestation&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['attestationButton'] }</a></li>
                          </c:if>
                          <c:if test="${grouperRequestContainer.stemContainer.canReadAttributes}">
                            <li>
                              <a href="#" onclick="return guiV2link('operation=UiV2StemAttributeAssignment.viewAttributeAssignments&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;">
                                ${textContainer.text['stemViewAttributeAssignmentsButton'] }
                              </a>
                           </li>
                          </c:if>
                          <c:if test="${isWheelGroupMember || grouperRequestContainer.stemContainer.canAdminPrivileges}">

                            <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.viewAudits&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                                >${textContainer.text['stemViewAuditButton'] }</a></li>
                          </c:if>

                          <c:if test="${grouperRequestContainer.deprovisioningContainer.canReadDeprovisioning}">
                            <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Deprovisioning.deprovisioningOnFolderReport&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['deprovisioningMoreActionsMenuLabel'] }</a></li>
                          </c:if>         
                          <c:if test="${grouperRequestContainer.provisioningContainer.canReadProvisioning}">
                            <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2Provisioning.viewProvisioningOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['provisioningMoreActionsMenuLabel'] }</a></li>
                          </c:if>
                        </c:if>
                        <c:if test="${grouperRequestContainer.grouperReportContainer.reportingEnabled}">
                          <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                            >${textContainer.text['stemViewReportButton'] }</a></li>
                        </c:if>
                        
                        <c:if test="${grouperRequestContainer.objectTypeContainer.canReadObjectType}">
                          <li><a href="javascript:void(0)" onclick="return guiV2link('operation=UiV2GrouperObjectTypes.viewObjectTypesOnFolder&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                            >${textContainer.text['objectTypeMoreActionsMenuLabel'] }</a></li>
                          
                        </c:if>


                        <c:if test="${grouperRequestContainer.stemContainer.canAdminPrivileges }">

                          <br />
                          <li class="dropdown-item disabled grouper-menu-subheader">${textContainer.text['stemViewMoreActionsDelete']}</li>

                          <li><a href="#" onclick="return guiV2link('operation=UiV2Stem.stemDelete&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}'); return false;"
                              >${textContainer.text['stemViewDeleteStemButton'] }</a></li>
                        </c:if>

                      </ul>
                    </div>

