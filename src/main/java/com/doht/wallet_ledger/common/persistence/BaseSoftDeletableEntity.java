package com.doht.wallet_ledger.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Lớp cha cho các thực thể quản trị/danh mục hỗ trợ xóa mềm (ví dụ: người dùng, tài khoản ngân hàng).
 *
 * <p>Không kế thừa lớp này cho các bảng sổ cái tài chính bất biến.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class BaseSoftDeletableEntity extends BaseAuditableEntity {

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    public void softDelete(UUID deletedBy, Instant deletedAt) {
        Objects.requireNonNull(deletedAt, "deletedAt must not be null");
        if (this.deleted) {
            return;
        }
        this.deleted = true;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
