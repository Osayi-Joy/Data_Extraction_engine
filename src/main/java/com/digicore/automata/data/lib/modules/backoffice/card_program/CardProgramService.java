package com.digicore.automata.data.lib.modules.backoffice.card_program;

import com.digicore.automata.data.lib.modules.backoffice.card_program.dto.CardProgramDto;
import com.digicore.automata.data.lib.modules.backoffice.card_program.dto.CardProgramRequest;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;

/**
 * @author Joy Osayi
 * @createdOn Jan-07(Sun)-2024
 */

public interface CardProgramService {
    CardProgramDto createCardProgram(CardProgramRequest cardProgramRequest);

    CardProgramDto retrieveCardProgram(String cardProgramId);

    PaginatedResponseDTO<CardProgramDto> getAllCardPrograms(int pageNumber, int pageSize);

    PaginatedResponseDTO<CardProgramDto> searchOrFilterCardPrograms(AutomataSearchRequest automataSearchRequest);

    CsvDto<CardProgramDto> prepareCardProgramsCSV(CsvDto<CardProgramDto> parameter);

    void cardProgramExistenceCheck(String cardProgramName, String cardProgramId);
}
