package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;

public class TargetDaoUpdateGroupRequest {

  public TargetDaoUpdateGroupRequest() {
    // TODO Auto-generated constructor stub
  }
  private ProvisioningGroup targetGroup;
  
  public ProvisioningGroup getTargetGroup() {
    return targetGroup;
  }
  
  public void setTargetGroup(ProvisioningGroup targetGroup) {
    this.targetGroup = targetGroup;
  }

  public TargetDaoUpdateGroupRequest(ProvisioningGroup targetGroup) {
    super();
    this.targetGroup = targetGroup;
  }
  
  
}