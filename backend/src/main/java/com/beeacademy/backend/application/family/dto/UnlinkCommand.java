package com.beeacademy.backend.application.family.dto;

import java.util.UUID;

public record UnlinkCommand(
        UUID actorId,
        UUID linkId,
        String reason
) {
    public UnlinkCommand {
        if (actorId == null) {
            throw new IllegalArgumentException("Actor ID không được để trống");
        }
        if (linkId == null) {
            throw new IllegalArgumentException("Link ID không được để trống");
        }
    }
}

