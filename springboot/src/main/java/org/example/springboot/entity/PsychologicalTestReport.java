package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 心理测试报告实体
 *
 * 说明：
 * - answersJson：用户在前端提交的原始答案（JSON字符串）
 * - reportContent：AI生成的报告正文（纯文本）
 * - reportJson：AI生成的结构化结果（JSON字符串，便于前端/后续扩展）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("psychological_test_report")
@Schema(description = "心理测试报告实体类")
public class PsychologicalTestReport {

    @TableId(type = IdType.AUTO)
    @Schema(description = "报告ID")
    private Long id;

    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    @TableField("test_type")
    @Schema(description = "测试类型，如: DEPRESSION / PERSONALITY")
    private String testType;

    @TableField("title")
    @Schema(description = "报告标题")
    private String title;

    @TableField("answers_json")
    @Schema(description = "用户提交答案(JSON)")
    private String answersJson;

    @TableField("total_score")
    @Schema(description = "总分（由AI评估/推导，具体取值由prompt定义）")
    private Integer totalScore;

    @TableField("risk_level")
    @Schema(description = "风险等级 0-3（0安全，3危机）")
    private Integer riskLevel;

    @TableField("report_summary")
    @Schema(description = "报告摘要（用于AI对话上下文、列表展示）")
    private String reportSummary;

    @TableField("report_content")
    @Schema(description = "报告正文（纯文本）")
    private String reportContent;

    @TableField("report_json")
    @Schema(description = "报告结构化JSON（便于前端解析/审计）")
    private String reportJson;

    @TableField("status")
    @Schema(description = "状态：PENDING/PROCESSING/COMPLETED/FAILED")
    private String status;

    @TableField("error_message")
    @Schema(description = "失败原因")
    private String errorMessage;

    @TableField("created_at")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

