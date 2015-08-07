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

In the class that extends `io.dropwizard.Application` add the bundle providing the name of the [AutoScalingGroup](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-as-group.html),
the [Stack Name](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-stackname), and the [AWS Region](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html#cfn-pseudo-param-region).

    public class Main extends Application <AppConfig> {

      @Override
      public void initialize(Bootstrap<StipendConfig> bootstrap) {
        String autoScalingGroup = ...;
        String stackName = ...;
        String region = ...;
      
        bootstrap.addBundle(new CfSignalResourceBundle(autoScalingGroup, stackName, region);
      }

    }

### License ###
Apache License Version 2.0
