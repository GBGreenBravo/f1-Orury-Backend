package org.orury.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.orury.client.auth.controller.AuthController;
import org.orury.client.auth.jwt.JwtTokenProvider;
import org.orury.client.auth.service.AuthService;
import org.orury.client.auth.strategy.LoginStrategyManager;
import org.orury.client.comment.controller.CommentController;
import org.orury.client.comment.controller.CommentLikeController;
import org.orury.client.comment.service.CommentLikeService;
import org.orury.client.comment.service.CommentService;
import org.orury.client.gym.controller.GymController;
import org.orury.client.gym.controller.GymLikeController;
import org.orury.client.gym.service.GymLikeService;
import org.orury.client.gym.service.GymService;
import org.orury.client.post.controller.PostController;
import org.orury.client.post.controller.PostLikeController;
import org.orury.client.post.service.PostLikeService;
import org.orury.client.post.service.PostService;
import org.orury.client.review.controller.ReviewController;
import org.orury.client.review.controller.ReviewReactionController;
import org.orury.client.review.service.ReviewReactionService;
import org.orury.client.review.service.ReviewService;
import org.orury.client.user.controller.UserController;
import org.orury.client.user.service.UserService;
import org.orury.common.config.SlackMessage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureRestDocs
@ExtendWith(MockitoExtension.class)
@WebMvcTest({
        AuthController.class,
        CommentController.class,
        CommentLikeController.class,
        GymController.class,
        GymLikeController.class,
        PostController.class,
        PostLikeController.class,
        ReviewController.class,
        ReviewReactionController.class,
        UserController.class
})
@ActiveProfiles("test")
public abstract class ControllerTest {
    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper mapper;

    @MockBean
    protected AuthService authService;

    @MockBean
    protected CommentService commentService;

    @MockBean
    protected CommentLikeService commentLikeService;

    @MockBean
    protected GymService gymService;

    @MockBean
    protected GymLikeService gymLikeService;

    @MockBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockBean
    protected LoginStrategyManager loginStrategyManager;

    @MockBean
    protected PostService postService;

    @MockBean
    protected PostLikeService postLikeService;

    @MockBean
    protected ReviewService reviewService;

    @MockBean
    protected ReviewReactionService reviewReactionService;

    @MockBean
    protected UserService userService;

    @MockBean
    protected SlackMessage slackMessage;
}