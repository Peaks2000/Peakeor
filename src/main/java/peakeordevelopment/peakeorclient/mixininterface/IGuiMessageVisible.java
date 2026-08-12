/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixininterface;

public interface IGuiMessageVisible extends IGuiMessage {
    boolean peakeor$isStartOfEntry();

    void peakeor$setStartOfEntry(boolean start);
}
