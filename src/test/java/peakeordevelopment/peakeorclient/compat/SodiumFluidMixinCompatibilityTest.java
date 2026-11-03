/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.compat;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SodiumFluidMixinCompatibilityTest {
    private static final String MIXIN = "peakeordevelopment/peakeorclient/mixin/sodium/SodiumDefaultFluidRendererMixin";
    private static final String TARGET = "net/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer";

    @Test
    void fluidInjectionSelectorsResolveAgainstSupportedSodium() throws IOException {
        ClassNode target = readClass(TARGET);
        int checked = 0;
        for (MethodNode handler : readClass(MIXIN).methods) {
            for (AnnotationNode annotation : annotations(handler)) {
                Object selectors = value(annotation, "method");
                if (!(selectors instanceof List<?> methods)) continue;
                for (Object selector : methods) {
                    resolve(target, (String) selector);
                    checked++;
                }
            }
        }
        assertEquals(7, checked, "Check every fluid-renderer injection, including both visibility hooks");
    }

    @Test
    void returnHooksUseOnlyReturnValueAndTargetArguments() throws IOException {
        ClassNode target = readClass(TARGET);
        int checked = 0;
        for (MethodNode handler : readClass(MIXIN).methods) {
            for (AnnotationNode annotation : annotations(handler)) {
                if (!annotation.desc.equals("Lcom/llamalad7/mixinextras/injector/ModifyReturnValue;")) continue;
                List<?> selectors = (List<?>) value(annotation, "method");
                MethodNode method = resolve(target, (String) selectors.getFirst());
                List<Type> expected = new ArrayList<>();
                expected.add(Type.getReturnType(method.desc));
                expected.addAll(Arrays.asList(Type.getArgumentTypes(method.desc)));
                assertEquals(expected, Arrays.asList(Type.getArgumentTypes(handler.desc)), handler.name);
                assertEquals(Type.getReturnType(method.desc), Type.getReturnType(handler.desc), handler.name);
                // A captured local may be unavailable at one of Sodium's early returns.
                if (handler.invisibleParameterAnnotations != null) {
                    for (List<AnnotationNode> parameter : handler.invisibleParameterAnnotations) {
                        if (parameter == null) continue;
                        assertTrue(parameter.stream().noneMatch(a -> a.desc.equals("Lcom/llamalad7/mixinextras/sugar/Local;")), handler.name);
                    }
                }
                checked++;
            }
        }
        assertEquals(2, checked);
    }

    private static MethodNode resolve(ClassNode target, String selector) {
        int descriptorStart = selector.indexOf('(');
        String name = descriptorStart < 0 ? selector : selector.substring(0, descriptorStart);
        String descriptor = descriptorStart < 0 ? null : selector.substring(descriptorStart);
        return target.methods.stream()
            .filter(method -> method.name.equals(name) && (descriptor == null || method.desc.equals(descriptor)))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Missing Sodium target: " + selector));
    }

    private static ClassNode readClass(String name) throws IOException {
        try (InputStream input = SodiumFluidMixinCompatibilityTest.class.getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(input, name);
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, ClassReader.SKIP_CODE);
            return node;
        }
    }

    private static List<AnnotationNode> annotations(MethodNode method) {
        List<AnnotationNode> result = new ArrayList<>();
        if (method.visibleAnnotations != null) result.addAll(method.visibleAnnotations);
        if (method.invisibleAnnotations != null) result.addAll(method.invisibleAnnotations);
        return result;
    }

    private static Object value(AnnotationNode annotation, String key) {
        if (annotation.values == null) return null;
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (annotation.values.get(i).equals(key)) return annotation.values.get(i + 1);
        }
        return null;
    }
}
