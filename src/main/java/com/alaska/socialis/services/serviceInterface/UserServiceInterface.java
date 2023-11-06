package com.alaska.socialis.services.serviceInterface;

import java.io.IOException;
import java.util.Set;

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

        public void updateUserCoverImage(Long userId, MultipartFile multipartFile)
                        throws EntityNotFoundException, IOException;

        public void updateUserProfileImage(Long userId, MultipartFile multipartFile)
                        throws EntityNotFoundException, IOException;

        public void updateUserInfo(Long userId, UserInfoRequeset requestBody, BindingResult validationResult)
                        throws EntityNotFoundException, ValidationErrorsException;

        public UserSummaryDto fetchUserInformationByUsername(String username) throws EntityNotFoundException;

        public UserDto fetchUserInfoFullInformation(String username) throws EntityNotFoundException;

        public void followUser(Long followerId, Long followingId);

        public Set<UserSummaryDto> fetchAllUserFollowers(String username) throws EntityNotFoundException;

        public Set<UserSummaryDto> fetchAllUserFollowing(String username) throws EntityNotFoundException;
}
