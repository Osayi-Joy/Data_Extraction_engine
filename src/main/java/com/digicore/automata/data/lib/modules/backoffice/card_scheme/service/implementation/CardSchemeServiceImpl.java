package com.digicore.automata.data.lib.modules.backoffice.card_scheme.service.implementation;

import com.digicore.automata.data.lib.modules.backoffice.card_scheme.specification.CardSchemeSpecification;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.common.util.BeanUtilWrapper;
import org.springframework.data.domain.Page;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardDto;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardRequest;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.model.CardScheme;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.repository.CardRepository;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.service.CardSchemeService;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.registhentication.registration.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.digicore.automata.data.lib.modules.common.util.PageableUtil.*;
import static com.digicore.automata.data.lib.modules.exception.messages.CardSchemeErrorMessages.*;
import static com.digicore.automata.data.lib.modules.exception.messages.IssuerErrorMessages.*;

/**
 * @author peaceobute
 * @since 2023/12/21
 */
@Service
@RequiredArgsConstructor
public class CardSchemeServiceImpl implements CardSchemeService {

    private final CardRepository cardRepository;
    private final CardSchemeSpecification cardSpecification;
    private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
    private final SettingService settingService;


    /**
     * create card Scheme
     * @param cardRequest
     * @return cardDto
     */
    @Override
    public CardDto createCardScheme(CardRequest cardRequest) {
        if (cardRepository.existsByCardSchemeId(cardRequest.getCardSchemeId())) {
            throw exceptionHandler.processCustomException(
                    settingService.retrieveValue(CARD_ALREADY_EXISTS_MESSAGE_KEY),
                    settingService.retrieveValue(CARD_ALREADY_EXISTS_CODE_KEY),
                    HttpStatus.CONFLICT
            );
        }

        CardScheme cardScheme = buildNewCardProfile(cardRequest);
        CardScheme savedCardScheme = cardRepository.save(cardScheme);

        return mapCardEntityToDto(savedCardScheme);
    }

    /**
     * enable card Scheme
     * @param cardSchemeId
     */
    @Override
    public void enableCardScheme(String cardSchemeId) {
        CardScheme singleCard = cardRepository.findFirstByCardStatusAndCardSchemeIdOrderByCreatedDate(Status.INACTIVE,
                cardSchemeId).orElseThrow(() ->
                exceptionHandler.processBadRequestException(
                        settingService.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                        settingService.retrieveValue(CARD_NOT_FOUND_CODE_KEY)
                ));
        singleCard.setCardStatus(Status.ACTIVE);
        cardRepository.save(singleCard);

    }

    /**
     * disable card Scheme
     * @param cardSchemeId
     */
    @Override
    public void disableCardScheme(String cardSchemeId) {
        CardScheme singleCard = cardRepository.findFirstByCardStatusAndCardSchemeIdOrderByCreatedDate(Status.ACTIVE,
                cardSchemeId).orElseThrow(() ->
                exceptionHandler.processBadRequestException(
                        settingService.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                        settingService.retrieveValue(CARD_NOT_FOUND_CODE_KEY)
                ));
        singleCard.setCardStatus(Status.INACTIVE);
        cardRepository.save(singleCard);
    }
    /**
     * update card scheme
     * @param cardRequest
     * @return cardDto
     */
    @Override
    public CardDto updateCardScheme(CardRequest cardRequest) {

        CardScheme cardScheme = getCardSchemeDetail(cardRequest.getCardSchemeId());

        cardScheme.setCardSchemeName(cardRequest.getCardSchemeName() != null ?
                cardRequest.getCardSchemeName() : cardScheme.getCardSchemeName());
        cardScheme.setCardSchemeId(cardRequest.getCardSchemeId() != null ?
                cardRequest.getCardSchemeId() : cardScheme.getCardSchemeId());

        CardScheme updatedCardScheme = cardRepository.save(cardScheme);

        return mapCardEntityToDto(updatedCardScheme);

    }

    @Override
    public PaginatedResponseDTO<CardDto> getAllCardSchemes(int pageNumber, int pageSize) {

        Page<CardScheme> cardScheme = cardRepository.findAllByIsDeleted(false, getPageable(pageNumber, pageSize));
        return getCardPaginatedResponse(cardScheme);

    }

    private PaginatedResponseDTO<CardDto> getCardPaginatedResponse(Page<CardScheme> cardScheme) {
        return PaginatedResponseDTO.<CardDto>builder()
                .content(cardScheme.getContent().stream().map(this::mapCardEntityToDto).toList())
                .currentPage(cardScheme.getNumber() + 1)
                .totalPages(cardScheme.getTotalPages())
                .totalItems(cardScheme.getTotalElements())
                .isFirstPage(cardScheme.isFirst())
                .isLastPage(cardScheme.isLast())
                .build();
    }

    /**
     * create map card entity to card response dto
     * @param cardScheme
     * @return cardDto
     */
    private CardDto mapCardEntityToDto(CardScheme cardScheme) {
        CardDto cardDto = new CardDto();
        cardDto.setCardSchemeName(cardScheme.getCardSchemeName());
        cardDto.setCardSchemeId(cardScheme.getCardSchemeId());
        cardDto.setDateCreated(cardScheme.getCreatedDate() != null ? cardScheme.getCreatedDate().toString() : null);
        cardDto.setCardStatus(cardScheme.getCardStatus());
        cardDto.setDateLastModified(cardScheme.getLastModifiedDate() != null ? cardScheme.getLastModifiedDate().toString() : null);
        return cardDto;
    }

    @Override
    public CsvDto<CardDto> prepareCardSchemeCSV(CsvDto<CardDto> parameter) {
        Specification<CardScheme> specification = cardSpecification.buildSpecification(
                parameter.getAutomataSearchRequest());

        List<CardDto> data = cardRepository.findAll(specification).stream()
                .map(this::mapCardEntityToDto)
                .toList();

        if (data.isEmpty()) {
            throw exceptionHandler.processCustomException("No record found", "GEN_006", HttpStatus.NOT_FOUND);
        } else {
            parameter.setCsvHeader(new String[]{
                    "Card Scheme Name",
                    "Card Scheme Id",
                    "Card Status",
                    "Created Date",
                    "Last Modified"
            });
            parameter.getFieldMappings().put("Card Scheme Name", CardDto::getCardSchemeName);
            parameter.getFieldMappings().put("Card Scheme Id", CardDto::getCardSchemeId);
            parameter.getFieldMappings().put("Card Status", cardDto -> cardDto.getCardStatus().toString());
            parameter.getFieldMappings().put("Created Date", CardDto::getDateCreated);
            parameter.getFieldMappings().put("Last Modified", CardDto::getDateLastModified);
            parameter.setData(data);

            LocalDateTime currentDateTime = LocalDateTime.now();
            String fileName = "CardScheme-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentDateTime);
            parameter.setFileName(fileName);

            return parameter;
        }
    }


    /**
     * view card scheme detail
     * @param cardSchemeId
     * @return cardDto
     */
    @Override
    public CardDto viewCardSchemeDetail(String cardSchemeId) {

        CardScheme card = cardRepository
               .findFirstByIsDeletedFalseAndCardSchemeIdOrderByCreatedDate(cardSchemeId)
                .orElseThrow(() ->
                        exceptionHandler.processBadRequestException(
                                settingService.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                                settingService.retrieveValue(CARD_NOT_FOUND_CODE_KEY)
                        )
                );
        return mapCardEntityToDto(card);
    }

    public CardScheme getCardSchemeDetail(String cardSchemeId){
       return cardRepository
                .findFirstByIsDeletedFalseAndCardSchemeIdOrderByCreatedDate(cardSchemeId)
                .orElseThrow(() ->
                        exceptionHandler.processBadRequestException(
                                settingService.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                                settingService.retrieveValue(CARD_NOT_FOUND_CODE_KEY)
                        )
                );


    }

    /**
     * create map card request to card entity to save in the db
     * @param cardRequest
     * @return card profile
     */
    private CardScheme buildNewCardProfile(CardRequest cardRequest) {
        CardScheme profile = new CardScheme();
        profile.setCardSchemeId(cardRequest.getCardSchemeId());
        profile.setCardSchemeName(cardRequest.getCardSchemeName());
        profile.setCardStatus(Status.ACTIVE);
        return profile;
    }

    @Override
    public PaginatedResponseDTO<CardDto> searchOrFilterCardScheme(AutomataSearchRequest automataSearchRequest) {
        Specification<CardScheme> specification = cardSpecification.buildSpecification(automataSearchRequest);
        Page<CardScheme> cardPage = cardRepository.findAll(
                specification,
                getPageable(automataSearchRequest.getPage(), automataSearchRequest.getSize()));

        return getCardPaginatedResponse(cardPage);
    }

    @Override
    public CardDto retrieveCardScheme(String cardSchemeId) {
        CardScheme cardScheme = cardRepository
                .findFirstByIsDeletedFalseAndCardSchemeIdOrderByCreatedDate(cardSchemeId)
                .orElseThrow(() ->
                        exceptionHandler.processBadRequestException(
                                settingService.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                                settingService.retrieveValue(CARD_NOT_FOUND_CODE_KEY)
                        )
                );
        return mapCardEntityToDto(cardScheme);
    }

    @Override
    public void cardSchemeExistenceCheck(String cardSchemeId){
        if (cardRepository.existsByCardSchemeId(cardSchemeId)) {
            throw exceptionHandler.processCustomException(
                    settingService.retrieveValue(CARD_ALREADY_EXISTS_MESSAGE_KEY),
                    settingService.retrieveValue(CARD_ALREADY_EXISTS_CODE_KEY),
                    HttpStatus.CONFLICT
            );
        }
    }
    @Override
    public void cardSchemeNotFoundCheck(String cardSchemeId){
        if (!cardRepository.existsByCardSchemeId(cardSchemeId)) {
            throw exceptionHandler.processCustomException(
                    settingService.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                    settingService.retrieveValue(CARD_NOT_FOUND_CODE_KEY),
                    HttpStatus.CONFLICT
            );
        }
    }
    @Override
    public void existByStatusAndCardSchemeId(Status cardStatus, String cardSchemeId){
        if(!cardRepository.existsByCardStatusAndCardSchemeId(cardStatus,
                cardSchemeId)) {
            throw exceptionHandler.processBadRequestException(
                    settingService.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                    settingService.retrieveValue(CARD_NOT_FOUND_CODE_KEY));
        }
    }
}
