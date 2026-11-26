package com.dessertrip.dessertrip_backend.controller.friend;

import com.dessertrip.dessertrip_backend.dto.friend.FriendRequestCreateRequest;
import com.dessertrip.dessertrip_backend.dto.friend.FriendRequestCreateResponse;
import com.dessertrip.dessertrip_backend.service.friend.FriendService;
import com.dessertrip.dessertrip_backend.dto.friend.FriendListResponse;
import com.dessertrip.dessertrip_backend.dto.friend.FriendRequestUpdateRequest;
import com.dessertrip.dessertrip_backend.dto.friend.FriendRequestUpdateResponse;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.dessertrip.dessertrip_backend.dto.friend.FriendCodeSearchResponse;

@RestController
@RequestMapping("/api/v1/users")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/me/friend-requests")
    public ResponseEntity<FriendRequestCreateResponse> sendFriendRequest(
            @Valid @RequestBody FriendRequestCreateRequest request
    ) {
        FriendRequestCreateResponse response =
                friendService.sendFriendRequest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping("/search-by-code")
    public ResponseEntity<FriendCodeSearchResponse> searchByFriendCode(
            @RequestParam("friend_code") String friendCode
    ) {
        return ResponseEntity.ok(
                friendService.searchByFriendCode(friendCode)
        );
    }
    @PatchMapping("/me/friend-requests/{requestId}")
    public ResponseEntity<FriendRequestUpdateResponse> updateFriendRequest(
            @PathVariable("requestId") Integer requestId,
            @Valid @RequestBody FriendRequestUpdateRequest request
    ) {
        return ResponseEntity.ok(
                friendService.updateFriendRequest(requestId, request)
        );
    }
    @GetMapping("/me/friends")
    public ResponseEntity<FriendListResponse> getFriends() {
        return ResponseEntity.ok(friendService.getFriends());
    }
}