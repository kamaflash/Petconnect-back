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

    @Column(length = 500)
    private String location;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private boolean profilePublic;

    @Column(nullable = false)
    private boolean notificationsEnabled;

    @Column(nullable = false, length = 20)
    private String profileType;

    /** Cambio de tipo (vet/shelter) pendiente de aprobación de un administrador. */
    @Column(length = 20)
    private String pendingProfileType;

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

    public void updateTypeSpecificFields(String specialty, String licenseNumber, String charityNumber, String storeName,
            String website) {
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
        this.charityNumber = charityNumber;
        this.storeName = storeName;
        this.website = website;
    }

    /** Los tipos "vet" y "shelter" requieren aprobación de un administrador. */
    public static boolean requiresApproval(String profileType) {
        if (profileType == null || profileType.isBlank()) {
            return false;
        }
        String normalized = profileType.trim().toLowerCase();
        return normalized.equals("vet") || normalized.equals("shelter");
    }

    /**
     * Solicita un cambio de tipo. Los tipos profesionales (vet/shelter) se
     * aprueban automaticamente solo si el numero acreditativo guardado tiene
     * formato valido; en caso contrario se rechazan con excepcion y no queda
     * nada pendiente. Los demas tipos se siguen aplicando directamente.
     */
    public void requestProfileType(String requestedType) {
        if (requestedType == null || requestedType.isBlank()) {
            return;
        }
        String requested = requestedType.trim();
        if (requested.equalsIgnoreCase(this.profileType)) {
            return;
        }
        if (requiresApproval(requested)) {
            String normalized = requested.toLowerCase();
            if (normalized.equals("vet")) {
                if (!ProfileCredentialValidator.isValidLicenseNumber(this.licenseNumber)) {
                    throw new com.petconnect.users.domain.exceptions.InvalidProfileCredentialException(
                            "licenseNumber",
                            "Numero de colegiado invalido. Formato esperado: M-12345 o COLVET-M-12345 "
                                    + "con prefijo de provincia valido.");
                }
            } else {
                if (!ProfileCredentialValidator.isValidCharityNumber(this.charityNumber)) {
                    throw new com.petconnect.users.domain.exceptions.InvalidProfileCredentialException(
                            "charityNumber",
                            "Numero de registro invalido. Formato esperado: CIF valido (p. ej. G-12345674) "
                                    + "o referencia autonomica (p. ej. ASOC-2021-4582).");
                }
            }
            // Acreditacion valida: aprobacion automatica, sin pasar por admin.
            this.profileType = normalized;
            this.pendingProfileType = null;
        } else {
            this.profileType = requested;
            this.pendingProfileType = null;
        }
    }

    /** Aprueba la solicitud pendiente y la convierte en el tipo activo. */
    public void approvePendingProfileType() {
        if (this.pendingProfileType != null && !this.pendingProfileType.isBlank()) {
            this.profileType = this.pendingProfileType;
            this.pendingProfileType = null;
        }
    }

    /** Descarta la solicitud pendiente (rechazo o cancelación por el propio usuario). */
    public void rejectPendingProfileType() {
        this.pendingProfileType = null;
    }

    public void updateLocation(String location, Double latitude, Double longitude) {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getLocation() {
        return location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
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

    public String getPendingProfileType() {
        return pendingProfileType;
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