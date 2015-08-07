package net.eldeen.dropwizard;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.ResourceSignalStatus;
import com.amazonaws.services.cloudformation.model.SignalResourceRequest;
import com.amazonaws.util.EC2MetadataUtils;
import io.dropwizard.Bundle;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Signal the AutoScalingGroup via CloudFormation Signal when running on an AWS EC2 instance.
 */
public class CfSignalResourceBundle implements Bundle {

  private static final Logger LOGGER = LoggerFactory.getLogger(CfSignalResourceBundle.class);

  private final String asgResourceName;
  private final String stackName;
  private final String awsRegion;

  /**
   * Create a bundle to signal the AutoScalingGroup via CloudFormation Signal
   *
   * @param asgResourceName the name of the auto scaling group cloudformation resource to signal
   * @param stackName       the name of the stack containing the auto scaling group to signal
   * @param awsRegion       the aws region the stack is in, e.g. us-west-2
   */
  public CfSignalResourceBundle(String asgResourceName, String stackName, String awsRegion) {
    this.asgResourceName = asgResourceName;
    this.stackName = stackName;
    this.awsRegion = awsRegion;
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {

  }

  @Override
  public void run(Environment environment) {

    final String instanceId = EC2MetadataUtils.getInstanceId();
    if (instanceId == null || instanceId.trim() == "") {
      LOGGER.debug("Unable to fetch EC2 Instance ID, assuming not running on AWS and thus not signalling");
      return;
    }

    environment.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
      @Override
      public void serverStarted(Server server) {
        AmazonCloudFormation client = null;
        try {
          client = new AmazonCloudFormationClient();
          client.setRegion(Region.getRegion(Regions.valueOf(awsRegion.toUpperCase().replace('-', '_'))));
          SignalResourceRequest request = new SignalResourceRequest();
          request.setUniqueId(instanceId);
          request.setLogicalResourceId(asgResourceName);
          request.setStackName(stackName);
          request.setStatus(ResourceSignalStatus.SUCCESS);
          client.signalResource(request);
        }
        catch (Exception e) {
          LOGGER.error("There was a problem signaling " + asgResourceName + " in stack " + stackName, e);
        }
        finally {
          if (client != null) {
            client.shutdown();
          }
        }
      }
    });
  }
}