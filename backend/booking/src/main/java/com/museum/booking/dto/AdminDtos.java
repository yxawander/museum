package com.museum.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 后台管理相关请求体。
 */
public final class AdminDtos {

    private AdminDtos() {
    }

    public record ExhibitionForm(
            @NotBlank(message = "请输入展览名称")
            @Size(max = 100, message = "展览名称不能超过 100 个字符")
            String title,
            @Size(max = 200, message = "副标题不能超过 200 个字符") String subtitle,
            @NotBlank(message = "请选择展览分类") String category,
            String coverUrl,
            String description,
            @NotBlank(message = "请输入展出地点") String venue,
            @NotNull(message = "请选择开始日期") LocalDate startDate,
            @NotNull(message = "请选择结束日期") LocalDate endDate,
            @NotNull(message = "请选择开放时间") LocalTime openTime,
            @NotNull(message = "请选择闭馆时间") LocalTime closeTime,
            @NotNull(message = "请输入票价")
            @DecimalMin(value = "0", message = "票价不能为负数")
            BigDecimal ticketPrice,
            @Size(max = 500, message = "参观提示不能超过 500 个字符") String tips,
            String status,
            Integer sortOrder) {
    }

    public record SlotForm(
            @NotNull(message = "请选择展览") Long exhibitionId,
            @NotNull(message = "请选择参观日期") LocalDate visitDate,
            @NotNull(message = "请选择开始时间") LocalTime startTime,
            @NotNull(message = "请选择结束时间") LocalTime endTime,
            @NotNull(message = "请输入容量")
            @Min(value = 1, message = "容量至少为 1")
            @Max(value = 5000, message = "容量不能超过 5000")
            Integer capacity,
            String status,
            @Size(max = 255, message = "备注不能超过 255 个字符") String remark) {
    }

    public record BatchSlotForm(
            @NotNull(message = "请选择展览") Long exhibitionId,
            @NotNull(message = "请选择开始日期") LocalDate startDate,
            @NotNull(message = "请选择结束日期") LocalDate endDate,
            @NotEmpty(message = "请选择至少一个时段") List<String> startTimes,
            @NotNull(message = "请输入容量")
            @Min(value = 1, message = "容量至少为 1")
            @Max(value = 5000, message = "容量不能超过 5000")
            Integer capacity,
            Boolean skipMonday) {
    }

    public record AnnouncementForm(
            @NotBlank(message = "请输入公告标题")
            @Size(max = 120, message = "标题不能超过 120 个字符")
            String title,
            @Size(max = 255, message = "摘要不能超过 255 个字符") String summary,
            @NotBlank(message = "请输入公告正文") String content,
            @NotBlank(message = "请选择公告分类") String category,
            String coverUrl,
            Integer isTop,
            Integer status) {
    }

    public record ReplyForm(
            @NotBlank(message = "请输入回复内容")
            @Size(max = 1000, message = "回复内容不能超过 1000 个字符")
            String reply) {
    }

    public record StatusForm(
            @NotNull(message = "缺少状态值") Integer status) {
    }

    public record ResetPasswordForm(
            @NotBlank(message = "请输入新密码")
            @Size(min = 6, max = 20, message = "密码长度为 6-20 位")
            String newPassword) {
    }

    public record UserCreateForm(
            @NotBlank(message = "请输入用户名")
            @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名为 4-20 位字母、数字或下划线")
            String username,
            @NotBlank(message = "请输入密码")
            @Size(min = 6, max = 20, message = "密码长度为 6-20 位")
            String password,
            @NotBlank(message = "请输入真实姓名") String realName,
            @NotBlank(message = "请输入手机号")
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
            String phone,
            @Email(message = "邮箱格式不正确") String email,
            @NotBlank(message = "请选择角色")
            @Pattern(regexp = "^(STAFF|ADMIN)$", message = "只能创建工作人员或管理员")
            String role) {
    }
}