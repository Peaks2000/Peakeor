/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.compat;

import com.mojang.renderpearl.api.pipeline.ShaderType;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PipelineReloadCompatibilityTest {
    private ClassNode readClass(String name) throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(stream, name);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return node;
        }
    }

    @Test
    void pipelineShaderIdsResolveThroughTheNativeResourceConverter() throws Exception {
        ClassNode pipelines = readClass("peakeordevelopment/peakeorclient/renderer/PeakeorRenderPipelines");
        int checked = 0;
        Set<String> locations = new HashSet<>();
        for (var method : pipelines.methods) {
            for (var instruction : method.instructions) {
                if (!(instruction instanceof MethodInsnNode call)) continue;
                assertFalse(call.name.equals("join") || call.name.equals("compilePipeline"), "Custom pipeline compilation must use the native reload stages");
                if (call.name.equals("withLocation")) {
                    var literal = (LdcInsnNode) call.getPrevious().getPrevious();
                    assertTrue(locations.add((String) literal.cst), "Pipeline locations must be unique");
                }
                if (!call.name.equals("withVertexShader") && !call.name.equals("withFragmentShader")) continue;
                var literal = (LdcInsnNode) call.getPrevious().getPrevious();
                ShaderType type = call.name.equals("withVertexShader") ? ShaderType.VERTEX : ShaderType.FRAGMENT;
                Identifier file = type.idConverter().idToFile(Identifier.fromNamespaceAndPath("peakeor-client", (String) literal.cst));
                try (var source = getClass().getClassLoader().getResourceAsStream("assets/" + file.getNamespace() + "/" + file.getPath())) {
                    assertNotNull(source, file.toString());
                }
                checked++;
            }
        }
        assertEquals(locations.size() * 2, checked);
        assertTrue(checked > 20);
    }

    @Test
    void nativeReloadStillCollectsRequiredPipelines() throws Exception {
        ClassNode manager = readClass("net/minecraft/client/renderer/ShaderManager");
        assertTrue(manager.methods.stream().anyMatch(method -> {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("net/minecraft/client/renderer/RenderPipelines") && call.name.equals("requiredPipelines")) return true;
            }
            return false;
        }));
        ClassNode hook = readClass("peakeordevelopment/peakeorclient/mixin/RenderPipelinesMixin");
        assertTrue(hook.methods.stream().anyMatch(method -> method.desc.equals("(Ljava/util/List;)Ljava/util/List;")));
    }
}
