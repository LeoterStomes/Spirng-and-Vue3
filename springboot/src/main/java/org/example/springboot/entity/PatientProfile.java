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
@TableName("patient_profile")
@Schema(description = "患者档案实体")
public class PatientProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    @Schema(description = "患者用户ID")
    private Long userId;

    @TableField("doctor_id")
    @Schema(description = "主治医生用户ID")
    private Long doctorId;

    @TableField("chief_complaint")
    @Schema(description = "主诉")
    private String chiefComplaint;

    @TableField("diagnosis_summary")
    @Schema(description = "诊断摘要")
    private String diagnosisSummary;

    @TableField("risk_level")
    @Schema(description = "风险等级 0-3")
    private Integer riskLevel;

    @TableField("treatment_plan")
    @Schema(description = "治疗方案")
    private String treatmentPlan;

    @TableField("medication_notes")
    @Schema(description = "用药备注")
    private String medicationNotes;

    @TableField("personality_traits")
    @Schema(description = "性格特点（AI参考）")
    private String personalityTraits;

    @TableField("communication_style")
    @Schema(description = "沟通偏好: gentle/direct/encouraging")
    private String communicationStyle;

    @TableField("tags")
    @Schema(description = "标签JSON")
    private String tags;

    @TableField("status")
    @Schema(description = "状态: active/archived")
    private String status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
