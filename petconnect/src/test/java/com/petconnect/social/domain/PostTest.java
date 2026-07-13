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
}