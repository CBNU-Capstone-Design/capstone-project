package cbnu.subscribe_service.point.domain;

import cbnu.subscribe_service.common.generic.LongTypeIdentifier;
import cbnu.subscribe_service.common.generic.LongTypeIdentifierJavaType;

public class PointId extends LongTypeIdentifier {
    public PointId(Long id) {
        super(id);
    }

    public static PointId make(Long id) {
        return new PointId(id);
    }

    @Override
    public Long longValue() {
        return super.longValue();
    }

    @Override
    public Long nextValue() {
        return super.nextValue();
    }

    public static class PointIdJavaType extends LongTypeIdentifierJavaType<PointId> {
        public PointIdJavaType() {
            super(PointId.class);
        }
    }
}
