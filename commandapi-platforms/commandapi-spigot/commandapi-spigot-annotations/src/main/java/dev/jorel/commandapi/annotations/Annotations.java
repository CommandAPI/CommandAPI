/*******************************************************************************
 * Copyright 2018, 2021 Jorel Ali (Skepter) - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package dev.jorel.commandapi.annotations;

import com.google.auto.service.AutoService;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.annotations.arguments.AAdvancementArgument;
import dev.jorel.commandapi.annotations.arguments.AAngleArgument;
import dev.jorel.commandapi.annotations.arguments.AAsyncPlayerProfileArgument;
import dev.jorel.commandapi.annotations.arguments.AAxisArgument;
import dev.jorel.commandapi.annotations.arguments.ABiomeArgument;
import dev.jorel.commandapi.annotations.arguments.ABlockPredicateArgument;
import dev.jorel.commandapi.annotations.arguments.ABlockStateArgument;
import dev.jorel.commandapi.annotations.arguments.ABooleanArgument;
import dev.jorel.commandapi.annotations.arguments.AChatArgument;
import dev.jorel.commandapi.annotations.arguments.AChatColorArgument;
import dev.jorel.commandapi.annotations.arguments.AChatComponentArgument;
import dev.jorel.commandapi.annotations.arguments.ADoubleArgument;
import dev.jorel.commandapi.annotations.arguments.AEnchantmentArgument;
import dev.jorel.commandapi.annotations.arguments.AEntitySelectorArgument;
import dev.jorel.commandapi.annotations.arguments.AEntityTypeArgument;
import dev.jorel.commandapi.annotations.arguments.AFloatArgument;
import dev.jorel.commandapi.annotations.arguments.ADoubleRangeArgument;
import dev.jorel.commandapi.annotations.arguments.AFunctionArgument;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import dev.jorel.commandapi.annotations.arguments.AIntegerArgument;
import dev.jorel.commandapi.annotations.arguments.AIntegerRangeArgument;
import dev.jorel.commandapi.annotations.arguments.AItemStackArgument;
import dev.jorel.commandapi.annotations.arguments.AItemStackPredicateArgument;
import dev.jorel.commandapi.annotations.arguments.ALiteralArgument;
import dev.jorel.commandapi.annotations.arguments.ALocation2DArgument;
import dev.jorel.commandapi.annotations.arguments.ALocationArgument;
import dev.jorel.commandapi.annotations.arguments.ALongArgument;
import dev.jorel.commandapi.annotations.arguments.ALootTableArgument;
import dev.jorel.commandapi.annotations.arguments.AMathOperationArgument;
import dev.jorel.commandapi.annotations.arguments.AMultiLiteralArgument;
import dev.jorel.commandapi.annotations.arguments.ANBTCompoundArgument;
import dev.jorel.commandapi.annotations.arguments.ANamespacedKeyArgument;
import dev.jorel.commandapi.annotations.arguments.AObjectiveArgument;
import dev.jorel.commandapi.annotations.arguments.AObjectiveCriteriaArgument;
import dev.jorel.commandapi.annotations.arguments.AParticleArgument;
import dev.jorel.commandapi.annotations.arguments.APlayerProfileArgument;
import dev.jorel.commandapi.annotations.arguments.APotionEffectArgument;
import dev.jorel.commandapi.annotations.arguments.ARecipeArgument;
import dev.jorel.commandapi.annotations.arguments.ARotationArgument;
import dev.jorel.commandapi.annotations.arguments.AScoreHolderArgument;
import dev.jorel.commandapi.annotations.arguments.AScoreboardSlotArgument;
import dev.jorel.commandapi.annotations.arguments.ASoundArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import dev.jorel.commandapi.annotations.arguments.ATeamArgument;
import dev.jorel.commandapi.annotations.arguments.ATextArgument;
import dev.jorel.commandapi.annotations.arguments.ATimeArgument;
import dev.jorel.commandapi.annotations.arguments.AUUIDArgument;
import dev.jorel.commandapi.annotations.arguments.AWorldArgument;
import dev.jorel.commandapi.annotations.arguments.Primitive;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main annotation processor for annotation-based arguments
 */
@AutoService(Processor.class)
public class Annotations extends AbstractProcessor {

	private static final Class<?>[] ARGUMENT_ANNOTATIONS = new Class[] { AAdvancementArgument.class,
		AAngleArgument.class, AAxisArgument.class, AAsyncPlayerProfileArgument.class, ABiomeArgument.class, ABlockPredicateArgument.class,
		ABlockStateArgument.class, ABooleanArgument.class, AChatArgument.class,
		AChatColorArgument.class, AChatComponentArgument.class, ADoubleArgument.class,
		AEnchantmentArgument.class, AEntitySelectorArgument.ManyEntities.class, AEntitySelectorArgument.ManyPlayers.class,
		AEntitySelectorArgument.OneEntity.class, AEntitySelectorArgument.OnePlayer.class, AEntityTypeArgument.class,
		AFloatArgument.class, ADoubleRangeArgument.class, AFunctionArgument.class,
		AGreedyStringArgument.class, AIntegerArgument.class, AIntegerRangeArgument.class, AItemStackArgument.class,
		AItemStackPredicateArgument.class, ALiteralArgument.class, ALocation2DArgument.class,
		ALocationArgument.class, ALongArgument.class, ALootTableArgument.class, AMathOperationArgument.class,
		AMultiLiteralArgument.class, ANamespacedKeyArgument.class, ANBTCompoundArgument.class, AObjectiveArgument.class,
		AObjectiveCriteriaArgument.class, AParticleArgument.class, APlayerProfileArgument.class,
		APotionEffectArgument.class, ARecipeArgument.class, ARotationArgument.class, AScoreboardSlotArgument.class,
		AScoreHolderArgument.Single.class, AScoreHolderArgument.Multiple.class, ASoundArgument.class, AStringArgument.class, ATeamArgument.class,
		ATextArgument.class, ATimeArgument.class, AUUIDArgument.class, AWorldArgument.class };

	// List of stuff we can deal with
	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Stream
				.concat(Arrays.stream(new Class<?>[] { Alias.class, Command.class, Default.class, NeedsOp.class,
						Permission.class, Subcommand.class, Help.class }), Arrays.stream(ARGUMENT_ANNOTATIONS))
				.map(Class::getCanonicalName).collect(Collectors.toSet());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getElementsAnnotatedWith(Command.class)) {
			try {
				processCommand(element);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	// Indentation, because half of this file is actually just making stuff look nice
	private String indent(int indent) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			builder.append("    ");
		}
		return builder.toString();
	}
	
	private String simpleFromQualified(String name) {
		if(name == null) {
			return null;
		}
		return name.split("\\.")[name.split("\\.").length - 1];
	}
	
	private SortedSet<String> calculateImports(Element classElement) {
		SortedSet<String> imports = new TreeSet<>();
		
		imports.add(CommandAPICommand.class.getCanonicalName());
		if(classElement.getAnnotation(NeedsOp.class) != null) {
			imports.add(CommandPermission.class.getCanonicalName());
		}
		for (Element methodElement : classElement.getEnclosedElements()) {
			if (methodElement.getAnnotation(Subcommand.class) != null) {
				imports.add(MultiLiteralArgument.class.getCanonicalName());
			}
			if (methodElement.getAnnotation(NeedsOp.class) != null) {
				imports.add(CommandPermission.class.getCanonicalName());
			}
			
			if(methodElement instanceof ExecutableElement method) {
				for(VariableElement parameter : method.getParameters()) {
					Annotation argument = getArgument(parameter);
					if(argument != null) {
						
						if (argument instanceof ANBTCompoundArgument) {
							// Get NBTCompoundArgument type from type mirror
							imports.add(parameter.asType().toString());
						} else {
							imports.addAll(Arrays.asList(getPrimitive(argument).value()));
						}

						if(argument.annotationType().getEnclosingClass() == null) {
							// Normal arguments
							imports.add("dev.jorel.commandapi.arguments." + argument.annotationType().getSimpleName().substring(1));
						} else {
							// Nested arguments, like EntitySelectorArgument
							imports.add("dev.jorel.commandapi.arguments." + argument.annotationType().getEnclosingClass().getSimpleName().substring(1));
						}
						
						if(argument instanceof ALocationArgument || argument instanceof ALocation2DArgument) {
							imports.add(LocationType.class.getCanonicalName());
						}
					}
					
				}
			}
		}
		
		for(String import_ : new TreeSet<>(imports)) {
			if(import_.contains("<")) {
				imports.add(import_.substring(0, import_.indexOf("<")));
				imports.add(import_.substring(import_.indexOf("<") + 1, import_.indexOf(">")));
			}
		}
		
		return imports;
	}
	
	private void emitImports(PrintWriter out, Element classElement) {
		String previousImport = "";
		for(String import_ : calculateImports(classElement)) {
			// Separate different packages
			if(previousImport.contains(".") && import_.contains(".") &&
				!previousImport.substring(0, previousImport.indexOf(".")).equals(import_.substring(0, import_.indexOf(".")))) {
					out.println();
			}
			// Don't import stuff like "String"
			if(!import_.contains(".") || import_.contains("<")) {
				continue;
			}
			
			out.print("import ");
			out.print(import_);
			out.println(";");
			previousImport = import_;
		}
		out.println();
	}
	
	// (https://www.baeldung.com/java-annotation-processing-builder)
	private void emitPackage(PrintWriter out, TypeElement commandClass) {
		int lastDot = commandClass.getQualifiedName().toString().lastIndexOf('.');
	    if (lastDot > 0) {
	    	out.print("package ");
			out.print(commandClass.getQualifiedName().toString().substring(0, lastDot));
			out.println(";");
			out.println();
	    }
	}
	
	private int emitSubcommand(PrintWriter out, Element methodElement, int indent) {
		if (methodElement.getAnnotation(Subcommand.class) != null) {
			out.println(indent(indent) + ".withArguments(");
			indent++;
			out.print(indent(indent) + "new MultiLiteralArgument(\"subcommand\", ");
			
			if(methodElement.getAnnotation(Subcommand.class).value().length == 0) {
				processingEnv.getMessager().printMessage(Kind.ERROR, "Invalid @Subcommand on " + methodElement.getSimpleName() + " - no subcommand name was found");
			}
			
			// @Subcommand (name)
			out.print(Arrays.stream(methodElement.getAnnotation(Subcommand.class).value())
				.map(x -> "\"" + x + "\"").collect(Collectors.joining(", ")));
			
			out.println(")");
			indent++;
			out.println(indent(indent) + ".setListed(false)");
			
			// @NeedsOp
			if (methodElement.getAnnotation(NeedsOp.class) != null) {
				out.println(indent(indent) + ".withPermission(CommandPermission.OP)");
			}
			
			// @Permission
			if (methodElement.getAnnotation(Permission.class) != null) {
				out.print(indent(indent) + ".withPermission(\"");
				out.print(methodElement.getAnnotation(Permission.class).value());
				out.println("\")");
			}
			indent--;
			indent--;
			out.println(indent(indent) + ")");
		}
		return indent;
	}
	
	private <T extends Annotation> Map<Integer, String> emitArgumentsAndGenerateArgumentMapping(PrintWriter out, Element methodElement, int indent) throws IllegalArgumentException {
		Map<Integer, String> argumentMapping = new HashMap<>();
		
		ExecutableElement executableMethodElement = (ExecutableElement) methodElement;
		for(int i = 1; i < executableMethodElement.getParameters().size(); i++) {
			VariableElement parameter = executableMethodElement.getParameters().get(i);
			T argumentAnnotation = getArgument(parameter);
			if (argumentAnnotation == null) {
				processingEnv.getMessager().printMessage(Kind.ERROR,
						"Parameter " + parameter.getSimpleName() + " in method "
								+ methodElement.getSimpleName()
								+ " does not have an argument annotation on it! ");
				throw new IllegalArgumentException();
			}
			
			emitArgument(out, argumentAnnotation, parameter, indent);
			
			// Handle return types
			Primitive primitive = getPrimitive(argumentAnnotation);
			if (argumentAnnotation instanceof ANBTCompoundArgument) {
				argumentMapping.put(i - 1, parameter.asType().toString());
			} else {
				if(primitive.value().length == 1) {
					argumentMapping.put(i - 1, primitive.value()[0]);
				}
			}
		}
		
		return argumentMapping;
	}
	
	private int emitExecutes(PrintWriter out, Map<Integer, String> argumentMapping, ExecutableType methodType, TypeElement commandClass, Element methodElement, int indent) {
		String[] firstParam = methodType.getParameterTypes().get(0).toString().split("\\.");
		out.print(indent(indent));
		switch (firstParam[firstParam.length - 1]) {
		case "Player" -> out.print(".executesPlayer");
		case "ConsoleCommandSender" -> out.print(".executesConsole");
		case "BlockCommandSender" -> out.print(".executesCommandBlock");
		case "ProxiedCommandSender" -> out.print(".executesProxy");
		case "NativeProxyCommandSender" -> out.print(".executesNative");
		case "Entity" -> out.print(".executesEntity");
		case "CommandSender" -> out.print(".executes");
		default -> out.print(".executes");
		}

		out.println("((sender, args) -> {");
		indent++;
		out.print(indent(indent));

		// Return int or void?
		if (methodType.getReturnType().toString().equals("int")) {
			out.print("return ");
		}

		out.print(commandClass.getSimpleName());
		out.print(".");
		out.print(methodElement.getSimpleName());
		out.print("(sender");
		
		for(int i = 0; i < argumentMapping.size(); i++) {
			String fromArgumentMap = argumentMapping.get(i);
			out.print(", (");

			if(fromArgumentMap.contains("<")) {
				out.print(simpleFromQualified(fromArgumentMap.substring(0, fromArgumentMap.indexOf("<"))));
				out.print("<");
				out.print(simpleFromQualified(fromArgumentMap.substring(fromArgumentMap.indexOf("<") + 1, fromArgumentMap.indexOf(">"))));
				out.print(">");
			} else {
				out.print(simpleFromQualified(fromArgumentMap));
			}
			out.print(") args.get(");
			out.print(i);
			out.print(")");
		}
		//populate stuff here
		
		out.println(");");
		indent--;
		out.println(indent(indent) + "})");
		
		return indent;
	}
	
	private int emitClassDeclarationStart(PrintWriter out, TypeElement commandClass, int indent) {
		out.println("// This class was automatically generated by the CommandAPI");
		out.print("public class ");
		out.print(commandClass.getSimpleName() + "$Command");
		out.println(" {");
		out.println();
		indent++;
		return indent;
	}

	private void emitClassPermission(PrintWriter out, Element classElement, int indent) {
		if (classElement.getAnnotation(Permission.class) == null)
			return;

		out.print(indent(indent) + ".withPermission(\"");
		out.print(classElement.getAnnotation(Permission.class).value());
		out.println("\")");
	}

	private boolean methodPermissionAnnotationExists(Element methodElement) {
		return methodElement.getAnnotation(Permission.class) != null;
	}

	private void emitMethodPermission(PrintWriter out, Element methodElement, int indent) {
		out.print(indent(indent) + ".withPermission(\"");
		out.print(methodElement.getAnnotation(Permission.class).value());
		out.println("\")");
	}
	
	private void emitAlias(PrintWriter out, Element classElement, int indent) {
		if (classElement.getAnnotation(Alias.class) != null) {
			out.print(indent(indent) + ".withAliases(");
			out.print(Arrays.stream(classElement.getAnnotation(Alias.class).value())
					.map(x -> "\"" + x + "\"").collect(Collectors.joining(", ")));
			out.println(")");
		}
	}
	
	private void emitNeedsOp(PrintWriter out, Element classElement, int indent) {
		if (classElement.getAnnotation(NeedsOp.class) != null) {
			out.println(indent(indent) + ".withPermission(CommandPermission.OP)");
		}
	}
	
	private void emitHelp(PrintWriter out, Element classElement, int indent) {
		if (classElement.getAnnotation(Help.class) != null) {
			Help helpAnnotation = classElement.getAnnotation(Help.class);
			
			if(helpAnnotation.shortDescription().isEmpty()) {
				out.print(indent(indent) + ".withFullDescription(\"");
				out.print(helpAnnotation.value());
				out.println("\")");
			} else {
				out.print(indent(indent) + ".withHelp(\"");
				out.print(helpAnnotation.shortDescription());
				out.print("\", \"");
				out.print(helpAnnotation.value());
				out.println("\")");
			}
		}
	}

	private void processCommand(Element classElement) throws IOException {
		TypeElement commandClass = (TypeElement) classElement;
		JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(commandClass.getQualifiedName() + "$Command");
		int indent = 0;

		try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
			emitPackage(out, commandClass); // Package name
			emitImports(out, classElement); // Imports	
			emitClassDeclarationStart(out, commandClass, indent); // Class declaration

			// Main registration method
			out.println(indent(indent) + "@SuppressWarnings(\"unchecked\")");
			out.println(indent(indent) + "public static void register() {");
			out.println();
			indent++;

			for (Element methodElement : classElement.getEnclosedElements()) {
				if (methodElement.getAnnotation(Default.class) != null
						|| methodElement.getAnnotation(Subcommand.class) != null) {
					
					ExecutableType methodType = (ExecutableType) methodElement.asType();
					if(!methodElement.getModifiers().contains(Modifier.STATIC)) {
						processingEnv.getMessager().printMessage(Kind.ERROR, "Method " + methodElement.getSimpleName() + " must be static to be used as a command");
					}

					out.print(indent(indent) + "new CommandAPICommand(\"");
					out.print(commandClass.getAnnotation(Command.class).value());
					out.println("\")");
					indent++;

					indent = emitSubcommand(out, methodElement, indent);  // @Subcommand (Also handle @Alias for @Subcommand)
					emitNeedsOp(out, classElement, indent);               // @NeedsOp
					if (methodPermissionAnnotationExists(methodElement))
						emitMethodPermission(out, methodElement, indent); // @Permission provided by method
					else
						emitClassPermission(out, classElement, indent);   // @Permission provided by class (only if method does not have Permission annotation)
					emitAlias(out, classElement, indent);                 // @Alias
					emitHelp(out, classElement, indent);                  // @Help
					
					//Maps parameter index to argument's primitive type
					Map<Integer, String> argumentMapping = null;
					try {
						argumentMapping = emitArgumentsAndGenerateArgumentMapping(out, methodElement, indent);
					} catch (IllegalArgumentException e) {
						return;
					}
					
					// .executes
					indent = emitExecutes(out, argumentMapping, methodType, commandClass, methodElement, indent);

					// Register command
					out.println(indent(indent) + ".register();");
					out.println();
					indent--;
				}
			}
			out.println(indent(indent) + "}"); // register()
			indent--;
			out.println();
			out.println("}"); // $Command class
		}
	}

	private <T extends Annotation> void emitArgument(PrintWriter out, T argumentAnnotation, VariableElement parameter, int indent) {
		out.print(indent(indent) + ".withArguments(new ");
		// We're assuming that the name of the argument MUST be "A" + the same name
		if(argumentAnnotation.annotationType().getEnclosingClass() == null) {
			// Normal arguments
			out.print(argumentAnnotation.annotationType().getSimpleName().substring(1));
		} else {
			// Nested arguments, like EntitySelectorArgument
			out.print(argumentAnnotation.annotationType().getEnclosingClass().getSimpleName().substring(1));
			out.print(".");
			out.print(argumentAnnotation.annotationType().getSimpleName());
		}
		
		// Node name
		out.print("(\"");
		out.print(parameter.getSimpleName());
		out.print("\"");
		
		// Handle parameters
		// Number arguments
		if(argumentAnnotation instanceof AIntegerArgument argument) {
			out.print(", " + argument.min() + ", " + argument.max());
		} else if(argumentAnnotation instanceof ALongArgument argument) {
			out.print(", " + argument.min() + "L, " + argument.max() + "L");
		} else if(argumentAnnotation instanceof AFloatArgument argument) {
			out.print(", " + argument.min() + "F, " + argument.max() + "F");
		} else if(argumentAnnotation instanceof ADoubleArgument argument) {
			out.print(", " + argument.min() + "D, " + argument.max() + "D");
		}
		
		// Non-number arguments
		else if(argumentAnnotation instanceof ALocation2DArgument argument) {
			out.print(", " + LocationType.class.getSimpleName() + "." + argument.value().toString());
		} else if(argumentAnnotation instanceof ALocationArgument argument) {
			out.print(", " + LocationType.class.getSimpleName() + "." + argument.value().toString());
		} else if(argumentAnnotation instanceof AMultiLiteralArgument argument) {
			out.print(", " + Arrays.stream(argument.value()).map(s -> "\"" + s + "\"").collect(Collectors.joining(", ")));
		} else if(argumentAnnotation instanceof ALiteralArgument argument) {
			out.print(", \"");
			out.print(argument.value());
			out.print("\"");
		}
		
		out.print(")");
		
		if(argumentAnnotation instanceof ALiteralArgument) {
			out.print(".setListed(true)");
		}
		
		out.println(")");
	}

	// Checks if an annotation mirror is an argument annotation
	private boolean isArgument(AnnotationMirror mirror) {
		final String mirrorName = mirror.getAnnotationType().toString();
		return Arrays.stream(ARGUMENT_ANNOTATIONS).map(Class::getCanonicalName).anyMatch(mirrorName::equals);
	}

	// Get the Primitive annotation from an annotation
	private <T extends Annotation> Primitive getPrimitive(T annotation) {
		return annotation.annotationType().getDeclaredAnnotation(Primitive.class);
	}

	@SuppressWarnings("unchecked")
	private <T extends Annotation> T getArgument(VariableElement tMirror) {
		for(AnnotationMirror mirror : tMirror.getAnnotationMirrors()) {
			if(isArgument(mirror)) {
				T argumentAnnotation = null;
				String mirrorCanonicalName = mirror.getAnnotationType().toString();
				try {
					argumentAnnotation = tMirror.getAnnotationsByType((Class<T>) Class.forName(mirrorCanonicalName))[0];
				} catch (ClassNotFoundException e) {
					// We might be in a nested class. Let's try accessing that
					try {
						// Replace final . with $
						mirrorCanonicalName = mirrorCanonicalName.substring(0, mirrorCanonicalName.lastIndexOf(".")) + "$" + mirrorCanonicalName.substring(mirrorCanonicalName.lastIndexOf(".") + 1);
						argumentAnnotation = tMirror.getAnnotationsByType((Class<T>) Class.forName(mirrorCanonicalName))[0];
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
				}
				
				return argumentAnnotation;
			}
		}
		return null;
	}

}
