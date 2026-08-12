/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.systems.accounts;

import peakeordevelopment.peakeorclient.PeakeorClient;
import peakeordevelopment.peakeorclient.systems.System;
import peakeordevelopment.peakeorclient.systems.Systems;
import peakeordevelopment.peakeorclient.systems.accounts.types.CrackedAccount;
import peakeordevelopment.peakeorclient.systems.accounts.types.MicrosoftAccount;
import peakeordevelopment.peakeorclient.systems.accounts.types.SessionAccount;
import peakeordevelopment.peakeorclient.utils.misc.NbtException;
import peakeordevelopment.peakeorclient.utils.misc.NbtUtils;
import peakeordevelopment.peakeorclient.utils.network.PeakeorExecutor;
import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

public class Accounts extends System<Accounts> implements Iterable<Account<?>> {
    private List<Account<?>> accounts = new ArrayList<>();

    public Accounts() {
        super("accounts");
    }

    public static Accounts get() {
        return Systems.get(Accounts.class);
    }

    public void add(Account<?> account) {
        accounts.add(account);
        save();
    }

    public boolean exists(Account<?> account) {
        return accounts.contains(account);
    }

    public void remove(Account<?> account) {
        if (accounts.remove(account)) {
            save();
        }
    }

    public int size() {
        return accounts.size();
    }

    @Override
    public void save(File folder) {
        super.save(folder);

        File accountFile = folder == null ? getFile() : new File(folder, getFile().getName());
        if (accountFile == null || !accountFile.exists()) return;

        try {
            Files.setPosixFilePermissions(accountFile.toPath(), EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE
            ));
        } catch (UnsupportedOperationException e) {
            boolean restricted = accountFile.setReadable(false, false)
                && accountFile.setWritable(false, false)
                && accountFile.setExecutable(false, false)
                && accountFile.setReadable(true, true)
                && accountFile.setWritable(true, true);
            if (!restricted) PeakeorClient.LOG.warn("Could not enforce owner-only permissions on the account database.");
        } catch (IOException e) {
            PeakeorClient.LOG.warn("Could not enforce owner-only permissions on the account database.", e);
        }
    }

    @Override
    public @NonNull Iterator<Account<?>> iterator() {
        return accounts.iterator();
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();

        tag.put("accounts", NbtUtils.listToTag(accounts));

        return tag;
    }

    @Override
    public Accounts fromTag(CompoundTag tag) {
        PeakeorExecutor.execute(() -> accounts = NbtUtils.listFromTag(tag.getListOrEmpty("accounts"), tag1 -> {
            CompoundTag t = (CompoundTag) tag1;
            if (!t.contains("type")) return null;

            AccountType type;
            try {
                type = AccountType.valueOf(t.getStringOr("type", ""));
            } catch (IllegalArgumentException _) {
                return null;
            }

            try {
                return switch (type) {
                    case Cracked -> new CrackedAccount(null).fromTag(t);
                    case Microsoft -> new MicrosoftAccount(null).fromTag(t);
                    case Session -> new SessionAccount(null).fromTag(t);
                };
            } catch (NbtException _) {
                return null;
            }
        }));

        return this;
    }
}
