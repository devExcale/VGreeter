package ovh.excale.vgreeter.entity;

import org.hibernate.annotations.IdGeneratorType;
import ovh.excale.vgreeter.utilities.UUIDv7Generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IdGeneratorType(UUIDv7Generator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface GenerateUUIDv7 {

}