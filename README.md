cf-signal-resource-bundle
====

Dropwizard bundle to signal the AWS AutoScalingGroup via the AWS CloudFormation SignalResource API when running on an AWS EC2 instance.

### Maven Dependency ###

    <dependency>
      <groupId>net.eldeen.dropwizard</groupId>
      <artifactId>cf-signal-resource-bundle</artifactId>
      <version>1.0-SNAPSHOT</version>
    <dependency>

### Example Usage ###

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

### License ###

[Apache License Version 2.0](LICENSE.md)
