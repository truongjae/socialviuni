package com.viuniteam.socialviuni.repository;

import com.viuniteam.socialviuni.entity.Like;
import com.viuniteam.socialviuni.entity.Post;
import com.viuniteam.socialviuni.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface LikeRepository extends JpaRepository<Like,Long> {
    void deleteByUserAndPost(User user, Post post);
    Long countByPost(Post post);
    Like findOneByPostAndUser(Post post, User user);
    Boolean existsByPostAndUser(Post post,User user);
    List<Like> findAllByPostOrderByCreatedDate(Post post);
}
