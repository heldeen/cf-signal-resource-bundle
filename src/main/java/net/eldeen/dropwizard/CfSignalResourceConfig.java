package net.eldeen.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 */
public class CfSignalResourceConfig extends Configuration {

  @NotBlank
  private String asgResourceName;
  @NotBlank
  private String stackName;
  @NotBlank
  private String awsRegion;

  @JsonProperty
  public String getAsgResourceName() {
    return asgResourceName;
  }

  @JsonProperty
  public void setAsgResourceName(final String asgResourceName) {
    this.asgResourceName = asgResourceName;
  }

  @JsonProperty
  public String getStackName() {
    return stackName;
  }

  @JsonProperty
  public void setStackName(final String stackName) {
    this.stackName = stackName;
  }

  @JsonProperty
  public String getAwsRegion() {
    return awsRegion;
  }

  @JsonProperty
  public void setAwsRegion(final String awsRegion) {
    this.awsRegion = awsRegion;
  }
}
