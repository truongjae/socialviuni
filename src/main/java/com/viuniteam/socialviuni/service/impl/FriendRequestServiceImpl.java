package com.viuniteam.socialviuni.service.impl;

import com.viuniteam.socialviuni.dto.Profile;
import com.viuniteam.socialviuni.dto.response.friend.FriendResponse;
import com.viuniteam.socialviuni.dto.response.friendrequest.FriendRequestResponse;
import com.viuniteam.socialviuni.entity.Friend;
import com.viuniteam.socialviuni.entity.FriendRequest;
import com.viuniteam.socialviuni.entity.User;
import com.viuniteam.socialviuni.exception.BadRequestException;
import com.viuniteam.socialviuni.exception.JsonException;
import com.viuniteam.socialviuni.exception.OKException;
import com.viuniteam.socialviuni.exception.ObjectNotFoundException;
import com.viuniteam.socialviuni.mapper.response.friend.FriendResponseMapper;
import com.viuniteam.socialviuni.mapper.response.friendrequest.FriendRequestResponseMapper;
import com.viuniteam.socialviuni.mapper.response.user.UserInfoResponseMapper;
import com.viuniteam.socialviuni.repository.FriendRequestRepository;
import com.viuniteam.socialviuni.service.FriendRequestService;
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
public class FriendRequestServiceImpl implements FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserService userService;
    private final FriendService friendService;
    private final Profile profile;
    private final FriendRequestResponseMapper friendRequestResponseMapper;
    private final UserInfoResponseMapper userInfoResponseMapper;
    @Override
    public void save(FriendRequest friendRequest) {
        friendRequestRepository.save(friendRequest);
    }

    @Override
    public void addFriendRequest(Long idTarget) {
        Long idSource = profile.getId();
        if(friendService.isFriend(idSource,idTarget))
            throw new BadRequestException("Đã kết bạn rồi");
        else {
            if (this.isFriendRequest(idSource,idTarget))
                throw new BadRequestException("Đã gửi lời mời kết bạn rồi");
            if (friendService.itIsMe(profile.getId(),idTarget))
                throw new BadRequestException("Không thể gửi lời mời kết bạn với chính mình");
            else {
                User userSource = userService.findOneById(idSource);
                User userTarget = userService.findOneById(idTarget);
                if(this.isFriendRequest(idTarget,idSource)){ // neu user1 add user2 roi nhung user2 tiep tuc add user 1 thi cho ca 2 thanh ban luon
                    List<FriendRequest> friendRequestSourceList = userSource.getFriendRequests();
                    for(FriendRequest friendRequest : friendRequestSourceList){
                        if(friendRequest.getUser().getId() == userTarget.getId()){
                            friendRequestSourceList.remove(friendRequest);
                            userSource.setFriendRequests(friendRequestSourceList);
                            userService.update(userSource);
                            friendRequestRepository.deleteFriendRequestById(friendRequest.getId());
                            break;
                        }
                    }
                    friendService.addFriend(idSource,idTarget);
                    throw new OKException("Chấp nhận lời mời kết bạn thành công");
                }
                else {
                    List<FriendRequest> friendRequestTargetList = userTarget.getFriendRequests();
                    // add friend request source to list target
                    FriendRequest friendTarget = new FriendRequest();
                    friendTarget.setUser(userSource);
                    this.save(friendTarget);
                    friendRequestTargetList.add(friendTarget);
                    userTarget.setFriendRequests(friendRequestTargetList);
                    userService.update(userTarget);
                    throw new OKException("Đã gửi lời mời kết bạn thành công");
                }
            }
        }
    }

    @Override
    public void removeFriendRequest(Long idTarget) {
        Long idSource = profile.getId();
        if(friendService.isFriend(idSource,idTarget))
            throw new BadRequestException("Đã kết bạn rồi");
        else {
            if (this.isFriendRequest(idSource,idTarget) || this.isFriendRequest(idTarget,idSource)){//kiem tra xem 1 trong 2 nguoi co gui loi moi ket ban voi nhau khong

                User userSource = userService.findOneById(idSource);
                User userTarget = userService.findOneById(idTarget);
                List<FriendRequest> friendRequestSourceList = userSource.getFriendRequests();
                List<FriendRequest> friendRequestTargetList = userTarget.getFriendRequests();

                for(FriendRequest friendRequest : friendRequestSourceList){
                    if(friendRequest.getUser().getId() == userTarget.getId()) {
                        friendRequestSourceList.remove(friendRequest);
                        userSource.setFriendRequests(friendRequestSourceList);
                        userService.update(userSource);
                        friendRequestRepository.deleteFriendRequestById(friendRequest.getId());
                        break;
                    }
                }
                for(FriendRequest friendRequest : friendRequestTargetList){
                    if(friendRequest.getUser().getId() == userSource.getId()) {
                        friendRequestTargetList.remove(friendRequest);
                        userTarget.setFriendRequests(friendRequestTargetList);
                        userService.update(userTarget);
                        friendRequestRepository.deleteFriendRequestById(friendRequest.getId());
                        break;
                    }
                }
                throw new OKException("Đã hủy lời mời kết bạn thành công");
            }
            else
                throw new BadRequestException("Chưa gửi lời mời kết bạn");
        }
    }


    @Override
    public boolean isFriendRequest(Long idSource, Long idTarget){
        User userSource = userService.findOneById(idSource);
        User userTarget = userService.findOneById(idTarget);
        List<FriendRequest> friendRequestTargetList = userTarget.getFriendRequests();
        for(FriendRequest friendRequest : friendRequestTargetList){
            if(friendRequest.getUser().getId() == userSource.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<FriendRequestResponse> getAll() {
        User user = userService.findOneById(profile.getId());
        if(user == null)
            throw new ObjectNotFoundException("Người dùng không tồn tại");

        List<FriendRequest> friendRequestList = user.getFriendRequests();
        List<FriendRequestResponse> friendRequestResponseList = new ArrayList<>();
        friendRequestList.forEach(friend -> {
            FriendRequestResponse friendRequestResponse = friendRequestResponseMapper.from(friend);
            friendRequestResponse.setUserInfoResponse(userInfoResponseMapper.from(friend.getUser()));
            friendRequestResponseList.add(friendRequestResponse);
        });
        return friendRequestResponseList;
    }
}
