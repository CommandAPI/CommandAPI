package dev.jorel.commandapi;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * A wrapper around VarHandle with better type safety using generics and a
 * toggleable underlying implementation depending on whether we're using mojang
 * mappings or non-mojang mappings
 *
 * @param <Type>
 * @param <FieldType>
 */
public class SafeVarHandle<Type, FieldType> {

	public static boolean USING_MOJANG_MAPPINGS = MojangMappedVersionHandler.isMojangMapped();

	private VarHandle handle;

	private SafeVarHandle(VarHandle handle) {
		this.handle = handle;
	}

	public static <Type, FieldType> SafeVarHandle<Type, FieldType> of(Class<? super Type> classType, String fieldName, String mojangMappedFieldName, Class<? super FieldType> fieldType)
		throws ReflectiveOperationException {
		return new SafeVarHandle<>(
			MethodHandles.privateLookupIn(classType, MethodHandles.lookup()).findVarHandle(classType, USING_MOJANG_MAPPINGS ? mojangMappedFieldName : fieldName, fieldType));
	}

	public static <Type, FieldType> SafeVarHandle<Type, FieldType> ofOrNull(Class<? super Type> classType, String fieldName, String mojangMappedFieldName, Class<? super FieldType> fieldType) {
		try {
			return of(classType, fieldName, mojangMappedFieldName, fieldType);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <Type, FieldType> SafeVarHandle<Type, FieldType> ofOrNull(Class<? super Type> classType, String fieldName, Class<? super FieldType> fieldType) {
		return ofOrNull(classType, fieldName, fieldName, fieldType);
	}

	public FieldType get(Type instance) {
		return (FieldType) handle.get(instance);
	}

	public FieldType getStatic() {
		return (FieldType) handle.get(null);
	}

	public void set(Type instance, FieldType param) {
		handle.set(instance, param);
	}
}