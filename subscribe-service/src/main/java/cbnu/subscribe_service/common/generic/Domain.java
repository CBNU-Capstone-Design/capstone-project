package cbnu.subscribe_service.common.generic;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@MappedSuperclass
public abstract class Domain<T extends Domain<T, TID>, TID> {

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamp(3)")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamp(3)")
    private Instant updatedAt;

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        return equals((T) other);
    }

    protected boolean equals(T other) {
        if (other == null) {
            return false;
        }

        if (getId() == null) {
            return false;
        }

        if (other.getClass().equals(getClass())) {
            return getId().equals(other.getId());
        }

        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return getId() == null ? 0 : getId().hashCode();
    }

    abstract public TID getId();
}
