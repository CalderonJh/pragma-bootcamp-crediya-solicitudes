package com.co.crediya.requests.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.*;

@Builder
@Schema(description = "Información de un usuario")
public record UserDTO(UUID id, String email, String role) {}
