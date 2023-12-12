package com.alaska.socialis.services;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OidcUser loadUser(OAuth2UserRequest userRequest) {
        OidcUser oidcUser = (OidcUser) super.loadUser(userRequest);
        // Process oidcUser and generate JWT token
        // String jwtToken = generateJwtToken(oidcUser);
        // You can store the token in the database or send it back to the frontend
        return oidcUser;
    }

    private String generateJwtToken(OidcUser oidcUser, Map<String, String> userInfo) {
        return "";
    }

    private Set<GrantedAuthority> buildAuthorities() {
        // Build authorities based on your application's roles and permissions
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.addAll(AuthorityUtils.createAuthorityList("ROLE_USER"));
        return authorities;
    }
}
