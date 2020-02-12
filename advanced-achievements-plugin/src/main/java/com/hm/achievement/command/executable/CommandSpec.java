package com.hm.achievement.command.executable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation providing information about the plugin's commands.
 *
 * @author Pyves
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandSpec {

	public String name();

	public String permission();

	public int minArgs();

	public int maxArgs();
}
