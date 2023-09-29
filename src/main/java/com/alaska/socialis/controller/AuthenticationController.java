package com.alaska.socialis.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alaska.socialis.exceptions.TokenExpiredException;
import com.alaska.socialis.exceptions.UnauthorizedRequestException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.TokenRequest;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.model.dto.AuthResponse;
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

        // ! dispatch an event to send email for accout created

        return new ResponseEntity<AuthResponse>(responseBody, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Validated(LoginValidationGroup.class) @RequestBody User user,
            BindingResult validationResult, HttpServletRequest request) throws ValidationErrorsException {
        User authUser = this.authService.authenticateUser(validationResult, user);

        String token = this.authService.generateJwt(authUser, request);
        String refreshToken = this.jwtService.generateRefreshToken(authUser);

        AuthResponse responseBody = AuthResponse.builder().status(HttpStatus.OK)
                .data(this.buildDto(authUser)).accessToken(token).refreshToken(refreshToken).build();

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

    private UserDto buildDto(User newUser) {
        return UserDto.builder().id(newUser.getId()).firstname(newUser.getFirstname()).lastname(newUser.getLastname())
                .email(newUser.getEmail()).username(newUser.getUsername()).createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt()).enabled(newUser.isEnabled()).build();
    }
}
