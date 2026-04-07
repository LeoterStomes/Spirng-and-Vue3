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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("clinical_note")
@Schema(description = "临床备注")
public class ClinicalNote {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("doctor_id")
    @Schema(description = "医生用户ID")
    private Long doctorId;

    @TableField("patient_id")
    @Schema(description = "患者用户ID")
    private Long patientId;

    @TableField("note_type")
    @Schema(description = "备注类型: daily/consultation/risk_alert/followup")
    private String noteType;

    @TableField("content")
    @Schema(description = "备注内容")
    private String content;

    @TableField("related_diary_id")
    @Schema(description = "关联情绪日记ID")
    private Long relatedDiaryId;

    @TableField("related_session_id")
    @Schema(description = "关联咨询会话ID")
    private Long relatedSessionId;

    @TableField("related_report_id")
    @Schema(description = "关联心理测试报告ID")
    private Long relatedReportId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
