package com.digicore.automata.data.lib.modules.backoffice.card_scheme_service.implementation;

import com.azure.core.http.rest.Page;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardDto;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.dto.CardRequest;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.model.CardProfile;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.repository.CardRepository;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.specification.CardProfileSpecification;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme_service.CardSchemeService;
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

import static com.digicore.automata.data.lib.modules.exception.messages.CardSchemeErrorMessages.*;

/**
 * @author peaceobute
 * @since 2023/12/21
 */
@Service
@RequiredArgsConstructor
public class CardSchemeServiceImpl implements CardSchemeService {

    private final CardRepository cardRepository;
    private final CardProfileSpecification cardSpecification;
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
                    service.retrieveValue(CARD_ALREADY_EXISTS_MESSAGE_KEY),
                    service.retrieveValue(CARD_ALREADY_EXISTS_CODE_KEY),
                    HttpStatus.CONFLICT
            );
        }

        CardProfile cardProfile = buildNewCardProfile(cardRequest);
        CardProfile savedCardProfile = cardRepository.save(cardProfile);

        return mapCardProfileToDto(savedCardProfile);
    }

    /**
     * enable card Scheme
     * @param cardSchemeId
     */
    @Override
    public void enableCardScheme(String cardSchemeId) {
        CardProfile singleCard = cardRepository.findByCardStatusAndCardSchemeId(Status.INACTIVE,
                cardSchemeId).orElseThrow(() ->
                exceptionHandler.processBadRequestException(
                        service.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                        service.retrieveValue(CARD_NOT_FOUND_CODE_KEY)
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
        CardProfile singleCard = cardRepository.findByCardStatusAndCardSchemeId(Status.ACTIVE,
                cardSchemeId).orElseThrow(() ->
                exceptionHandler.processBadRequestException(
                        service.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                        service.retrieveValue(CARD_NOT_FOUND_CODE_KEY)
                ));
        singleCard.setCardStatus(Status.INACTIVE);
        cardRepository.save(singleCard);
    }

    /**
     * delete card scheme detail
     *
     */
    @Override
    public void deleteCardScheme(String cardSchemeId) {
        CardProfile card = getCardSchemeDetail(cardSchemeId);
        card.setDeleted(true);
        cardRepository.save(card);
    }


    /**
     * update card scheme
     * @param cardSchemeId, cardRequest
     * @return cardDto
     */
    @Override
    public CardDto updateCardScheme(String cardSchemeId, CardRequest cardRequest) {

        CardProfile cardProfile = getCardSchemeDetail(cardRequest.getCardSchemeId());

        cardProfile.setCardSchemeName(cardRequest.getCardSchemeName() != null ?
                cardRequest.getCardSchemeName() : cardProfile.getCardSchemeName());
        cardProfile.setCardSchemeId(cardRequest.getCardSchemeId() != null ?
                cardRequest.getCardSchemeId() : cardProfile.getCardSchemeId());

        CardProfile updatedCardProfile = cardRepository.save(cardProfile);

        return mapCardProfileToDto(updatedCardProfile);

    }

    @Override
    public PaginatedResponseDTO<CardDto> getAllCardSchemes(int pageNumber, int pageSize) {

        Page<CardProfile> issuerPage = cardRepository.findAllByIsDeleted(false, getPageable(pageNumber, pageSize));
        return getIssuerPaginatedResponse(issuerPage);

    }

    @Override
    public CsvDto<CardDto> prepareIssuersCSV(CsvDto<CardDto> parameter) {
        Specification<CardProfile> specification = cardSpecification.buildSpecification(
                parameter.getAutomataSearchRequest());

        List<CardDto> data = cardRepository.findAll(specification).stream()
                .map(this::mapCardProfileToDto)
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
            String fileName = "Issuers-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(currentDateTime);
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

        CardProfile card = cardRepository
               .findFirstByIsDeletedFalseAndCardSchemeIdOrderByCreatedDate(cardSchemeId)
                .orElseThrow(() ->
                        exceptionHandler.processBadRequestException(
                                service.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                                service.retrieveValue(CARD_NOT_FOUND_CODE_KEY)
                        )
                );
        return mapCardProfileToDto(card);
    }

    public CardProfile getCardSchemeDetail(String cardSchemeId){
       return cardRepository
                .findFirstByIsDeletedFalseAndCardSchemeIdOrderByCreatedDate(cardSchemeId)
                .orElseThrow(() ->
                        exceptionHandler.processBadRequestException(
                                service.retrieveValue(CARD_NOT_FOUND_MESSAGE_KEY),
                                service.retrieveValue(CARD_NOT_FOUND_CODE_KEY)
                        )
                );


    }


    /**
     * create map card entity to card response dto
     * @param savedCardProfile
     * @return cardDto
     */
    private CardDto mapCardProfileToDto(CardProfile savedCardProfile) {
        CardDto cardDto = new CardDto();
        cardDto.setCardSchemeId(savedCardProfile.getCardSchemeId());
        cardDto.setCardSchemeName(savedCardProfile.getCardSchemeName());
        cardDto.setCardStatus(savedCardProfile.getCardStatus());
        cardDto.setDateCreated(savedCardProfile.getCreatedDate());
        cardDto.setDateLastModified(savedCardProfile.getLastModifiedDate());
        return cardDto;
    }

    /**
     * create map card request to card entity to save in the db
     * @param cardRequest
     * @return card profile
     */
    private CardProfile buildNewCardProfile(CardRequest cardRequest) {
        CardProfile profile = new CardProfile();
        profile.setCardSchemeId(cardRequest.getCardSchemeId());
        profile.setCardSchemeName(profile.getCardSchemeName());
        return profile;
    }
}
