package org.orury.domain.gym.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.orury.domain.config.InfrastructureTest;
import org.orury.domain.global.constants.NumberConstants;
import org.orury.domain.gym.domain.entity.Gym;
import org.orury.domain.gym.domain.entity.GymLikePK;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.orury.domain.GymDomainFixture.TestGym.createGym;
import static org.orury.domain.GymDomainFixture.TestGymLike.createGymLike;
import static org.orury.domain.GymDomainFixture.TestGymLikePK.createGymLikePK;

@DisplayName("[Reader] 암장 ReaderImpl 테스트")
class GymReaderImplTest extends InfrastructureTest {

    @Test
    @DisplayName("존재하는 암장id가 들어오면, 정상적으로 Gym Entity를 반환한다.")
    void should_RetrieveGymById() {
        // given
        Long gymId = 3L;
        Gym gym = createGym(gymId).build().get();

        given(gymRepository.findById(gymId))
                .willReturn(Optional.of(gym));

        // when
        gymReader.findGymById(gymId);

        // then
        then(gymRepository).should(times(1))
                .findById(anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 암장id가 들어와도, (NotFound 예외를 발생시키지 않고) Option.empty()를 반환한다.")
    void when_NotExistingGymId_Then_NotFoundException() {
        // given
        Long gymId = 4L;

        given(gymRepository.findById(gymId))
                .willReturn(Optional.empty());

        // when & then
        gymReader.findGymById(gymId);

        then(gymRepository).should(times(1))
                .findById(anyLong());
    }

    @Test
    @DisplayName("존재하는 암장id가 들어오면, true를 반환한다.")
    void when_ExistingGymId_Then_ReturnTrue() {
        // given
        Long gymId = 7L;

        given(gymRepository.existsById(gymId))
                .willReturn(true);

        // when & then
        assertTrue(gymReader.existsGymById(gymId));
    }

    @Test
    @DisplayName("존재하지 않는 암장id가 들어오면, true를 반환한다.")
    void when_NotExistingGymId_Then_ReturnFalse() {
        Long gymId = 8L;

        given(gymRepository.existsById(gymId))
                .willReturn(false);

        // when & then
        assertFalse(gymReader.existsGymById(gymId));
    }

    @Test
    @DisplayName("암장명에 검색어를 포함하는 암장 리스트를 반환한다.")
    void when_AnyGymContainsSearchWordInTitle_Then_ReturnGymList() {
        // given
        String searchWord = "더클라임";
        List<Gym> gyms = List.of(createGym(1L).build().get(), createGym(2L).build().get());

        given(gymRepository.findByNameContainingOrAddressContainingOrRoadAddressContaining(searchWord, searchWord, searchWord))
                .willReturn(gyms);

        // when
        gymReader.findGymsBySearchWord(searchWord);

        // then
        then(gymRepository).should(times(1))
                .findByNameContainingOrAddressContainingOrRoadAddressContaining(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("암장명에 검색어를 포함하는 암장이 없다면 빈 리스트를 반환한다.")
    void when_EveryGymDoesNotContainsSearchWordInTitle_Then_EmptyList() {
        // given
        String searchWord = "축구공";
        List<Gym> emptyList = Collections.emptyList();

        given(gymRepository.findByNameContainingOrAddressContainingOrRoadAddressContaining(searchWord, searchWord, searchWord))
                .willReturn(emptyList);

        // when
        gymReader.findGymsBySearchWord(searchWord);

        // then
        then(gymRepository).should(times(1))
                .findByNameContainingOrAddressContainingOrRoadAddressContaining(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("주어진 좌표 범위 내에 있는 암장 리스트를 반환한다.")
    void when_GymsInAreaGrid_Then_ReturnGymList() {
        // given
        Double bottom = 37.5;
        Double top = 37.6;
        Double left = 127.0;
        Double right = 127.1;
        List<Gym> gyms = List.of(createGym(1L).build().get(), createGym(2L).build().get());

        given(gymRepository.findByLatitudeBetweenAndLongitudeBetweenOrderByLikeCount(bottom, top, left, right))
                .willReturn(gyms);

        // when
        gymReader.findGymsInAreaGrid(Map.of("bottom", bottom, "top", top, "left", left, "right", right));

        // then
        then(gymRepository).should(times(1))
                .findByLatitudeBetweenAndLongitudeBetweenOrderByLikeCount(bottom, top, left, right);
    }

    @Test
    @DisplayName("유저id로 좋아요한 암장 리스트를 반환한다. (첫 조회인 경우)")
    void when_FirstGymsLikedByUser_Then_ReturnGymList() {
        // given
        Long userId = 1L;
        Long cursor = NumberConstants.FIRST_CURSOR;
        Pageable pageRequest = PageRequest.of(0, NumberConstants.GYM_PAGINATION_SIZE);

        given(gymLikeRepository.findByGymLikePK_UserIdOrderByGymLikePKDesc(userId, pageRequest))
                .willReturn(List.of(
                        createGymLike(11L, userId).build().get(),
                        createGymLike(10L, userId).build().get()
                ));

        given(gymRepository.findById(anyLong()))
                .willReturn(Optional.of(createGym(1L).build().get()));

        // when
        gymReader.findGymsByUserLiked(userId, cursor, pageRequest);

        // then
        then(gymLikeRepository).should(times(1))
                .findByGymLikePK_UserIdOrderByGymLikePKDesc(anyLong(), any(Pageable.class));
        then(gymLikeRepository).should(times(0))
                .findByGymLikePK_UserIdAndGymLikePK_GymIdLessThanOrderByGymLikePKDesc(anyLong(), anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("유저id로 좋아요한 암장 리스트를 반환한다. (첫 조회가 아닌 경우)")
    void when_GymsLikedByUser_Then_ReturnGymList() {
        // given
        Long userId = 1L;
        Long cursor = 2L;
        Pageable pageRequest = PageRequest.of(0, NumberConstants.GYM_PAGINATION_SIZE);

        given(gymLikeRepository.findByGymLikePK_UserIdAndGymLikePK_GymIdLessThanOrderByGymLikePKDesc(userId, cursor, pageRequest))
                .willReturn(List.of(
                        createGymLike(11L, userId).build().get(),
                        createGymLike(10L, userId).build().get()
                ));

        given(gymRepository.findById(anyLong()))
                .willReturn(Optional.of(createGym(1L).build().get()));

        // when
        gymReader.findGymsByUserLiked(userId, cursor, pageRequest);

        // then
        then(gymLikeRepository).should(times(0))
                .findByGymLikePK_UserIdOrderByGymLikePKDesc(anyLong(), any(Pageable.class));
        then(gymLikeRepository).should(times(1))
                .findByGymLikePK_UserIdAndGymLikePK_GymIdLessThanOrderByGymLikePKDesc(anyLong(), anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("존재하는 (userId와 gymId로 구성된) 암장좋아요PK가 들어오면, true를 반환한다.")
    void when_ExistingGymLikePK_Then_ReturnTrue() {
        // given
        GymLikePK gymLikePK = createGymLikePK().build().get();

        given(gymLikeRepository.existsById(gymLikePK))
                .willReturn(true);

        // when & then
        assertTrue(gymReader.existsGymLikeById(gymLikePK));
    }

    @Test
    @DisplayName("존재하지 않는 (userId와 gymId로 구성된) 암장좋아요PK가 들어오면, false를 반환한다.")
    void when_NotExistingGymLikePK_Then_ReturnFalse() {
        // given
        GymLikePK gymLikePK = createGymLikePK().build().get();

        given(gymLikeRepository.existsById(gymLikePK))
                .willReturn(false);

        // when & then
        assertFalse(gymReader.existsGymLikeById(gymLikePK));
    }

    @Test
    @DisplayName("userId와 gymId로 구성된 암장좋아요가 존재하면, true를 반환한다.")
    void when_ExistingGymLikeConsistOfUserIdAndGymId_Then_ReturnTrue() {
        Long userId = 2L;
        Long gymId = 5L;

        given(gymLikeRepository.existsByGymLikePK_UserIdAndGymLikePK_GymId(userId, gymId))
                .willReturn(true);

        // when & then
        assertTrue(gymReader.existsGymLikeByUserIdAndGymId(userId, gymId));
    }

    @Test
    @DisplayName("userId와 gymId로 구성된 암장좋아요가 존재하지 않으면, false를 반환한다.")
    void when_NotExistingGymLikeConsistOfUserIdAndGymId_Then_ReturnFalse() {
        // given
        Long userId = 2L;
        Long gymId = 5L;

        given(gymLikeRepository.existsByGymLikePK_UserIdAndGymLikePK_GymId(userId, gymId))
                .willReturn(false);

        // when & then
        assertFalse(gymReader.existsGymLikeByUserIdAndGymId(userId, gymId));
    }
}
