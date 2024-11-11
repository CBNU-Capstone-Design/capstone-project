package cbnu.subscribe_service.common.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.util.Assert;

@NoArgsConstructor
public abstract class AggregateRoot<T extends Domain<T, TID>, TID> extends Domain<T, TID> {

    @Transient
    private final transient List<Object> domainEvents = new ArrayList();

    protected void registerEvent(Object event) {
        Assert.notNull(event, "Domain event must not be null");
        this.domainEvents.add(event);
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        this.domainEvents.clear();
    }

    @DomainEvents
    protected Collection<Object> domainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }
}
