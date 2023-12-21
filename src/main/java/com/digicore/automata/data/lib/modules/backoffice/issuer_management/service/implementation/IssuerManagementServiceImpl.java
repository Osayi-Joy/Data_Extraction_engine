package com.digicore.automata.data.lib.modules.backoffice.issuer_management.service.implementation;

import com.digicore.automata.data.lib.modules.backoffice.issuer_management.dto.IssuerDto;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.dto.IssuerRequest;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.model.Issuer;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.repository.IssuerRepository;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.service.IssuerService;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.specification.IssuerSpecification;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.registhentication.registration.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.digicore.automata.data.lib.modules.common.util.PageableUtil.*;
import static com.digicore.automata.data.lib.modules.exception.messages.IssuerErrorMessages.*;

/**
 * @author Joy Osayi
 * @createdOn Dec-20(Wed)-2023
 */
@Service
@RequiredArgsConstructor
public class IssuerManagementServiceImpl implements IssuerService {
    private final IssuerRepository issuerRepository;
    private final IssuerSpecification issuerSpecification;
    private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
    private final SettingService settingService;


    @Override
    public PaginatedResponseDTO<IssuerDto> getAllIssuers(int pageNumber, int pageSize) {
        Page<Issuer> issuerPage = issuerRepository.findAllByIsDeleted(false, getPageable(pageNumber, pageSize));
        return getIssuerPaginatedResponse(issuerPage);
    }

    @Override
    public PaginatedResponseDTO<IssuerDto> searchOrFilterIssuers(AutomataSearchRequest automataSearchRequest) {
        Specification<Issuer> specification = issuerSpecification.buildSpecification(automataSearchRequest);
        Page<Issuer> issuerPage = issuerRepository.findAll(
                specification,
                getPageable(automataSearchRequest.getPage(), automataSearchRequest.getSize()));

        return getIssuerPaginatedResponse(issuerPage);
    }


//    public PaginatedResponseDTO<IssuerDto> filterIssuers(AutomataSearchRequest searchRequest) {
//        Specification<Issuer> specification = issuerSpecification.buildSpecification(searchRequest);
//        Page<Issuer> issuerPage = issuerRepository.findAll(
//                specification,
//                getPageable(searchRequest.getPage(), searchRequest.getSize()));
//
//        return getIssuerPaginatedResponse(issuerPage);
//    }

    @Override
    public CsvDto<IssuerDto> prepareIssuersCSV(CsvDto<IssuerDto> parameter) {
        Specification<Issuer> specification = issuerSpecification.buildSpecification(
                parameter.getAutomataSearchRequest());

        List<IssuerDto> data = issuerRepository.findAll(specification).stream()
                .map(this::mapIssuerEntityToDto)
                .toList();

        if (data.isEmpty()) {
            throw exceptionHandler.processCustomException("No record found", "GEN_006", HttpStatus.NOT_FOUND);
        } else {
            parameter.setCsvHeader(new String[]{
                    "Card Issuer Name",
                    "Card Issuer Id",
                    "Issuer Status",
                    "Created Date",
                    "Last Modified"
            });
            parameter.getFieldMappings().put("Card Issuer Name", IssuerDto::getCardIssuerName);
            parameter.getFieldMappings().put("Card Issuer Id", IssuerDto::getCardIssuerId);
            parameter.getFieldMappings().put("Issuer Status", issuerDto -> issuerDto.getIssuerStatus().toString());
            parameter.getFieldMappings().put("Created Date", IssuerDto::getCreatedDate);
            parameter.getFieldMappings().put("Last Modified", IssuerDto::getLastModified);
            parameter.setData(data);

            LocalDateTime currentDateTime = LocalDateTime.now();
            String fileName = "Issuers-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentDateTime);
            parameter.setFileName(fileName);

            return parameter;
        }
    }

    @Override
    public IssuerDto retrieveIssuer(String cardIssuerId) {
        Issuer issuer = issuerRepository
                .findFirstByIsDeletedFalseAndCardIssuerIdOrderByCreatedDate(cardIssuerId)
                .orElseThrow(() ->
                        exceptionHandler.processBadRequestException(
                                settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY),
                                settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY)
                        )
                );
        return mapIssuerEntityToDto(issuer);
    }


    @Override
    public IssuerDto createIssuer(IssuerRequest issuerRequest) {
        if (issuerRepository.existsByCardIssuerId(issuerRequest.getCardIssuerId())) {
            throw exceptionHandler.processCustomException(
                    settingService.retrieveValue(ISSUER_ALREADY_EXISTS_MESSAGE_KEY),
                    settingService.retrieveValue(ISSUER_ALREADY_EXISTS_CODE_KEY),
                    HttpStatus.CONFLICT
            );
        }

        Issuer issuer = new Issuer();
        issuer.setCardIssuerName(issuerRequest.getCardIssuerName());
        issuer.setCardIssuerId(issuerRequest.getCardIssuerId());

        Issuer savedIssuer = issuerRepository.save(issuer);

        return mapIssuerEntityToDto(savedIssuer);
    }


    @Override
    public IssuerDto editIssuer(IssuerRequest issuerRequest) {
        Issuer existingIssuer = getIssuer(issuerRequest.getCardIssuerId());
        existingIssuer.setCardIssuerName(issuerRequest.getCardIssuerName() != null ?
                issuerRequest.getCardIssuerName() : existingIssuer.getCardIssuerName());
        existingIssuer.setCardIssuerId(issuerRequest.getCardIssuerId() != null ?
                issuerRequest.getCardIssuerId() : existingIssuer.getCardIssuerId());

        Issuer updatedIssuer = issuerRepository.save(existingIssuer);

        return mapIssuerEntityToDto(updatedIssuer);
    }

    private Issuer getIssuer(String cardIssuerId) {
        return issuerRepository
                .findFirstByIsDeletedFalseAndCardIssuerIdOrderByCreatedDate(cardIssuerId)
                .orElseThrow(() ->
                        exceptionHandler.processBadRequestException(
                                settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY),
                                settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY)
                        )
                );
    }

    @Override
    public void enableIssuer(String cardIssuerId) {
        Issuer issuer = issuerRepository.findByIssuerStatusAndCardIssuerId(Status.INACTIVE,
                cardIssuerId).orElseThrow(() ->
                exceptionHandler.processBadRequestException(
                        settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY),
                        settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY)
                ));
        issuer.setIssuerStatus(Status.ACTIVE);
        issuerRepository.save(issuer);
    }

    @Override
    public void disableIssuer(String cardIssuerId) {
        Issuer issuer = issuerRepository.findByIssuerStatusAndCardIssuerId(Status.ACTIVE,
                cardIssuerId).orElseThrow(() ->
                exceptionHandler.processBadRequestException(
                        settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY),
                        settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY)
                ));
        issuer.setIssuerStatus(Status.INACTIVE);
        issuerRepository.save(issuer);
    }

    @Override
    public void deleteIssuer(String cardIssuerId) {
        Issuer issuer = getIssuer(cardIssuerId);
        issuer.setDeleted(true);
        issuerRepository.save(issuer);
    }

    @Override
    public void issuerExistenceCheck(String cardIssuerId){
        if (issuerRepository.existsByCardIssuerId(cardIssuerId)) {
            throw exceptionHandler.processCustomException(
                    settingService.retrieveValue(ISSUER_ALREADY_EXISTS_MESSAGE_KEY),
                    settingService.retrieveValue(ISSUER_ALREADY_EXISTS_CODE_KEY),
                    HttpStatus.CONFLICT
            );
        }
    }
    @Override
    public void issuerNotFoundCheck(String cardIssuerId){
        if (!issuerRepository.existsByCardIssuerId(cardIssuerId)) {
            throw exceptionHandler.processCustomException(
                    settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY),
                    settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY),
                    HttpStatus.CONFLICT
            );
        }
    }
    @Override
    public void existByStatusAndCardIssuerId(Status issuerStatus, String cardIssuerId){
        if(!issuerRepository.existsByIssuerStatusAndCardIssuerId(issuerStatus,
                cardIssuerId)) {
            throw exceptionHandler.processBadRequestException(
                            settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY),
                            settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY));
        }
    }
    private PaginatedResponseDTO<IssuerDto> getIssuerPaginatedResponse(Page<Issuer> issuerPage) {
        return PaginatedResponseDTO.<IssuerDto>builder()
                .content(issuerPage.getContent().stream().map(this::mapIssuerEntityToDto)
                        .toList())
                .currentPage(issuerPage.getNumber() + 1)
                .totalPages(issuerPage.getTotalPages())
                .totalItems(issuerPage.getTotalElements())
                .isFirstPage(issuerPage.isFirst())
                .isLastPage(issuerPage.isLast())
                .build();
    }

    private IssuerDto mapIssuerEntityToDto(Issuer issuer) {
        IssuerDto issuerDto = new IssuerDto();
        issuerDto.setCardIssuerName(issuer.getCardIssuerName());
        issuerDto.setCardIssuerId(issuer.getCardIssuerId());
        issuerDto.setCreatedDate(issuer.getCreatedDate().toString());
        if (issuer.getLastModifiedDate() != null) {
            issuerDto.setLastModified(issuer.getLastModifiedDate().toString());
        }
        return issuerDto;
    }



}
