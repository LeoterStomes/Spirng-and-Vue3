package org.example.springboot.service;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.AiService.PsychologicalSupportService;
import org.example.springboot.AiService.StructOutPut;
import org.example.springboot.DTO.command.ConsultationSessionCreateDTO;
import org.example.springboot.DTO.command.PsychologicalTestStartAiChatDTO;
import org.example.springboot.DTO.command.PsychologicalTestSubmitDTO;
import org.example.springboot.DTO.response.PsychologicalTestReportResponseDTO;
import org.example.springboot.AiService.PromptManage;
import org.example.springboot.entity.PsychologicalTestReport;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.PsychologicalTestReportMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 心理测试报告业务逻辑
 */
@Slf4j
@Service
public class PsychologicalTestReportService {

    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    @Resource
    private PsychologicalTestReportMapper reportMapper;

    @Autowired
    private PsychologicalSupportService psychologicalSupportService;

    @Autowired
    @Qualifier("open-ai")
    private ChatClient chatClient;

    /**
     * 创建心理测试报告并异步生成AI内容
     */
    @Transactional(rollbackFor = Exception.class)
    public PsychologicalTestReportResponseDTO createReport(Long userId, PsychologicalTestSubmitDTO submitDTO) {
        PsychologicalTestReport report = new PsychologicalTestReport();
        report.setUserId(userId);
        report.setTestType(submitDTO.getTestType());
        report.setTitle(submitDTO.getTitle());
        report.setAnswersJson(JSONUtil.toJsonStr(submitDTO.getAnswers()));
        report.setStatus(STATUS_PROCESSING);
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        // 默认标题
        if (!StringUtils.hasText(report.getTitle())) {
            report.setTitle(getTestTypeDisplayName(report.getTestType()) + " - " + LocalDateTime.now().toLocalDate());
        }

        reportMapper.insert(report);

        Long reportId = report.getId();
        log.info("已创建心理测试报告记录，reportId={}, userId={}, testType={}", reportId, userId, submitDTO.getTestType());

        // 提交后立即返回，报告在后台异步生成，避免前端请求超时
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    CompletableFuture.runAsync(() -> generateReportSync(reportId));
                }
            });
        } else {
            CompletableFuture.runAsync(() -> generateReportSync(reportId));
        }

        return toResponseDTO(report);
    }

    /**
     * 同步生成心理测试报告（AI）
     */
    private PsychologicalTestReport generateReportSync(Long reportId) {
        PsychologicalTestReport report = reportMapper.selectById(reportId);
        if (report == null) {
            log.warn("生成心理测试报告失败：报告不存在，reportId={}", reportId);
            return null;
        }

        try {
            log.info("开始生成心理测试报告，reportId={}, testType={}", reportId, report.getTestType());

            String testType = report.getTestType();
            String answersJson = report.getAnswersJson();
            if (!StringUtils.hasText(testType) || !StringUtils.hasText(answersJson)) {
                throw new BusinessException("测试类型或答案数据为空，无法生成报告");
            }

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(PromptManage.PSYCHOLOGICAL_TEST_REPORT_SYSTEM_PROMPT)
            ));

            String userMessage =
                    "testType=" + testType + "\n" +
                    "testTypeDisplayName=" + getTestTypeDisplayName(testType) + "\n" +
                    "answersJson=" + answersJson + "\n" +
                    "要求：请直接生成并返回严格JSON，不要输出任何额外解释。";

            StructOutPut.PsychologicalTestReportResult result = chatClient
                    .prompt(prompt)
                    .user(userMessage)
                    .call()
                    .entity(StructOutPut.PsychologicalTestReportResult.class);

            if (result == null) {
                throw new BusinessException("AI返回的心理测试报告结果为空");
            }

            report.setTotalScore(result.totalScore());
            report.setRiskLevel(result.riskLevel());
            report.setReportSummary(result.summary());
            report.setReportContent(result.reportText());
            report.setReportJson(JSONUtil.toJsonStr(result));
            report.setStatus(STATUS_COMPLETED);
            report.setErrorMessage(null);
            report.setUpdatedAt(LocalDateTime.now());

            reportMapper.updateById(report);

            log.info("心理测试报告生成完成，reportId={}, riskLevel={}, totalScore={}",
                    reportId, result.riskLevel(), result.totalScore());
            return report;
        } catch (Exception e) {
            log.error("心理测试报告生成失败，reportId={}, error={}", reportId, e.getMessage(), e);
            report.setStatus(STATUS_FAILED);
            report.setErrorMessage(e.getMessage());
            report.setUpdatedAt(LocalDateTime.now());
            reportMapper.updateById(report);
            return report;
        }
    }

    /**
     * 获取用户自己的报告详情
     */
    public PsychologicalTestReportResponseDTO getReportById(Long reportId, Long userId) {
        PsychologicalTestReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此报告");
        }
        return toResponseDTO(report);
    }

    /**
     * 获取用户的报告列表（按创建时间倒序）
     */
    public List<PsychologicalTestReportResponseDTO> listReports(Long userId) {
        List<PsychologicalTestReport> reports = reportMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PsychologicalTestReport>()
                        .eq(PsychologicalTestReport::getUserId, userId)
                        .orderByDesc(PsychologicalTestReport::getCreatedAt)
        );
        return reports.stream().map(this::toResponseDTO).toList();
    }

    /**
     * 从报告创建 AI 心理疏导对话会话
     */
    public StructOutPut.StreamChatSession startAiChatFromReport(Long reportId, Long userId, PsychologicalTestStartAiChatDTO startDTO) {
        PsychologicalTestReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException("无权访问此报告");
        }

        if (!STATUS_COMPLETED.equals(report.getStatus())) {
            throw new BusinessException("报告尚未生成完成，请稍后再试");
        }
        if (!StringUtils.hasText(report.getReportSummary()) && !StringUtils.hasText(report.getReportContent())) {
            throw new BusinessException("报告内容为空，无法进入AI对话");
        }

        String sessionTitle = startDTO != null && StringUtils.hasText(startDTO.getSessionTitle())
                ? startDTO.getSessionTitle()
                : "心理测试报告咨询 - " + LocalDateTime.now().toString();

        String initialMessage = buildInitialMessageForAiChat(report);

        ConsultationSessionCreateDTO createDTO = new ConsultationSessionCreateDTO();
        createDTO.setSessionTitle(sessionTitle);
        createDTO.setInitialMessage(initialMessage);

        // 复用你现有的心理疏导会话创建/落库逻辑
        return psychologicalSupportService.startChatSession(userId, createDTO);
    }

    private String buildInitialMessageForAiChat(PsychologicalTestReport report) {
        String summary = report.getReportSummary();
        String content = report.getReportContent();

        String combined = "";
        if (StringUtils.hasText(summary)) {
            combined += "心理测试报告摘要：\n" + summary + "\n\n";
        }
        if (StringUtils.hasText(content)) {
            // 防止 initialMessage 太长影响接口/模型
            combined += "心理测试报告正文（节选）：\n" + truncate(content, 900) + "\n\n";
        }

        String riskHint = report.getRiskLevel() == null
                ? ""
                : ("（风险等级：" + report.getRiskLevel() + "，总分：" + report.getTotalScore() + "）\n\n");

        return "我完成了一份心理测试，请你基于我的报告帮助我理解情况，并给出具体的心理疏导建议与可执行的下一步行动/可能的治疗或求助方向。\n\n"
                + riskHint
                + combined
                + "如果你判断存在较高心理风险，请优先给出紧急应对建议，并鼓励我尽快联系专业机构/学校心理中心。"
                ;
    }

    private String truncate(String s, int maxLen) {
        if (s == null) {
            return null;
        }
        if (s.length() <= maxLen) {
            return s;
        }
        return s.substring(0, maxLen) + "...";
    }

    private PsychologicalTestReportResponseDTO toResponseDTO(PsychologicalTestReport report) {
        PsychologicalTestReportResponseDTO dto = new PsychologicalTestReportResponseDTO();
        dto.setId(report.getId());
        dto.setTestType(report.getTestType());
        dto.setTestTypeDisplayName(getTestTypeDisplayName(report.getTestType()));
        dto.setTitle(report.getTitle());
        dto.setStatus(report.getStatus());
        dto.setStatusDisplayName(getStatusDisplayName(report.getStatus()));
        dto.setTotalScore(report.getTotalScore());
        dto.setRiskLevel(report.getRiskLevel());
        dto.setReportSummary(report.getReportSummary());
        dto.setReportContent(report.getReportContent());
        dto.setErrorMessage(report.getErrorMessage());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        return dto;
    }

    private String getTestTypeDisplayName(String testType) {
        if ("DEPRESSION".equalsIgnoreCase(testType)) {
            return "抑郁倾向测试";
        }
        if ("PERSONALITY".equalsIgnoreCase(testType)) {
            return "人格倾向测试";
        }
        if ("ENNEAGRAM_108".equalsIgnoreCase(testType)) {
            return "九型人格测试（108题）";
        }
        return "心理测试";
    }

    private String getStatusDisplayName(String status) {
        if ("PENDING".equalsIgnoreCase(status)) {
            return "排队中";
        }
        if ("PROCESSING".equalsIgnoreCase(status)) {
            return "生成中";
        }
        if ("COMPLETED".equalsIgnoreCase(status)) {
            return "已完成";
        }
        if ("FAILED".equalsIgnoreCase(status)) {
            return "生成失败";
        }
        return "未知状态";
    }
}

