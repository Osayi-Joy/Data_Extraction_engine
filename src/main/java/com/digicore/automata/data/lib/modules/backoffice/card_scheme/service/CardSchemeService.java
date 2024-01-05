package com.digicore.automata.data.lib.modules.backoffice.card_scheme.service;

import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardDto;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardRequest;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.dto.IssuerDto;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.registration.enums.Status;
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

    CardDto updateCardScheme(CardRequest cardRequest);

    PaginatedResponseDTO<CardDto> getAllCardSchemes(int pageNumber, int pageSize);

    CardDto viewCardSchemeDetail(String cardSchemeId);

    CsvDto<CardDto> prepareCardSchemeCSV(CsvDto<CardDto> parameter);

    PaginatedResponseDTO<CardDto> searchOrFilterCardScheme(AutomataSearchRequest automataSearchRequest);

    CardDto retrieveCardScheme(String cardSchemeId);

    void cardSchemeExistenceCheck(String cardSchemeId);

    void cardSchemeNotFoundCheck(String cardSchemeId);

    void existByStatusAndCardSchemeId(Status cardStatus, String cardSchemeId);
}
