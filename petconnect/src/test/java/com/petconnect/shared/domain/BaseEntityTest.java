package com.petconnect.shared.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    private static class TestEntity extends BaseEntity {
        protected TestEntity() {
            super();
        }

        protected TestEntity(UUID id) {
            super(id);
        }

        protected TestEntity(UUID id, LocalDateTime createdAt) {
            super(id, createdAt);
        }
    }

    @Test
    void shouldNotGenerateIdOnDefaultConstructor() {
        var entity = new TestEntity();
        assertNull(entity.getId());
    }

    @Test
    void shouldSetCreatedAtOnDefaultConstructor() {
        var entity = new TestEntity();
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void shouldAcceptCustomId() {
        var id = UUID.randomUUID();
        var entity = new TestEntity(id);
        assertEquals(id, entity.getId());
    }

    @Test
    void shouldAcceptCustomIdAndCreatedAt() {
        var id = UUID.randomUUID();
        var createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        var entity = new TestEntity(id, createdAt);
        assertEquals(id, entity.getId());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(createdAt, entity.getUpdatedAt());
    }

    @Test
    void shouldBeEqualForSameId() {
        var id = UUID.randomUUID();
        var entity1 = new TestEntity(id);
        var entity2 = new TestEntity(id);
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentIds() {
        var entity1 = new TestEntity(UUID.randomUUID());
        var entity2 = new TestEntity(UUID.randomUUID());
        assertNotEquals(entity1, entity2);
    }

    @Test
    void shouldBeEqualForSameInstance() {
        var entity = new TestEntity();
        assertEquals(entity, entity);
    }

    @Test
    void shouldNotBeEqualForNull() {
        var entity = new TestEntity();
        assertNotNull(entity);
    }

    @Test
    void shouldNotBeEqualForDifferentClass() {
        var entity = new TestEntity();
        assertNotEquals("string", entity);
    }
}