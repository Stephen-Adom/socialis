package com.alaska.socialis.services.serviceInterface;

import java.io.IOException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.model.dto.UserSummaryDto;
import com.alaska.socialis.model.requestModel.UserInfoRequeset;

public interface UserServiceInterface {
        public UserDetails fetchUserDetailsByUsername(String username) throws EntityNotFoundException;

        public UserDto updateUserCoverImage(Long userId, MultipartFile multipartFile)
                        throws EntityNotFoundException, IOException;

        public UserDto updateUserProfileImage(Long userId, MultipartFile multipartFile)
                        throws EntityNotFoundException, IOException;

        public UserDto updateUserInfo(Long userId, UserInfoRequeset requestBody, BindingResult validationResult)
                        throws EntityNotFoundException, ValidationErrorsException;

        public UserSummaryDto fetchUserInformationByUsername(String username) throws EntityNotFoundException;
}
