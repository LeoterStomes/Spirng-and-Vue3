package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.AiService.StructOutPut;
import org.example.springboot.DTO.command.PsychologicalTestStartAiChatDTO;
import org.example.springboot.DTO.command.PsychologicalTestSubmitDTO;
import org.example.springboot.DTO.response.PsychologicalTestReportResponseDTO;
import org.example.springboot.common.Result;
import org.example.springboot.service.PsychologicalTestReportService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 心理测试接口
 */
@Tag(name = "心理测试", description = "心理测试提交、报告生成与AI对话入口")
@RestController
@Slf4j
@RequestMapping("/psychological-test")
public class PsychologicalTestController {

    @Resource
    private PsychologicalTestReportService psychologicalTestReportService;

    /**
     * 提交心理测试并创建报告（AI异步生成）
     */
    @Operation(summary = "提交心理测试并生成报告", description = "提交测试答案，系统将生成心理测试报告（AI异步生成）")
    @PostMapping("/reports")
    public Result<PsychologicalTestReportResponseDTO> submitTest(
            @Valid @RequestBody PsychologicalTestSubmitDTO submitDTO
    ) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            PsychologicalTestReportResponseDTO response = psychologicalTestReportService.createReport(userId, submitDTO);
            return Result.success("已创建报告并开始生成", response);
        } catch (Exception e) {
            log.error("提交心理测试失败: {}", e.getMessage(), e);
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户报告列表
     */
    @Operation(summary = "获取报告列表", description = "获取当前用户的心理测试报告列表")
    @GetMapping("/reports")
    public Result<List<PsychologicalTestReportResponseDTO>> listMyReports() {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            List<PsychologicalTestReportResponseDTO> list = psychologicalTestReportService.listReports(userId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取报告列表失败: {}", e.getMessage(), e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取报告详情
     */
    @Operation(summary = "获取报告详情", description = "获取指定报告详情（仅所属用户可访问）")
    @GetMapping("/reports/{reportId}")
    public Result<PsychologicalTestReportResponseDTO> getReportDetail(@PathVariable Long reportId) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            PsychologicalTestReportResponseDTO dto = psychologicalTestReportService.getReportById(reportId, userId);
            return Result.success(dto);
        } catch (Exception e) {
            log.error("获取报告详情失败: reportId={}, error={}", reportId, e.getMessage(), e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 从报告进入 AI 助手对话
     */
    @Operation(summary = "从报告进入AI对话", description = "用户打开报告后，可一键创建心理疏导对话会话，并把报告内容作为上下文")
    @PostMapping("/reports/{reportId}/start-ai-chat")
    public Result<StructOutPut.StreamChatSession> startAiChatFromReport(
            @PathVariable Long reportId,
            @RequestBody(required = false) PsychologicalTestStartAiChatDTO startDTO
    ) {
        Long userId = JwtTokenUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }

        try {
            StructOutPut.StreamChatSession session =
                    psychologicalTestReportService.startAiChatFromReport(reportId, userId, startDTO);
            return Result.success(session);
        } catch (Exception e) {
            log.error("从报告进入AI对话失败: reportId={}, error={}", reportId, e.getMessage(), e);
            return Result.error("进入失败: " + e.getMessage());
        }
    }
}

