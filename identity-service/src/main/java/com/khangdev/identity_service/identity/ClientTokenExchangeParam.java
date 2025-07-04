package com.khangdev.identity_service.identity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientTokenExchangeParam {
    String grant_type;
    String client_id;
    String client_secret;
    String scope;
}