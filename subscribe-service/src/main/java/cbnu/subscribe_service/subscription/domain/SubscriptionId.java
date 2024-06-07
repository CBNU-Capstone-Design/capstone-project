package cbnu.subscribe_service.subscription.domain;


import cbnu.subscribe_service.common.generic.LongTypeIdentifier;
import cbnu.subscribe_service.common.generic.LongTypeIdentifierJavaType;

public class SubscriptionId extends LongTypeIdentifier {
    public SubscriptionId(Long id) {
        super(id);
    }

    public Long getId() {
        return longValue();
    }

    @Override
    public Long longValue() {
        return super.longValue();
    }

    @Override
    public Long nextValue() {
        return super.nextValue();
    }


    public static class SubscriptionIdJavaType extends LongTypeIdentifierJavaType<SubscriptionId> {
        public SubscriptionIdJavaType() {
            super(SubscriptionId.class);
        }
    }
}
