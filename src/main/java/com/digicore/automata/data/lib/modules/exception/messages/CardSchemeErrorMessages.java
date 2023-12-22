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
    public static final String CARD_NOT_FOUND_MESSAGE_KEY = "ISSUER_NOT_FOUND_MESSAGE";

    public static final String CARD_NOT_FOUND_CODE = "IS_001";
    public static final String CARD_NOT_FOUND_CODE_KEY = "ISSUER_NOT_FOUND_CODE";
    public static final String CARD_ALREADY_EXISTS_MESSAGE = "Issuer with cardIssuerId already exists.";
    public static final String CARD_ALREADY_EXISTS_MESSAGE_KEY = "ISSUER_ALREADY_EXISTS_MESSAGE";

    public static final String CARD_ALREADY_EXISTS_CODE = "IS_002";
    public static final String CARD_ALREADY_EXISTS_CODE_KEY = "ISSUER_ALREADY_EXISTS_CODE";

}
