package org.orury.domain.meeting.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.orury.domain.crew.domain.entity.Crew;
import org.orury.domain.meeting.domain.MeetingMemberStore;
import org.orury.domain.meeting.domain.entity.Meeting;
import org.orury.domain.meeting.domain.entity.MeetingMember;
import org.orury.domain.meeting.domain.entity.MeetingMemberPK;
import org.orury.domain.user.domain.entity.User;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.orury.domain.MeetingDomainFixture.TestMeeting.createMeeting;
import static org.orury.domain.MeetingDomainFixture.TestMeetingMember.createMeetingMember;

@ExtendWith(MockitoExtension.class)
@DisplayName("[Store] 일정멤버 StoreImpl 테스트")
@ActiveProfiles("test")
class MeetingMemberStoreImplTest {
    private MeetingMemberStore meetingMemberStore;
    private MeetingMemberRepository meetingMemberRepository;
    private MeetingRepository meetingRepository;

    @BeforeEach
    void setUp() {
        meetingMemberRepository = mock(MeetingMemberRepository.class);
        meetingRepository = mock(MeetingRepository.class);

        meetingMemberStore = new MeetingMemberStoreImpl(meetingMemberRepository, meetingRepository);
    }

    @DisplayName("일정멤버를 받아, 일정멤버를 저장하고, 일정의 memberCount를 증가시킨다.")
    @Test
    void addMember() {
        // given
        MeetingMember meetingMember = mock(MeetingMember.class);
        given(meetingMember.getMeetingMemberPK())
                .willReturn(mock(MeetingMemberPK.class));

        // when
        meetingMemberStore.addMember(meetingMember);

        // then
        then(meetingMemberRepository).should(only())
                .save(any());
        then(meetingRepository).should(only())
                .increaseMemberCount(anyLong());
    }

    @DisplayName("일정멤버를 받아, 일정멤버를 삭제하고, 일정의 memberCount를 감소시킨다.")
    @Test
    void removeMember() {
        // given
        MeetingMember meetingMember = createMeetingMember().build().get();
        ;

        // when
        meetingMemberStore.removeMember(meetingMember.getMeetingMemberPK().getUserId(), meetingMember.getMeetingMemberPK().getMeetingId());

        // then
        then(meetingMemberRepository).should(only())
                .deleteByMeetingMemberPK_UserIdAndMeetingMemberPK_MeetingId(anyLong(), anyLong());
        then(meetingRepository).should(only())
                .decreaseMemberCount(anyLong());
    }

    @DisplayName("유저id와 크루id를 받아, 크루에 해당 유저가 참여한 일정에서 유저를 삭제하고, 일정의 memberCount를 감소시킨다.")
    @Test
    void removeAllByUserIdAndCrewId() {
        // given & when
        Long userId = 235L;
        List<Meeting> meetings = List.of(createMeeting(1L).build().get(), createMeeting(2L).build().get(), createMeeting(3L).build().get());
        given(meetingRepository.findAllByCrew_Id(anyLong()))
                .willReturn(meetings);
        given(meetingMemberRepository.findByMeetingMemberPK_MeetingIdAndMeetingMemberPK_UserId(anyLong(), anyLong()))
                .willReturn(
                        Optional.of(createMeetingMember(1L, userId).build().get()),
                        Optional.empty(),
                        Optional.of(createMeetingMember(3L, userId).build().get())
                );

        meetingMemberStore.removeAllByUserIdAndCrewId(
                mock(User.class).getId(),
                mock(Crew.class).getId()
        );

        // then
        then(meetingRepository).should(times(1))
                .findAllByCrew_Id(anyLong());
        then(meetingMemberRepository).should(times(2))
                .delete(any(MeetingMember.class));
        then(meetingRepository).should(times(2))
                .decreaseMemberCount(anyLong());
    }
}