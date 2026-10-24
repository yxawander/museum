package com.museum.booking.vo;

import com.museum.booking.entity.Feedback;
import com.museum.booking.entity.SysUser;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 意见反馈视图对象。
 */
@Data
@Builder
public class FeedbackVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String type;
    private String typeText;
    private String content;
    private String contact;
    private String reply;
    private String status;
    private String statusText;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;

    public static FeedbackVO from(Feedback feedback, SysUser user) {
        return FeedbackVO.builder()
                .id(feedback.getId())
                .userId(feedback.getUserId())
                .username(user == null ? null : user.getUsername())
                .realName(user == null ? null : user.getRealName())
                .type(feedback.getType())
                .typeText(typeText(feedback.getType()))
                .content(feedback.getContent())
                .contact(feedback.getContact())
                .reply(feedback.getReply())
                .status(feedback.getStatus())
                .statusText("REPLIED".equals(feedback.getStatus()) ? "已回复" : "待处理")
                .repliedAt(feedback.getRepliedAt())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    public static String typeText(String type) {
        return switch (type == null ? "" : type) {
            case "SUGGESTION" -> "意见建议";
            case "COMPLAINT" -> "投诉";
            case "PRAISE" -> "表扬";
            default -> "其他";
        };
    }
}