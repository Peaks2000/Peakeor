/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package peakeordevelopment.peakeorclient.utils.misc;

public class Version {
    private final String string;
    private final int[] numbers;

    public Version(String string) {
        this.string = string;
        this.numbers = new int[4];

        String[] split = string.split("\\.");
        if (split.length < 3 || split.length > 4) throw new IllegalArgumentException("Version string needs to have 3 or 4 numbers.");

        for (int i = 0; i < split.length; i++) {
            try {
                numbers[i] = Integer.parseInt(split[i]);
            } catch (NumberFormatException _) {
                throw new IllegalArgumentException("Failed to parse version string.");
            }
        }
    }

    public boolean isZero() {
        return numbers[0] == 0 && numbers[1] == 0 && numbers[2] == 0 && numbers[3] == 0;
    }

    public boolean isHigherThan(Version version) {
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] > version.numbers[i]) return true;
            if (numbers[i] < version.numbers[i]) return false;
        }

        return false;
    }

    @Override
    public String toString() {
        return string;
    }
}
