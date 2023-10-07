package com.alaska.socialis.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.event.RegistrationCompleteEvent;
import com.alaska.socialis.exceptions.TokenExpiredException;
import com.alaska.socialis.exceptions.UnauthorizedRequestException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.TokenRequest;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.model.dto.AuthResponse;
import com.alaska.socialis.model.requestModel.UserEmailValidationRequest;
import com.alaska.socialis.model.requestModel.UsernameValidationRequest;
import com.alaska.socialis.model.validationGroups.LoginValidationGroup;
import com.alaska.socialis.model.validationGroups.RegisterValidationGroup;
import com.alaska.socialis.services.AuthenticationService;
import com.alaska.socialis.services.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(
            @Validated(RegisterValidationGroup.class) @RequestBody User user,
            BindingResult validationResult, HttpServletRequest request)
            throws ValidationErrorsException, UserAlreadyExistException {

        User newUser = this.authService.registerUser(validationResult, user);

        String token = this.authService.generateJwt(newUser, request);
        String refreshToken = this.jwtService.generateRefreshToken(newUser);

        AuthResponse responseBody = AuthResponse.builder().status(HttpStatus.CREATED)
                .data(this.buildDto(newUser)).accessToken(token).refreshToken(refreshToken).build();

        // ! dispatch an event to send email for account created
        this.publisher.publishEvent(new RegistrationCompleteEvent(newUser, this.authService.applicationUrl(request)));

        return new ResponseEntity<AuthResponse>(responseBody, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Validated(LoginValidationGroup.class) @RequestBody User user,
            BindingResult validationResult, HttpServletRequest request) throws ValidationErrorsException {
        User authUser = this.authService.authenticateUser(validationResult, user);

        User updatedAuthUser = this.authService.updateLoginCount(authUser);

        String token = this.authService.generateJwt(updatedAuthUser, request);
        String refreshToken = this.jwtService.generateRefreshToken(updatedAuthUser);

        AuthResponse responseBody = AuthResponse.builder().status(HttpStatus.OK)
                .data(this.buildDto(updatedAuthUser)).accessToken(token).refreshToken(refreshToken).build();

        return new ResponseEntity<AuthResponse>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<Map<String, String>> requestAccessToken(@Valid @RequestBody TokenRequest refreshToken,
            BindingResult validationResult, HttpServletRequest request)
            throws UnauthorizedRequestException, TokenExpiredException {

        if (validationResult.hasErrors()) {
            throw new UnauthorizedRequestException("Invalid Request. Refresh Token not available",
                    HttpStatus.BAD_REQUEST);
        }

        Map<String, String> accessToken = this.authService.requestAccessToken(refreshToken, request);

        return new ResponseEntity<Map<String, String>>(accessToken, HttpStatus.OK);
    }

    @PostMapping("/validate_email")
    public ResponseEntity<Map<String, Object>> validateEmailAddress(
            @Valid @RequestBody UserEmailValidationRequest userEmail, BindingResult validationBindingResult)
            throws ValidationErrorsException {
        Map<String, Object> errorBody = new HashMap<String, Object>();
        Boolean emailExist = this.authService.validateEmailAddress(userEmail, validationBindingResult);
        errorBody.put("status", HttpStatus.OK);
        errorBody.put("email_exist", emailExist);

        return new ResponseEntity<Map<String, Object>>(errorBody, HttpStatus.OK);
    }

    @PostMapping("/validate_username")
    public ResponseEntity<Map<String, Object>> validateUsername(
            @Valid @RequestBody UsernameValidationRequest username, BindingResult validationBindingResult)
            throws ValidationErrorsException {
        Map<String, Object> errorBody = new HashMap<String, Object>();
        Boolean usernameExist = this.authService.validateUsername(username, validationBindingResult);
        errorBody.put("status", HttpStatus.OK);
        errorBody.put("username_exist", usernameExist);

        return new ResponseEntity<Map<String, Object>>(errorBody, HttpStatus.OK);
    }

    private UserDto buildDto(User newUser) {
        return UserDto.builder().id(newUser.getId()).firstname(newUser.getFirstname()).lastname(newUser.getLastname())
                .email(newUser.getEmail()).username(newUser.getUsername()).createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt()).enabled(newUser.isEnabled()).loginCount(newUser.getLoginCount())
                .build();
    }
}
