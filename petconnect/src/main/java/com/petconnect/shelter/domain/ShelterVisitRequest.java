package com.petconnect.shelter.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shelter_visit_requests")
public class ShelterVisitRequest extends BaseEntity {

    @Column(name = "shelter_user_id", nullable = false)
    private UUID shelterUserId;

    @Column(name = "visitor_user_id", nullable = false)
    private UUID visitorUserId;

    @Column(name = "visitor_name", nullable = false, length = 120)
    private String visitorName;

    @Column(name = "visit_at", nullable = false)
    private LocalDateTime visitAt;

    @Column(name = "proposed_visit_at")
    private LocalDateTime proposedVisitAt;

    @Column(name = "party_size", nullable = false)
    private int partySize;

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShelterVisitStatus status;

    @Column(name = "response_note", length = 1000)
    private String responseNote;

    protected ShelterVisitRequest() {
        super();
    }

    public ShelterVisitRequest(UUID shelterUserId, UUID visitorUserId, String visitorName, LocalDateTime visitAt,
            int partySize, String message) {
        super();
        this.shelterUserId = shelterUserId;
        this.visitorUserId = visitorUserId;
        this.visitorName = visitorName;
        this.visitAt = visitAt;
        this.partySize = partySize;
        this.message = message;
        this.status = ShelterVisitStatus.PENDING;
    }

    public void updateStatus(ShelterVisitStatus status, String responseNote, LocalDateTime proposedVisitAt) {
        if (status == ShelterVisitStatus.CONFIRMED && this.status == ShelterVisitStatus.ALTERNATIVE_PROPOSED
                && this.proposedVisitAt != null) {
            this.visitAt = this.proposedVisitAt;
        }
        this.status = status;
        this.responseNote = responseNote;
        this.proposedVisitAt = proposedVisitAt;
    }

    public UUID getShelterUserId() {
        return shelterUserId;
    }

    public UUID getVisitorUserId() {
        return visitorUserId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public LocalDateTime getVisitAt() {
        return visitAt;
    }

    public LocalDateTime getProposedVisitAt() {
        return proposedVisitAt;
    }

    public int getPartySize() {
        return partySize;
    }

    public String getMessage() {
        return message;
    }

    public ShelterVisitStatus getStatus() {
        return status;
    }

    public String getResponseNote() {
        return responseNote;
    }
}
