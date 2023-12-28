package com.alaska.socialis.model;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import com.alaska.socialis.utils.NotificationActivityType;
import com.alaska.socialis.utils.NotificationTargetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(indexes = {
        @Index(name = "FK_NOTIFICATION_USER_ID", columnList = "user_id"),
        @Index(name = "FK_NOTIFICATION_SOURCE_ID", columnList = "source_id"),
        @Index(name = "FK_NOTIFICATION_TARGET_ID", columnList = "target_id"),
})
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

    @Column(name = "target_id")
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
