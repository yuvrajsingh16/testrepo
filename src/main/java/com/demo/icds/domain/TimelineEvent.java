package com.demo.icds.domain;

import java.time.Instant;

public record TimelineEvent(Instant time, String type, String message) {
}
