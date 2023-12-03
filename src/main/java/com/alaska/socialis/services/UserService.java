package com.alaska.socialis.services;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.alaska.socialis.event.UserFollowEvent;
import com.alaska.socialis.exceptions.EntityNotFoundException;
import com.alaska.socialis.exceptions.ValidationErrorsException;
import com.alaska.socialis.model.User;
import com.alaska.socialis.model.UserDto;
import com.alaska.socialis.model.UserFollows;
import com.alaska.socialis.model.dto.UserSummaryDto;
import com.alaska.socialis.model.dto.UserSummaryFollowingDto;
import com.alaska.socialis.model.requestModel.UserInfoRequeset;
import com.alaska.socialis.repository.UserFollowsRepository;
import com.alaska.socialis.repository.UserRepository;
import com.alaska.socialis.services.serviceInterface.UserServiceInterface;

@Service
public class UserService implements UserServiceInterface {

    private static final String COVER_IMAGE_CLOUD_PATH = "socialis/user/cover_images";
    private static final String PROFILE_IMAGE_CLOUD_PATH = "socialis/user/profile_images";
    private static final String UPDATE_LIVE_USER_PATH = "/feed/user/update";
    private static final String ADD_FOLLOWERS_COUNT_PATH = "/feed/followers/count";
    private static final String ADD_FOLLOWING_COUNT_PATH = "/feed/following/count";
    private static final String REMOVE_FOLLOWERS_COUNT_PATH = "/feed/followers/count/remove";
    private static final String REMOVE_FOLLOWING_COUNT_PATH = "/feed/following/count/remove";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private UserFollowsRepository userFollowsRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public UserDetails fetchUserDetailsByUsername(String username) {
        UserDetails user = this.userRepository.findByUsername(username);
        return user;
    }

    @Override
    public void updateUserCoverImage(Long userId, MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        Optional<User> user = this.userRepository.findById(userId);
        String currentImage = user.get().getCoverImageUrl();

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> result = this.imageUploadService.uploadImageToCloud(COVER_IMAGE_CLOUD_PATH,
                multipartFile);

        if (Objects.nonNull(currentImage)) {

            this.executorService.execute(() -> this.imageUploadService
                    .deleteUploadedImage(COVER_IMAGE_CLOUD_PATH + "/", currentImage));
        }

        user.get().setCoverImageUrl((String) result.get("secure_url"));

        User updatedUser = this.userRepository.save(user.get());

        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" + updatedUser.getUsername(),
                this.buildDto(updatedUser));

    }

    @Override
    public void updateUserProfileImage(Long userId, MultipartFile multipartFile)
            throws EntityNotFoundException, IOException {
        Optional<User> user = this.userRepository.findById(userId);
        String currentImage = user.get().getImageUrl();

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> result = this.imageUploadService.uploadImageToCloud(PROFILE_IMAGE_CLOUD_PATH,
                multipartFile);

        if (Objects.nonNull(currentImage)) {

            this.executorService.execute(() -> this.imageUploadService
                    .deleteUploadedImage(PROFILE_IMAGE_CLOUD_PATH + "/", currentImage));
        }

        user.get().setImageUrl((String) result.get("secure_url"));

        User updatedUser = this.userRepository.save(user.get());

        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" + updatedUser.getUsername(),
                this.buildDto(updatedUser));

    }

    @Override
    public void updateUserInfo(Long userId, UserInfoRequeset requestBody, BindingResult validationResult)
            throws EntityNotFoundException, ValidationErrorsException {
        Optional<User> user = this.userRepository.findById(userId);

        if (validationResult.hasErrors()) {
            throw new ValidationErrorsException(validationResult.getFieldErrors(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (user.isEmpty()) {
            throw new EntityNotFoundException("User with id " + userId + " does not exist", HttpStatus.NOT_FOUND);
        }

        User existingUser = user.get();

        if (!existingUser.getFirstname().equals(requestBody.getFirstname()) && !"".equals(requestBody.getFirstname())) {
            existingUser.setFirstname(requestBody.getFirstname());
        }

        if (!existingUser.getLastname().equals(requestBody.getLastname()) && !"".equals(requestBody.getLastname())) {
            existingUser.setLastname(requestBody.getLastname());
        }

        if (!existingUser.getUsername().equals(requestBody.getUsername()) && !"".equals(requestBody.getUsername())) {
            existingUser.setUsername(requestBody.getUsername());
        }

        existingUser.setPhonenumber(requestBody.getPhonenumber());

        existingUser.setAddress(requestBody.getAddress());

        existingUser.setBio(requestBody.getBio());

        User updatedUser = this.userRepository.save(existingUser);

        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH, this.buildDto(updatedUser));
    }

    @Override
    public UserSummaryFollowingDto fetchUserInfoFullInformation(String username) throws EntityNotFoundException {
        User userInfo = (User) this.userRepository.findByUsername(username);
        if (Objects.isNull(userInfo)) {
            throw new EntityNotFoundException("User with username " + username + " does not exist",
                    HttpStatus.NOT_FOUND);
        }

        return this.buildUserSummaryFollowingInfo(userInfo);
    }

    public UserSummaryFollowingDto buildUserSummaryFollowingInfo(User user) {
        UserSummaryFollowingDto userInfo = new UserSummaryFollowingDto();

        user = userRepository.findById(user.getId()).get();

        userInfo.setId(user.getId());
        userInfo.setFirstname(user.getFirstname());
        userInfo.setLastname(user.getLastname());
        userInfo.setImageUrl(user.getImageUrl());
        userInfo.setBio(user.getBio());
        userInfo.setCoverImageUrl(user.getCoverImageUrl());
        userInfo.setAddress(user.getAddress());
        userInfo.setPhonenumber(user.getPhonenumber());
        userInfo.setCreatedAt(user.getCreatedAt());
        userInfo.setUsername(user.getUsername());
        userInfo.setTotalPost(user.getNoOfPosts());
        userInfo.setFollowers(user.getNoOfFollowers());
        userInfo.setFollowing(user.getNoOfFollowing());
        userInfo.setFollowersList(user.getFollowers().stream().map(follower -> follower.getFollower().getUsername())
                .collect(Collectors.toSet()));
        userInfo.setFollowingList(user.getFollowing().stream().map(following -> following.getFollowing().getUsername())
                .collect(Collectors.toSet()));
        return userInfo;
    }

    public UserSummaryDto buildUserSummaryInfo(User user) {
        UserSummaryDto userInfo = new UserSummaryDto();
        userInfo.setId(user.getId());
        userInfo.setFirstname(user.getFirstname());
        userInfo.setLastname(user.getLastname());
        userInfo.setImageUrl(user.getImageUrl());
        userInfo.setBio(user.getBio());
        userInfo.setUsername(user.getUsername());
        userInfo.setTotalPost(user.getNoOfPosts());
        userInfo.setFollowers(user.getNoOfFollowers());
        userInfo.setFollowing(user.getNoOfFollowing());
        return userInfo;
    }

    public UserDto buildDto(User newUser) {
        return UserDto.builder().id(newUser.getId()).uid(newUser.getUid()).firstname(newUser.getFirstname())
                .lastname(newUser.getLastname())
                .email(newUser.getEmail()).username(newUser.getUsername()).createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt()).enabled(newUser.isEnabled()).loginCount(newUser.getLoginCount())
                .imageUrl(newUser.getImageUrl()).bio(newUser.getBio()).coverImageUrl(newUser.getCoverImageUrl())
                .phonenumber(newUser.getPhonenumber()).address(newUser.getAddress())
                .noOfFollowers(newUser.getNoOfFollowers()).noOfPosts(newUser.getNoOfPosts())
                .noOfFollowing(newUser.getNoOfFollowing()).build();
    }

    @Override
    public UserSummaryFollowingDto followUser(Long followerId, Long followingId) {
        Optional<User> follower = this.userRepository.findById(followerId);
        Optional<User> following = this.userRepository.findById(followingId);

        UserFollows userFollows = new UserFollows();
        follower.get().setNoOfFollowing(follower.get().getNoOfFollowing() + 1);
        following.get().setNoOfFollowers(following.get().getNoOfFollowers() + 1);
        userFollows.setFollower(follower.get());
        userFollows.setFollowing(following.get());

        userFollowsRepository.save(userFollows);

        Optional<User> followerUpdate = this.userRepository.findById(follower.get().getId());
        Optional<User> followingUpdate = this.userRepository.findById(following.get().getId());

        UserSummaryFollowingDto followingUpdateDto = this.buildUserSummaryFollowingInfo(followingUpdate.get());
        UserSummaryFollowingDto followerUpdateDto = this.buildUserSummaryFollowingInfo(followerUpdate.get());

        this.eventPublisher.publishEvent(new UserFollowEvent(follower.get(), following.get()));

        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" + follower.get().getUsername(),
                this.buildDto(followerUpdate.get()));
        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" + following.get().getUsername(),
                this.buildDto(followingUpdate.get()));

        this.messagingTemplate.convertAndSend(ADD_FOLLOWING_COUNT_PATH + "-" + follower.get().getUsername(),
                followingUpdateDto);
        this.messagingTemplate.convertAndSend(ADD_FOLLOWERS_COUNT_PATH + "-" + following.get().getUsername(),
                followerUpdateDto);

        return this.buildUserSummaryFollowingInfo(followingUpdate.get());
    }

    @Override
    public UserSummaryFollowingDto unfollowUser(Long followerId, Long followingId) throws EntityNotFoundException {
        Optional<UserFollows> followExist = this.userFollowsRepository.findByFollowerIdAndFollowingId(followerId,
                followingId);

        if (followExist.isEmpty()) {
            throw new EntityNotFoundException("User Follow does not exist", HttpStatus.NOT_FOUND);
        }

        Optional<User> follower = this.userRepository.findById(followerId);
        Optional<User> following = this.userRepository.findById(followingId);

        follower.get().setNoOfFollowing(follower.get().getNoOfFollowing() - 1);
        following.get().setNoOfFollowers(following.get().getNoOfFollowers() - 1);
        userFollowsRepository.delete(followExist.get());

        User followerUpdate = this.userRepository.save(follower.get());
        User followingUpdate = this.userRepository.save(following.get());

        UserSummaryFollowingDto followingUpdateDto = this.buildUserSummaryFollowingInfo(followingUpdate);
        UserSummaryFollowingDto followerUpdateDto = this.buildUserSummaryFollowingInfo(followerUpdate);

        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" + follower.get().getUsername(),
                this.buildDto(followerUpdate));
        this.messagingTemplate.convertAndSend(UPDATE_LIVE_USER_PATH + "-" + following.get().getUsername(),
                this.buildDto(followingUpdate));

        this.messagingTemplate.convertAndSend(REMOVE_FOLLOWING_COUNT_PATH + "-" +
                follower.get().getUsername(), followingUpdateDto);

        this.messagingTemplate.convertAndSend(REMOVE_FOLLOWERS_COUNT_PATH + "-" + following.get().getUsername(),
                followerUpdateDto);

        return this.buildUserSummaryFollowingInfo(followingUpdate);
    }

    @Override
    public Set<UserSummaryFollowingDto> fetchAllUserFollowers(String username) throws EntityNotFoundException {
        User userExist = (User) this.userRepository.findByUsername(username);

        if (Objects.isNull(userExist)) {
            throw new EntityNotFoundException("User with username " + username + " not found", HttpStatus.NOT_FOUND);
        }

        Set<UserFollows> allFollowers = this.userFollowsRepository.findAllByFollowingId(userExist.getId());

        Set<UserSummaryFollowingDto> allUserSummaryInfo = allFollowers.stream()
                .map(follower -> this.buildUserSummaryFollowingInfo(follower.getFollower()))
                .collect(Collectors.toSet());

        return allUserSummaryInfo;
    }

    @Override
    public Set<UserSummaryFollowingDto> fetchAllUserFollowing(String username) throws EntityNotFoundException {
        User userExist = (User) this.userRepository.findByUsername(username);

        if (Objects.isNull(userExist)) {
            throw new EntityNotFoundException("User with username " + username + " not found", HttpStatus.NOT_FOUND);
        }

        Set<UserFollows> allFollowings = this.userFollowsRepository.findAllByFollowerId(userExist.getId());

        Set<UserSummaryFollowingDto> allUserSummaryInfo = allFollowings.stream()
                .map(following -> this.buildUserSummaryFollowingInfo(following.getFollowing()))
                .collect(Collectors.toSet());

        return allUserSummaryInfo;
    }
}
