package com.thevoidcult.items;

import java.util.Arrays;
import java.util.List;

public enum SinsList {
    NONE,
    WRATH,
    GREED,
    GLUTTONY,
    ENVY,
    PRIDE,
    SLOTH;

    public static final List<SinsList> WORKING_SINS = Arrays.stream(values())
            .filter(sin -> sin != NONE)
            .toList();
}
