package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.google.GrouperGoogleProvisioner;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GoogleProvisionerConfiguration extends ProvisionerConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "provisioner." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(provisioner)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return GrouperGoogleProvisioner.class.getName();
  }

}