package com.museum.booking.service;

import com.museum.booking.common.BusinessException;
import com.museum.booking.dto.ReservationDtos;
import com.museum.booking.entity.Exhibition;
import com.museum.booking.entity.Reservation;
import com.museum.booking.entity.SysUser;
import com.museum.booking.entity.VisitSlot;
import com.museum.booking.mapper.ExhibitionMapper;
import com.museum.booking.mapper.ReservationMapper;
import com.museum.booking.mapper.SysUserMapper;
import com.museum.booking.mapper.VisitSlotMapper;
import com.museum.booking.vo.ReservationVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationMapper reservationMapper;
    @Mock
    private VisitSlotMapper slotMapper;
    @Mock
    private ExhibitionMapper exhibitionMapper;
    @Mock
    private SysUserMapper userMapper;

    @InjectMocks
    private ReservationService reservationService;

    private VisitSlot slot;
    private Exhibition exhibition;
    private SysUser user;

    @BeforeEach
    void setUp() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        slot = new VisitSlot();
        slot.setId(10L);
        slot.setExhibitionId(20L);
        slot.setVisitDate(tomorrow);
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(11, 0));
        slot.setCapacity(100);
        slot.setBookedCount(0);
        slot.setStatus("OPEN");

        exhibition = new Exhibition();
        exhibition.setId(20L);
        exhibition.setTitle("曾侯乙编钟");
        exhibition.setCoverUrl("/images/exhibitions/bells.svg");
        exhibition.setVenue("综合馆一楼");
        exhibition.setStatus("PUBLISHED");
        exhibition.setStartDate(LocalDate.now().minusDays(10));
        exhibition.setEndDate(LocalDate.now().plusDays(30));

        user = new SysUser();
        user.setId(3L);
        user.setUsername("visitor");
        user.setRealName("李明");
        user.setRole("VISITOR");
    }

    private ReservationDtos.CreateRequest request() {
        return new ReservationDtos.CreateRequest(10L, "李明", "420106199808081234", "13800000003", 2, "测试");
    }

    private void stubSuccessQueries() {
        when(slotMapper.selectById(10L)).thenReturn(slot);
        when(exhibitionMapper.selectById(20L)).thenReturn(exhibition);
        when(reservationMapper.selectCount(any())).thenReturn(0L);
        when(slotMapper.reserveCapacity(10L, 2)).thenReturn(1);
        when(reservationMapper.insert(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId(99L);
            return 1;
        });
        when(exhibitionMapper.selectBatchIds(anyList())).thenReturn(List.of(exhibition));
        when(userMapper.selectBatchIds(anyList())).thenReturn(List.of(user));
    }

    @Test
    void shouldCreateReservationAndReturnTicket() {
        stubSuccessQueries();

        ReservationVO result = reservationService.create(3L, request());

        assertThat(result.getOrderNo()).startsWith("HB");
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getStatusText()).isEqualTo("待参观");
        assertThat(result.getExhibitionTitle()).isEqualTo("曾侯乙编钟");
        assertThat(result.getQrContent()).contains(result.getOrderNo());
        verify(slotMapper).reserveCapacity(10L, 2);
        verify(reservationMapper).insert(any(Reservation.class));
    }

    @Test
    void shouldRejectWhenCapacityIsNotEnough() {
        when(slotMapper.selectById(10L)).thenReturn(slot);
        when(exhibitionMapper.selectById(20L)).thenReturn(exhibition);
        when(reservationMapper.selectCount(any())).thenReturn(0L);
        when(slotMapper.reserveCapacity(10L, 2)).thenReturn(0);

        assertThatThrownBy(() -> reservationService.create(3L, request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("余票不足");
        verify(reservationMapper, never()).insert(any(Reservation.class));
    }

    @Test
    void shouldRejectDuplicatedReservation() {
        when(slotMapper.selectById(10L)).thenReturn(slot);
        when(exhibitionMapper.selectById(20L)).thenReturn(exhibition);
        when(reservationMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> reservationService.create(3L, request()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("请勿重复预约");
        verify(slotMapper, never()).reserveCapacity(anyLong(), anyInt());
    }

    @Test
    void shouldCancelAndReleaseCapacity() {
        Reservation reservation = new Reservation();
        reservation.setId(5L);
        reservation.setUserId(3L);
        reservation.setSlotId(10L);
        reservation.setVisitDate(LocalDate.now().plusDays(1));
        reservation.setStartTime(LocalTime.of(9, 0));
        reservation.setStatus("CONFIRMED");
        reservation.setTicketCount(2);
        when(reservationMapper.selectById(5L)).thenReturn(reservation);
        when(reservationMapper.update(any(), any())).thenReturn(1);

        reservationService.cancel(3L, 5L, "行程有变", false);

        verify(reservationMapper).update(any(), any());
        verify(slotMapper).releaseCapacity(10L, 2);
    }

    @Test
    void shouldRejectCancelAfterVisitStarted() {
        Reservation reservation = new Reservation();
        reservation.setId(6L);
        reservation.setUserId(3L);
        reservation.setSlotId(10L);
        reservation.setVisitDate(LocalDate.now().minusDays(1));
        reservation.setStartTime(LocalTime.of(9, 0));
        reservation.setStatus("CONFIRMED");
        reservation.setTicketCount(1);
        when(reservationMapper.selectById(6L)).thenReturn(reservation);

        assertThatThrownBy(() -> reservationService.cancel(3L, 6L, null, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无法取消");
        verify(slotMapper, never()).releaseCapacity(anyLong(), anyInt());
    }
}