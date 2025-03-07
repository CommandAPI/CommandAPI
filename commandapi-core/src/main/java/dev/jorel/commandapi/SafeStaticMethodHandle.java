package dev.jorel.commandapi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class SafeStaticMethodHandle<ReturnType> {
	private MethodHandle handle;

	private SafeStaticMethodHandle(MethodHandle handle) {
		this.handle = handle;
	}

	private static <ReturnType> SafeStaticMethodHandle<ReturnType> of(
		Class<?> classType,
		String methodName, String mojangMappedMethodName,
		Class<? super ReturnType> returnType,
		Class<?>... parameterTypes
	) throws ReflectiveOperationException {
		return new SafeStaticMethodHandle<>(
			MethodHandles.privateLookupIn(classType, MethodHandles.lookup())
				.findStatic(
					classType, SafeVarHandle.USING_MOJANG_MAPPINGS ? mojangMappedMethodName : methodName,
					MethodType.methodType(returnType, parameterTypes)
				)
		);
	}

	public static <ReturnType> SafeStaticMethodHandle<ReturnType> ofOrNull(
		Class<?> classType,
		String methodName, String mojangMappedMethodName,
		Class<? super ReturnType> returnType,
		Class<?>... parameterTypes
	) {
		try {
			return of(classType, methodName, mojangMappedMethodName, returnType, parameterTypes);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ReturnType invoke(Object... args) throws Throwable {
		return (ReturnType) handle.invokeWithArguments(args);
	}

	public ReturnType invokeOrNull(Object... args) {
		try {
			return invoke(args);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
}
