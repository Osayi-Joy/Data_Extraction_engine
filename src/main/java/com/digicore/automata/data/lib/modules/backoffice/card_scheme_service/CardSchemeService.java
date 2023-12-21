package com.digicore.automata.data.lib.modules.backoffice.card_scheme_service;

import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardDto;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import org.springframework.stereotype.Service;

/**
 * @author peaceobute
 * @since 2023/12/21
 */
@Service
public interface CardSchemeService {

    CardDto createCardScheme(CardRequest cardRequest);

    void enableCardScheme(String cardSchemeId);

    void disableCardScheme(String cardSchemeId);

    void deleteCardScheme(String cardSchemeId);

    CardDto updateCardScheme(String cardSchemeId, CardRequest cardRequest);

    PaginatedResponseDTO<CardDto> getAllCardSchemes(int pageNumber, int pageSize);

    CardDto getCardSchemeDetail(String cardSchemeId);

}
