package com.sheffield.instrumenter;

import java.util.Set;

public interface PropertySource {

	boolean hasParameter(String name);

	Set<String> getParameterNames();

	void setParameter(String name, String value) throws IllegalAccessException;

}
