package com.digicore.automata.data.lib.modules.common.audit_trail.service;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import com.digicore.automata.data.lib.modules.common.audit_trail.dto.LogActivityDTO;
import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.automata.data.lib.modules.common.util.AutomataSearchRequest;
import com.digicore.registhentication.common.dto.response.PaginatedResponseDTO;
import com.digicore.request.processor.dto.AuditLogDTO;
import java.util.List;

public interface AuditTrailService {

 PaginatedResponseDTO<AuditLogDTO> retrieveSelfAuditTrail(AutomataSearchRequest automataSearchRequest);
 PaginatedResponseDTO<AuditLogDTO> retrieveAllAuditTrail(AutomataSearchRequest automataSearchRequest);
 PaginatedResponseDTO<AuditLogDTO> filterAuditTrailsByActivityAndDateRange(AutomataSearchRequest automataSearchRequest);
 CsvDto<AuditLogDTO> prepareAuditTrailsCSV(CsvDto<AuditLogDTO> parameter);
 PaginatedResponseDTO<AuditLogDTO> searchAuditTrails(AutomataSearchRequest automataSearchRequest);
 List<LogActivityDTO> fetchLogActivityTypes();

}
