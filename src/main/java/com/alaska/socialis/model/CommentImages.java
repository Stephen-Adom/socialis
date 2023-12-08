package com.alaska.socialis.model;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comment_images")
public class CommentImages {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String mediaUrl;
    private String mediaType;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Getter(AccessLevel.NONE)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Getter(AccessLevel.NONE)
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "comment_id", referencedColumnName = "id")
    @Getter(AccessLevel.NONE)
    private Comment comment;
}
