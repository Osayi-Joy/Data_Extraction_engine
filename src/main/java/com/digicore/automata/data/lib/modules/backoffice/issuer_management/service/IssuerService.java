package com.digicore.automata.data.lib.modules.backoffice.issuer_management.service;

import com.digicore.automata.data.lib.modules.backoffice.issuer_management.dto.IssuerDto;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.dto.IssuerRequest;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.registration.enums.Status;

/**
 * @author Joy Osayi
 * @createdOn Dec-20(Wed)-2023
 */

public interface IssuerService {
    PaginatedResponseDTO<IssuerDto> getAllIssuers(int pageNumber, int pageSize);

    PaginatedResponseDTO<IssuerDto> searchOrFilterIssuers(AutomataSearchRequest automataSearchRequest);

    CsvDto<IssuerDto> prepareIssuersCSV(CsvDto<IssuerDto> parameter);

    IssuerDto retrieveIssuer(String cardIssuerId);

    IssuerDto createIssuer(IssuerRequest issuerRequest);

    IssuerDto editIssuer(IssuerRequest issuerRequest);

    void enableIssuer(String cardIssuerId);

    void disableIssuer(String cardIssuerId);

    void deleteIssuer(String cardIssuerId);

    void issuerExistenceCheck(String cardIssuerId);

    void issuerNotFoundCheck(String cardIssuerId);

    void existByStatusAndCardIssuerId(Status issuerStatus, String cardIssuerId);
}
