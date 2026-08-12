/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.mixininterface;

import com.mojang.authlib.GameProfile;

public interface IGuiMessage {
    String peakeor$getText();

    int peakeor$getId();

    void peakeor$setId(int id);

    GameProfile peakeor$getSender();

    void peakeor$setSender(GameProfile profile);
}
