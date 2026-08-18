package de.denniskniep.safed.common.verifications;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"status", "type", "value"})
public record Evidence(EvidenceStatus status, String type, String value) {
}
