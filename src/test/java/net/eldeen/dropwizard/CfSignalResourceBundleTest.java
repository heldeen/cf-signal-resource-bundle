package net.eldeen.dropwizard;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRule;

public class CfSignalResourceBundleTest {

  @Mock
  private Environment environment;

  @Rule
  public MockitoJUnitRule mockitoJUnitRule = new MockitoJUnitRule(this);

  @Test
  public void testRunNoAWS() throws Exception {

    final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
    when(environment.lifecycle()).thenReturn(lifecycleEnvironment);

    new CfSignalResourceBundle("autoScalingGroup", "stackName", "us-west-2").run(environment);

    verifyZeroInteractions(lifecycleEnvironment);
  }

  @Test
  @Ignore
  public void testRunAWS() throws Exception {

    final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
    when(environment.lifecycle()).thenReturn(lifecycleEnvironment);

    new CfSignalResourceBundle("autoScalingGroup", "stackName", "us-west-2").run(environment);

    verify(lifecycleEnvironment).addServerLifecycleListener(any(ServerLifecycleListener.class));
  }
}