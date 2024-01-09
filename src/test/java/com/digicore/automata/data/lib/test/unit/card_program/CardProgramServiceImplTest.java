package com.digicore.automata.data.lib.test.unit.card_program;

import com.digicore.api.helper.exception.ZeusRuntimeException;
import com.digicore.api.helper.response.ApiError;
import com.digicore.automata.data.lib.modules.backoffice.card_program.dto.CardProgramDto;
import com.digicore.automata.data.lib.modules.backoffice.card_program.dto.CardProgramRequest;
import com.digicore.automata.data.lib.modules.backoffice.card_program.implementation.CardProgramServiceImpl;
import com.digicore.automata.data.lib.modules.backoffice.card_program.model.CardProgram;
import com.digicore.automata.data.lib.modules.backoffice.card_program.respository.CardProgramRepository;
import com.digicore.automata.data.lib.modules.backoffice.card_program.specification.CardProgramSpecification;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.model.CardScheme;
import com.digicore.automata.data.lib.modules.backoffice.card_scheme.service.CardSchemeService;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.model.Issuer;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.service.IssuerService;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.registhentication.registration.enums.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.digicore.automata.data.lib.modules.exception.messages.CardProgramErrorMessage.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Joy Osayi
 * @createdOn Jan-07(Sun)-2024
 */
@ExtendWith(MockitoExtension.class)
public class CardProgramServiceImplTest {
    @InjectMocks
    private CardProgramServiceImpl cardProgramService;

    @Mock
    private CardProgramRepository cardProgramRepository;
    @Mock
    private CardProgramSpecification cardProgramSpecification;
    @Mock
    private ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
    @Mock
    private SettingService settingService;
    @Mock
    private IssuerService issuerService;
    @Mock
    private CardSchemeService cardSchemeService;


    @Test
    void createCardProgram_Success() {
        // Given
        CardProgramRequest cardProgramRequest = new CardProgramRequest();
        cardProgramRequest.setCardProgramName("CardProgramName");
        cardProgramRequest.setCardProgramId("CardProgramId");
        cardProgramRequest.setCardSchemeId("cardSchemeId");
        cardProgramRequest.setIssuerId("cardIssuerId");

        // Mocking the issuerService
        Issuer issuer = new Issuer();
        issuer.setCardIssuerId("cardIssuerId");
        issuer.setCardIssuerName("cardIssuerName");
        when(issuerService.getIssuerByIssuerId(anyString())).thenReturn(issuer);

        // Mocking the cardSchemeService
        CardScheme cardScheme = new CardScheme();
        cardScheme.setCardSchemeId("cardSchemeId");
        cardScheme.setCardSchemeName("cardSchemeName");
        when(cardSchemeService.getCardSchemeByCardSchemeId(anyString())).thenReturn(cardScheme);

        when(cardProgramRepository.existsByCardProgramNameOrCardProgramId(
                cardProgramRequest.getCardProgramName(), cardProgramRequest.getCardProgramId())).thenReturn(false);

        CardProgram savedCardProgram = new CardProgram();
        savedCardProgram.setCardProgramName(cardProgramRequest.getCardProgramName());
        savedCardProgram.setCardProgramId(cardProgramRequest.getCardProgramId());
        savedCardProgram.setCardProgramStatus(Status.ACTIVE);

        when(cardProgramRepository.save(any(CardProgram.class))).thenReturn(savedCardProgram);

        // When
        CardProgramDto result = cardProgramService.createCardProgram(cardProgramRequest);

        // Then
        assertNotNull(result);
        assertEquals(cardProgramRequest.getCardProgramName(), result.getCardProgramName());
        assertEquals(cardProgramRequest.getCardProgramId(), result.getCardProgramId());
        assertEquals(Status.ACTIVE, result.getCardProgramStatus());

    }


    @Test
    void createCardProgram_DuplicateCardProgramId() {
        // Given
        CardProgramRequest cardProgramRequest = new CardProgramRequest();
        cardProgramRequest.setCardProgramName("DuplicateCardProgramName");
        cardProgramRequest.setCardProgramId("DuplicateCardProgramId");

        when(cardProgramRepository.existsByCardProgramNameOrCardProgramId(
                cardProgramRequest.getCardProgramName(), cardProgramRequest.getCardProgramId())).thenReturn(true);

        // When & Then
        when(exceptionHandler.processCustomException(
                settingService.retrieveValue(CARD_PROGRAM_ALREADY_EXISTS_MESSAGE_KEY),
                settingService.retrieveValue(CARD_PROGRAM_ALREADY_EXISTS_CODE_KEY),
                HttpStatus.CONFLICT))
                .thenReturn(new ZeusRuntimeException(HttpStatus.CONFLICT, new ApiError(
                        CARD_PROGRAM_ALREADY_EXISTS_MESSAGE, CARD_PROGRAM_ALREADY_EXISTS_CODE)));

        Assertions.assertThrows(ZeusRuntimeException.class, () -> {
            cardProgramService.createCardProgram(cardProgramRequest);
        });
    }


    @Test
    public void testRetrieveCardProgram() {
        // Given
        String cardProgramId = "testCardProgramId";
        when(cardProgramRepository.findFirstByCardProgramIdOrderByCreatedDate(anyString()))
                .thenReturn(Optional.of(createTestCardProgram("testCardProgramId", "testCardProgramName")));

        // When
        CardProgramDto result = cardProgramService.retrieveCardProgram(cardProgramId);

        // Then
        assertNotNull(result);
        assertEquals("testCardProgramName", result.getCardProgramName());
        assertEquals("testCardProgramId", result.getCardProgramId());
        // Add more assertions as needed
    }

    @Test
    public void testGetAllCardPrograms() {
        // Given
        int pageNumber = 1;
        int pageSize = 10;
        when(cardProgramRepository.findAll(any(Pageable.class)))
                .thenReturn(createTestCardProgramPage());

        // When
        PaginatedResponseDTO<CardProgramDto> result = cardProgramService.getAllCardPrograms(pageNumber, pageSize);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalItems());
        assertTrue(result.getIsFirstPage());
        assertTrue(result.getIsLastPage());
        // Add more assertions as needed
    }


    @Test
    public void testSearchOrFilterCardPrograms() {
        // Given
        int pageNumber = 1;
        int pageSize = 10;
        String startDate = "2023-12-20";
        String endDate = "2023-12-22";
        AutomataSearchRequest automataSearchRequest = new AutomataSearchRequest();
        automataSearchRequest.setStartDate(startDate);
        automataSearchRequest.setEndDate(endDate);
        automataSearchRequest.setPage(pageNumber);
        automataSearchRequest.setSize(pageSize);
        automataSearchRequest.setStatus(Status.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());

        when(cardProgramSpecification.buildSpecification(automataSearchRequest)).thenReturn(new CardProgramSpecification(automataSearchRequest));
        when(cardProgramRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(createTestCardProgramPage());

        // When
        PaginatedResponseDTO<CardProgramDto> result = cardProgramService.searchOrFilterCardPrograms(automataSearchRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getTotalItems());
        assertTrue(result.getIsFirstPage());
        assertTrue(result.getIsLastPage());
        verify(cardProgramRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    public void testPrepareCardProgramsCSV() {

        // Given
        int pageNumber = 1;
        int pageSize = 10;
        String startDate = "2023-12-20";
        String endDate = "2023-12-22";
        AutomataSearchRequest automataSearchRequest = new AutomataSearchRequest();
        automataSearchRequest.setStartDate(startDate);
        automataSearchRequest.setEndDate(endDate);
        automataSearchRequest.setPage(pageNumber);
        automataSearchRequest.setSize(pageSize);
        automataSearchRequest.setStatus(Status.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());

        CsvDto<CardProgramDto> parameter = new CsvDto<>();
        parameter.setAutomataSearchRequest(automataSearchRequest);

        List<CardProgram> cardProgramList = Arrays.asList(
                createTestCardProgram("CardProgramId1", "cardProgramName1"),
                createTestCardProgram("CardProgramId2", "cardProgramName2")
        );

        when(cardProgramSpecification.buildSpecification(parameter.getAutomataSearchRequest())).thenReturn(new CardProgramSpecification(automataSearchRequest));
        when(cardProgramRepository.findAll(any(Specification.class))).thenReturn(cardProgramList);

        // When
        CsvDto<CardProgramDto> result = cardProgramService.prepareCardProgramsCSV(parameter);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCsvHeader());
        assertNotNull(result.getFieldMappings());
        assertNotNull(result.getData());
    }



    private Page<CardProgram> createTestCardProgramPage() {
        List<CardProgram> cardProgramList = Arrays.asList(
                createTestCardProgram("CardProgramId1", "cardProgramName1"),
                createTestCardProgram("CardProgramId2", "cardProgramName2")
        );
        return new PageImpl<>(cardProgramList, PageRequest.of(0, 10), cardProgramList.size());
    }
    private CardProgram createTestCardProgram(String cardProgramId, String cardProgramName) {
        CardProgram testCardProgram = new CardProgram();
        testCardProgram.setCardProgramId(cardProgramId);
        testCardProgram.setCardProgramName(cardProgramName);
        return testCardProgram;
    }

}
