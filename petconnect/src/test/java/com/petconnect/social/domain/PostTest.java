package com.petconnect.social.domain;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    private final UUID authorId = UUID.randomUUID();

    @Test
    void shouldCreatePost() {
        var post = new Post(authorId, "http://image.url", "My caption");
        assertEquals(authorId, post.getAuthorId());
        assertEquals("http://image.url", post.getImageUrl());
        assertEquals("My caption", post.getCaption());
        assertEquals(0, post.getLikesCount());
        assertEquals(0, post.getCommentsCount());
        assertTrue(post.isActive());
    }

    @Test
    void shouldIncrementLikes() {
        var post = new Post(authorId, "http://image.url", "caption");
        post.incrementLikes();
        assertEquals(1, post.getLikesCount());
    }

    @Test
    void shouldNotDecrementLikesBelowZero() {
        var post = new Post(authorId, "http://image.url", "caption");
        post.decrementLikes();
        assertEquals(0, post.getLikesCount());
    }

    @Test
    void shouldDecrementLikes() {
        var post = new Post(authorId, "http://image.url", "caption");
        post.incrementLikes();
        post.incrementLikes();
        post.decrementLikes();
        assertEquals(1, post.getLikesCount());
    }

    @Test
    void shouldIncrementComments() {
        var post = new Post(authorId, "http://image.url", "caption");
        post.incrementComments();
        assertEquals(1, post.getCommentsCount());
    }

    @Test
    void shouldNotDecrementCommentsBelowZero() {
        var post = new Post(authorId, "http://image.url", "caption");
        post.decrementComments();
        assertEquals(0, post.getCommentsCount());
    }

    @Test
    void shouldSoftDelete() {
        var post = new Post(authorId, "http://image.url", "caption");
        post.softDelete();
        assertFalse(post.isActive());
    }

    @Test
    void shouldRestore() {
        var post = new Post(authorId, "http://image.url", "caption");
        post.softDelete();
        post.restore();
        assertTrue(post.isActive());
    }

    // ========== INTENCION DE LA PUBLICACION ==========

    @Test
    void legacyPostWithoutIntentDefaultsToUpdate() {
        // Las publicaciones anteriores a la migracion no tienen intencion guardada.
        var post = new Post(authorId, "http://image.url", "caption");
        assertEquals(PostIntent.UPDATE, post.getIntentOrDefault());
        assertFalse(post.isSolved());
    }

    @Test
    void shouldKeepDeclaredIntent() {
        var post = new Post(authorId, "http://image.url", "caption", "", null, PostIntent.QUESTION);
        assertEquals(PostIntent.QUESTION, post.getIntentOrDefault());
    }

    @Test
    void shouldFallBackToUpdateWhenIntentIsNull() {
        var post = new Post(authorId, "http://image.url", "caption", "", null, null);
        assertEquals(PostIntent.UPDATE, post.getIntentOrDefault());
    }

    @Test
    void shouldUpdateIntent() {
        var post = new Post(authorId, "http://image.url", "caption");
        post.setIntent(PostIntent.RECOMMENDATION);
        assertEquals(PostIntent.RECOMMENDATION, post.getIntentOrDefault());

        post.setIntent(null);
        assertEquals(PostIntent.UPDATE, post.getIntentOrDefault());
    }

    @Test
    void shouldResolveOnlyQuestions() {
        var question = new Post(authorId, "http://image.url", "caption", "", null, PostIntent.QUESTION);
        question.setSolved(true);
        assertTrue(question.isSolved());

        // Retirar la seleccion deja la pregunta sin resolver.
        question.setSolved(false);
        assertFalse(question.isSolved());
    }

    @Test
    void shouldNotResolveNonQuestionPosts() {
        var update = new Post(authorId, "http://image.url", "caption", "", null, PostIntent.UPDATE);
        update.setSolved(true);
        assertFalse(update.isSolved());
    }

    // ========== PARSEO DE INTENCION ==========

    @Test
    void shouldParseIntentCaseInsensitively() {
        assertEquals(PostIntent.MEETUP, PostIntent.parse("meetup"));
        assertEquals(PostIntent.MEETUP, PostIntent.parse(" MEETUP "));
    }

    @Test
    void shouldParseEmptyIntentAsNull() {
        assertNull(PostIntent.parse(null));
        assertNull(PostIntent.parse(""));
        assertNull(PostIntent.parse("   "));
    }

    @Test
    void shouldRejectUnknownIntent() {
        assertThrows(IllegalArgumentException.class, () -> PostIntent.parse("NOT_A_REAL_INTENT"));
    }

    // ========== UBICACION DE LA PUBLICACION ==========

    @Test
    void legacyPostWithoutLocationHasNullLocation() {
        // Las publicaciones anteriores a la migracion no tienen ubicacion guardada.
        var post = new Post(authorId, "http://image.url", "caption");
        assertNull(post.getLocation());
        assertNull(post.getLatitude());
        assertNull(post.getLongitude());
    }

    @Test
    void shouldKeepDeclaredLocation() {
        var post = new Post(authorId, "http://image.url", "caption", "", null,
                PostIntent.URGENT_LOST, "Calle Mayor 1, Madrid", 40.416775, -3.70379);
        assertEquals("Calle Mayor 1, Madrid", post.getLocation());
        assertEquals(40.416775, post.getLatitude());
        assertEquals(-3.70379, post.getLongitude());
    }

    @Test
    void shouldNormalizeBlankLocationToNull() {
        var post = new Post(authorId, "http://image.url", "caption", "", null,
                PostIntent.MEETUP, "   ", null, null);
        assertNull(post.getLocation());
    }

    @Test
    void shouldUpdateLocation() {
        var post = new Post(authorId, "http://image.url", "caption");
        post.setLocation("Parque del Retiro, Madrid", 40.41526, -3.6844);
        assertEquals("Parque del Retiro, Madrid", post.getLocation());

        post.setLocation(null, null, null);
        assertNull(post.getLocation());
        assertNull(post.getLatitude());
        assertNull(post.getLongitude());
    }
}