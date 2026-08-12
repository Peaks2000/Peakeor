/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.addons;

import peakeordevelopment.peakeorclient.PeakeorClient;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.util.ArrayList;
import java.util.List;

public class AddonManager {
    public static final List<PeakeorAddon> ADDONS = new ArrayList<>();

    public static void init() {
        // Peakeor pseudo addon
        {
            PeakeorClient.ADDON = new PeakeorAddon() {
                @Override
                public void onInitialize() {}

                @Override
                public String getPackage() {
                    return "peakeordevelopment.peakeorclient";
                }

                @Override
                public String getWebsite() {
                    return "https://github.com/Peaks2000/peakeor-client";
                }

                @Override
                public GithubRepo getRepo() {
                    return new GithubRepo("Peaks2000", "peakeor-client");
                }

                @Override
                public String getCommit() {
                    String commit = PeakeorClient.MOD_META.getCustomValue(PeakeorClient.MOD_ID + ":commit").getAsString();
                    return commit.isEmpty() ? null : commit;
                }
            };

            ModMetadata metadata = FabricLoader.getInstance().getModContainer(PeakeorClient.MOD_ID).get().getMetadata();

            PeakeorClient.ADDON.name = metadata.getName();
            PeakeorClient.ADDON.authors = new String[metadata.getAuthors().size()];
            if (metadata.containsCustomValue(PeakeorClient.MOD_ID + ":color")) {
                PeakeorClient.ADDON.color.parse(metadata.getCustomValue(PeakeorClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                PeakeorClient.ADDON.authors[i++] = author.getName();
            }

            ADDONS.add(PeakeorClient.ADDON);
        }

        // Addons
        for (EntrypointContainer<PeakeorAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("peakeor", PeakeorAddon.class)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            PeakeorAddon addon;
            try {
                addon = entrypoint.getEntrypoint();
            } catch (Throwable throwable) {
                throw new RuntimeException("Exception during addon init \"%s\".".formatted(metadata.getName()), throwable);
            }

            addon.name = metadata.getName();

            if (metadata.getAuthors().isEmpty()) throw new RuntimeException("Addon \"%s\" requires at least 1 author to be defined in it's fabric.mod.json. See https://fabricmc.net/wiki/documentation:fabric_mod_json_spec".formatted(addon.name));
            addon.authors = new String[metadata.getAuthors().size()];

            if (metadata.containsCustomValue(PeakeorClient.MOD_ID + ":color")) {
                addon.color.parse(metadata.getCustomValue(PeakeorClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                addon.authors[i++] = author.getName();
            }

            ADDONS.add(addon);
        }
    }
}
