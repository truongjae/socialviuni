package com.viuniteam.socialviuni.repository;

import com.viuniteam.socialviuni.entity.Follower;
import com.viuniteam.socialviuni.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FollowerRepository extends JpaRepository<Follower,Long> {
    @Override
    void deleteById(Long id);

    Follower findOneById(Long id);

    Page<Follower> findByUserOrderByIdDesc(User user, Pageable pageable);
}
