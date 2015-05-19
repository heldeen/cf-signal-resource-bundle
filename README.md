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

In the class that extends `io.dropwizard.Application` add the bundle providing the name of the [AutoScalingGroup](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-as-group.html)
and the [Stack Name](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/pseudo-parameter-reference.html).

    public class Main extends Application <AppConfig> {

      @Override
      public void initialize(Bootstrap<StipendConfig> bootstrap) {
        bootstrap.addBundle(new CfSignalResourceBundle(System.getenv("autoScalingGroup"), System.getenv("stackName"));
      }

    }

### License ###
Apache License Version 2.0