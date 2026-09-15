/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.sign;

import com.mojang.serialization.DataResult;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.storage.TagValueInput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaSignTextCompatibilityTest {
    @BeforeAll
    static void bootstrapVanilla() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void normalFrontAndBackTextUseVanillaNbtCodec() {
        SignBlockEntity sign = loadSign(signTag(
            lines(Component.literal("front one"), Component.literal("front two"), Component.empty(), Component.literal("front four")),
            lines(Component.literal("back one"), Component.empty(), Component.literal("back three"), Component.empty())
        ), new ProblemReporter.Collector());

        assertEquals("front one", sign.getText(SignTextSlot.FRONT).getMessages(false).get(0).getString());
        assertEquals("front two", sign.getText(SignTextSlot.FRONT).getMessages(false).get(1).getString());
        assertEquals("back one", sign.getText(SignTextSlot.BACK).getMessages(false).get(0).getString());
        assertEquals("back three", sign.getText(SignTextSlot.BACK).getMessages(false).get(2).getString());
    }

    @Test
    void translatedTextRemainsAComponentAndUsesVanillaLanguageResolution() {
        Component translated = Component.translatable("block.minecraft.oak_sign");
        SignBlockEntity sign = loadSign(signTag(lines(translated), emptyLines()), new ProblemReporter.Collector());
        Component loaded = sign.getText(SignTextSlot.FRONT).getMessages(false).get(0);

        TranslatableContents contents = assertInstanceOf(TranslatableContents.class, loaded.getContents());
        assertEquals("block.minecraft.oak_sign", contents.getKey());
        assertEquals("Oak Sign", loaded.getString());
    }

    @Test
    void unknownTranslationKeyRendersAsItsKeyLikeVanilla() {
        String key = "peakeor.test.missing_translation_key";
        SignBlockEntity sign = loadSign(signTag(lines(Component.translatable(key)), emptyLines()), new ProblemReporter.Collector());

        assertEquals(key, sign.getText(SignTextSlot.FRONT).getMessages(false).get(0).getString());
    }

    @Test
    void unresolvableTranslationFormatFallsBackToTheUnformattedTemplate() {
        Component unusual = Component.translatableWithFallback("peakeor.test.missing_format", "value=%q", "ignored");
        SignBlockEntity sign = loadSign(signTag(lines(unusual), emptyLines()), new ProblemReporter.Collector());

        assertEquals("value=%q", sign.getText(SignTextSlot.FRONT).getMessages(false).get(0).getString());
    }

    @Test
    void malformedFrontTextFallsBackToFourEmptyLinesWithoutAffectingBackText() {
        CompoundTag root = signTag(emptyLines(), lines(Component.literal("valid back")));
        CompoundTag malformed = new CompoundTag();
        malformed.put("messages", new ListTag());
        root.put("front_text", malformed);
        ProblemReporter.Collector problems = new ProblemReporter.Collector();

        SignBlockEntity sign = loadSign(root, problems);

        assertTrue(sign.getText(SignTextSlot.FRONT).getMessages(false).stream().allMatch(component -> component.getString().isEmpty()));
        assertEquals("valid back", sign.getText(SignTextSlot.BACK).getMessages(false).get(0).getString());
        assertFalse(problems.isEmpty());
    }

    @Test
    void malformedComponentMakesVanillaRejectThatSignTextPayload() {
        CompoundTag root = signTag(emptyLines(), lines(Component.literal("valid back")));
        CompoundTag malformed = encode(SignText.EMPTY);
        malformed.getListOrEmpty("messages").set(0, IntTag.valueOf(42));
        root.put("front_text", malformed);
        ProblemReporter.Collector problems = new ProblemReporter.Collector();

        SignBlockEntity sign = loadSign(root, problems);

        assertTrue(sign.getText(SignTextSlot.FRONT).getMessages(false).stream().allMatch(component -> component.getString().isEmpty()));
        assertEquals("valid back", sign.getText(SignTextSlot.BACK).getMessages(false).get(0).getString());
        assertFalse(problems.isEmpty());
    }

    @Test
    void nbtBackedComponentStaysUnresolvedOnTheVanillaClient() {
        CompoundTag encodedComponent = new CompoundTag();
        encodedComponent.putString("nbt", "CustomName");
        encodedComponent.putString("block", "~ ~ ~");
        Component nbtComponent = ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, encodedComponent).getOrThrow();

        SignBlockEntity sign = loadSign(signTag(lines(nbtComponent), emptyLines()), new ProblemReporter.Collector());
        Component loaded = sign.getText(SignTextSlot.FRONT).getMessages(false).get(0);

        assertInstanceOf(NbtContents.class, loaded.getContents());
        assertEquals("", loaded.getString());
    }

    @Test
    void absentTextDefaultsToFourEmptyLinesOnBothSides() {
        SignBlockEntity sign = loadSign(new CompoundTag(), new ProblemReporter.Collector());

        assertTrue(sign.getText(SignTextSlot.FRONT).getMessages(false).stream().allMatch(component -> component.getString().isEmpty()));
        assertTrue(sign.getText(SignTextSlot.BACK).getMessages(false).stream().allMatch(component -> component.getString().isEmpty()));
    }

    @Test
    void oversizedButValidComponentUsesTheVanillaCodecWithoutCustomTruncation() {
        String oversized = "x".repeat(50_000);
        SignBlockEntity sign = loadSign(signTag(lines(Component.literal(oversized)), emptyLines()), new ProblemReporter.Collector());

        assertEquals(oversized, sign.getText(SignTextSlot.FRONT).getMessages(false).get(0).getString());
    }

    @Test
    void signEditorPacketKeepsVanillaPlainTextAndSelectedSide() {
        String translatedLine = Component.translatable("block.minecraft.oak_sign").getString();
        ServerboundSignUpdatePacket front = new ServerboundSignUpdatePacket(BlockPos.ZERO, List.of(translatedLine, "", "", ""), SignTextSlot.FRONT);
        ServerboundSignUpdatePacket back = new ServerboundSignUpdatePacket(BlockPos.ZERO, List.of(translatedLine, "", "", ""), SignTextSlot.BACK);

        assertTrue(front.slot() == SignTextSlot.FRONT);
        assertFalse(back.slot() == SignTextSlot.FRONT);
        assertEquals("Oak Sign", front.lines().get(0));
        assertEquals("Oak Sign", back.lines().get(0));
    }

    private static SignBlockEntity loadSign(CompoundTag tag, ProblemReporter reporter) {
        SignBlockEntity sign = new SignBlockEntity(BlockPos.ZERO, Blocks.OAK_SIGN.defaultBlockState());
        sign.loadWithComponents(TagValueInput.create(reporter, RegistryAccess.EMPTY, tag));
        return sign;
    }

    private static CompoundTag signTag(Component[] front, Component[] back) {
        CompoundTag root = new CompoundTag();
        root.put("front_text", encode(new SignText(Arrays.asList(front), Arrays.asList(front), net.minecraft.world.item.DyeColor.BLACK, false)));
        root.put("back_text", encode(new SignText(Arrays.asList(back), Arrays.asList(back), net.minecraft.world.item.DyeColor.BLACK, false)));
        return root;
    }

    private static CompoundTag encode(SignText text) {
        DataResult<Tag> encoded = SignText.CODEC.encodeStart(NbtOps.INSTANCE, text);
        return assertInstanceOf(CompoundTag.class, encoded.getOrThrow());
    }

    private static Component[] emptyLines() {
        return lines();
    }

    private static Component[] lines(Component... provided) {
        Component[] lines = {Component.empty(), Component.empty(), Component.empty(), Component.empty()};
        System.arraycopy(provided, 0, lines, 0, provided.length);
        return lines;
    }
}
