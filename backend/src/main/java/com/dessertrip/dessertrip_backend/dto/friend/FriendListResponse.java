package com.dessertrip.dessertrip_backend.dto.friend;

import java.util.List;

public record FriendListResponse(
        List<FriendListItemResponse> friends
) {
}