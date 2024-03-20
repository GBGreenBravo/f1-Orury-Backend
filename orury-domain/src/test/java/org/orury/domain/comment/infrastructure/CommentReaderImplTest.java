package org.orury.domain.comment.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orury.domain.comment.domain.entity.Comment;
import org.orury.domain.comment.domain.entity.CommentLikePK;
import org.orury.domain.config.InfrastructureTest;
import org.orury.domain.global.constants.NumberConstants;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.orury.domain.DomainFixtureFactory.TestComment.*;
import static org.orury.domain.DomainFixtureFactory.TestCommentLikePK.createCommentLikePK;

@DisplayName("[Reader] 댓글 ReaderImpl 테스트")
class CommentReaderImplTest extends InfrastructureTest {

    @Test
    @DisplayName("존재하는 댓글id가 들어오면, 정상적으로 Comment Entity를 반환한다.")
    void should_RetrieveCommentById() {
        // given
        Long commentId = 3L;
        Comment comment = createComment(commentId).build().get();

        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));

        // when
        commentReader.findCommentById(commentId);

        // then
        then(commentRepository).should(times(1))
                .findById(anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 댓글id가 들어와도, (NotFound 예외를 발생시키지 않고) Option.empty()를 반환한다.")
    void when_NotExistingCommentId_Then_NotFoundException() {
        // given
        Long commentId = 2L;

        given(commentRepository.findById(commentId))
                .willReturn(Optional.empty());

        // when & then
        commentReader.findCommentById(commentId);

        then(commentRepository).should(times(1))
                .findById(anyLong());
    }

    @Test
    @DisplayName("존재하는 댓글id가 들어오면, true를 반환한다.")
    void when_ExistingCommentId_Then_ReturnTrue() {
        // given
        Long commentId = 7L;

        given(commentRepository.existsById(commentId))
                .willReturn(true);

        // when & then
        assertTrue(commentReader.existsCommentById(commentId));
    }

    @Test
    @DisplayName("존재하지 않는 댓글id가 들어오면, false를 반환한다.")
    void when_NotExistingCommentId_Then_ReturnFalse() {
        Long commentId = 8L;

        given(commentRepository.existsById(commentId))
                .willReturn(false);

        // when & then
        assertFalse(commentReader.existsCommentById(commentId));
    }

    @Test
    @DisplayName("존재하는 댓글좋아요PK가 들어오면, true를 반환한다.")
    void when_ExistingCommentLikePK_Then_ReturnTrue() {
        // given
        CommentLikePK commentLikePK = createCommentLikePK().build().get();

        given(commentLikeRepository.existsById(commentLikePK))
                .willReturn(true);

        // when & then
        assertTrue(commentReader.existsCommentLikeById(commentLikePK));
    }

    @Test
    @DisplayName("존재하지 않는 댓글좋아요PK가 들어오면, false를 반환한다.")
    void when_NotExistingCommentLikePK_Then_ReturnFalse() {
        // given
        CommentLikePK commentLikePK = createCommentLikePK().build().get();

        given(commentLikeRepository.existsById(commentLikePK))
                .willReturn(false);

        // when & then
        assertFalse(commentReader.existsCommentLikeById(commentLikePK));
    }

    @Test
    @DisplayName("존재하는 댓글좋아요의 유저id와 댓글id가 들어오면, true를 반환한다.")
    void when_ExistingUserIdAndCommentId_Then_ReturnTrue() {
        // given
        Long userId = 6L;
        Long commentId = 7L;

        given(commentLikeRepository.existsCommentLikeByCommentLikePK_UserIdAndCommentLikePK_CommentId(userId, commentId))
                .willReturn(true);

        // when & then
        assertTrue(commentReader.existsCommentLikeByUserIdAndCommentId(userId, commentId));
    }

    @Test
    @DisplayName("존재하지 않는 댓글좋아요의 유저id와 댓글id가 들어오면, false를 반환한다.")
    void when_NotExistingUserIdAndCommentId_Then_ReturnFalse() {
        // given
        Long userId = 7L;
        Long commentId = 8L;

        given(commentLikeRepository.existsCommentLikeByCommentLikePK_UserIdAndCommentLikePK_CommentId(userId, commentId))
                .willReturn(false);

        // when & then
        assertFalse(commentReader.existsCommentLikeByUserIdAndCommentId(userId, commentId));
    }

    @Test
    @DisplayName("게시글Id와 최종값이 아닌 cursor가 들어오면, (cursor 다음의 페이지만큼의) 해당 게시글의 댓글 목록을 댓글/대댓글 모두 작성순으로 정렬하여 반환한다.")
    void when_PostIdAndNotLastCursor_Then_RetrieveNextPageOfCommentsOfPostOrderByCreatedDesc() {
        // given
        Long postId = 9L;
        Long cursor = 465L;
        PageRequest pageRequest = PageRequest.of(0, NumberConstants.COMMENT_PAGINATION_SIZE);

        Comment comment1 = createParentComment().id(1L).build().get();
        Comment comment2 = createParentComment().id(2L).build().get();
        Comment comment3 = createParentComment().id(3L).build().get();
        Comment comment1_1 = createChildComment().id(4L).parentId(1L).build().get();
        Comment comment1_2 = createChildComment().id(5L).parentId(1L).build().get();
        Comment comment3_1 = createChildComment().id(6L).parentId(3L).build().get();
        Comment comment3_2 = createChildComment().id(7L).parentId(3L).build().get();
        Comment comment3_3 = createChildComment().id(8L).parentId(3L).build().get();
        Comment comment1_3 = createChildComment().id(9L).parentId(1L).build().get();

        List<Comment> parentComments = List.of(
                comment1,
                comment2,
                comment3
        );
        List<Comment> childCommentsFor1 = List.of(
                comment1_1,
                comment1_2,
                comment1_3
        );
        List<Comment> childCommentsFor3 = List.of(
                comment3_1,
                comment3_2,
                comment3_3
        );

        List<Comment> expectedComments = List.of(
                comment1, comment1_1, comment1_2, comment1_3,
                comment2,
                comment3, comment3_1, comment3_2, comment3_3
        );

        given(commentRepository.findByPostIdAndParentIdAndIdGreaterThanOrderByIdAsc(postId, NumberConstants.PARENT_COMMENT, cursor, pageRequest))
                .willReturn(parentComments);
        given(commentRepository.findByParentIdOrderByIdAsc(anyLong()))
                .willReturn(childCommentsFor1, Collections.emptyList(), childCommentsFor3);

        // when
        List<Comment> actualComments = commentReader.getCommentsByPostIdAndCursor(postId, cursor, pageRequest);

        // then
        assertEquals(expectedComments, actualComments);

        then(commentRepository).should(times(1))
                .findByPostIdAndParentIdAndIdGreaterThanOrderByIdAsc(anyLong(), anyLong(), anyLong(), any());
        then(commentRepository).should(times(parentComments.size()))
                .findByParentIdOrderByIdAsc(anyLong());
    }

    @Test
    @DisplayName("cursor가 -1로 전체 목록 조회가 끝났음을 알리는 인자로 들어오면, 빈 배열을 응답한다.")
    void when_PostIdAndLastCursor_Then_RetrieveEmptyList() {
        // given
        Long postId = 9L;
        Long cursor = NumberConstants.LAST_CURSOR;
        PageRequest pageRequest = PageRequest.of(0, NumberConstants.COMMENT_PAGINATION_SIZE);

        // when
        List<Comment> actualComments = commentReader.getCommentsByPostIdAndCursor(postId, cursor, pageRequest);

        // then
        assertEquals(Collections.emptyList(), actualComments);

        then(commentRepository).should(never())
                .findByPostIdAndParentIdAndIdGreaterThanOrderByIdAsc(anyLong(), anyLong(), anyLong(), any());
        then(commentRepository).should(never())
                .findByParentIdOrderByIdAsc(anyLong());
    }

    @Test
    @DisplayName("유저Id와 초기값의 cursor가 들어오면, (첫 페이지만큼의) 유저가 작성한 댓글 목록을 최신순으로 반환한다.")
    void when_UserIdAndFirstCursor_Then_RetrieveFirstPageOfCommentsByUserOrderByCreatedDesc() {
        // given
        Long userId = 3L;
        Long cursor = NumberConstants.FIRST_CURSOR;
        PageRequest pageRequest = PageRequest.of(0, NumberConstants.COMMENT_PAGINATION_SIZE);

        // when
        commentReader.getCommentsByUserIdAndCursor(userId, cursor, pageRequest);

        // then
        then(commentRepository).should(times(1))
                .findByUserIdOrderByIdDesc(anyLong(), any());
        then(commentRepository).should(never())
                .findByUserIdAndIdLessThanOrderByIdDesc(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("유저Id와 초기값이 아닌 cursor가 들어오면, cursor 다음의 페이지만큼 유저가 작성한 댓글 목록을 최신순으로 반환한다.")
    void when_UserIdAndNotFirstCursor_Then_RetrieveNextPageOfCommentsByUserOrderByCreatedDesc() {
        // given
        Long userId = 3L;
        Long cursor = 523L;
        PageRequest pageRequest = PageRequest.of(0, NumberConstants.COMMENT_PAGINATION_SIZE);

        // when
        commentReader.getCommentsByUserIdAndCursor(userId, cursor, pageRequest);

        // then
        then(commentRepository).should(never())
                .findByUserIdOrderByIdDesc(anyLong(), any());
        then(commentRepository).should(times(1))
                .findByUserIdAndIdLessThanOrderByIdDesc(anyLong(), anyLong(), any());
    }
}
