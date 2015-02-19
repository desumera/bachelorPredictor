package com.j0llysnowman.domain.enums;

/**
 * Created by dsumera on 2/18/15.
 */
public enum BachelorContestant {

    BECCA,

    //    BRITT,

    //    CARLY,

    //    JADE,

    KAITLYN,

    //    MEGAN,

    WHITNEY;

    public static boolean matchesAny(String word) {

        if (word == null) {
            return false;
        }

        word = word.toUpperCase();

        for (BachelorContestant bachelorContestant : BachelorContestant.values()) {
            if (word.contains(bachelorContestant.name())) {
                return true;
            }
        }

        return false;
    }
}
