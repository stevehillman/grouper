package edu.internet2.middleware.grouper.app.jexlTester;


public enum ScriptExampleForProvisioningMembershipTranslation implements ScriptExample {
  
  GENERIC;

  @Override
  public ScriptType retrieveScriptType() {
    return ScriptType.PROVISIONING_MEMBERSHIP_TRANSLATION;
  }

}
