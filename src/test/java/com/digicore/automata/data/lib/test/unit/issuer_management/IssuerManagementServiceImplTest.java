package com.digicore.automata.data.lib.test.unit.issuer_management;

import com.digicore.api.helper.exception.ZeusRuntimeException;
import com.digicore.api.helper.response.ApiError;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.dto.IssuerDto;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.dto.IssuerRequest;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.repository.IssuerRepository;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.service.implementation.IssuerManagementServiceImpl;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.specification.IssuerSpecification;
import com.digicore.automata.data.lib.modules.backoffice.issuer_management.model.Issuer;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.digicore.automata.data.lib.modules.common.util.PageableUtil.getPageable;
import static com.digicore.automata.data.lib.modules.exception.messages.IssuerErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Joy Osayi
 * @createdOn Dec-20(Wed)-2023
 */


@ExtendWith(MockitoExtension.class)
public class IssuerManagementServiceImplTest {

    @InjectMocks
    private IssuerManagementServiceImpl issuerService;

    @Mock
    private IssuerRepository issuerRepository;

    @Mock
    private IssuerSpecification issuerSpecification;

    @Mock
    private ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;

    @Mock
    private SettingService settingService;


    @Test
    void createIssuer_Success() {
        // Given
        IssuerRequest issuerRequest = new IssuerRequest("IssuerName", "IssuerId");

        when(issuerRepository.existsByCardIssuerId(issuerRequest.getCardIssuerId())).thenReturn(false);

        Issuer savedIssuer = new Issuer();
        savedIssuer.setCardIssuerName(issuerRequest.getCardIssuerName());
        savedIssuer.setCardIssuerId(issuerRequest.getCardIssuerId());
        savedIssuer.setIssuerStatus(Status.ACTIVE);

        when(issuerRepository.save(any(Issuer.class))).thenReturn(savedIssuer);

        // When
        IssuerDto result = issuerService.createIssuer(issuerRequest);

        // Then
        assertNotNull(result);
        assertEquals(issuerRequest.getCardIssuerName(), result.getCardIssuerName());
        assertEquals(issuerRequest.getCardIssuerId(), result.getCardIssuerId());
        assertEquals(Status.ACTIVE, result.getIssuerStatus());
    }

    @Test
    void createIssuer_DuplicateIssuerId() {
        // Given
        IssuerRequest issuerRequest = new IssuerRequest("IssuerName", "DuplicateIssuerId");

        when(issuerRepository.existsByCardIssuerId(issuerRequest.getCardIssuerId())).thenReturn(true);

        // When & Then
        when(exceptionHandler.processCustomException(
                settingService.retrieveValue(ISSUER_ALREADY_EXISTS_MESSAGE_KEY),
                settingService.retrieveValue(ISSUER_ALREADY_EXISTS_CODE_KEY),
                HttpStatus.CONFLICT))
                .thenReturn(new ZeusRuntimeException(HttpStatus.CONFLICT, new ApiError(ISSUER_ALREADY_EXISTS_MESSAGE, ISSUER_ALREADY_EXISTS_CODE)));

        Assertions.assertThrows(ZeusRuntimeException.class, () -> {
            issuerService.createIssuer(issuerRequest);
        });
    }

    @Test
    void retrieveIssuer_whenIssuerExists_thenReturnIssuerDto() {
        // Given
        String cardIssuerId = "123";
        String cardIssuerName = "IssuerName";
        Issuer issuer = createIssuer(cardIssuerName, cardIssuerId);

        when(issuerRepository.findFirstByIsDeletedFalseAndCardIssuerIdOrderByCreatedDate(cardIssuerId))
                .thenReturn(Optional.of(issuer));

        // When
        IssuerDto result = issuerService.retrieveIssuer(cardIssuerId);

        // Then
        assertEquals(issuer.getCardIssuerName(), result.getCardIssuerName());
        assertEquals(issuer.getCardIssuerId(), result.getCardIssuerId());
        assertEquals(issuer.getCreatedDate().toString(), result.getCreatedDate());
        assertEquals(issuer.getIssuerStatus(), result.getIssuerStatus());
    }

    @Test
    void retrieveIssuer_whenIssuerNotExists_thenThrowException() {
        // Given
        String cardIssuerId = "123";

        when(issuerRepository.findFirstByIsDeletedFalseAndCardIssuerIdOrderByCreatedDate(cardIssuerId))
                .thenReturn(Optional.empty());

        when(settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY)).thenReturn("Issuer not found");
        when(settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY)).thenReturn("ISSUER_NOT_FOUND");

        // When & Then
        when(exceptionHandler.processBadRequestException(
                settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY),
                settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY)))
                .thenReturn(new ZeusRuntimeException(HttpStatus.BAD_REQUEST, new ApiError(ISSUER_NOT_FOUND_MESSAGE, ISSUER_NOT_FOUND_CODE)));

        Assertions.assertThrows(ZeusRuntimeException.class, () -> {
            issuerService.retrieveIssuer(cardIssuerId);
        });
    }

    @Test
    void editIssuer_shouldEditIssuerSuccessfully() {
        // Given
        IssuerRequest issuerRequest = new IssuerRequest("EditedIssuerName", "EditedIssuerId");
        String cardIssuerId = "ExistingIssuerId";
        Issuer existingIssuer = new Issuer();
        existingIssuer.setCardIssuerName("ExistingIssuerName");
        existingIssuer.setCardIssuerId("ExistingIssuerId");

        when(issuerRepository.findFirstByIsDeletedFalseAndCardIssuerIdOrderByCreatedDate("ExistingIssuerId"))
                .thenReturn(Optional.of(existingIssuer));

        when(issuerRepository.save(any(Issuer.class))).thenAnswer(invocation -> {
            Issuer savedIssuer = invocation.getArgument(0);
            savedIssuer.setCardIssuerName("EditedIssuerName");
            savedIssuer.setCardIssuerId("EditedIssuerId");
            return savedIssuer;
        });

        // When
        IssuerDto editedIssuerDto = issuerService.editIssuer(cardIssuerId, issuerRequest);

        // Then
        assertNotNull(editedIssuerDto);
        assertEquals("EditedIssuerName", editedIssuerDto.getCardIssuerName());
        assertEquals("EditedIssuerId", editedIssuerDto.getCardIssuerId());
    }

    @Test
    void enableIssuer_shouldEnableIssuerSuccessfully() {
        // Given
        String cardIssuerId = "ExistingIssuerId";

        Issuer existingIssuer = new Issuer();
        existingIssuer.setCardIssuerName("ExistingIssuerName");
        existingIssuer.setCardIssuerId("ExistingIssuerId");
        existingIssuer.setIssuerStatus(Status.INACTIVE);

        when(issuerRepository.findByIssuerStatusAndCardIssuerId(Status.INACTIVE, cardIssuerId))
                .thenReturn(Optional.of(existingIssuer));

        when(issuerRepository.save(any(Issuer.class))).thenAnswer(invocation -> {
            Issuer savedIssuer = invocation.getArgument(0);
            savedIssuer.setIssuerStatus(Status.ACTIVE);
            return savedIssuer;
        });

        // When
        issuerService.enableIssuer(cardIssuerId);

        // Then
        assertEquals(Status.ACTIVE, existingIssuer.getIssuerStatus());
    }

    @Test
    void disableIssuer_shouldDisableIssuerSuccessfully() {
        // Given
        String cardIssuerId = "ExistingIssuerId";

        Issuer existingIssuer = new Issuer();
        existingIssuer.setCardIssuerName("ExistingIssuerName");
        existingIssuer.setCardIssuerId("ExistingIssuerId");
        existingIssuer.setIssuerStatus(Status.ACTIVE);

        when(issuerRepository.findByIssuerStatusAndCardIssuerId(Status.ACTIVE, cardIssuerId))
                .thenReturn(Optional.of(existingIssuer));

        when(issuerRepository.save(any(Issuer.class))).thenAnswer(invocation -> {
            Issuer savedIssuer = invocation.getArgument(0);
            savedIssuer.setIssuerStatus(Status.INACTIVE);
            return savedIssuer;
        });

        // When
        issuerService.disableIssuer(cardIssuerId);

        // Then
        assertEquals(Status.INACTIVE, existingIssuer.getIssuerStatus());
    }

    @Test
    void deleteIssuer_shouldSoftDeleteIssuerSuccessfully() {
        // Given
        String cardIssuerId = "ExistingIssuerId";

        Issuer existingIssuer = new Issuer();
        existingIssuer.setCardIssuerName("ExistingIssuerName");
        existingIssuer.setCardIssuerId("ExistingIssuerId");
        existingIssuer.setDeleted(false);

        when(issuerRepository.findFirstByIsDeletedFalseAndCardIssuerIdOrderByCreatedDate(cardIssuerId))
                .thenReturn(Optional.of(existingIssuer));

        when(issuerRepository.save(any(Issuer.class))).thenAnswer(invocation -> {
            Issuer savedIssuer = invocation.getArgument(0);
            savedIssuer.setDeleted(true);
            return savedIssuer;
        });

        // When
        issuerService.deleteIssuer(cardIssuerId);

        // Then
        assertTrue(existingIssuer.isDeleted());
    }

    @Test
    void issuerExistenceCheck_shouldThrowExceptionWhenIssuerExists() {
        // Given
        String cardIssuerId = "ExistingIssuerId";

        when(issuerRepository.existsByCardIssuerId(cardIssuerId)).thenReturn(true);

        // When & Then
        when(exceptionHandler.processCustomException(
                settingService.retrieveValue(ISSUER_ALREADY_EXISTS_MESSAGE_KEY),
                settingService.retrieveValue(ISSUER_ALREADY_EXISTS_CODE_KEY),
                HttpStatus.CONFLICT))
                .thenReturn(new ZeusRuntimeException(HttpStatus.CONFLICT, new ApiError(ISSUER_ALREADY_EXISTS_MESSAGE, ISSUER_ALREADY_EXISTS_CODE)));

        // Verify that the expected exception is thrown when the issuer exists
        ZeusRuntimeException exception = assertThrows(ZeusRuntimeException.class,
                () -> issuerService.issuerExistenceCheck(cardIssuerId));

        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals(ISSUER_ALREADY_EXISTS_MESSAGE, exception.getErrors().get(0).getMessage());
        assertEquals(ISSUER_ALREADY_EXISTS_CODE, exception.getErrors().get(0).getCode());
    }

    @Test
    void issuerNotFoundCheck_shouldThrowExceptionWhenIssuerNotExists() {
        // Given
        String cardIssuerId = "NonExistingIssuerId";

        when(issuerRepository.existsByCardIssuerId(cardIssuerId)).thenReturn(false);

        // When & Then
        when(exceptionHandler.processCustomException(
                settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY),
                settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY),
                HttpStatus.CONFLICT))
                .thenReturn(new ZeusRuntimeException(HttpStatus.CONFLICT, new ApiError(ISSUER_NOT_FOUND_MESSAGE, ISSUER_NOT_FOUND_CODE)));

        // Verify that the expected exception is thrown when the issuer is not found
        ZeusRuntimeException exception = assertThrows(ZeusRuntimeException.class,
                () -> issuerService.issuerNotFoundCheck(cardIssuerId));

        assertEquals(HttpStatus.CONFLICT, exception.getHttpStatus());
        assertEquals(ISSUER_NOT_FOUND_MESSAGE, exception.getErrors().get(0).getMessage());
        assertEquals(ISSUER_NOT_FOUND_CODE, exception.getErrors().get(0).getCode());
    }

    @Test
    void existByStatusAndCardIssuerId_shouldThrowExceptionWhenIssuerNotExistsWithStatus() {
        // Given
        String cardIssuerId = "ExistingIssuerId";
        Status issuerStatus = Status.ACTIVE;

        when(issuerRepository.existsByIssuerStatusAndCardIssuerId(issuerStatus, cardIssuerId))
                .thenReturn(false);

        // When & Then
        when(exceptionHandler.processBadRequestException(
                settingService.retrieveValue(ISSUER_NOT_FOUND_MESSAGE_KEY),
                settingService.retrieveValue(ISSUER_NOT_FOUND_CODE_KEY)))
                .thenReturn(new ZeusRuntimeException(HttpStatus.BAD_REQUEST, new ApiError(ISSUER_NOT_FOUND_MESSAGE, ISSUER_NOT_FOUND_CODE)));

        // Verify that the expected exception is thrown when the issuer with status is not found
        ZeusRuntimeException exception = assertThrows(ZeusRuntimeException.class,
                () -> issuerService.existByStatusAndCardIssuerId(issuerStatus, cardIssuerId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals(ISSUER_NOT_FOUND_MESSAGE, exception.getErrors().get(0).getMessage());
        assertEquals(ISSUER_NOT_FOUND_CODE, exception.getErrors().get(0).getCode());
    }



    @Test
    void getAllIssuers_shouldReturnPaginatedResponse() {
        // Given
        int pageNumber = 1;
        int pageSize = 10;

        List<Issuer> issuerList = List.of(
                createIssuer("Issuer1", "123"),
                createIssuer("Issuer2", "456")
        );
        Page<Issuer> issuerPage = new PageImpl<>(issuerList, PageRequest.of(pageNumber - 1, pageSize), issuerList.size());

        when(issuerRepository.findAllByIsDeleted(false, getPageable(pageNumber, pageSize)))
                .thenReturn(issuerPage);

        // When
        PaginatedResponseDTO<IssuerDto> result = issuerService.getAllIssuers(pageNumber, pageSize);

        // Then
        assertEquals(pageNumber, result.getCurrentPage());
        assertEquals(issuerPage.getTotalPages(), result.getTotalPages());
        assertEquals(issuerPage.getTotalElements(), result.getTotalItems());
        assertEquals(issuerPage.isFirst(), result.getIsFirstPage());
        assertEquals(issuerPage.isLast(), result.getIsLastPage());

        List<IssuerDto> issuerDtoList = result.getContent();
        assertEquals(issuerList.size(), issuerDtoList.size());
        IntStream.range(0, issuerList.size())
                .forEach(index -> {
                    Issuer issuer = issuerList.get(index);
                    IssuerDto issuerDto = issuerDtoList.get(index);

                    assertEquals(issuer.getCardIssuerName(), issuerDto.getCardIssuerName());
                    assertEquals(issuer.getCardIssuerId(), issuerDto.getCardIssuerId());
                });


    }

    @Test
    void searchOrFilterIssuers_ShouldReturnPaginatedResponses() {
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
        Page<Issuer> issuerPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(issuerSpecification.buildSpecification(automataSearchRequest)).thenReturn(new IssuerSpecification(automataSearchRequest));
        when(issuerRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(issuerPage);

        // When
        PaginatedResponseDTO<IssuerDto> result = issuerService.searchOrFilterIssuers(automataSearchRequest);

        // Then
        assertNotNull(result);
        // Add assertions for the content, currentPage, totalPages, totalItems, isFirstPage, isLastPage, etc.

        // Verify that the repository method was called with the correct parameters
        verify(issuerRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void prepareIssuersCSV_ShouldReturnCsvDto() {
        // Given
        CsvDto<IssuerDto> csvDto = new CsvDto<>();
        AutomataSearchRequest automataSearchRequest = new AutomataSearchRequest();
        automataSearchRequest.setStartDate("2023-12-20");
        automataSearchRequest.setEndDate("2023-12-22");
        csvDto.setAutomataSearchRequest(automataSearchRequest);

        List<Issuer> issuers = List.of(
                createIssuer("Issuer1", "123"),
                createIssuer("Issuer2", "456")
        );

        when(issuerSpecification.buildSpecification(automataSearchRequest)).thenReturn(new IssuerSpecification(automataSearchRequest));
        when(issuerRepository.findAll(any(Specification.class))).thenReturn(issuers);

        // When
        CsvDto<IssuerDto> result = issuerService.prepareIssuersCSV(csvDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCsvHeader());
        assertNotNull(result.getFieldMappings());
        assertNotNull(result.getData());


        // Verify that the repository method was called with the correct parameters
        verify(issuerRepository).findAll(any(Specification.class));
    }


    private Issuer createIssuer(String name, String id) {
        Issuer issuer = new Issuer();
        issuer.setCardIssuerName(name);
        issuer.setCardIssuerId(id);
        issuer.setCreatedDate(LocalDateTime.now());
        issuer.setIssuerStatus(Status.ACTIVE);
        return issuer;
    }





}
