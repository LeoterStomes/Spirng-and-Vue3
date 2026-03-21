package org.example.springboot.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "心理测试报告响应DTO")
public class PsychologicalTestReportResponseDTO {

    @Schema(description = "报告ID")
    private Long id;

    @Schema(description = "测试类型")
    private String testType;

    @Schema(description = "测试类型显示名")
    private String testTypeDisplayName;

    @Schema(description = "报告标题")
    private String title;

    @Schema(description = "状态：PENDING/PROCESSING/COMPLETED/FAILED")
    private String status;

    @Schema(description = "状态显示名")
    private String statusDisplayName;

    @Schema(description = "总分")
    private Integer totalScore;

    @Schema(description = "风险等级 0-3")
    private Integer riskLevel;

    @Schema(description = "报告摘要（用于AI对话上下文）")
    private String reportSummary;

    @Schema(description = "报告正文（纯文本）")
    private String reportContent;

    @Schema(description = "失败原因（仅FAILED时有值）")
    private String errorMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

