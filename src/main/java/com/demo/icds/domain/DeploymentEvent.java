package com.demo.icds.domain;

import java.time.Instant;

public record DeploymentEvent(String version, Instant time) {
}
