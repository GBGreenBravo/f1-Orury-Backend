package org.orury.domain.post.infrastructure;

import org.orury.domain.post.domain.db.PostLike;
import org.orury.domain.post.domain.db.PostLikePK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, PostLikePK> {
    boolean existsPostLikeByPostLikePK_UserIdAndPostLikePK_PostId(Long userId, Long postId);

    boolean existsByPostLikePK(PostLikePK postLikePK);

    List<PostLike> findByPostLikePK_UserId(Long userId);
}
