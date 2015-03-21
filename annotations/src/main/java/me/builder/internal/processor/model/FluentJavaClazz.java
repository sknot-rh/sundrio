package me.builder.internal.processor.model;

import me.builder.Nested;
import me.codegen.model.JavaClazz;
import me.codegen.model.JavaMethod;
import me.codegen.model.JavaProperty;
import me.codegen.model.JavaType;
import me.codegen.model.JavaTypeBuilder;
import me.codegen.Method;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class FluentJavaClazz extends JavaClazz {

    public static FluentJavaClazz wrap(JavaClazz javaClazz) {
        return new FluentJavaClazz(javaClazz.getType(), javaClazz.getConstructor(), javaClazz.getMethods(), javaClazz.getFields(), javaClazz.getImports(), javaClazz.getAttributes());

    }

    public FluentJavaClazz(JavaType type,JavaMethod constructor, Set<Method<JavaType, JavaProperty>> methods, Set<JavaProperty> fields, Set<JavaType> imports, Map<String, Object> attributes) {
        super(type, constructor, methods, fields, imports, attributes);
    }

    @Override
    public Set<JavaType> getImports() {
        Set<JavaType> result = new CopyOnWriteArraySet<>();
        Set<JavaType> tmp = new CopyOnWriteArraySet<>();
        tmp.addAll(super.getImports());
        tmp.addAll(getReferencedTypes());
        
        for (JavaProperty property : getFields()) {
            if (property.isArray()) {
                result.add(new JavaTypeBuilder()
                        .withPackageName(List.class.getPackage().getName())
                        .withClassName(List.class.getSimpleName())
                        .build());

                result.add(new JavaTypeBuilder()
                        .withPackageName(ArrayList.class.getPackage().getName())
                        .withClassName(ArrayList.class.getSimpleName())
                        .build());
            }
        }
        
        for (JavaType t : tmp) {
            if (t.getPackageName() != null && !t.getPackageName().equals(getType().getPackageName())  && !t.getPackageName().equals("java.lang")) {
                result.add(t);
            }
        }
        return result;
    }

    private List<JavaType> getReferencedTypes() {
        List<JavaType> types = new ArrayList<>();
        for (JavaProperty field : getFields()) {
            types.add(field.getType());
            types.addAll(Arrays.asList(field.getType().getGenericTypes()));
            
            if (field.getType().getDefaultImplementation() != null) {
                types.add(field.getType().getDefaultImplementation());
            }
            
            boolean buildable = field.getAttributes().containsKey(JavaClazzFactory.BUILDABLE) && (boolean) field.getAttributes().get(JavaClazzFactory.BUILDABLE);
            if (buildable) {
                types.add(new JavaTypeBuilder()
                        .withPackageName(field.getType().getPackageName())
                        .withClassName(field.getType().getClassName() + "Fluent")
                        .build());

                types.add(new JavaTypeBuilder()
                        .withPackageName(field.getType().getPackageName())
                        .withClassName(field.getType().getClassName() + "Builder")
                        .build());

                types.add(new JavaTypeBuilder()
                        .withPackageName(Nested.class.getPackage().getName())
                        .withClassName(Nested.class.getSimpleName())
                        .build());
            }
        }

        for (JavaProperty field : getConstructor().getArguments()) {
            types.add(field.getType());
            types.addAll(Arrays.asList(field.getType().getGenericTypes()));
        }
        
        if (getType().getSuperClass() != null) {
            boolean buildable = getType().getSuperClass().getAttributes().containsKey(JavaClazzFactory.BUILDABLE) && (boolean) getType().getSuperClass().getAttributes().get(JavaClazzFactory.BUILDABLE);
            if (buildable) {
                types.add(new JavaTypeBuilder()
                        .withPackageName(getType().getSuperClass().getPackageName())
                        .withClassName(getType().getSuperClass().getClassName() + "Fluent")
                        .build());
            }
        }
        return types;
    }
}
