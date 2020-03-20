package se.arkalix.util.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to mark things that are not part of the public Kalix
 * API. Classes, interfaces, methods, and so on, marked with this annotation
 * may change in breaking ways even between patch versions of the library.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Internal {}
