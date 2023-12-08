package com.alaska.socialis.model;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.AccessType;

import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private NotificationActivityType activityType;

    @ManyToOne()
    @JoinColumn(name = "source_id", referencedColumnName = "id")
    private User source;

    private Long targetId;

    private NotificationTargetType targetType;

    private boolean isRead = false;

    @Column(name = "read_at", nullable = true)
    private Date readAt;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public boolean getRead() {
        return this.isRead;
    }
}
