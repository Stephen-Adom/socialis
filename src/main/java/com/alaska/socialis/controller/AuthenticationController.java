package com.alaska.socialis.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.event.RegistrationCompleteEvent;
import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.TokenExpiredException;
import com.alaska.socialis.exceptions.UnauthorizedRequestException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.NewPasswordModel;
import com.alaska.socialis.model.TokenRequest;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.model.dto.AuthResponse;
import com.alaska.socialis.model.requestModel.EmailValidationTokenRequest;
import com.alaska.socialis.model.requestModel.ResetPasswordRequest;
import com.alaska.socialis.model.requestModel.UserEmailValidationRequest;
import com.alaska.socialis.model.requestModel.UsernameValidationRequest;
import com.alaska.socialis.model.validationGroups.LoginValidationGroup;
import com.alaska.socialis.model.validationGroups.RegisterValidationGroup;
import com.alaska.socialis.services.AuthenticationService;
import com.alaska.socialis.services.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ApplicationEventPublisher publisher;

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

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
        executorService.submit(() -> this.publisher
                .publishEvent(new RegistrationCompleteEvent(newUser, this.authService.applicationUrl(request))));

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
        Map<String, Object> responseBody = new HashMap<String, Object>();
        Boolean emailExist = this.authService.validateEmailAddress(userEmail, validationBindingResult);
        responseBody.put("status", HttpStatus.OK);
        responseBody.put("email_exist", emailExist);

        return new ResponseEntity<Map<String, Object>>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/validate_username")
    public ResponseEntity<Map<String, Object>> validateUsername(
            @Valid @RequestBody UsernameValidationRequest username, BindingResult validationBindingResult)
            throws ValidationErrorsException {
        Map<String, Object> responseBody = new HashMap<String, Object>();
        Boolean usernameExist = this.authService.validateUsername(username, validationBindingResult);
        responseBody.put("status", HttpStatus.OK);
        responseBody.put("username_exist", usernameExist);

        return new ResponseEntity<Map<String, Object>>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/verify_email_token")
    public ResponseEntity<Map<String, Object>> verifyEmailToken(
            @RequestBody @Valid EmailValidationTokenRequest emailToken,
            BindingResult validationResult) throws ValidationErrorsException, EntityNotFoundException {
        Boolean tokenIsValid = this.authService.verifyEmailToken(emailToken, validationResult);

        Map<String, Object> responseBody = new HashMap<String, Object>();
        responseBody.put("token_valid", tokenIsValid);
        responseBody.put("status", HttpStatus.OK);

        return new ResponseEntity<Map<String, Object>>(responseBody, HttpStatus.OK);
    }

    @GetMapping("/resend_verification_link")
    public ResponseEntity<Map<String, Object>> resend_verification_link(
            @RequestParam(name = "email", required = true) String userEmail,
            HttpServletRequest request)
            throws EntityNotFoundException {

        User user = this.authService.resend_verification_link(userEmail);

        this.publisher.publishEvent(new RegistrationCompleteEvent(user, this.authService.applicationUrl(request)));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Email verification link sent");
        responseBody.put("status", HttpStatus.OK);

        return new ResponseEntity<Map<String, Object>>(responseBody, HttpStatus.OK);

    }

    @GetMapping("/resend_verification_token")
    public ResponseEntity<Map<String, Object>> resend_verification_token(
            @RequestParam("token") String verificationToken, HttpServletRequest request)
            throws UnauthorizedRequestException {
        User user = this.authService.resend_verification_token(verificationToken);

        this.publisher.publishEvent(new RegistrationCompleteEvent(user, this.authService.applicationUrl(request)));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Email verification link sent");
        responseBody.put("status", HttpStatus.OK);

        return new ResponseEntity<Map<String, Object>>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/reset_password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody @Valid ResetPasswordRequest userEmail,
            BindingResult validationResult, HttpServletRequest request)
            throws ValidationErrorsException, EntityNotFoundException {
        this.authService.resetPassword(userEmail, validationResult, request);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Password Reset link sent");
        responseBody.put("status", HttpStatus.OK);

        return new ResponseEntity<Map<String, Object>>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/change_password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam(name = "token", required = true) String passwordToken,
            @RequestBody @Valid NewPasswordModel newPassword, BindingResult validationResult)
            throws UnauthorizedRequestException, ValidationErrorsException {
        this.authService.changePassword(passwordToken, newPassword, validationResult);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "New Password has been set successfully");
        responseBody.put("status", HttpStatus.OK);

        return new ResponseEntity<Map<String, Object>>(responseBody, HttpStatus.OK);
    }

    private UserDto buildDto(User newUser) {
        return UserDto.builder().id(newUser.getId()).firstname(newUser.getFirstname()).lastname(newUser.getLastname())
                .email(newUser.getEmail()).username(newUser.getUsername()).createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt()).enabled(newUser.isEnabled()).loginCount(newUser.getLoginCount())
                .imageUrl(newUser.getImageUrl())
                .build();
    }
}
