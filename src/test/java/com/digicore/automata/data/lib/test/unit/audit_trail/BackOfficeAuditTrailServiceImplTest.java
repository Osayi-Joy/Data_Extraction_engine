package com.digicore.automata.data.lib.test.unit.audit_trail;

import com.digicore.api.helper.exception.ZeusRuntimeException;
import com.digicore.automata.data.lib.modules.backoffice.audit_trail.implementation.BackOfficeAuditTrailServiceImpl;
import com.digicore.automata.data.lib.modules.common.audit_trail.specification.BackOfficeAuditTrailSpecification;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.request.processor.dto.AuditLogDTO;
import com.digicore.request.processor.model.AuditLog;
import com.digicore.request.processor.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

@ExtendWith(MockitoExtension.class)
public class BackOfficeAuditTrailServiceImplTest {

  @Mock
  private AuditLogRepository auditLogRepository;
  @Mock
  private ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
  @Mock
  BackOfficeAuditTrailSpecification backOfficeAuditTrailSpecification;
  @InjectMocks
  private BackOfficeAuditTrailServiceImpl backOfficeAuditTrailService;


  @Test
  void filterAuditTrailsByActivityAndDateRange() {

    AuditLog auditLog1 = new AuditLog();
    auditLog1.setActivity("Create Activity");
    auditLog1.setLogStartDate(LocalDateTime.of(2003, Month.SEPTEMBER, 7, 0, 0));

    AuditLog auditLog2 = new AuditLog();
    auditLog1.setActivity("Create Activity");
    auditLog1.setLogStartDate(LocalDateTime.of(2003, Month.OCTOBER, 10, 0, 0));

    Page<AuditLog> aggregatorPage = new PageImpl<>(List.of(auditLog1, auditLog2),
        PageRequest.of(0, 10, Sort.by("logStartDate").descending()), 2);

    AutomataSearchRequest automataSearchRequest = new AutomataSearchRequest();
    automataSearchRequest.setKey("Create Activity");
    automataSearchRequest.setStartDate("2003-09-07");
    automataSearchRequest.setEndDate("2003-10-10");
    automataSearchRequest.setPage(0);
    automataSearchRequest.setSize(10);

    Pageable pageable = PageRequest.of(0, 10, Sort.by("logStartDate").descending());
    when(auditLogRepository.findAllByActivityAndLogStartDateBetween(
        any(String.class),
        any(LocalDateTime.class),
        any(LocalDateTime.class),
        eq(pageable))
    ).thenReturn(aggregatorPage);

    PaginatedResponseDTO<AuditLogDTO> responseDTO = backOfficeAuditTrailService.filterAuditTrailsByActivityAndDateRange(
        automataSearchRequest);

    assertEquals(2, responseDTO.getContent().size());
    verify(auditLogRepository, times(1)).findAllByActivityAndLogStartDateBetween(
        any(String.class),
        any(LocalDateTime.class),
        any(LocalDateTime.class),
        eq(pageable));
    verifyNoMoreInteractions(auditLogRepository);
  }

  @Test
  public void testPrepareAuditTrailsCSV_Success() {
    AuditLog auditLog1 = new AuditLog();
    auditLog1.setId(1L);
    auditLog1.setActivity("Create Activity");
    auditLog1.setLogStartDate(LocalDateTime.of(2003, Month.SEPTEMBER, 7, 0, 0));

    AuditLog auditLog2 = new AuditLog();
    auditLog2.setId(2L);
    auditLog1.setActivity("Create Activity");
    auditLog1.setLogStartDate(LocalDateTime.of(2003, Month.OCTOBER, 10, 0, 0));

    AutomataSearchRequest automataSearchRequest = new AutomataSearchRequest();
    automataSearchRequest.setKey("Create Activity");
    automataSearchRequest.setPage(0);
    automataSearchRequest.setSize(10);

    CsvDto<AuditLogDTO> parameter = new CsvDto<>();
    parameter.setAutomataSearchRequest(automataSearchRequest);

    when(auditLogRepository.findAllByActivityAndLogStartDateBetween(any(String.class), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(auditLog1, auditLog2)));

    CsvDto<AuditLogDTO> result = backOfficeAuditTrailService.prepareAuditTrailsCSV(parameter);

    assertNotNull(result);
    verify(auditLogRepository).findAllByActivityAndLogStartDateBetween(any(String.class), any(), any(), any());
    verify(auditLogRepository, times(1)).findAllByActivityAndLogStartDateBetween(any(String.class), any(), any(),
        any());
    verifyNoMoreInteractions(auditLogRepository);
  }

  @Test
  public void testPrepareAuditTrailsCSV_Fail() {
    AutomataSearchRequest automataSearchRequest = new AutomataSearchRequest();
    automataSearchRequest.setKey("Create Activity");
    automataSearchRequest.setPage(0);
    automataSearchRequest.setSize(10);

    CsvDto<AuditLogDTO> parameter = new CsvDto<>();
    parameter.setAutomataSearchRequest(automataSearchRequest);

    when(auditLogRepository.findAllByActivityAndLogStartDateBetween(any(String.class), any(), any(), any()))
        .thenReturn(new PageImpl<>(emptyList()));
    when(exceptionHandler.processCustomException(any(), any(), any()))
        .thenReturn(new ZeusRuntimeException("No record found ", "GEN_006", HttpStatus.NOT_FOUND));

    ZeusRuntimeException thrown = assertThrows(ZeusRuntimeException.class,
        () -> backOfficeAuditTrailService.prepareAuditTrailsCSV(parameter));

    assertEquals(HttpStatus.NOT_FOUND, thrown.getHttpStatus());
    verify(auditLogRepository).findAllByActivityAndLogStartDateBetween(any(String.class), any(), any(), any());
    verify(auditLogRepository, times(1)).findAllByActivityAndLogStartDateBetween(any(String.class), any(), any(),
        any());
    verifyNoMoreInteractions(auditLogRepository);
  }

  @Test
  void searchAuditTrails() {
    AuditLog auditLog1 = new AuditLog();
    auditLog1.setId(1L);
    auditLog1.setActivity("Create Activity");
    auditLog1.setLogStartDate(LocalDateTime.of(2003, Month.SEPTEMBER, 7, 0, 0));

    AuditLog auditLog2 = new AuditLog();
    auditLog2.setId(2L);
    auditLog1.setActivity("Create Activity");
    auditLog1.setLogStartDate(LocalDateTime.of(2003, Month.OCTOBER, 10, 0, 0));

    AutomataSearchRequest billentSearchRequest = new AutomataSearchRequest();
    billentSearchRequest.setKey("Create Activity");
    billentSearchRequest.setPage(0);
    billentSearchRequest.setSize(10);

    when(backOfficeAuditTrailSpecification.buildSpecification(billentSearchRequest)).thenReturn(
        new BackOfficeAuditTrailSpecification(billentSearchRequest));
    when(auditLogRepository.findAll(any(BackOfficeAuditTrailSpecification.class),
        any(Pageable.class))).thenReturn(new PageImpl<>(List.of(auditLog1, auditLog2)));

    PaginatedResponseDTO<AuditLogDTO> result = backOfficeAuditTrailService.searchAuditTrails(
        billentSearchRequest);

    assertEquals(2, result.getContent().size());
    verify(auditLogRepository, times(1)).findAll(any(BackOfficeAuditTrailSpecification.class),
        any(Pageable.class));
    verifyNoMoreInteractions(auditLogRepository);
  }

}
