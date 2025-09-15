package com.co.crediya.requests.model.notifications;

import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
  private UUID id;
  private String subject;
  private String template;
  private String key;

  /** Transient */
  private Map<String, Object> params;
}
