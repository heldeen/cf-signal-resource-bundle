package net.eldeen.dropwizard;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.validation.Valid;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.ResourceSignalStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class CfSignalResourceBundleTest {

  static class TestConfig extends Configuration {

    @Valid
    private CfSignalResourceConfig cfSignalResourceConfig;

    @JsonProperty
    public CfSignalResourceConfig getCfSignalResourceConfig() {
      return cfSignalResourceConfig;
    }
  }

  @Spy
  private TestConfig testConfig;

  @Mock
  private Environment environment;
  @Mock
  private LifecycleEnvironment lifecycleEnvironment;

  @Captor
  private ArgumentCaptor<CfSignalResourceBundle.CfSignalResourceLifcycleListener> listenerArgumentCaptor;

  @Rule
  public MockitoRule mockitoJUnitRule = MockitoJUnit.rule();

  @Before
  public void setupTestConfigWithDefaults() {
    testConfig.cfSignalResourceConfig = new CfSignalResourceConfig();
    testConfig.cfSignalResourceConfig.setAsgResourceName("autoScalingGroup");
    testConfig.cfSignalResourceConfig.setStackName("stackName");
    testConfig.cfSignalResourceConfig.setAwsRegion("us-west-2");
    testConfig.cfSignalResourceConfig.setEc2InstanceId("i-123");
  }

  @Before
  public void setupLifeCycleEnvironment() {
    when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
  }

  @Test
  public void testRunNoAWS() throws Exception {
    AmazonCloudFormation amazonCloudFormation = mock(AmazonCloudFormation.class);

    testConfig.cfSignalResourceConfig.setEc2InstanceId("");

    new CfSignalResourceBundle(amazonCloudFormation).run(testConfig, environment);

    verify(testConfig).getCfSignalResourceConfig();
    verifyZeroInteractions(lifecycleEnvironment, amazonCloudFormation);
  }

  @Test
  public void testRunAWS() throws Exception {
    AmazonCloudFormation amazonCloudFormation = mock(AmazonCloudFormation.class);

    new CfSignalResourceBundle(amazonCloudFormation).run(testConfig, environment);

    verify(testConfig).getCfSignalResourceConfig();
    verify(lifecycleEnvironment).addLifeCycleListener(any(CfSignalResourceBundle.CfSignalResourceLifcycleListener.class));
  }

  @Test
  public void lifecycleListenerSignalsSuccess() throws Exception {
    AmazonCloudFormation amazonCloudFormation = mock(AmazonCloudFormation.class);

    final String uniqueId = "i-123";
    testConfig.cfSignalResourceConfig.setEc2InstanceId(uniqueId);

    CfSignalResourceBundle cfSignalResourceBundle =
      new CfSignalResourceBundle(amazonCloudFormation);
    cfSignalResourceBundle.run(testConfig, environment);

    verify(testConfig).getCfSignalResourceConfig();

    verify(lifecycleEnvironment).addLifeCycleListener(listenerArgumentCaptor.capture());

    LifeCycle event = mock(LifeCycle.class);

    listenerArgumentCaptor.getValue().lifeCycleStarted(event);

    verify(amazonCloudFormation)
      .signalResource(
        argThat(
          allOf(
            hasProperty("status", equalTo(ResourceSignalStatus.SUCCESS.toString())),
            hasProperty("stackName", equalTo(testConfig.getCfSignalResourceConfig().getStackName())),
            hasProperty("logicalResourceId", equalTo(testConfig.getCfSignalResourceConfig().getAsgResourceName())),
            hasProperty("uniqueId", equalTo(uniqueId)))));

    assertThat(cfSignalResourceBundle.getInternalCloudFormation(), nullValue());
  }

  @Test
  public void lifecycleListenerSignalsFailureOnStartup() throws Exception {
    AmazonCloudFormation amazonCloudFormation = mock(AmazonCloudFormation.class);

    final String uniqueId = "i-123";
    testConfig.cfSignalResourceConfig.setEc2InstanceId(uniqueId);

    CfSignalResourceBundle cfSignalResourceBundle =
      new CfSignalResourceBundle(amazonCloudFormation);
    cfSignalResourceBundle.run(testConfig, environment);

    verify(testConfig).getCfSignalResourceConfig();

    verify(lifecycleEnvironment).addLifeCycleListener(listenerArgumentCaptor.capture());

    LifeCycle event = mock(LifeCycle.class);
    when(event.isStopping()).thenReturn(Boolean.FALSE);
    when(event.isStopped()).thenReturn(Boolean.FALSE);

    listenerArgumentCaptor.getValue().lifeCycleFailure(event, new Throwable("testing"));

    verify(amazonCloudFormation)
      .signalResource(
        argThat(
          allOf(
            hasProperty("status", equalTo(ResourceSignalStatus.FAILURE.toString())),
            hasProperty("stackName", equalTo(testConfig.getCfSignalResourceConfig().getStackName())),
            hasProperty("logicalResourceId", equalTo(testConfig.getCfSignalResourceConfig().getAsgResourceName())),
            hasProperty("uniqueId", equalTo(uniqueId)))));

    assertThat(cfSignalResourceBundle.getInternalCloudFormation(), nullValue());
  }

  @Test
  public void lifecycleListenerDoesNotSignalFailureOnShutdown() throws Exception {
    AmazonCloudFormation amazonCloudFormation = mock(AmazonCloudFormation.class);

    final String uniqueId = "i-123";
    testConfig.cfSignalResourceConfig.setEc2InstanceId(uniqueId);

    new CfSignalResourceBundle(amazonCloudFormation).run(testConfig, environment);

    verify(testConfig).getCfSignalResourceConfig();

    verify(lifecycleEnvironment).addLifeCycleListener(listenerArgumentCaptor.capture());

    LifeCycle event = mock(LifeCycle.class);
    when(event.isStopping()).thenReturn(Boolean.TRUE);
    when(event.isStopped()).thenReturn(Boolean.FALSE);

    verifyZeroInteractions(amazonCloudFormation);
  }

  @Test
  public void useInternalAmazonCloudformationClient() {
    CfSignalResourceBundle<Configuration> cfSignalResourceBundle = new CfSignalResourceBundle<>();
    assertSame(cfSignalResourceBundle.getCloudFormation(testConfig.cfSignalResourceConfig), cfSignalResourceBundle.getInternalCloudFormation());
  }

}