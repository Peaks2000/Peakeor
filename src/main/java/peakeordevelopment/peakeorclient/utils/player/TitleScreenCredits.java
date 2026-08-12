/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.utils.player;

import peakeordevelopment.peakeorclient.PeakeorClient;
import peakeordevelopment.peakeorclient.addons.AddonManager;
import peakeordevelopment.peakeorclient.addons.GithubRepo;
import peakeordevelopment.peakeorclient.addons.PeakeorAddon;
import peakeordevelopment.peakeorclient.gui.GuiThemes;
import peakeordevelopment.peakeorclient.gui.screens.CommitsScreen;
import peakeordevelopment.peakeorclient.mixininterface.IComponent;
import peakeordevelopment.peakeorclient.utils.network.Http;
import peakeordevelopment.peakeorclient.utils.network.PeakeorExecutor;
import peakeordevelopment.peakeorclient.utils.render.PeakeorToast;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static peakeordevelopment.peakeorclient.PeakeorClient.mc;

public class TitleScreenCredits {
    private static final List<Credit> credits = new ArrayList<>();

    private TitleScreenCredits() {
    }

    private static void init() {
        // Add addons
        for (PeakeorAddon addon : AddonManager.ADDONS) add(addon);

        // Sort by width (Peakeor always first)
        credits.sort(Comparator.comparingInt(value -> value.addon == PeakeorClient.ADDON ? Integer.MIN_VALUE : -mc.font.width(value.text)));

        // Check for latest commits
        PeakeorExecutor.execute(() -> {
            for (Credit credit : credits) {
                if (credit.addon.getRepo() == null || credit.addon.getCommit() == null) continue;

                GithubRepo repo = credit.addon.getRepo();
                Http.Request request = Http.get("https://api.github.com/repos/%s/branches/%s".formatted(repo.getOwnerName(), repo.branch()));
                request.exceptionHandler(e -> PeakeorClient.LOG.error("Could not fetch repository information for addon '{}'.", credit.addon.name, e));
                repo.authenticate(request);
                HttpResponse<Response> res = request.sendJsonResponse(Response.class);

                switch (res.statusCode()) {
                    case Http.UNAUTHORIZED -> {
                        String message = "Invalid authentication token for repository '%s'".formatted(repo.getOwnerName());
                        PeakeorToast toast = new PeakeorToast.Builder("GitHub: Unauthorized").icon(Items.BARRIER).text(message).build();
                        mc.gui.toastManager().addToast(toast);
                        PeakeorClient.LOG.warn(message);
                        if (System.getenv("peakeor.github.authorization") == null) {
                            PeakeorClient.LOG.info("Consider setting an authorization " +
                                "token with the 'peakeor.github.authorization' environment variable.");
                            PeakeorClient.LOG.info("See: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens");
                        }
                    }
                    case Http.FORBIDDEN ->
                        PeakeorClient.LOG.warn("Could not fetch updates for addon '{}': Rate-limited by GitHub.", credit.addon.name);
                    case Http.NOT_FOUND ->
                        PeakeorClient.LOG.warn("Could not fetch updates for addon '{}': GitHub repository '{}' not found.", credit.addon.name, repo.getOwnerName());
                    case Http.SUCCESS -> {
                        if (!credit.addon.getCommit().equals(res.body().commit.sha)) {
                            synchronized (credit.text) {
                                credit.text.append(Component.literal("*").withStyle(ChatFormatting.RED));
                                ((IComponent) ((Component) credit.text)).peakeor$invalidateCache(); // ???
                            }
                        }
                    }
                }
            }
        });
    }

    private static void add(PeakeorAddon addon) {
        Credit credit = new Credit(addon);

        credit.text.append(Component.literal(addon.name).withStyle(style -> style.withColor(addon.color.getPacked())));
        credit.text.append(Component.literal(" by ").withStyle(ChatFormatting.GRAY));

        for (int i = 0; i < addon.authors.length; i++) {
            if (i > 0) {
                credit.text.append(Component.literal(i == addon.authors.length - 1 ? " & " : ", ").withStyle(ChatFormatting.GRAY));
            }

            credit.text.append(Component.literal(addon.authors[i]).withStyle(ChatFormatting.WHITE));
        }

        credits.add(credit);
    }

    public static void render(GuiGraphicsExtractor graphics) {
        if (credits.isEmpty()) init();

        int y = 3;
        for (Credit credit : credits) {
            synchronized (credit.text) {
                int x = mc.gui.screen().width - 3 - mc.font.width(credit.text);

                graphics.text(mc.font, credit.text, x, y, -1);
            }

            y += mc.font.lineHeight + 2;
        }
    }

    public static boolean onClicked(double mouseX, double mouseY) {
        int y = 3;
        for (Credit credit : credits) {
            int width;
            synchronized (credit.text) {
                width = mc.font.width(credit.text);
            }

            int x = mc.gui.screen().width - 3 - width;

            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + mc.font.lineHeight + 2) {
                if (credit.addon.getRepo() != null && credit.addon.getCommit() != null) {
                    mc.gui.setScreen(new CommitsScreen(GuiThemes.get(), credit.addon));
                    return true;
                }
            }

            y += mc.font.lineHeight + 2;
        }

        return false;
    }

    private static class Credit {
        public final PeakeorAddon addon;
        public final MutableComponent text = Component.empty();

        public Credit(PeakeorAddon addon) {
            this.addon = addon;
        }
    }

    private static class Response {
        public Commit commit;
    }

    private static class Commit {
        public String sha;
    }
}
