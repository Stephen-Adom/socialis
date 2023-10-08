package com.alaska.socialis.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.TokenExpiredException;
import com.alaska.socialis.exceptions.UnauthorizedRequestException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.EmailVerificationToken;
import com.alaska.socialis.model.TokenRequest;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.requestModel.EmailValidationTokenRequest;
import com.alaska.socialis.model.requestModel.UserEmailValidationRequest;
import com.alaska.socialis.model.requestModel.UsernameValidationRequest;
import com.alaska.socialis.repository.EmailVerificationTokenRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.AuthenticationServiceInterface;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationService implements AuthenticationServiceInterface {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Override
    public User registerUser(BindingResult validationResult, User user)
            throws ValidationErrorsException, UserAlreadyExistException {
        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException("User with email already exist", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException("User with username already exist", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User newUser = User.builder().firstname(user.getFirstname()).lastname(user.getLastname())
                .email(user.getEmail()).username(user.getUsername())
                .password(this.passwordEncoder.encode(user.getPassword())).build();

        return this.userRepository.save(newUser);
    }

    @Override
    public User authenticateUser(BindingResult validationResult, User user) throws ValidationErrorsException {
        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        try {
            this.authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        } catch (Exception e) {
            throw new ValidationErrorsException(e.getMessage() + ": Username and/or password is incorrect",
                    HttpStatus.UNAUTHORIZED);
        }

        return (User) this.userRepository.findByUsername(user.getUsername());
    }

    @Override
    public Map<String, String> requestAccessToken(TokenRequest refreshToken, HttpServletRequest request)
            throws UnauthorizedRequestException, TokenExpiredException {

        try {
            String authUsername = this.jwtService.extractUsernameFromToken(refreshToken.getRefreshToken());

            User userDetails = (User) this.userRepository.findByUsername(authUsername);
            Map<String, String> newTokens = new HashMap<String, String>();

            if (this.jwtService.isTokenValid(refreshToken.getRefreshToken(), userDetails)) {
                newTokens.put("accessToken", this.generateJwt(userDetails, request));
                newTokens.put("refreshToken", this.generateRefreshToken(userDetails));

                return newTokens;
            }

            return newTokens;

        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Refresh token is expired", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public User updateLoginCount(User user) {
        user.setLoginCount(user.getLoginCount() + 1);

        return this.userRepository.save(user);
    }

    @Override
    public Boolean validateEmailAddress(UserEmailValidationRequest userEmail, BindingResult validationResult)
            throws ValidationErrorsException {
        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return this.userRepository.existsByEmail(userEmail.getEmail());
    }

    @Override
    public Boolean validateUsername(UsernameValidationRequest username, BindingResult validationResult)
            throws ValidationErrorsException {

        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return this.userRepository.existsByUsername(username.getUsername());
    }

    @Override
    public void saveEmailVerificationToken(User user, String token) {
        EmailVerificationToken verificationToken = new EmailVerificationToken(user, token);
        this.tokenRepository.save(verificationToken);
    }

    @Override
    public Boolean verifyEmailToken(EmailValidationTokenRequest emailToken, BindingResult validationResult)
            throws ValidationErrorsException, EntityNotFoundException {
        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<EmailVerificationToken> tokenExist = this.tokenRepository.findByToken(emailToken.getToken());

        if (tokenExist.isEmpty()) {
            throw new EntityNotFoundException("Token does not exist", HttpStatus.NOT_FOUND);
        }

        Long currentDateInMillis = new Date().getTime();

        if (tokenExist.get().getExpirationTime().getTime() - currentDateInMillis <= 0) {
            this.tokenRepository.delete(tokenExist.get());
            return false;
        }

        User user = tokenExist.get().getUser();

        user.setEnabled(true);

        this.userRepository.save(user);

        return true;
    }

    @Override
    public User resend_verification_link(String userEmail)
            throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findByEmail(userEmail);
        log.info(userEmail);
        // System.out.println(user.get());

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with email " + userEmail + " does not exist", HttpStatus.NOT_FOUND);
        }

        Optional<EmailVerificationToken> verificationToken = this.tokenRepository.findByUserId(user.get().getId());

        if (verificationToken.isPresent()) {
            this.tokenRepository.delete(verificationToken.get());
        }

        return user.get();
    }

    public String generateJwt(User user, HttpServletRequest request) {
        Map<String, Object> userClaims = new HashMap<String, Object>();
        userClaims.put("iss", request.getServerName());
        userClaims.put("roles", user.getAuthorities());

        return this.jwtService.generateToken(userClaims, user);
    }

    public String generateRefreshToken(User user) {
        return this.jwtService.generateRefreshToken(user);
    }

    public String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
