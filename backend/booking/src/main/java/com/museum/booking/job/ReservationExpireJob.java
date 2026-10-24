package com.museum.booking.job;

import com.museum.booking.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时将已过参观时段的“待参观”订单标记为“已过期”。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpireJob {

    private final ReservationService reservationService;

    @Scheduled(fixedDelay = 600_000, initialDelay = 30_000)
    public void expire() {
        int count = reservationService.expirePastReservations();
        if (count > 0) {
            log.info("已将 {} 条过期预约标记为 EXPIRED", count);
        }
    }
}