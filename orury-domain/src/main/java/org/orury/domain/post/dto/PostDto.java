package org.orury.domain.post.dto;

import org.orury.domain.post.db.Post;
import org.orury.domain.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;


/**
 * DTO for {@link Post}
 */

public record PostDto(
        Long id,
        String title,
        String content,
        int viewCount,
        int commentCount,
        int likeCount,
        List<String> images,
        int category,
        UserDto userDto,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDto of(
            Long id,
            String title,
            String content,
            int viewCount,
            int commentCount,
            int likeCount,
            List<String> images,
            int category,
            UserDto userDto,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new PostDto(
                id,
                title,
                content,
                viewCount,
                commentCount,
                likeCount,
                images,
                category,
                userDto,
                createdAt,
                updatedAt
        );
    }

    public static PostDto from(Post entity) {
        return PostDto.of(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getViewCount(),
                entity.getCommentCount(),
                entity.getLikeCount(),
                entity.getImages(),
                entity.getCategory(),
                UserDto.from(entity.getUser()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PostDto from(Post entity, List<String> images) {
        return PostDto.of(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getViewCount(),
                entity.getCommentCount(),
                entity.getLikeCount(),
                images,
                entity.getCategory(),
                UserDto.from(entity.getUser()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static PostDto from(Post entity, List<String> imgUrls, String postUserImageUrl) {
        return PostDto.of(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getViewCount(),
                entity.getCommentCount(),
                entity.getLikeCount(),
                imgUrls,
                entity.getCategory(),
                UserDto.from(entity.getUser(), postUserImageUrl),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Post toEntity() {
        return Post.of(
                id,
                title,
                content,
                viewCount,
                commentCount,
                likeCount,
                images,
                category,
                userDto.toEntity(),
                createdAt,
                updatedAt
        );
    }

    public Post toEntity(List<String> newImages) {
        return Post.of(
                id,
                title,
                content,
                viewCount,
                commentCount,
                likeCount,
                newImages,
                category,
                userDto.toEntity(),
                createdAt,
                updatedAt
        );
    }
}
