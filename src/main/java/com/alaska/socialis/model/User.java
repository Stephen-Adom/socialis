package com.alaska.socialis.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.alaska.socialis.model.validationGroups.LoginValidationGroup;
import com.alaska.socialis.model.validationGroups.RegisterValidationGroup;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "user_email_unique", columnNames = "email"),
        @UniqueConstraint(name = "user_username_unique", columnNames = "username"),
        @UniqueConstraint(name = "user_uid_unique", columnNames = "uid")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "uid", nullable = false, unique = true)
    private String uid;

    @NotBlank(message = "Firstname is required", groups = RegisterValidationGroup.class)
    private String firstname;

    @NotBlank(message = "Lastname is required", groups = RegisterValidationGroup.class)
    private String lastname;

    @NotBlank(message = "Username is required", groups = { RegisterValidationGroup.class, LoginValidationGroup.class })
    private String username;

    @Email(message = "Enter a valid email address", groups = RegisterValidationGroup.class)
    @NotBlank(message = "Email address is required", groups = RegisterValidationGroup.class)
    private String email;

    @Column(nullable = true, columnDefinition = "VARCHAR(255)")
    private String address;

    @Column(nullable = true)
    private String phonenumber;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @Column(name = "cover_image_url", nullable = true)
    private String coverImageUrl;

    @Column(name = "bio", columnDefinition = "VARCHAR(255)", nullable = true)
    private String bio;

    @NotBlank(message = "Password is required", groups = { RegisterValidationGroup.class, LoginValidationGroup.class })
    private String password;

    @Builder.Default
    private boolean enabled = false;

    @Builder.Default
    private int loginCount = 0;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Post> posts = new ArrayList<Post>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<Comment>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private EmailVerificationToken verificationToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<CommentLike> commentLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<ReplyLike> replyLikes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Bookmark> bookmarks = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public int getLoginCount() {
        return this.loginCount;
    }

    public void setLoginCount(int count) {
        this.loginCount = count;
    }

}
