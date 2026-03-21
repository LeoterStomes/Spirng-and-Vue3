package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 从报告进入 AI 对话的请求DTO
 */
@Data
@Schema(description = "从心理测试报告进入AI对话DTO")
public class PsychologicalTestStartAiChatDTO {

    @Schema(description = "会话标题（可选）")
    @Size(max = 200, message = "会话标题长度不能超过200字符")
    private String sessionTitle;
}

