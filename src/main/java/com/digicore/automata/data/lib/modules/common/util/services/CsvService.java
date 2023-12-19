package com.digicore.automata.data.lib.modules.common.util.services;

import com.digicore.automata.data.lib.modules.common.dto.CsvDto;
import com.digicore.common.util.CSVUtil;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.function.Function;

import static com.digicore.automata.data.lib.modules.exception.messages.BackOfficeProfileErrorMessage.EXPORT_FORMANT_NOT_SUPPORTED_CODE;
import static com.digicore.automata.data.lib.modules.exception.messages.BackOfficeProfileErrorMessage.EXPORT_FORMANT_NOT_SUPPORTED_MESSAGE;

/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */
@Component
@RequiredArgsConstructor
public class CsvService {
    private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;

    public <T> void prepareCSVExport(
            CsvDto<T> parameter,
            Function<CsvDto<T>, CsvDto<T>> prepareData
    ) {
        if (parameter.getAutomataSearchRequest().getDownloadFormat().equalsIgnoreCase("CSV")) {
            CsvDto<T> response = prepareData.apply(parameter);
            CSVUtil.generateCsv(
                    response.getData(),
                    response.getCsvHeader(),
                    response.getFieldMappings(),
                    response.getResponse(),
                    response.getFileName()
            );
        } else {
            throw exceptionHandler.processCustomException(
                    EXPORT_FORMANT_NOT_SUPPORTED_MESSAGE,
                    EXPORT_FORMANT_NOT_SUPPORTED_CODE,
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE
            );
        }
    }
}