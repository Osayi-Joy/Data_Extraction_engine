package com.digicore.automata.data.lib.modules.backoffice.card_program.implementation;

import com.digicore.automata.data.lib.modules.backoffice.card_program.CardProgramService;
import com.digicore.automata.data.lib.modules.backoffice.card_program.dto.CardProgramDto;
import com.digicore.automata.data.lib.modules.backoffice.card_program.dto.CardProgramRequest;
import com.digicore.automata.data.lib.modules.backoffice.card_program.model.CardProgram;
import com.digicore.automata.data.lib.modules.backoffice.card_program.respository.CardProgramRepository;
import com.digicore.automata.data.lib.modules.backoffice.card_program.specification.CardProgramSpecification;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.service.CardSchemeService;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.service.IssuerService;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.common.util.BeanUtilWrapper;
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

import static com.digicore.automata.data.lib.modules.common.util.PageableUtil.getPageable;
import static com.digicore.automata.data.lib.modules.exception.messages.CardProgramErrorMessage.*;

/**
 * @author Joy Osayi
 * @createdOn Jan-07(Sun)-2024
 */
@Service
@RequiredArgsConstructor
public class CardProgramServiceImpl implements CardProgramService {
    private final CardProgramRepository cardProgramRepository;
    private final CardProgramSpecification cardProgramSpecification;
    private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
    private final SettingService settingService;
    private final IssuerService issuerService;
    private final CardSchemeService cardSchemeService;

    @Override
    public CardProgramDto createCardProgram(CardProgramRequest cardProgramRequest) {
        cardProgramExistenceCheck(
                cardProgramRequest.getCardProgramName(), cardProgramRequest.getCardProgramId());

        CardProgram cardProgram = new CardProgram();

        BeanUtilWrapper.copyNonNullProperties(cardProgramRequest,cardProgram);


        cardProgram.setIssuerId(issuerService
                .getIssuerByIssuerId(cardProgramRequest.getIssuerId()));
        cardProgram.setCardSchemeId(cardSchemeService
                .getCardSchemeByCardSchemeId(cardProgramRequest.getCardSchemeId()));
        cardProgram.setCardProgramStatus(Status.ACTIVE);

        cardProgram = cardProgramRepository.save(cardProgram);

        return mapToDto(cardProgram);
    }

    @Override
    public CardProgramDto retrieveCardProgram(String cardProgramId) {
        CardProgram cardProgram = cardProgramRepository
                .findFirstByCardProgramIdOrderByCreatedDate(cardProgramId)
                .orElseThrow(() ->
                        exceptionHandler.processBadRequestException(
                                settingService.retrieveValue(CARD_PROGRAM_NOT_FOUND_MESSAGE_KEY),
                                settingService.retrieveValue(CARD_PROGRAM_NOT_FOUND_CODE_KEY)
                        )
                );
        return mapToDto(cardProgram);
    }

    @Override
    public PaginatedResponseDTO<CardProgramDto> getAllCardPrograms(int pageNumber, int pageSize) {
        Page<CardProgram> cardProgramPage = cardProgramRepository.findAll(getPageable(pageNumber, pageSize));
        return getCardProgramPaginatedResponse(cardProgramPage);
    }

    @Override
    public PaginatedResponseDTO<CardProgramDto> searchOrFilterCardPrograms(AutomataSearchRequest automataSearchRequest) {
        Specification<CardProgram> specification = cardProgramSpecification.buildSpecification(automataSearchRequest);
        Page<CardProgram> cardProgramPage = cardProgramRepository.findAll(
                specification,
                getPageable(automataSearchRequest.getPage(), automataSearchRequest.getSize()));

        return getCardProgramPaginatedResponse(cardProgramPage);
    }

    @Override
    public CsvDto<CardProgramDto> prepareCardProgramsCSV(CsvDto<CardProgramDto> parameter) {
        Specification<CardProgram> specification = cardProgramSpecification.buildSpecification(
                parameter.getAutomataSearchRequest());

        List<CardProgramDto> data = cardProgramRepository.findAll(specification).stream()
                .map(this::mapToDto)
                .toList();

        if (data.isEmpty()) {
            throw exceptionHandler.processCustomException("No record found", "GEN_006", HttpStatus.NOT_FOUND);
        } else {
            parameter.setCsvHeader(new String[]{
                    "Card Program Name",
                    "Card Program Id",
                    "Issuer Id",
                    "Card Scheme Id",
                    "Daily Reconciliation Trigger Time",
                    "Monthly Reporting Trigger Date and Time",
                    "Card Scheme Settlement Data Source",
                    "In-House Transaction Data Source",
                    "Reconciliation In-House Notification Emails",
                    "Reconciliation Partner Notification Emails",
                    "Reconciliation In-House Storage Location",
                    "Reconciliation Partner Storage Location",
                    "Revenue Reporting In-House Notification Emails",
                    "Revenue Reporting Partner Notification Emails",
                    "Revenue Reporting In-House Storage Location",
                    "Revenue Reporting Partner Storage Location",
                    "Card Program Status",
                    "Created Date",
                    "Last Modified Date"
            });

            parameter.getFieldMappings().put("Card Program Name", CardProgramDto::getCardProgramName);
            parameter.getFieldMappings().put("Card Program Id", CardProgramDto::getCardProgramId);
            parameter.getFieldMappings().put("Issuer Id", CardProgramDto::getIssuerId);
            parameter.getFieldMappings().put("Card Scheme Id", CardProgramDto::getCardSchemeId);
            parameter.getFieldMappings().put("Daily Reconciliation Trigger Time", cardProgramDto -> cardProgramDto.getDailyReconciliationTriggerTime().toString());
            parameter.getFieldMappings().put("Monthly Reporting Trigger Date and Time", cardProgramDto -> cardProgramDto.getMonthlyReportingTriggerDateAndTime().toString());
            parameter.getFieldMappings().put("Card Scheme Settlement Data Source", CardProgramDto::getCardSchemeSettlementDataSource);
            parameter.getFieldMappings().put("In-House Transaction Data Source", CardProgramDto::getInHouseTransactionDataSource);
            parameter.getFieldMappings().put("Reconciliation In-House Notification Emails", cardProgramDto -> String.join(",", cardProgramDto.getReconciliationInHouseNotificationEmails()));
            parameter.getFieldMappings().put("Reconciliation Partner Notification Emails", cardProgramDto -> String.join(",", cardProgramDto.getReconciliationPartnerNotificationEmails()));
            parameter.getFieldMappings().put("Reconciliation In-House Storage Location", CardProgramDto::getReconciliationInHouseStorageLocation);
            parameter.getFieldMappings().put("Reconciliation Partner Storage Location", CardProgramDto::getReconciliationPartnerStorageLocation);
            parameter.getFieldMappings().put("Revenue Reporting In-House Notification Emails", cardProgramDto -> String.join(",", cardProgramDto.getRevenueReportingInHouseNotificationEmails()));
            parameter.getFieldMappings().put("Revenue Reporting Partner Notification Emails", cardProgramDto -> String.join(",", cardProgramDto.getRevenueReportingPartnerNotificationEmails()));
            parameter.getFieldMappings().put("Revenue Reporting In-House Storage Location", CardProgramDto::getRevenueReportingInHouseStorageLocation);
            parameter.getFieldMappings().put("Revenue Reporting Partner Storage Location", CardProgramDto::getRevenueReportingPartnerStorageLocation);
            parameter.getFieldMappings().put("Card Program Status", cardProgramDto -> cardProgramDto.getCardProgramStatus().toString());
            parameter.getFieldMappings().put("Created Date", cardProgramDto -> cardProgramDto.getCreatedDate().toString());
            parameter.getFieldMappings().put("Last Modified Date", cardProgramDto -> cardProgramDto.getLastModifiedDate().toString());

            parameter.setData(data);

            LocalDateTime currentDateTime = LocalDateTime.now();
            String fileName = "CardPrograms-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentDateTime);
            parameter.setFileName(fileName);

            return parameter;
        }
    }




    @Override
    public void cardProgramExistenceCheck(String cardProgramName, String cardProgramId) {
        if (cardProgramRepository.existsByCardProgramNameOrCardProgramId
                (cardProgramName, cardProgramId)) {
            throw exceptionHandler.processCustomException(
                    settingService.retrieveValue(CARD_PROGRAM_ALREADY_EXISTS_MESSAGE_KEY),
                    settingService.retrieveValue(CARD_PROGRAM_ALREADY_EXISTS_CODE_KEY),
                    HttpStatus.CONFLICT
            );
        }
    }
    private PaginatedResponseDTO<CardProgramDto> getCardProgramPaginatedResponse(Page<CardProgram> cardProgramPage) {
        return PaginatedResponseDTO.<CardProgramDto>builder()
                .content(cardProgramPage.getContent().stream().map(this::mapToDto)
                        .toList())
                .currentPage(cardProgramPage.getNumber() + 1)
                .totalPages(cardProgramPage.getTotalPages())
                .totalItems(cardProgramPage.getTotalElements())
                .isFirstPage(cardProgramPage.isFirst())
                .isLastPage(cardProgramPage.isLast())
                .build();
    }
    private CardProgramDto mapToDto(CardProgram cardProgram) {
        CardProgramDto cardProgramDto = new CardProgramDto();

        BeanUtilWrapper.copyNonNullProperties(cardProgram, cardProgramDto);

        return cardProgramDto;
    }


}
