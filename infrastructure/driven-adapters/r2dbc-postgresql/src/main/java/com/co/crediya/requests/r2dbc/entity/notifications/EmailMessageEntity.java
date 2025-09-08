package com.co.crediya.requests.r2dbc.entity.notifications;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("mensajes_correo")
public class EmailMessageEntity {
  @Column("id")
  private UUID id;

  @Column("asunto")
  private String subject;

  @Column("texto")
  private String body;

  @Column("clave")
  private String key;
}
