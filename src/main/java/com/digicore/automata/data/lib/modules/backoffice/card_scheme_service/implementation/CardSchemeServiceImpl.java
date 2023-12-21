package com.digicore.automata.data.lib.modules.backoffice.card_scheme_service.implementation;

import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardDto;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardRequest;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.repository.CardRepository;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.specification.CardProfileSpecification;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme_service.CardSchemeService;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author peaceobute
 * @since 2023/12/21
 */
@Service
@RequiredArgsConstructor
public class CardSchemeServiceImpl implements CardSchemeService {

    private final CardRepository cardRepository;
    private final CardProfileSpecification specification;
    private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
    private final SettingService service;

    /**
     * create card Scheme
     * @param cardRequest
     * @return cardDto
     */
    @Override
    public CardDto createCardScheme(CardRequest cardRequest) {
        if (cardRepository.existsByCardSchemeId(cardRequest.getCardSchemeId())) {
            throw exceptionHandler.processCustomException(
                    service.retrieveValue(ISSUER_ALREADY_EXISTS_MESSAGE_KEY),
                    service.retrieveValue(ISSUER_ALREADY_EXISTS_CODE_KEY),
                    HttpStatus.CONFLICT
            );
        }

    }

    @Override
    public void enableCardScheme(String cardSchemeId) {

    }

    @Override
    public void disableCardScheme(String cardSchemeId) {

    }

    @Override
    public void deleteCardScheme(String cardSchemeId) {

    }

    @Override
    public CardDto updateCardScheme(String cardSchemeId, CardRequest cardRequest) {
        return null;
    }

    @Override
    public PaginatedResponseDTO<CardDto> getAllCardSchemes(int pageNumber, int pageSize) {
        return null;
    }

    @Override
    public CardDto getCardSchemeDetail(String cardSchemeId) {
        return null;
    }
}
