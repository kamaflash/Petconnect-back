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

    @Column(length = 255)
    private String coverImageUrl;

    private LocalDate dateOfBirth;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(nullable = false)
    private boolean profilePublic;

    @Column(nullable = false)
    private boolean notificationsEnabled;

    @Column(nullable = false, length = 20)
    private String profileType;

    // Campos específicos por tipo de perfil
    @Column(length = 100)
    private String specialty; // Para vet y trainer

    @Column(length = 50)
    private String licenseNumber; // Para vet

    @Column(length = 100)
    private String charityNumber; // Para shelter

    @Column(length = 100)
    private String storeName; // Para seller

    @Column(length = 255)
    private String website;

    protected UserProfile() {
        super();
    }

    public UserProfile(UUID authUserId, String firstName, String lastName, String profileType) {
        super();
        this.authUserId = authUserId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileType = profileType;
        this.avatarUrl = "https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692382/s1evvoaojmehmibovsmo.png";
        this.coverImageUrl = "https://res.cloudinary.com/dxjcjee8f/image/upload/v1783692381/usiszz095a8bvfryznaz.png";
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

    public void updateTypeSpecificFields(String specialty, String licenseNumber, String charityNumber, String storeName, String website) {
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
        this.charityNumber = charityNumber;
        this.storeName = storeName;
        this.website = website;
    }

    public void updateLocation(String city, String country) {
        this.city = city;
        this.country = country;
    }

    public void updateAvatar(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
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

    public String getCoverImageUrl() {
        return coverImageUrl;
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

    public String getProfileType() {
        return profileType;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getCharityNumber() {
        return charityNumber;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getWebsite() {
        return website;
    }
}