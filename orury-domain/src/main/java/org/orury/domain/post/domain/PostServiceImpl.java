package org.orury.domain.post.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.orury.common.util.ImageUtil;
import org.orury.domain.global.constants.NumberConstants;
import org.orury.domain.global.image.ImageReader;
import org.orury.domain.global.image.ImageStore;
import org.orury.domain.post.domain.dto.PostDto;
import org.orury.domain.post.domain.dto.PostLikeDto;
import org.orury.domain.post.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.orury.common.util.S3Folder.POST;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class PostServiceImpl implements PostService {
    private final PostReader postReader;
    private final PostStore postStore;
    private final ImageReader imageReader;
    private final ImageStore imageStore;

    @Override
    @Transactional(readOnly = true)
    public List<PostDto> getPostDtosByCategory(int category, Long cursor, Pageable pageable) {
        return postReader.findByCategoryOrderByIdDesc(category, cursor, pageable).stream()
                .map(this::postImageConverter)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDto> getPostDtosBySearchWord(String searchWord, Long cursor, Pageable pageable) {
        return postReader.findByTitleContainingOrContentContainingOrderByIdDesc(searchWord, cursor, pageable).stream()
                .map(this::postImageConverter)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostDto> getPostDtosByUserId(Long userId, Long cursor, Pageable pageable) {
        return postReader.findByUserIdOrderByIdDesc(userId, cursor, pageable).stream()
                .map(this::postImageConverter)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostDto> getHotPostDtos(Pageable pageable) {
        return postReader.findByLikeCountGreaterThanEqualAndCreatedAtGreaterThanEqualOrderByLikeCountDescCreatedAtDesc(
                NumberConstants.HOT_POSTS_BOUNDARY,
                LocalDateTime.now().minusMonths(1L),
                pageable
        ).map(this::postImageConverter);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDto getPostDtoById(Long id) {
        var post = postReader.findById(id);
        var isLike = postReader.isPostLiked(post.getUser().getId(), id);
        var links = imageReader.getImageLinks(POST, post.getImages());
        return PostDto.from(post, links, isLike);
    }

    @Override
    public void createPost(PostDto postDto, List<MultipartFile> files) {
        // id == null -> 생성, id != null -> 수정
        if (postDto.id() != null) {
            var oldImages = postDto.images();
            if (!ImageUtil.imagesValidation(oldImages)) imageStore.delete(POST, oldImages);
        }
        var newImages = imageStore.upload(POST, files);
        postStore.save(postDto.toEntity(newImages));
    }

    @Override
    public void deletePost(PostDto postDto) {
        var oldImages = postDto.images();
        if (!ImageUtil.imagesValidation(oldImages)) imageStore.delete(POST, oldImages);
        postStore.delete(postDto.toEntity());
    }

    @Override
    public int getNextPage(Page<PostDto> postDtos, int page) {
        return (postDtos.hasNext()) ? page + 1 : NumberConstants.LAST_PAGE;
    }

    @Override
    public void createPostLike(PostLikeDto postLikeDto) {
        postReader.findById(postLikeDto.postLikePK().getPostId());
        if (postReader.existsByPostLikePK(postLikeDto.postLikePK())) return;
        postStore.save(postLikeDto.toEntity());
    }

    @Override
    public void deletePostLike(PostLikeDto postLikeDto) {
        postReader.findById(postLikeDto.postLikePK().getPostId());
        if (!postReader.existsByPostLikePK(postLikeDto.postLikePK())) return;
        postStore.delete(postLikeDto.toEntity());
    }

    @Override
    public void updateViewCount(Long id) {
        postStore.updateViewCount(id);
    }

    @Override
    public List<PostDto> getPosts() {
        return postReader.findAll().stream().map(this::postImageConverter).toList();
    }

    private PostDto postImageConverter(Post post) {
        var links = imageReader.getImageLinks(POST, post.getImages());
        return PostDto.from(post, links);
    }
}
