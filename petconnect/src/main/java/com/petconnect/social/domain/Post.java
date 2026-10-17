package com.petconnect.social.domain;

import com.petconnect.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Column(name = "author_id", nullable = false)
    private UUID authorId; // This is the authUserId, not the UserProfile ID

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "caption", length = 1000)
    private String caption;

    @Column(name = "tags", length = 2000)
    private String tags;

    @Column(name = "pet_id")
    private UUID petId;

    @Column(name = "likes_count", nullable = false)
    private int likesCount;

    @Column(name = "comments_count", nullable = false)
    private int commentsCount;

    @Column(name = "active", nullable = false)
    private boolean active;

    /**
     * Intención declarada por el autor. Es nullable para mantener compatibles las
     * publicaciones creadas antes de esta funcionalidad: al leerlas se resuelve a
     * {@link PostIntent#UPDATE} mediante {@link #getIntentOrDefault()}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "intent", length = 30)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PostIntent intent;

    /** Solo significativo en publicaciones de tipo {@link PostIntent#QUESTION}. */
    @Column(nullable = false)
    private boolean solved;

    /**
     * Dirección confirmada en el selector de ubicación (como en el perfil).
     * Nullable para no romper las publicaciones anteriores a esta funcionalidad,
     * que simplemente se muestran sin ubicación.
     */
    @Column(name = "location", length = 500)
    private String location;

    /** Solo se rellena cuando el usuario confirma la ubicación en el mapa. */
    @Column(name = "latitude")
    private Double latitude;

    /** Solo se rellena cuando el usuario confirma la ubicación en el mapa. */
    @Column(name = "longitude")
    private Double longitude;

    protected Post() {
        super();
    }

    public Post(UUID authorId, String imageUrl, String caption) {
        super();
        this.authorId = authorId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.tags = "";
        this.likesCount = 0;
        this.commentsCount = 0;
        this.active = true;
    }

    public Post(UUID authorId, String imageUrl, String caption, String tags) {
        super();
        this.authorId = authorId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.tags = tags;
        this.petId = null;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.active = true;
    }

    public Post(UUID authorId, String imageUrl, String caption, String tags, UUID petId) {
        this(authorId, imageUrl, caption, tags);
        this.petId = petId;
    }

    /**
     * Constructor que además fija la intención. Es el que usa la capa de
     * presentación; las publicaciones sin intención declarada caen en
     * {@link PostIntent#UPDATE}, equivalente al comportamiento previo.
     */
    public Post(UUID authorId, String imageUrl, String caption, String tags, UUID petId,
            PostIntent intent) {
        this(authorId, imageUrl, caption, tags, petId);
        this.intent = intent == null ? PostIntent.UPDATE : intent;
    }

    /**
     * Constructor completo con ubicación. La dirección y las coordenadas siguen
     * el mismo patrón que el perfil de usuario: la dirección es el texto
     * confirmado en el selector y las coordenadas solo llegan si se confirma
     * en el mapa (pueden ser {@code null}).
     */
    public Post(UUID authorId, String imageUrl, String caption, String tags, UUID petId,
            PostIntent intent, String location, Double latitude, Double longitude) {
        this(authorId, imageUrl, caption, tags, petId, intent);
        this.location = normalizeLocation(location);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** Dirección sin espacios sobrantes; {@code null} o vacío se guarda como {@code null}. */
    private static String normalizeLocation(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        return location.trim();
    }

    public void incrementLikes() {
        this.likesCount++;
    }

    public void decrementLikes() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }

    public void incrementComments() {
        this.commentsCount++;
    }

    public void decrementComments() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTags() {
        return tags;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
    }

    /** Actualiza la intención; {@code null} la deja como actualización. */
    public void setIntent(PostIntent intent) {
        this.intent = intent == null ? PostIntent.UPDATE : intent;
    }

    /**
     * Actualiza la ubicación (dirección + coordenadas opcionales), con la misma
     * semántica que el perfil: vacío equivale a sin ubicación.
     */
    public void setLocation(String location, Double latitude, Double longitude) {
        this.location = normalizeLocation(location);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** Dirección confirmada en el selector; {@code null} en publicaciones sin ubicación. */
    public String getLocation() {
        return location;
    }

    /** Solo presente cuando el autor confirmó la ubicación en el mapa. */
    public Double getLatitude() {
        return latitude;
    }

    /** Solo presente cuando el autor confirmó la ubicación en el mapa. */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Intención efectiva de la publicación. Nunca devuelve {@code null}: los posts
     * anteriores a la migración se presentan como {@link PostIntent#UPDATE}.
     *
     * <p>Se expone en JSON con el nombre {@code intent} para que los clientes lean
     * siempre un valor concreto, incluso en publicaciones antiguas.</p>
     */
    @com.fasterxml.jackson.annotation.JsonProperty("intent")
    public PostIntent getIntentOrDefault() {
        return intent == null ? PostIntent.UPDATE : intent;
    }

    /** Indica si la publicación es una pregunta ya resuelta por su autor. */
    public boolean isSolved() {
        return solved;
    }

    /**
     * Marca o desmarca la pregunta como resuelta. Se limita a preguntas; en el
     * resto de intenciones la marca no aplica y se ignora.
     */
    public void setSolved(boolean solved) {
        this.solved = solved && getIntentOrDefault() == PostIntent.QUESTION;
    }

    public void softDelete() {
        this.active = false;
    }

    public void restore() {
        this.active = true;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public boolean isActive() {
        return active;
    }
}