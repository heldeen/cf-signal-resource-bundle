package net.eldeen.dropwizard;

import javax.validation.Valid;

import com.amazonaws.util.EC2MetadataUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotBlank;

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
 *       <td>{@code stackName}</td>
 *       <td>none</td>
 *       <td>Name of the <a href="http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-stackname">AWS Cloudformation Stack</a></td>
 *   </tr>
 *   <tr>
 *     <td>{@code awsRegion}</td>
 *     <td>The current region of the EC2 Instance via {@link EC2MetadataUtils#getEC2InstanceRegion()}. Only works when
 *     running on an EC2 Instance.</td>
 *   </tr>
 * </table>
 *
 * The asgResourcename and stackName are required. The awsRegion is optional as it's default is
 * {@link EC2MetadataUtils#getEC2InstanceRegion()}
 */
public class CfSignalResourceConfig extends Configuration {

  @Valid
  @NotBlank
  private String asgResourceName;

  @Valid
  @NotBlank
  private String stackName;

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
