package com.digicore.automata.data.lib.modules.exception.messages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author peaceobute
 * @createdOn Dec-20(Wed)-2023
 */
@Component
@RequiredArgsConstructor
public class CardSchemeErrorMessages {
    public static final String CARD_NOT_FOUND_MESSAGE_KEY = "CARD_SCHEME_NOT_FOUND_MESSAGE";

    public static final String CARD_NOT_FOUND_CODE = "CARD_SCHEME_001";
    public static final String CARD_NOT_FOUND_CODE_KEY = "CARD_SCHEME_NOT_FOUND_CODE";
    public static final String CARD_ALREADY_EXISTS_MESSAGE = "CARD_SCHEME WITH CARD_SCHEME_ID ALREADY EXISTS.";
    public static final String CARD_ALREADY_EXISTS_MESSAGE_KEY = "CARD_SCHEME_ALREADY_EXISTS_MESSAGE";

    public static final String CARD_ALREADY_EXISTS_CODE = "CARD_SCHEME_002";
    public static final String CARD_ALREADY_EXISTS_CODE_KEY = "CARD_SCHEME_ALREADY_EXISTS_CODE";

}
