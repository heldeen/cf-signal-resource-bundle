package net.eldeen.dropwizard;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;

import com.amazonaws.util.EC2MetadataUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import io.dropwizard.Configuration;

/**
 * <p>
 * config for {@link CfSignalResourceBundle}.
 * </p>
 * <b>Configuration Parameters:</b>
 * <table>
 *   <caption>Configuration Parameters</caption>
 *   <tr>
 *     <td>Name</td>
 *     <td>Default</td>
 *     <td>Description</td>
 *   </tr>
 *   <tr>
 *     <td>{@code asgResourcename}</td>
 *     <td>none</td>
 *     <td>Name of your <a href="http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-as-group.html">Auto Scaling Group</a></td>
 *   </tr>
 *   <tr>
 *     <td>{@code stackName}</td>
 *     <td>none</td>
 *     <td>Name of the <a href="http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-stackname">AWS Cloudformation Stack</a></td>
 *   </tr>
 *   <tr>
 *     <td>{@code ec2InstanceId}</td>
 *     <td>The current EC2 Instance ID via {@link EC2MetadataUtils#getInstanceId()}. Only works when
 *     running on an EC2 Instance.</td>
 *     <td>Specify the EC2 Instance ID if you don't want it automatically looked up.</td>
 *   </tr>
 *   <tr>
 *     <td>{@code awsRegion}</td>
 *     <td>The current region of the EC2 Instance via {@link EC2MetadataUtils#getEC2InstanceRegion()}. Only works when
 *     running on an EC2 Instance.</td>
 *     <td>Specify the EC2 Instance Region if you don't want it automatically looked up.</td>
 *   </tr>
 *   <tr>
 *     <td>{@code skip}</td>
 *     <td>{@code false}</td>
 *     <td>If this bundle should skip running altogether; useful for cases where a single artifact is run in both AWS
 *     and non-AWS environments. When {@code true}, the bundle then ignores all other normally required properties thus
 *     they are optional.</td>
 *   </tr>
 * </table>
 *
 * The {@code asgResourcename} and {@code stackName} are required. The {@code awsRegion} and {@code ec2InstanceId} are
 * optional as the defaults are {@link EC2MetadataUtils#getInstanceId()} {@link EC2MetadataUtils#getEC2InstanceRegion()}
 */
public class CfSignalResourceConfig extends Configuration {

  @Valid
  private boolean skip = false;

  private String asgResourceName;

  private String stackName;

  private String ec2InstanceId;

  private String awsRegion;

  @AssertTrue(message = "both 'asgResourceName' and 'stackName' must not be blank when 'skip == false'")
  private boolean isValid() {
    return skip || !Strings.isNullOrEmpty(asgResourceName) && !Strings.isNullOrEmpty(stackName);
  }

  @JsonProperty
  public boolean isSkip() {
    return skip;
  }

  @JsonProperty
  public void setSkip(final boolean skip) {
    this.skip = skip;
  }

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
  public String getEc2InstanceId() {
    return ec2InstanceId;
  }

  @JsonProperty
  public void setEc2InstanceId(final String ec2InstanceId) {
    this.ec2InstanceId = ec2InstanceId;
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
