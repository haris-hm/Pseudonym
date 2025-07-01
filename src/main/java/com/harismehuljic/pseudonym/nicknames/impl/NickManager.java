package com.harismehuljic.pseudonym.nicknames.impl;

import com.harismehuljic.pseudonym.Pseudonym;

import java.util.ArrayList;

public interface NickManager {
    static boolean validateNickname(String nickname) {
        if (nickname.matches(Pseudonym.CONFIG_DATA.getValidNicknameCharacters())
                && nickname.length() <= Pseudonym.CONFIG_DATA.getCharacterLimit()) {

            ArrayList<String> illegalNicknames = Pseudonym.CONFIG_DATA.getIllegalNicknames();

            if (Pseudonym.CONFIG_DATA.enforceIllegalNicknames()) {
                for (String word : illegalNicknames) {
                    if (nickname.contains(word)) {
                        return false;
                    }
                }
            }

            return true;
        }

        return !Pseudonym.CONFIG_DATA.enforceValidNicknameCharacters();
    }

    void pseudonym$updateDisplayName(NickPlayer nickPlayer);

}
