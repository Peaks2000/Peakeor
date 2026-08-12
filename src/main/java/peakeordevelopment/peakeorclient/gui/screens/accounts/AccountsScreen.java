/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.gui.screens.accounts;

import peakeordevelopment.peakeorclient.gui.GuiTheme;
import peakeordevelopment.peakeorclient.gui.WindowScreen;
import peakeordevelopment.peakeorclient.gui.widgets.WAccount;
import peakeordevelopment.peakeorclient.gui.widgets.containers.WContainer;
import peakeordevelopment.peakeorclient.gui.widgets.containers.WHorizontalList;
import peakeordevelopment.peakeorclient.gui.widgets.pressable.WButton;
import peakeordevelopment.peakeorclient.systems.accounts.Account;
import peakeordevelopment.peakeorclient.systems.accounts.AccountType;
import peakeordevelopment.peakeorclient.systems.accounts.Accounts;
import peakeordevelopment.peakeorclient.utils.network.PeakeorExecutor;
import org.jspecify.annotations.Nullable;

import static peakeordevelopment.peakeorclient.PeakeorClient.mc;

public class AccountsScreen extends WindowScreen {
    public AccountsScreen(GuiTheme theme) {
        super(theme, "Accounts");
    }

    @Override
    public void initWidgets() {
        // Accounts
        for (Account<?> account : Accounts.get()) {
            WAccount wAccount = add(theme.account(this, account)).expandX().widget();
            wAccount.refreshScreenAction = this::reload;
        }

        // Add account
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        addButton(l, "Cracked", () -> mc.gui.setScreen(new AddCrackedAccountScreen(theme, this)));
        addButton(l, "Session", () -> mc.gui.setScreen(new AddSessionAccountScreen(theme, this)));
        addButton(l, "Microsoft", () -> mc.gui.setScreen(new AddMicrosoftAccountScreen(theme, this)));
    }

    private void addButton(WContainer c, String text, Runnable action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = action;
    }

    public static void addAccount(@Nullable AddAccountScreen screen, AccountsScreen parent, Account<?> account) {
        if (screen != null) screen.locked = true;

        PeakeorExecutor.execute(() -> {
            if (!account.fetchInfo()) {
                mc.execute(() -> {
                    if (screen != null) screen.locked = false;
                });
                return;
            }

            Accounts.get().add(account);

            if (account.login()) {
                if (account.getType() != AccountType.Cracked) account.getCache().loadHead(parent::reload);
                Accounts.get().save();
            }

            mc.execute(() -> {
                if (screen != null) {
                    screen.locked = false;
                    screen.onClose();
                }

                parent.reload();
            });
        });
    }

    @Override
    public boolean toClipboard() {
        return false;
    }

    @Override
    public boolean fromClipboard() {
        return false;
    }
}
