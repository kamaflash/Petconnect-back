package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(length = 255)
    private String location;

    private UUID organizerId;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    private int attendeesCount;

    @Column(nullable = false)
    private boolean active;

    protected Event() {
        super();
    }

    public Event(String title, String description, LocalDateTime eventDate, String location, UUID organizerId,
            String imageUrl, String category) {
        super();
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.organizerId = organizerId;
        this.imageUrl = imageUrl;
        this.category = category;
        this.attendeesCount = 0;
        this.active = true;
    }

    public void incrementAttendees() {
        this.attendeesCount++;
    }

    public void decrementAttendees() {
        if (this.attendeesCount > 0) {
            this.attendeesCount--;
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public String getLocation() {
        return location;
    }

    public UUID getOrganizerId() {
        return organizerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public int getAttendeesCount() {
        return attendeesCount;
    }

    public boolean isActive() {
        return active;
    }
}