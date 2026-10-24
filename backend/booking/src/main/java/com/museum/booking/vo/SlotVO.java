package com.museum.booking.vo;

import com.museum.booking.entity.VisitSlot;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 参观时段视图对象，包含实时余量。
 */
@Data
@Builder
public class SlotVO {

    private Long id;
    private Long exhibitionId;
    private String exhibitionTitle;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private Integer bookedCount;
    private Integer remaining;
    private String status;
    private String statusText;
    private String remark;

    public static SlotVO from(VisitSlot slot) {
        return from(slot, null);
    }

    public static SlotVO from(VisitSlot slot, String exhibitionTitle) {
        if (slot == null) {
            return null;
        }
        int remaining = Math.max(0, slot.getCapacity() - slot.getBookedCount());
        String statusText;
        if ("CLOSED".equals(slot.getStatus())) {
            statusText = "已关闭";
        } else if (remaining <= 0) {
            statusText = "已约满";
        } else {
            statusText = "可预约";
        }
        return SlotVO.builder()
                .id(slot.getId())
                .exhibitionId(slot.getExhibitionId())
                .exhibitionTitle(exhibitionTitle)
                .visitDate(slot.getVisitDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .capacity(slot.getCapacity())
                .bookedCount(slot.getBookedCount())
                .remaining(remaining)
                .status(slot.getStatus())
                .statusText(statusText)
                .remark(slot.getRemark())
                .build();
    }
}