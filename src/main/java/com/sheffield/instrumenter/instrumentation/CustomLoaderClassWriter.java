package com.sheffield.instrumenter.instrumentation;

import org.objectweb.asm.ClassWriter;

public class CustomLoaderClassWriter extends ClassWriter {
	private InstrumentingClassLoader loader;

	public CustomLoaderClassWriter(int flags, InstrumentingClassLoader loader) {
		super(flags);
		this.loader = loader;
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		Class<?> c, d;
		try {
			if (ClassStore.containsKey(type1)) {
				c = ClassStore.get(type1);
			} else {
				c = loader.loadOriginalClass(type1);
			}
			if (ClassStore.containsKey(type2)) {
				d = ClassStore.get(type2);
			} else {
				d = loader.loadOriginalClass(type2);
			}
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));
			return c.getName().replace('.', '/');
		}
	}

}
