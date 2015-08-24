package net.eldeen.dropwizard;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * qualifier used w/ injection to specify the AWS Instance ID
 */
@Qualifier
@Retention(RUNTIME)
@Target({ PARAMETER, FIELD, METHOD })
public @interface CfSignalResourceInstanceId {
}
