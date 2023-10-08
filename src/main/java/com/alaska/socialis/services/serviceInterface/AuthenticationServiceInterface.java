package com.alaska.socialis.services.serviceInterface;

import java.util.Map;

import org.springframework.validation.BindingResult;

import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.TokenExpiredException;
import com.alaska.socialis.exceptions.UnauthorizedRequestException;
import com.alaska.socialis.exceptions.UserAlreadyExistException;
import com.alaska.socialis.model.TokenRequest;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.requestModel.EmailValidationTokenRequest;
import com.alaska.socialis.model.requestModel.UserEmailValidationRequest;
import com.alaska.socialis.model.requestModel.UsernameValidationRequest;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationServiceInterface {
        public User registerUser(BindingResult validationResult, User user)
                        throws ValidationErrorsException, UserAlreadyExistException;

        public User authenticateUser(BindingResult validationResult, User user) throws ValidationErrorsException;

        public Map<String, String> requestAccessToken(TokenRequest refreshToken, HttpServletRequest request)
                        throws UnauthorizedRequestException, TokenExpiredException;

        public User updateLoginCount(User user);

        public Boolean validateEmailAddress(UserEmailValidationRequest userEmail,
                        BindingResult validationBindingResult) throws ValidationErrorsException;

        public Boolean validateUsername(UsernameValidationRequest username,
                        BindingResult validationBindingResult) throws ValidationErrorsException;

        public void saveEmailVerificationToken(User user, String token);

        public Boolean verifyEmailToken(EmailValidationTokenRequest emailToken, BindingResult validationResult)
                        throws ValidationErrorsException, EntityNotFoundException;
}
