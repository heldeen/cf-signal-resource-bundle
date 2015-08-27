cf-signal-resource-bundle
====

Dropwizard bundle to signal the AWS AutoScalingGroup via the AWS CloudFormation SignalResource API when running on an AWS EC2 instance.

## Why does this exist ##

If you're deploying [Dropwizard](http://dropwizard.io) webservices on AWS using [CloudFormation Templates](https://aws.amazon.com/cloudformation/) and [AutoScaling Groups](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-as-group.html) with [Rolling Updates](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-attribute-updatepolicy.html) then this is the bundle you've been looking for!

In short it makes your time to perform a rolling deploy as short as possible as it only depends on how fast your app can start. It's very handy when you app needs to do some work at startup sometimes (database migrations, provisioning, etc), but not others. 

## How to Use this bundle ##

### CloudFormation Template ###

The key here is to set the `Resources.<YourASG_Name>.UpdatePolicy.WaitOnResourceSignals` to `true`. You can read up on what that means exactly [here](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-attribute-updatepolicy.html#cfn-attributes-updatepolicy-rollingupdate-waitonresourcesignals) but the TL;DR is have the rolling update wait until signaled that everything worked to continue. This bundle provides that signaling!
    
    {
      "AWSTemplateFormatVersion" : "2010-09-09",
      "Description" : "AWS CloudFormation for a Dropwizard.io Webservice using instances in a AutoScalingGroup.",
      "Parameters" : {...},
      "Resources" : {
        "DropwizardWebserviceASG" : {
          "Type" : "AWS::AutoScaling::AutoScalingGroup",
          "UpdatePolicy" : {
            "AutoScalingRollingUpdate": {
              "MaxBatchSize": "2",
              "MinInstancesInService": 2,
              "PauseTime": "PT10M",
              "SuspendProcesses": [
                "HealthCheck",
                "ReplaceUnhealthy",
                "AZRebalance",
                "AlarmNotification",
                "ScheduledActions"
              ],
              "WaitOnResourceSignals": "true"
            }
          },
          "Properties" : {
            "AvailabilityZones" : [
              "us-west-2a",
              "us-west-2b"
            ],
            "LaunchConfigurationName" : { "Ref" : "LaunchConfig" },
            "MinSize" : 2,
            "MaxSize" : 8
          }
        },
        {...other resources}
      }
    }

### Maven Dependency ###

Add this to your Dropwizard Webservice pom.xml.

    <dependency>
      <groupId>net.eldeen.dropwizard</groupId>
      <artifactId>cf-signal-resource-bundle</artifactId>
      <version>2.0</version>
    <dependency>

### Registering the Bundle ###

In the class that extends `io.dropwizard.Application` add the bundle. Use config to provide the name of the [AutoScalingGroup](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-as-group.html),
the [Stack Name](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-stackname), and the [AWS Region](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-region).

    public class Main extends Application <AppConfig> {

      @Override
      public void initialize(Bootstrap<StipendConfig> bootstrap) {
      
        bootstrap.addBundle(new CfSignalResourceBundle());
      }
      
      // [...]

    }
    
And in your application's Dropwizard Config add
    
    public class AppConfig extends Configuration {
    
        @Valid
        private CfSignalResourceConfig cfSignalResourceConfig;
    
        @JsonProperty
        public CfSignalResourceConfig getCfSignalResource() {
          return cfSignalResourceConfig;
        }
        
        // [...]
      }
    
And add the actual config values to your configuration yml. The config value `asgRegion` is optional as it will be fetched from
[EC2MetadataUtils#getEC2InstanceRegion()](https://github.com/aws/aws-sdk-java/blob/master/aws-java-sdk-core/src/main/java/com/amazonaws/util/EC2MetadataUtils.java)
 
    cfSignalResource:
      asgResourceName: yourASG_ResourceName
      stackName: yourASG_StackName
      
### Environment Variables and Config ###
If you have these values as environment variables you may want to [have Dropwizard use those instead](http://www.dropwizard.io/manual/core.html#environment-variables).
 
    public class Main extends Application<AppConfig> {
        // [...]
        @Override
        public void initialize(Bootstrap<AppConfig> bootstrap) {
            // Enable variable substitution with environment variables
            bootstrap.setConfigurationSourceProvider(
                    new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                                                       new EnvironmentVariableSubstitutor()
                    )
            );
    
        }
    
        // [...]
    }
    
The config yaml can provide defaults, but use the environment variable values if they exist, following the rules of the
configured [EnvironmentVariableSubstitutor](https://github.com/dropwizard/dropwizard/blob/master/dropwizard-configuration/src/main/java/io/dropwizard/configuration/EnvironmentVariableSubstitutor.java)
from above.
 
    cfSignalResource:
      asgResourceName: ${ASG_RESOURCE_NAME}
      stackName: ${ASG_STACK_NAME:-yourASG_StackName}

## License ##

[Apache License Version 2.0](LICENSE.md)
