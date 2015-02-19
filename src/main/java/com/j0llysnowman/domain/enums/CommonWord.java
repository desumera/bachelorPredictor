package com.j0llysnowman.domain.enums;

/**
 * Created by dsumera on 2/18/15.
 */
public enum CommonWord {

    CHRIS,
    THIS,
    THAT,
    BACHELOR,
    THEBACHELOR,
    BACHELORNATION;

    public static CommonWord getInstance(String word) {
        for (CommonWord commonWord : CommonWord.values()) {
            if (commonWord.name().equalsIgnoreCase(word)) {
                return commonWord;
            }
        }

        return null;

    }
}
