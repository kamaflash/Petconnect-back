package com.petconnect.users.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @Column(nullable = false, unique = true)
    private UUID authUserId;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String bio;

    @Column(length = 255)
    private String avatarUrl;

    private LocalDate dateOfBirth;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(nullable = false)
    private boolean profilePublic;

    @Column(nullable = false)
    private boolean notificationsEnabled;

    protected UserProfile() {
        super();
    }

    public UserProfile(UUID authUserId, String firstName, String lastName) {
        super();
        this.authUserId = authUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePublic = true;
        this.notificationsEnabled = true;
    }

    public void updateProfile(String firstName, String lastName, String phone, String bio, LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.bio = bio;
        this.dateOfBirth = dateOfBirth;
    }

    public void updateLocation(String city, String country) {
        this.city = city;
        this.country = country;
    }

    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setProfilePublic(boolean profilePublic) {
        this.profilePublic = profilePublic;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public UUID getAuthUserId() {
        return authUserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public boolean isProfilePublic() {
        return profilePublic;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
}