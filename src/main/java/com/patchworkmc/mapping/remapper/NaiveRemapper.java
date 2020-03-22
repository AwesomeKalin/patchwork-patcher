package com.patchworkmc.mapping.remapper;

import java.util.HashMap;

import net.fabricmc.tinyremapper.IMappingProvider;

import com.patchworkmc.Patchwork;

/**
 * Remaps classes, methods, and fields based on the assumption that names are never repeated in the source mappings,
 * or that they duplicate directly in the target mappings. (i.e. 'valueOf' -> 'valueOf')
 *
 * <p>Mappings that are duplicated will be ignored after their first entry.</p>
 */
public class NaiveRemapper {
	private final HashMap<String, String> classes = new HashMap<>();
	private final HashMap<String, String> methods = new HashMap<>();
	private final HashMap<String, String> fields = new HashMap<>();

	public NaiveRemapper(IMappingProvider mappings) {
		mappings.load(new IMappingProvider.MappingAcceptor() {
			@Override
			public void acceptClass(String srcName, String dstName) {
				if (classes.get(srcName) == null) {
					classes.put(srcName, dstName);
				} else {
					throw new IllegalArgumentException("Duplicated class name " + srcName);
				}
			}

			@Override
			public void acceptMethod(IMappingProvider.Member method, String dstName) {
				if (!method.name.startsWith("func_")) {
					return;
				}

				// Have to include some exceptions
				String presentName = methods.get(method.name);

				if (presentName == null) {
					methods.put(method.name, dstName);
				} else if (!presentName.equals(dstName)) {
					Patchwork.LOGGER.debug("Duplicated method mapping for %s (proposed %s, but key already mapped to %s!)", method.name, dstName, presentName);
				}
			}

			@Override
			public void acceptMethodArg(IMappingProvider.Member method, int lvIndex, String dstName) {
				// NO-OP
			}

			@Override
			public void acceptMethodVar(IMappingProvider.Member method, int lvIndex, int startOpIdx, int asmIndex, String dstName) {
				// NO-OP
			}

			@Override
			public void acceptField(IMappingProvider.Member field, String dstName) {
				if (!field.name.startsWith("field_")) {
					return;
				}

				String presentName = fields.get(field.name);

				if (presentName == null) {
					fields.put(field.name, dstName);
				} else if (!presentName.equals(dstName)) {
					throw new IllegalArgumentException(String.format("Duplicated field mapping for %s (proposed %s, but key already mapped to %s!)", field.name, dstName, presentName));
				}
			}
		});
	}

	public String getClass(String volde) {
		return classes.getOrDefault(volde, volde);
	}

	public String getMethod(String volde) {
		if (!volde.startsWith("func_")) {
			throw new IllegalArgumentException("Cannot remap methods not starting with func_: " + volde);
		}

		return methods.getOrDefault(volde, volde);
	}

	public String getField(String volde) {
		if (!volde.startsWith("field_")) {
			throw new IllegalArgumentException("Cannot remap fields not starting with field_: " + volde);
		}

		return fields.getOrDefault(volde, volde);
	}
}
