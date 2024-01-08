package com.digicore.automata.data.lib.modules.exception.messages;

import com.digicore.automata.data.lib.modules.common.util.services.SystemUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Joy Osayi
 * @createdOn Jan-07(Sun)-2024
 */
@Component
@RequiredArgsConstructor
public class CardProgramErrorMessage {
    public static final String CARD_PROGRAM_NOT_FOUND_MESSAGE = "Card Program Not Found";
    public static final String CARD_PROGRAM_NOT_FOUND_MESSAGE_KEY = "CARD_PROGRAM_NOT_FOUND_MESSAGE";

    public static final String CARD_PROGRAM_NOT_FOUND_CODE = "CP_001";
    public static final String CARD_PROGRAM_NOT_FOUND_CODE_KEY = "CARD_PROGRAM_NOT_FOUND_CODE";
    public static final String CARD_PROGRAM_ALREADY_EXISTS_MESSAGE = "Card Program with cardProgramId or cardProgramName already exists.";
    public static final String CARD_PROGRAM_ALREADY_EXISTS_MESSAGE_KEY = "CARD_PROGRAM_ALREADY_EXISTS_MESSAGE";

    public static final String CARD_PROGRAM_ALREADY_EXISTS_CODE = "CP_002";
    public static final String CARD_PROGRAM_ALREADY_EXISTS_CODE_KEY = "CARD_PROGRAM_ALREADY_EXISTS_CODE";

    private final SystemUtil systemUtil;


    @PostConstruct
    public void loadStaticFieldsIntoDB() {
        systemUtil.loadStaticFieldsIntoDB(CardProgramErrorMessage.class,"CARD_PROGRAM_ERROR_MESSAGE");
    }
}
