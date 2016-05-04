package com.sheffield.instrumenter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InstrumentationProperties implements PropertySource {
	public static final String LOG_FILENAME = "";

	protected InstrumentationProperties() {
		reflectMap();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Parameter {
		String key();

		String group()

		default "Experimental";

		String description();

		boolean hasArgs();
	}

	public enum InstrumentationApproach {
		STATIC, ARRAY, NONE
	}

	@Parameter(key = "instrumentation_approach", description = "Determines the approach to be used during class instrumentation. A static approach inserts calls to ClassAnalyzer.lineFound etc to track which lines/branches have been covered. Using an array stores all line/branch executions in an array of integers and has a method to get all the values",  hasArgs = true)
	public static InstrumentationApproach INSTRUMENTATION_APPROACH = InstrumentationApproach.ARRAY;

	@Parameter(key = "instrument_lines", description = "Switch on line instrumentation",  hasArgs = true)
	public static boolean INSTRUMENT_LINES = true;

	@Parameter(key = "instrument_branches", description = "Switch on branch instrumentation",  hasArgs = true)
	public static boolean INSTRUMENT_BRANCHES = true;

	@Parameter(key = "write_class", description = "flag to determine whether or not to write classes. If set to true, the InstrumentingClassLoader will write out all classes to the value of BYTECODE_DIR",  hasArgs = true)
	public static boolean WRITE_CLASS = false;

	@Parameter(key = "bytecode_dir", description = "directory in which to store bytecode if the WRITE_CLASS property is set to true",  hasArgs = true)
	public static String BYTECODE_DIR = System.getProperty("user.home") + "/.bytecode/";

	@Parameter(key = "log_dir", description = "directory in which to store log files (application.log, timings.log)",  hasArgs = true)
	public static String LOG_DIR = System.getProperty("user.home") + "/.logs/";

	@Parameter(key = "log_timings", description = "set whether application timings should be written to a log file",  hasArgs = true)
	public static boolean LOG = true;

	@Parameter(key = "use_changed_flag", description = "It is possible to add a flag through instrumentation that will tell the ClassAnalyzer that a class has changed in some way. This creates a form of hybrid approach to instrumentation, but saves work at the time of collecting coverage data",  hasArgs = true)
	public static boolean USE_CHANGED_FLAG = true;

	protected Map<String, Field> parameterMap = new HashMap<String, Field>();
	protected Map<String, Parameter> annotationMap = new HashMap<String, Parameter>();

	private void reflectMap() {
		for (Field field : Arrays.asList(getClass().getFields())) {
			if (field.isAnnotationPresent(Parameter.class)) {
				Parameter p = field.getAnnotation(Parameter.class);
				String key = p.key();
				parameterMap.put(key, field);
				annotationMap.put(key, p);
			}
		}
	}

	@Override
	public boolean hasParameter(String name) {
		return parameterMap.keySet().contains(name);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setParameter(String key, String value) throws IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key)) {
			throw new IllegalArgumentException(key + " was not found in the InstrumentationProperties class");
		}
		Field f = parameterMap.get(key);
		Class<?> cl = f.getType();
		if (cl.isAssignableFrom(Number.class) || cl.isPrimitive()) {
			if (cl.equals(Long.class) || cl.equals(long.class)) {
				Long l = Long.parseLong(value);
				f.setLong(null, l);
			} else if (cl.equals(Double.class) || cl.equals(double.class)) {
				Double d = Double.parseDouble(value);
				f.setDouble(null, d);
			} else if (cl.equals(Float.class) || cl.equals(float.class)) {
				Float fl = Float.parseFloat(value);
				f.setFloat(null, fl);
			} else if (cl.equals(Integer.class) || cl.equals(int.class)) {
				Integer in = Integer.parseInt(value);
				f.setInt(null, in);
			} else if (cl.equals(Boolean.class) || cl.equals(boolean.class)) {
				Boolean bl = Boolean.parseBoolean(value);
				f.setBoolean(null, bl);
			}
		} else if (cl.isAssignableFrom(String.class)) {
			f.set(null, value);
		}
		if (f.getType().isEnum()) {
			f.set(null, Enum.valueOf((Class<Enum>) f.getType(), value.toUpperCase()));
		}
	}

	@Override
	public Set<String> getParameterNames() {
		return parameterMap.keySet();
	}

	private static InstrumentationProperties instance;

	public static InstrumentationProperties instance() {
		if (instance == null) {
			instance = new InstrumentationProperties();
		}
		return instance;
	}
}
