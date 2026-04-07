package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.common.Result;
import org.example.springboot.entity.*;
import org.example.springboot.enumClass.UserType;
import org.example.springboot.service.DoctorService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "医生工作台")
@RestController
@Slf4j
@RequestMapping("/doctor")
public class DoctorController {

    @Resource
    private DoctorService doctorService;

    private Long requireDoctor() {
        Integer role = JwtTokenUtils.getCurrentUserRole();
        if (!UserType.DOCTOR.getCode().equals(role) && !UserType.ADMIN.getCode().equals(role)) {
            throw new RuntimeException("权限不足，仅医生或管理员可访问");
        }
        return JwtTokenUtils.getCurrentUserId();
    }

    // ============== 工作台首页 ==============

    @Operation(summary = "医生工作台统计")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Long doctorId = requireDoctor();
        return Result.success(doctorService.getDashboardStats(doctorId));
    }

    // ============== 患者管理 ==============

    @Operation(summary = "我的患者列表")
    @GetMapping("/patients")
    public Result<Page<Map<String, Object>>> getMyPatients(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "风险等级筛选") @RequestParam(required = false) Integer riskLevel,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long doctorId = requireDoctor();
        return Result.success(doctorService.getMyPatients(doctorId, keyword, riskLevel, page, size));
    }

    @Operation(summary = "绑定患者")
    @PostMapping("/patients/bind")
    public Result<DoctorPatient> bindPatient(
            @Parameter(description = "患者用户名/手机号/邮箱") @RequestParam String keyword) {
        Long doctorId = requireDoctor();
        return Result.success("绑定成功", doctorService.bindPatient(doctorId, keyword));
    }

    @Operation(summary = "解绑患者")
    @PostMapping("/patients/{patientId}/unbind")
    public Result<Void> unbindPatient(@PathVariable Long patientId) {
        Long doctorId = requireDoctor();
        doctorService.unbindPatient(doctorId, patientId);
        return Result.success();
    }

    // ============== 患者档案 ==============

    @Operation(summary = "获取患者档案")
    @GetMapping("/patients/{patientId}/profile")
    public Result<PatientProfile> getProfile(@PathVariable Long patientId) {
        Long doctorId = requireDoctor();
        Map<String, Object> overview = doctorService.getPatientOverview(doctorId, patientId);
        PatientProfile profile = (PatientProfile) overview.get("profile");
        return Result.success(profile);
    }

    @Operation(summary = "更新患者档案")
    @PutMapping("/patients/{patientId}/profile")
    public Result<PatientProfile> updateProfile(
            @PathVariable Long patientId,
            @RequestBody PatientProfile dto) {
        Long doctorId = requireDoctor();
        return Result.success(doctorService.updateProfile(doctorId, patientId, dto));
    }

    // ============== 患者综合概览 ==============

    @Operation(summary = "患者综合概览")
    @GetMapping("/patients/{patientId}/overview")
    public Result<Map<String, Object>> getOverview(@PathVariable Long patientId) {
        Long doctorId = requireDoctor();
        return Result.success(doctorService.getPatientOverview(doctorId, patientId));
    }

    // ============== 患者数据 ==============

    @Operation(summary = "患者情绪日记")
    @GetMapping("/patients/{patientId}/diaries")
    public Result<Page<EmotionDiary>> getPatientDiaries(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long doctorId = requireDoctor();
        return Result.success(doctorService.getPatientDiaries(doctorId, patientId, page, size));
    }

    @Operation(summary = "患者咨询会话")
    @GetMapping("/patients/{patientId}/sessions")
    public Result<Page<ConsultationSession>> getPatientSessions(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long doctorId = requireDoctor();
        return Result.success(doctorService.getPatientSessions(doctorId, patientId, page, size));
    }

    @Operation(summary = "患者心理测试报告")
    @GetMapping("/patients/{patientId}/reports")
    public Result<Page<PsychologicalTestReport>> getPatientReports(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long doctorId = requireDoctor();
        return Result.success(doctorService.getPatientReports(doctorId, patientId, page, size));
    }

    // ============== 临床备注 ==============

    @Operation(summary = "添加临床备注")
    @PostMapping("/patients/{patientId}/notes")
    public Result<ClinicalNote> addNote(
            @PathVariable Long patientId,
            @RequestBody ClinicalNote note) {
        Long doctorId = requireDoctor();
        return Result.success("备注已添加", doctorService.addNote(doctorId, patientId, note));
    }

    @Operation(summary = "查看临床备注")
    @GetMapping("/patients/{patientId}/notes")
    public Result<Page<ClinicalNote>> getNotes(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long doctorId = requireDoctor();
        return Result.success(doctorService.getNotes(doctorId, patientId, page, size));
    }
}
