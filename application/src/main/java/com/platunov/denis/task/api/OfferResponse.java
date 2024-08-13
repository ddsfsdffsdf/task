package com.platunov.denis.task.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OfferResponse {
    ResponseCode code;
    String message;
    OfferDto offer;
}
