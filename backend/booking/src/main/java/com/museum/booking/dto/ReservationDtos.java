package com.museum.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 预约、核销、反馈相关请求体。
 */
public final class ReservationDtos {

    private ReservationDtos() {
    }

    public record CreateRequest(
            @NotNull(message = "请选择参观时段") Long slotId,
            @NotBlank(message = "请输入参观人姓名")
            @Size(max = 20, message = "姓名不能超过 20 个字符")
            String visitorName,
            @NotBlank(message = "请输入参观人身份证号")
            @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
            String visitorIdCard,
            @NotBlank(message = "请输入参观人手机号")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            String visitorPhone,
            @NotNull(message = "请选择预约人数")
            @Min(value = 1, message = "至少预约 1 人")
            @Max(value = 5, message = "单次最多预约 5 人")
            Integer ticketCount,
            @Size(max = 200, message = "备注不能超过 200 个字符") String remark) {
    }

    public record CancelRequest(
            @Size(max = 200, message = "取消原因不能超过 200 个字符") String reason) {
    }

    public record CheckinRequest(
            @NotBlank(message = "请输入预约单号或核销码") String code) {
    }

    public record FeedbackRequest(
            @NotBlank(message = "请选择反馈类型") String type,
            @NotBlank(message = "请输入反馈内容")
            @Size(max = 1000, message = "反馈内容不能超过 1000 个字符")
            String content,
            @Size(max = 100, message = "联系方式不能超过 100 个字符") String contact) {
    }
}