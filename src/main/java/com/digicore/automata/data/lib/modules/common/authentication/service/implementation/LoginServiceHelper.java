package com.digicore.automata.data.lib.modules.common.authentication.service.implementation;
/**
 * @author Joy Osayi
 * @createdOn Dec-15(Fri)-2023
 */

import static com.digicore.automata.data.lib.modules.common.constants.SystemConstants.CHECKER_ROLE_NAME;
import static com.digicore.automata.data.lib.modules.common.constants.SystemConstants.MAKER_ROLE_NAME;

import com.digicore.automata.data.lib.modules.common.authentication.dto.UserProfileDTO;
import com.digicore.automata.data.lib.modules.common.authentication.util.CommonUtil;
import com.digicore.config.security.JwtHelper;
import com.digicore.otp.enums.OtpType;
import com.digicore.otp.service.OtpService;
import com.digicore.registhentication.authentication.dtos.request.LoginRequestDTO;
import com.digicore.registhentication.authentication.dtos.response.LoginResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceHelper {
  private final JwtHelper jwtHelper;
  private final OtpService otpService;

  public LoginResponse getLoginResponse(
      LoginRequestDTO loginRequestDTO, UserProfileDTO userDetails) {
    if (MAKER_ROLE_NAME.equalsIgnoreCase(userDetails.getAssignedRole())
        || CHECKER_ROLE_NAME.equalsIgnoreCase(userDetails.getAssignedRole()))
      userDetails.setDefaultPassword(false);
    Map<String, String> claims =
        userDetails.isDefaultPassword()
            ? CommonUtil.getClaims(
                loginRequestDTO.getUsername(),
                userDetails,
                otpService.store(userDetails.getEmail(), OtpType.PASSWORD_UPDATE))
            : CommonUtil.getClaims(loginRequestDTO.getUsername(), userDetails);
    Map<String, Object> additionalInformation = new HashMap<>();
    additionalInformation.put("firstName", userDetails.getFirstName());
    additionalInformation.put("email", userDetails.getEmail());
    additionalInformation.put("role", userDetails.getAssignedRole());
    additionalInformation.put(
        "name", userDetails.getFirstName().concat(" ").concat(userDetails.getLastName()));
    additionalInformation.put("isDefaultPassword", userDetails.isDefaultPassword());
    additionalInformation.put("isEnabled2FA", userDetails.isEnabled2FA());
    return LoginResponse.builder()
        .accessToken(jwtHelper.createJwtForClaims(loginRequestDTO.getUsername(), claims))
        .additionalInformation(additionalInformation)
        .build();
  }
}
