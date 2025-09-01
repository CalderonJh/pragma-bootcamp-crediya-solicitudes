package com.co.crediya.requests.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de un usuario")
public class UserDTO {
  private String email;
  private String role;
}
