package com.digicore.automata.data.lib.modules.backoffice.authentication.service.implementation;

import com.digicore.automata.data.lib.modules.backoffice.authentication.model.BackOfficeUserAuthProfile;
import com.digicore.automata.data.lib.modules.backoffice.authentication.repository.BackOfficeUserAuthProfileRepository;
import com.digicore.automata.data.lib.modules.common.authentication.dto.UserProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.dto.VerifyCodeLoginDTO;
import com.digicore.automata.data.lib.modules.common.authentication.service.implementation.LoginServiceHelper;
import com.digicore.automata.data.lib.modules.common.settings.service.SettingService;
import com.digicore.common.util.BeanUtilWrapper;
import com.digicore.common.util.ClientUtil;
import com.digicore.registhentication.authentication.dtos.request.LoginRequestDTO;
import com.digicore.registhentication.authentication.dtos.response.LoginResponse;
import com.digicore.registhentication.authentication.enums.AuthenticationType;
import com.digicore.registhentication.authentication.services.AuthenticatorService;
import com.digicore.registhentication.exceptions.ExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.digicore.automata.data.lib.modules.exception.messages.LoginErrorMessage.LOGIN_FAILED_CODE_KEY;
import static com.digicore.automata.data.lib.modules.exception.messages.LoginErrorMessage.LOGIN_FAILED_MESSAGE_KEY;
import static com.digicore.automata.data.lib.modules.exception.messages.RegistrationErrorMessage.PROFILE_NOT_EXIST_CODE_KEY;
import static com.digicore.automata.data.lib.modules.exception.messages.RegistrationErrorMessage.PROFILE_NOT_EXIST_MESSAGE_KEY;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticatorServiceImpl implements AuthenticatorService<String> {

    private final BackOfficeUserAuthProfileRepository backOfficeUserAuthProfileRepository;
    private final BackOfficeUserAuthServiceImpl backOfficeUserAuthServiceImpl;
    private final LoginServiceHelper loginServiceHelper;
    private final ExceptionHandler<String, String, HttpStatus, String> exceptionHandler;
    private final SettingService settingService;
    @Value("${APP.QR_PREFIX:https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=\"}")
    public static String QR_PREFIX;

    @SneakyThrows
    @Override
    public String generateSecret() {
        String username = ClientUtil.getLoggedInUsername();
        if(ClientUtil.getLoggedInUsername() == "SYSTEM"){
            return null;
        }

        String secret = Base32.random();
        BackOfficeUserAuthProfile userFoundInDB =
                backOfficeUserAuthProfileRepository
                        .findFirstByUsernameOrderByCreatedDate(username)
                        .orElseThrow(
                                () ->
                                        exceptionHandler.processCustomException(
                                                settingService.retrieveValue(PROFILE_NOT_EXIST_MESSAGE_KEY),
                                                settingService.retrieveValue(PROFILE_NOT_EXIST_CODE_KEY),
                                                HttpStatus.UNAUTHORIZED));

        userFoundInDB.setSecret(secret);
        backOfficeUserAuthProfileRepository.save(userFoundInDB);

        String QRCode = QR_PREFIX + URLEncoder.encode(String.format(
                        "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                        "AUTOMATA", username, userFoundInDB.getSecret(), "AUTOMATA"),
                "UTF-8");

        Map<String, String> response = new HashMap<>();
        response.put("secret", secret);
        response.put("QRCode", QRCode);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(response);
    }

    @SneakyThrows
    @Override
    public void verifyCode(String code) {
        String username = ClientUtil.getLoggedInUsername();
        if(ClientUtil.getLoggedInUsername() != "SYSTEM"){
            BackOfficeUserAuthProfile userFoundInDB =
                    backOfficeUserAuthProfileRepository
                            .findFirstByUsernameOrderByCreatedDate(username)
                            .orElseThrow(
                                    () ->
                                            exceptionHandler.processCustomException(
                                                    settingService.retrieveValue(PROFILE_NOT_EXIST_MESSAGE_KEY),
                                                    settingService.retrieveValue(PROFILE_NOT_EXIST_CODE_KEY),
                                                    HttpStatus.UNAUTHORIZED));

            Totp totp = new Totp(userFoundInDB.getSecret());
            if (!isValidLong(code) || !totp.verify(code)) {
                throw new Exception("Verification failed");
            }
        }
    }

    public LoginResponse verifyLoginCode(VerifyCodeLoginDTO request) {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        BeanUtilWrapper.copyNonNullProperties(request, loginRequestDTO);
        if(loginRequestDTO.getAuthenticationType() == AuthenticationType.SOFT_TOKEN){
            System.out.println(ClientUtil.getLoggedInUsername());
            loginRequestDTO.setUsername(ClientUtil.getLoggedInUsername());
            UserProfileDTO userDetails =
                    (UserProfileDTO) backOfficeUserAuthServiceImpl.loadUserByUsername(loginRequestDTO.getUsername());
            System.out.println(userDetails);
            try {
                this.verifyCode(loginRequestDTO.getOtp());
                return loginServiceHelper.getLoginResponse(loginRequestDTO, userDetails);
            } catch (Exception e) {
                exceptionHandler.processCustomException(
                        settingService.retrieveValue(LOGIN_FAILED_MESSAGE_KEY),
                        settingService.retrieveValue(LOGIN_FAILED_CODE_KEY),
                        HttpStatus.UNAUTHORIZED,
                        settingService.retrieveValue(LOGIN_FAILED_CODE_KEY));System.out.println("userDetails");
                return null;
            }
        }
        exceptionHandler.processCustomException(
                settingService.retrieveValue(LOGIN_FAILED_MESSAGE_KEY),
                settingService.retrieveValue(LOGIN_FAILED_CODE_KEY),
                HttpStatus.UNAUTHORIZED,
                settingService.retrieveValue(LOGIN_FAILED_CODE_KEY));
        return null;
    }

    private boolean isValidLong(String code) {
        try {
            Long.parseLong(code);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public boolean enable2fa(){
        String username = ClientUtil.getLoggedInUsername();
        if(ClientUtil.getLoggedInUsername() == "SYSTEM"){
            return false;
        }

        BackOfficeUserAuthProfile userFoundInDB =
                backOfficeUserAuthProfileRepository
                        .findFirstByUsernameOrderByCreatedDate(username)
                        .orElseThrow(
                                () ->
                                        exceptionHandler.processCustomException(
                                                settingService.retrieveValue(PROFILE_NOT_EXIST_MESSAGE_KEY),
                                                settingService.retrieveValue(PROFILE_NOT_EXIST_CODE_KEY),
                                                HttpStatus.UNAUTHORIZED));

        if(!userFoundInDB.getSecret().isEmpty() && userFoundInDB.getSecret() != null){
            userFoundInDB.setEnabled2fa(true);
            backOfficeUserAuthProfileRepository.save(userFoundInDB);
            return true;
        }
        return false;
    }

    public BackOfficeUserAuthProfile disable2fa(){
        String username = ClientUtil.getLoggedInUsername();
        if(ClientUtil.getLoggedInUsername() != "SYSTEM"){
            BackOfficeUserAuthProfile userFoundInDB =
                    backOfficeUserAuthProfileRepository
                            .findFirstByUsernameOrderByCreatedDate(username)
                            .orElseThrow(
                                    () ->
                                            exceptionHandler.processCustomException(
                                                    settingService.retrieveValue(PROFILE_NOT_EXIST_MESSAGE_KEY),
                                                    settingService.retrieveValue(PROFILE_NOT_EXIST_CODE_KEY),
                                                    HttpStatus.UNAUTHORIZED));

            userFoundInDB.setEnabled2fa(false);
            backOfficeUserAuthProfileRepository.save(userFoundInDB);
            return userFoundInDB;
        }
        return null;
    }

    public boolean test2fa(String code){
        String username = ClientUtil.getLoggedInUsername();
        if(ClientUtil.getLoggedInUsername() != "SYSTEM") {
            BackOfficeUserAuthProfile userFoundInDB =
                    backOfficeUserAuthProfileRepository
                            .findFirstByUsernameOrderByCreatedDate(username)
                            .orElseThrow(
                                    () ->
                                            exceptionHandler.processCustomException(
                                                    settingService.retrieveValue(PROFILE_NOT_EXIST_MESSAGE_KEY),
                                                    settingService.retrieveValue(PROFILE_NOT_EXIST_CODE_KEY),
                                                    HttpStatus.UNAUTHORIZED));
            try {
                this.verifyCode(code);
                userFoundInDB.setEnabled2fa(true);
                backOfficeUserAuthProfileRepository.save(userFoundInDB);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

}
