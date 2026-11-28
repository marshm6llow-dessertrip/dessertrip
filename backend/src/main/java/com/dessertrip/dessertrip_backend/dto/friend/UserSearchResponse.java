package com.dessertrip.dessertrip_backend.dto.friend;

import java.util.List;

public record UserSearchResponse(
        List<UserSearchItemResponse> users
) {
}
