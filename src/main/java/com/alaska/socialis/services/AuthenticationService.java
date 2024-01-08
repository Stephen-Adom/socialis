package com.alaska.socialis.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.reactive.function.client.WebClient;

import com.alaska.socialis.event.ResetPasswordEvent;
import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.TokenExpiredException;
import com.alaska.socialis.exceptions.UnauthorizedRequestException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.EmailVerificationToken;
import com.alaska.socialis.model.NewPasswordModel;
import com.alaska.socialis.model.ResetPasswordModel;
import com.alaska.socialis.model.RevokedTokens;
import com.alaska.socialis.model.TokenRequest;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.UserInfoMono;
import com.alaska.socialis.model.requestModel.EmailValidationTokenRequest;
import com.alaska.socialis.model.requestModel.GoogleUserRequest;
import com.alaska.socialis.model.requestModel.PhoneValidationRequeset;
import com.alaska.socialis.model.requestModel.ResetPasswordRequest;
import com.alaska.socialis.model.requestModel.UserEmailValidationRequest;
import com.alaska.socialis.model.requestModel.UsernameValidationRequest;
import com.alaska.socialis.repository.EmailVerificationTokenRepository;
import com.alaska.socialis.repository.ResetPasswordRepository;
import com.alaska.socialis.repository.RevokedTokenRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.AuthenticationServiceInterface;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

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

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ResetPasswordRepository resetPasswordRepository;

    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-secret}")
    private String clientSecret;

    private final WebClient userInfoClient;

    public AuthenticationService(WebClient userInfoClient) {
        this.userInfoClient = userInfoClient;
    }

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

        String uid = "usr-" + UUID.randomUUID().toString();

        User newUser = User.builder().uid(uid).firstname(user.getFirstname()).lastname(user.getLastname())
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
    public User validateGoogleUserAndSignInUser(GoogleUserRequest googleUserRequest,
            BindingResult validationResult) throws ValidationErrorsException {

        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<User> userExist = this.userRepository.findByEmail(googleUserRequest.getEmail());

        if (userExist.isEmpty()) {
            String uid = "usr-" + UUID.randomUUID().toString();
            String username = googleUserRequest.getEmail().split("@")[0];

            User newUser = User.builder().uid(uid).firstname(googleUserRequest.getFirstName())
                    .lastname(googleUserRequest.getLastName())
                    .email(googleUserRequest.getEmail()).username(username)
                    .password(this.passwordEncoder.encode(googleUserRequest.getEmail()))
                    .imageUrl(googleUserRequest.getPhotoUrl()).loginCount(1).enabled(true).build();

            return this.userRepository.save(newUser);
        } else {

            return this.updateLoginCount(userExist.get());
        }
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
    public Boolean validatePhonenumber(PhoneValidationRequeset phone, BindingResult validationBindingResult)
            throws ValidationErrorsException {

        if (validationBindingResult.hasErrors()) {
            throw new ValidationErrorsException(validationBindingResult.getFieldErrors(),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return this.userRepository.existsByPhonenumber(phone.getPhonenumber());
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
            RevokedTokens token = new RevokedTokens();
            token.setToken(tokenExist.get().getToken());
            token.setUser(tokenExist.get().getUser());

            this.revokedTokenRepository.save(token);
            this.tokenRepository.delete(tokenExist.get());
            return false;
        }

        User user = tokenExist.get().getUser();

        user.setEnabled(true);

        this.userRepository.save(user);
        this.tokenRepository.delete(tokenExist.get());

        return true;
    }

    @Override
    public User resend_verification_link(String userEmail)
            throws EntityNotFoundException {
        Optional<User> user = this.userRepository.findByEmail(userEmail);

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with email " + userEmail + " does not exist", HttpStatus.NOT_FOUND);
        }

        Optional<EmailVerificationToken> verificationToken = this.tokenRepository.findByUserId(user.get().getId());

        if (verificationToken.isPresent()) {
            this.tokenRepository.delete(verificationToken.get());
        }

        return user.get();
    }

    @Override
    public User resend_verification_token(String verificationToken) throws UnauthorizedRequestException {
        Optional<RevokedTokens> tokenExist = this.revokedTokenRepository.findByToken(verificationToken);
        if (tokenExist.isEmpty()) {
            throw new UnauthorizedRequestException("Invalid Token request. Email already validated",
                    HttpStatus.BAD_REQUEST);
        }

        User user = tokenExist.get().getUser();
        this.revokedTokenRepository.delete(tokenExist.get());
        return user;
    }

    @Override
    public void resetPassword(ResetPasswordRequest userEmail, BindingResult validationResult,
            HttpServletRequest request)
            throws ValidationErrorsException, EntityNotFoundException {
        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<User> user = this.userRepository.findByEmail(userEmail.getEmail());

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with email " + userEmail.getEmail() + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        this.publisher.publishEvent(new ResetPasswordEvent(user.get(), this.applicationUrl(request)));
    }

    @Override
    public void changePassword(String passwordToken, NewPasswordModel newPassword, BindingResult validationResult)
            throws UnauthorizedRequestException, ValidationErrorsException {
        Optional<ResetPasswordModel> resetPassword = this.resetPasswordRepository.findByToken(passwordToken);

        if (resetPassword.isEmpty()) {
            throw new UnauthorizedRequestException("Invalid Token", HttpStatus.UNAUTHORIZED);
        }

        ResetPasswordModel resetPasswordExist = resetPassword.get();

        if (resetPasswordExist.getExpirationDate().getTime() - new Date().getTime() <= 0) {
            this.resetPasswordRepository.delete(resetPasswordExist);
            throw new UnauthorizedRequestException(
                    "Invalid Token: Token provided has expiried. Provide your email to get a new token",
                    HttpStatus.UNAUTHORIZED);
        }

        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        User user = resetPasswordExist.getUser();
        user.setPassword(this.passwordEncoder.encode(newPassword.getPassword()));
        this.userRepository.save(user);
        this.resetPasswordRepository.delete(resetPasswordExist);
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
        return "http://localhost:4200/auth" + request.getContextPath();
    }

    @Override
    public User authenticateGoogleInfo(String code, HttpServletRequest request) throws UnauthorizedRequestException {
        try {
            String token = new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(), new GsonFactory(), clientId,
                    clientSecret, code, "http://localhost:4200/auth/login").execute().getAccessToken();
            UserInfoMono user = userInfoClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/oauth2/v3/userinfo").queryParam("access_token", token).build())
                    .retrieve()
                    .bodyToMono(UserInfoMono.class).block();

            Optional<User> userExist = this.userRepository.findByEmail(user.email());
            User newUser;

            if (userExist.isEmpty()) {
                newUser = this.registerNewGoogleUser(user);
            } else {
                newUser = this.signInGoogleUser(userExist.get(), request);
            }

            return newUser;

        } catch (Exception e) {
            throw new UnauthorizedRequestException("User Unauthorized " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    public User registerNewGoogleUser(UserInfoMono user) {
        String uid = "usr-" + UUID.randomUUID().toString();
        String defaultUsername = user.family_name() + uid.substring(0, 5);

        User newUser = User.builder().uid(uid).firstname(user.given_name()).lastname(user.family_name())
                .email(user.email()).username(defaultUsername)
                .password("").enabled(user.email_verified()).build();

        return this.userRepository.save(newUser);
    }

    public User signInGoogleUser(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(), user.getPassword(),
                user.getAuthorities());
        authToken.setDetails(
                new WebAuthenticationDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);

        return user;
    }
}
