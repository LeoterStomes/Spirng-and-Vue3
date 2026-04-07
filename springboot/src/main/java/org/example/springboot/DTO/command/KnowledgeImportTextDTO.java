package org.example.springboot.DTO.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "文本知识导入请求")
public class KnowledgeImportTextDTO {

    @Schema(description = "文章标题", example = "睡眠焦虑的自助调节方法")
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Schema(description = "文本正文内容", example = "这里填写你导入的本地知识文本...")
    @NotBlank(message = "导入内容不能为空")
    @Size(max = 20000, message = "导入内容长度不能超过20000个字符")
    private String content;

    @Schema(description = "分类ID，不传则自动使用第一个启用分类", example = "1")
    private Long categoryId;

    @Schema(description = "标签（逗号分隔）", example = "焦虑,睡眠,心理健康")
    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;

    @Schema(description = "摘要（可选）", example = "睡眠焦虑的常见成因和应对技巧")
    @Size(max = 1000, message = "摘要长度不能超过1000个字符")
    private String summary;
}
