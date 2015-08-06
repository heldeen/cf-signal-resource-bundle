package net.eldeen.dropwizard;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingInstanceDetails;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingInstancesRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingInstancesResult;
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

  private final String logicalResourceId;
  private final Optional<String> stackName;

  /**
   * Create a bundle to signal the AutoScalingGroup via CloudFormation Signal
   * @param logicalResourceId the name of the auto scaling group to signal
   * @param stackName the name of the stack containing the auto scaling group to signal
   */
  public CfSignalResourceBundle(String logicalResourceId, String stackName) {
    this.logicalResourceId = logicalResourceId;
    this.stackName = Optional.ofNullable(stackName == null || stackName.trim().length() == 0?
                                         null : stackName);
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
          SignalResourceRequest request = new SignalResourceRequest();
          request.setUniqueId(instanceId);
          request.setLogicalResourceId(logicalResourceId);
          request.setStackName(stackName.orElseGet(this::lookupAsgName));
          request.setStatus(ResourceSignalStatus.SUCCESS);
          client.signalResource(request);
        }
        catch (Exception e) {
          LOGGER.error("There was a problem signalling " + logicalResourceId + " in stack " + stackName, e);
        }
        finally {
          if (client != null) {
            client.shutdown();
          }
        }
      }

      private String lookupAsgName() {
        AmazonAutoScalingClient client = null;
        try {
          client = new AmazonAutoScalingClient();
          DescribeAutoScalingInstancesRequest request = new DescribeAutoScalingInstancesRequest();
          request.setInstanceIds(Collections.singletonList(instanceId));
          final DescribeAutoScalingInstancesResult result =
            client.describeAutoScalingInstances(request);
          final List<AutoScalingInstanceDetails> autoScalingInstances = result.getAutoScalingInstances();
          final int size = autoScalingInstances.size();
          if (size != 1) {
            throw new RuntimeException("unable to lookup the ASG, got " + size + " results on lookup");
          }
          return autoScalingInstances.get(0).getAutoScalingGroupName();
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
