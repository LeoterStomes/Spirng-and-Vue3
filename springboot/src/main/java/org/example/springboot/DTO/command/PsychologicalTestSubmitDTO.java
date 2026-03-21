package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 心理测试提交DTO
 *
 * 说明：
 * answers 用于承载前端的题目回答（建议包含每题的得分score或选项score）。
 * 由于不同心理量表格式不同，这里采用通用Map(JSON)结构，AI将基于 answersJson 生成报告。
 */
@Data
@Schema(description = "心理测试提交DTO")
public class PsychologicalTestSubmitDTO {

    @NotBlank(message = "测试类型不能为空")
    @Schema(description = "测试类型，如: DEPRESSION / PERSONALITY")
    private String testType;

    @Schema(description = "报告标题（可选）")
    @Size(max = 200, message = "标题长度不能超过200字符")
    private String title;

    @NotNull(message = "answers不能为空")
    @Schema(description = "用户提交的原始答案JSON(Map结构)")
    private Map<String, Object> answers;
}

