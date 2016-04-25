package com.sheffield.instrumenter.instrumentation;

import java.util.HashMap;

public class ClassStore {
	private static HashMap<String, Class<?>> store = new HashMap<String, Class<?>>();
	/**
	 *
	 */
	private static final long serialVersionUID = -1002975153253026174L;

	public static void put(String name, Class<?> cl) {
		store.put(name, cl);
	}

	public static boolean containsKey(String name) {
		return store.containsKey(name.replace('.', '/')) || store.containsKey(name.replace('/', '.'));
	}

	public static Class<?> get(String name) {
		if (name == null) {
			return null;
		}
		if (store.containsKey(name)) {
			return store.get(name);
		}
		name = name.replace('/', '.');
		if (store.containsKey(name)) {
			return store.get(name);
		}

		try {
<<<<<<< HEAD
			Class<?> c = ClassLoader.getSystemClassLoader().loadClass(name);
=======
			Class<?> c = InstrumentingClassLoader.getInstance().loadClass(name);
>>>>>>> 1a83c1cae7760c85ac9e11a6cf8ae9a9cdabe819
			store.put(name, c);
			return c;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
