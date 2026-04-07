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
@TableName("doctor_patient")
@Schema(description = "医生-患者绑定关系")
public class DoctorPatient {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("doctor_id")
    @Schema(description = "医生用户ID")
    private Long doctorId;

    @TableField("patient_id")
    @Schema(description = "患者用户ID")
    private Long patientId;

    @TableField("bind_at")
    @Schema(description = "绑定时间")
    private LocalDateTime bindAt;

    @TableField("status")
    @Schema(description = "状态: active/removed")
    private String status;
}
