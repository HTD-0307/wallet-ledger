package com.doht.wallet_ledger.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BaseEntityTest {

    private static class SampleSoftDeletableEntity extends BaseSoftDeletableEntity {
        SampleSoftDeletableEntity() {
            super();
        }
    }

    @Test
    void shouldSoftDeleteAndRestoreCleanly() {
        SampleSoftDeletableEntity entity = new SampleSoftDeletableEntity();
        UUID actorId = UUID.randomUUID();
        Instant deletedAt = Instant.parse("2026-11-20T01:00:00Z");

        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getDeletedBy()).isNull();

        entity.softDelete(actorId, deletedAt);

        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(entity.getDeletedBy()).isEqualTo(actorId);

        // Gọi xóa mềm lần 2 (idempotent) không được ghi đè thời gian và người xóa ban đầu
        entity.softDelete(UUID.randomUUID(), deletedAt.plusSeconds(60));
        assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
        assertThat(entity.getDeletedBy()).isEqualTo(actorId);

        entity.restore();
        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getDeletedBy()).isNull();
    }

    @Test
    void shouldRejectNullDeletedAtOnSoftDelete() {
        SampleSoftDeletableEntity entity = new SampleSoftDeletableEntity();
        UUID actorId = UUID.randomUUID();

        assertThatThrownBy(() -> entity.softDelete(actorId, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("deletedAt");
    }

    @Test
    void shouldSatisfyHibernateSafeEqualsAndHashCodeContract() throws Exception {
        SampleSoftDeletableEntity first = new SampleSoftDeletableEntity();
        SampleSoftDeletableEntity second = new SampleSoftDeletableEntity();

        // Thực thể chưa lưu (id == null) không bằng thực thể khác nhưng có cùng hashCode theo lớp
        assertThat(first).isNotEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());

        UUID sharedId = UUID.randomUUID();
        setId(first, sharedId);
        setId(second, sharedId);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());

        setId(second, UUID.randomUUID());
        assertThat(first).isNotEqualTo(second);
    }

    private static void setId(BaseEntity entity, UUID id) throws Exception {
        Field idField = BaseEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }
}
