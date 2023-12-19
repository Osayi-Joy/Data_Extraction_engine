package com.digicore.automata.data.lib.modules.backoffice.audit_trail.implementation;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */


import com.digicore.automata.data.lib.modules.common.audit_trail.dto.LogActivityDTO;
import com.digicore.automata.data.lib.modules.common.audit_trail.service.AuditTrailService;
import com.digicore.automata.data.lib.modules.common.audit_trail.specification.BackOfficeAuditTrailSpecification;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.automata.data.lib.modules.common.util.PageableUtil;
import com.digicore.common.util.BeanUtilWrapper;
import com.digicore.common.util.ClientUtil;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.digicore.request.processor.dto.AuditLogDTO;
import com.digicore.request.processor.model.AuditLog;
import com.digicore.request.processor.repository.AuditLogRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackOfficeAuditTrailServiceImpl implements AuditTrailService {
    private final AuditLogRepository auditLogRepository;
    private final BackOfficeAuditTrailSpecification backOfficeAuditTrailSpecification;
    private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;

    @Override
    public PaginatedResponseDTO<AuditLogDTO> retrieveSelfAuditTrail(AutomataSearchRequest automataSearchRequest) {
        Page<AuditLog> auditLogs = auditLogRepository.findAllByEmail(ClientUtil.getLoggedInUsername(), PageableUtil.getPageable(automataSearchRequest.getPage(), automataSearchRequest.getSize(),"logStartDate"));
        return PaginatedResponseDTO.<AuditLogDTO>builder()
                .content(auditLogs.getContent().stream().map(auditLog -> {
                    AuditLogDTO auditLogDTO = new AuditLogDTO();
                    BeanUtilWrapper.copyNonNullProperties(auditLog,auditLogDTO);
                    return auditLogDTO;
                }).toList())
                .currentPage(auditLogs.getNumber() + 1)
                .totalPages(auditLogs.getTotalPages())
                .totalItems(auditLogs.getTotalElements())
                .isFirstPage(auditLogs.isFirst())
                .isLastPage(auditLogs.isLast())
                .build();
    }

    @Override
    public PaginatedResponseDTO<AuditLogDTO> retrieveAllAuditTrail(AutomataSearchRequest automataSearchRequest) {
        Page<AuditLog> auditLogs = auditLogRepository.findAllByAuditType("BACKOFFICE", PageableUtil.getPageable(automataSearchRequest.getPage(),automataSearchRequest.getSize(),"logStartDate"));
        return PaginatedResponseDTO.<AuditLogDTO>builder()
                .content(auditLogs.getContent().stream().map(auditLog -> {
                    AuditLogDTO auditLogDTO = new AuditLogDTO();
                    BeanUtilWrapper.copyNonNullProperties(auditLog,auditLogDTO);
                    return auditLogDTO;
                }).toList())
                .currentPage(auditLogs.getNumber() + 1)
                .totalPages(auditLogs.getTotalPages())
                .totalItems(auditLogs.getTotalElements())
                .isFirstPage(auditLogs.isFirst())
                .isLastPage(auditLogs.isLast())
                .build();
    }

    @Override
    public PaginatedResponseDTO<AuditLogDTO> filterAuditTrailsByActivityAndDateRange(AutomataSearchRequest searchRequest) {
      LocalDateTime startDate = null;
      LocalDateTime endDate = null;
      if (searchRequest.getStartDate() != null || searchRequest.getEndDate() != null) {
        startDate = LocalDate.parse(searchRequest.getStartDate(), DateTimeFormatter.ofPattern(PageableUtil.DATE_FORMAT)).atStartOfDay();
        endDate = PageableUtil.dateChecker(searchRequest.getEndDate(), startDate);
      }

      Page<AuditLog> auditLogs = auditLogRepository.findAllByActivityAndLogStartDateBetween(
                searchRequest.getKey(),
                startDate,
                endDate,
                PageableUtil.getPageable(searchRequest.getPage(), searchRequest.getSize(), "logStartDate")
        );
        return PaginatedResponseDTO.<AuditLogDTO>builder()
                .content(auditLogs.getContent().stream().map(auditLog -> {
                    AuditLogDTO auditLogDTO = new AuditLogDTO();
                    BeanUtilWrapper.copyNonNullProperties(auditLog,auditLogDTO);
                    return auditLogDTO;
                }).toList())
                .currentPage(auditLogs.getNumber() + 1)
                .totalPages(auditLogs.getTotalPages())
                .totalItems(auditLogs.getTotalElements())
                .isFirstPage(auditLogs.isFirst())
                .isLastPage(auditLogs.isLast())
                .build();
    }

    @Override
    public CsvDto<AuditLogDTO> prepareAuditTrailsCSV(CsvDto<AuditLogDTO> parameter) {
      LocalDateTime startDate = null;
      LocalDateTime endDate = null;
      if (parameter.getAutomataSearchRequest().getStartDate() != null
          || parameter.getAutomataSearchRequest().getEndDate() != null) {
        startDate = LocalDate.parse(parameter.getAutomataSearchRequest().getStartDate(),
                    DateTimeFormatter.ofPattern(PageableUtil.DATE_FORMAT)).atStartOfDay();
        endDate = PageableUtil.dateChecker(parameter.getAutomataSearchRequest().getEndDate(), startDate);
      }

      List<AuditLogDTO> data = auditLogRepository
          .findAllByActivityAndLogStartDateBetween(
              parameter.getAutomataSearchRequest().getKey(),
              startDate,
              endDate,
              PageableUtil.getPageable(
                  parameter.getAutomataSearchRequest().getPage(),
                  parameter.getAutomataSearchRequest().getSize(),
                      "logStartDate"
              )
          )
          .getContent().stream()
          .map(this::mapAuditLogToDTO)
          .toList();

      if (data.isEmpty()) {
        throw exceptionHandler.processCustomException("No record found ", "GEN_006", HttpStatus.NOT_FOUND);
      } else {
        parameter.setCsvHeader(new String[] {"Username", "Full Name", "Date and Time", "Activity Type", "Activity Description", "Audit Type"});
        parameter.getFieldMappings().put("Username", AuditLogDTO::getEmail);
        parameter.getFieldMappings().put("Full Name", AuditLogDTO::getName);
        parameter.getFieldMappings().put("Date and Time", auditLogDTO -> auditLogDTO.getLogStartDate().toString());
        parameter.getFieldMappings().put("Activity Type", AuditLogDTO::getActivity);
        parameter.getFieldMappings().put("Activity Description", AuditLogDTO::getActivityDescription);
        parameter.getFieldMappings().put("Audit Type", AuditLogDTO::getAuditType);
        parameter.setData(data);
        parameter.setFileName("AuditTrails".concat("-").concat(LocalDateTime.now().toString()));
        return parameter;
      }
    }

  @Override
  public PaginatedResponseDTO<AuditLogDTO> searchAuditTrails(
      AutomataSearchRequest automataSearchRequest) {
    Specification<AuditLog> specification =
        backOfficeAuditTrailSpecification.buildSpecification(automataSearchRequest);
    Page<AuditLog> auditLogPage = auditLogRepository.findAll(
        specification,
        PageableUtil.getPageable(automataSearchRequest.getPage(), automataSearchRequest.getSize(), "logStartDate"));

    return PaginatedResponseDTO.<AuditLogDTO>builder()
        .content(auditLogPage.getContent().stream()
            .map(this::mapAuditLogToDTO).toList())
        .currentPage(auditLogPage.getNumber() + 1)
        .totalPages(auditLogPage.getTotalPages())
        .totalItems(auditLogPage.getTotalElements())
        .isFirstPage(auditLogPage.isFirst())
        .isLastPage(auditLogPage.isLast())
        .build();
  }

  @Override
  public List<LogActivityDTO> fetchLogActivityTypes() {
     return auditLogRepository.findDistinctActivityTypes().stream()
          .map(LogActivityDTO::new)
          .toList();
  }

  private AuditLogDTO mapAuditLogToDTO(AuditLog auditLog) {
      AuditLogDTO auditLogDTO = new AuditLogDTO();
      BeanUtilWrapper.copyNonNullProperties(auditLog, auditLogDTO);
      return auditLogDTO;
    }


}
