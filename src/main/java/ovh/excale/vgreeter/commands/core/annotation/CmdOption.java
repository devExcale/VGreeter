package ovh.excale.vgreeter.commands.core.annotation;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CmdOption {

	String name();

	String description() default "";

	boolean required() default true;

	double minValueD() default OptionData.MIN_NEGATIVE_NUMBER;

	double maxValueD() default OptionData.MAX_POSITIVE_NUMBER;

	long minValueL() default (long) OptionData.MIN_NEGATIVE_NUMBER;

	long maxValueL() default (long) OptionData.MAX_POSITIVE_NUMBER;

	int minLength() default 0;

	int maxLength() default Integer.MAX_VALUE;

}