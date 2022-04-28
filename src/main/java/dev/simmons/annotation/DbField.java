package dev.simmons.annotation;

import dev.simmons.data.PostgresORM;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DbField {
    String name();
}
