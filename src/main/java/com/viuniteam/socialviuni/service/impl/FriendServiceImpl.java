package com.viuniteam.socialviuni.service.impl;

import com.viuniteam.socialviuni.dto.response.friend.FriendResponse;
import com.viuniteam.socialviuni.entity.Friend;
import com.viuniteam.socialviuni.entity.User;
import com.viuniteam.socialviuni.exception.BadRequestException;
import com.viuniteam.socialviuni.exception.JsonException;
import com.viuniteam.socialviuni.exception.OKException;
import com.viuniteam.socialviuni.exception.ObjectNotFoundException;
import com.viuniteam.socialviuni.mapper.response.friend.FriendResponseMapper;
import com.viuniteam.socialviuni.mapper.response.user.UserInfoResponseMapper;
import com.viuniteam.socialviuni.repository.FriendRepository;
import com.viuniteam.socialviuni.service.FriendService;
import com.viuniteam.socialviuni.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor

public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserService userService;
    private final FriendResponseMapper friendResponseMapper;
    private final UserInfoResponseMapper userInfoResponseMapper;

    @Override
    public void save(Friend friend) {
        friendRepository.save(friend);
    }

    @Override
    public void addFriend(Long idSource, Long idTarget) {

        if(this.itIsMe(idSource,idTarget))
            throw new BadRequestException("Không thể kết bạn với chính mình");

        if(this.isFriend(idSource,idTarget))
            throw new BadRequestException("Đã kết bạn rồi");
        else {
            User userSource = userService.findOneById(idSource);
            User userTarget = userService.findOneById(idTarget);
            List<Friend> friendSourceList = userSource.getFriends();
            List<Friend> friendTargetList = userTarget.getFriends();

            // add friend target to list
            Friend friendSource = new Friend();
            friendSource.setUser(userTarget);
            this.save(friendSource);
            friendSourceList.add(friendSource);
            userSource.setFriends(friendSourceList);
            userService.update(userSource);

            // add friend source to list
            Friend friendTarget = new Friend();
            friendTarget.setUser(userSource);
            this.save(friendTarget);
            friendTargetList.add(friendTarget);
            userTarget.setFriends(friendTargetList);
            userService.update(userTarget);

            throw new OKException("Kết bạn thành công");
        }
    }

    @Override
    public void removeFriend(Long idSource, Long idTarget) {

        if(this.itIsMe(idSource,idTarget))
            throw new BadRequestException("Không thể hủy kết bạn với chính mình");
        if(this.isFriend(idSource,idTarget)){
            User userSource = userService.findOneById(idSource);
            User userTarget = userService.findOneById(idTarget);

            List<Friend> friendSourceList = userSource.getFriends();
            List<Friend> friendTargetList = userTarget.getFriends();
            for(Friend friend : friendSourceList){
                if(friend.getUser().getId() == userTarget.getId()){
                    friendSourceList.remove(friend);
                    userSource.setFriends(friendSourceList);
                    userService.update(userSource);
                    friendRepository.deleteFriendById(friend.getId());
                    break;
                }
            }
            for(Friend friend : friendTargetList){
                if(friend.getUser().getId() == userSource.getId()){
                    friendTargetList.remove(friend);
                    userTarget.setFriends(friendTargetList);
                    userService.update(userTarget);
                    friendRepository.deleteFriendById(friend.getId());
                    break;
                }
            }

            throw new OKException("Hủy kết bạn thành công");
        }
        throw new BadRequestException("Chưa kết bạn");
    }

    @Override
    public List<FriendResponse> getAll(Long id) {
        User user = userService.findOneById(id);
        if(user == null)
            throw new ObjectNotFoundException("Người dùng không tồn tại");

        List<Friend> friendList = user.getFriends();
        List<FriendResponse> friendResponseList = new ArrayList<>();
        friendList.forEach(friend -> {
            FriendResponse friendResponse = friendResponseMapper.from(friend);
            friendResponse.setUserInfoResponse(userInfoResponseMapper.from(friend.getUser()));
            friendResponseList.add(friendResponse);
        });
        return friendResponseList;
    }

    @Override
    public boolean isFriend(Long idSource, Long idTarget) {
        User userSource = userService.findOneById(idSource);
        User userTarget = userService.findOneById(idTarget);
        if (userTarget==null || !userTarget.isActive()) throw new ObjectNotFoundException("Tài khoản không tồn tại");
        List<Friend> friendSourceList = userSource.getFriends();
        List<Friend> friendTargetList = userTarget.getFriends();
        for(Friend friend : friendSourceList)
            if(friend.getUser().getId() == userTarget.getId())
                return true;
        for(Friend friend : friendTargetList)
            if(friend.getUser().getId() == userSource.getId())
                return true;
        return false;
    }

    @Override
    public boolean itIsMe(Long idSource, Long idTarget) {
        return idSource == idTarget;
    }
}
