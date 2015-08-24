package net.eldeen.dropwizard;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.ResourceSignalStatus;
import com.amazonaws.services.cloudformation.model.SignalResourceRequest;
import com.amazonaws.util.EC2MetadataUtils;
import com.google.common.base.Throwables;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Signal the AutoScalingGroup via CloudFormation Signal when running on an AWS EC2 instance.
 */
public class CfSignalResourceBundle<T extends Configuration> implements ConfiguredBundle<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CfSignalResourceBundle.class);

  private final Supplier<Optional<String>> instanceIdProvider;
  private final Function<CfSignalResourceConfig, AmazonCloudFormation> cloudFormationSupplier;
  private final AtomicReference<AmazonCloudFormation> cloudFormationAtomicRef = new AtomicReference<>(null);

  public CfSignalResourceBundle() {
    instanceIdProvider = () -> Optional.ofNullable(EC2MetadataUtils.getInstanceId());

    cloudFormationSupplier = (cfSignalResourceConfig) -> {
      AmazonCloudFormation amazonCloudFormation = cloudFormationAtomicRef.get();

      if (amazonCloudFormation != null) {
        return amazonCloudFormation;
      }

      return cloudFormationAtomicRef.updateAndGet((unused) -> {

        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient();

        amazonCloudFormationClient.setRegion(Region.getRegion(Regions.valueOf(cfSignalResourceConfig.getAwsRegion()
                                                                                                    .toUpperCase()
                                                                                                    .replace('-', '_'))));
        return amazonCloudFormationClient;
      });
    };
  }

  @Inject
  public CfSignalResourceBundle(@CfSignalResourceInstanceId Optional<String> instanceId, AmazonCloudFormation amazonCloudFormation) {
    instanceIdProvider = () -> checkNotNull(instanceId);
    cloudFormationSupplier = (config) -> amazonCloudFormation;
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
  }

  public CfSignalResourceConfig getConfiguration() {
    return new CfSignalResourceConfig();
  }

  @Override
  public void run(T config, Environment environment) {

    final Optional<String> instanceId = instanceIdProvider.get();
    if (!instanceId.isPresent()) {
      LOGGER.debug("Unable to fetch EC2 Instance ID, assuming not running on AWS and thus not signalling");
      return;
    }

    environment.lifecycle()
               .addLifeCycleListener(
                 new CfSignalResourceLifcycleListener(getCfResourceBundleConfig(config).orElseGet(this::getConfiguration),
                                                      instanceId.get()));
  }

  private void sendSignal(CfSignalResourceConfig config, final String instanceId, boolean success) {
    try {
      AmazonCloudFormation client = cloudFormationSupplier.apply(config);
      SignalResourceRequest request = new SignalResourceRequest();
      request.setUniqueId(instanceId);
      request.setLogicalResourceId(config.getAsgResourceName());
      request.setStackName(config.getStackName());
      request.setStatus(success? ResourceSignalStatus.SUCCESS : ResourceSignalStatus.FAILURE);
      client.signalResource(request);
    }
    catch (Exception e) {
      LOGGER.error("There was a problem signaling " + config.getAsgResourceName()
                   + " in stack " + config.getStackName(), e);
    }
    finally {
      AmazonCloudFormation internalClient = cloudFormationAtomicRef.get();
      if (internalClient != null) {
        internalClient.shutdown();
      }
    }
  }

  private Optional<CfSignalResourceConfig> getCfResourceBundleConfig(final T config) {
    for (Method method : config.getClass().getMethods()) {
      if (CfSignalResourceConfig.class.equals(method.getReturnType())
        && method.getParameterCount() == 0) {
        try {
          return Optional.ofNullable((CfSignalResourceConfig) method.invoke(config, new Object[0]));
        }
        catch (IllegalAccessException e) {
          throw new RuntimeException("method exposing CfSignResourceConfig must be accessible", e);
        }
        catch (InvocationTargetException e) {
          Throwables.propagate(e);
        }
      }
    }
    return Optional.empty();
  }

  class CfSignalResourceLifcycleListener implements LifeCycle.Listener {

    private final CfSignalResourceConfig cfSignalResourceConfig;
    private final String instanceId;

    CfSignalResourceLifcycleListener(final CfSignalResourceConfig cfSignalResourceConfig, final String instanceId) {
      this.cfSignalResourceConfig = cfSignalResourceConfig;
      this.instanceId = instanceId;
    }

    @Override
    public void lifeCycleStarting(final LifeCycle event) {
      //dont care
    }

    @Override
    public void lifeCycleFailure(final LifeCycle event, final Throwable cause) {
      //because this method can be called if there is a failure on shutdown
      //only attempt to signal failure if the failure is on startup
      if (!(event.isStopping() || event.isStopped())) {
        sendSignal(cfSignalResourceConfig, instanceId, false);
      }
    }

    @Override
    public void lifeCycleStopping(final LifeCycle event) {
      //dont care
    }

    @Override
    public void lifeCycleStopped(final LifeCycle event) {
      //dont care
    }

    @Override
    public void lifeCycleStarted(final LifeCycle event) {
      sendSignal(cfSignalResourceConfig, instanceId, true);
    }
  }
}