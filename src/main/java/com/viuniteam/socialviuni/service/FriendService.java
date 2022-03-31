package com.viuniteam.socialviuni.service;

import com.viuniteam.socialviuni.dto.response.friend.FriendResponse;
import com.viuniteam.socialviuni.entity.Friend;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FriendService {
    void save(Friend friend);

    void addFriend(Long idSource, Long idTarget);

    void removeFriend(Long idSource, Long idTarget);

    List<FriendResponse> getAll(Long id);

    boolean isFriend(Long idSource, Long idTarget);

    boolean itIsMe(Long idSource,Long idTarget);
}
