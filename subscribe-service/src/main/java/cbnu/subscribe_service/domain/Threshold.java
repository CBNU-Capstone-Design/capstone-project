package cbnu.subscribe_service.domain;

import lombok.Getter;

@Getter
public enum Threshold {

    MIN(0L), MAX(10000000L);

    private final Long value;

    Threshold(Long value) {
        this.value = value;
    }
}
