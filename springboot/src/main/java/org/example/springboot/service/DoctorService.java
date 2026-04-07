package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.entity.*;
import org.example.springboot.enumClass.UserType;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoctorService {

    @Resource
    private DoctorPatientMapper doctorPatientMapper;
    @Resource
    private PatientProfileMapper patientProfileMapper;
    @Resource
    private ClinicalNoteMapper clinicalNoteMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private EmotionDiaryMapper emotionDiaryMapper;
    @Resource
    private ConsultationSessionMapper consultationSessionMapper;
    @Resource
    private PsychologicalTestReportMapper psychologicalTestReportMapper;

    // ============== 患者绑定 ==============

    @Transactional(rollbackFor = Exception.class)
    public DoctorPatient bindPatient(Long doctorId, String keyword) {
        User doctor = userMapper.selectById(doctorId);
        if (doctor == null || (!UserType.DOCTOR.getCode().equals(doctor.getUserType())
                && !UserType.ADMIN.getCode().equals(doctor.getUserType()))) {
            throw new BusinessException("当前用户不是医生/管理员");
        }

        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<>();
        q.and(w -> w.like(User::getUsername, keyword)
                .or().like(User::getPhone, keyword)
                .or().like(User::getEmail, keyword));
        q.eq(User::getUserType, UserType.USER.getCode());
        User patient = userMapper.selectOne(q.last("LIMIT 1"));

        if (patient == null) {
            throw new BusinessException("未找到匹配的患者用户");
        }

        LambdaQueryWrapper<DoctorPatient> existQ = new LambdaQueryWrapper<>();
        existQ.eq(DoctorPatient::getDoctorId, doctorId)
              .eq(DoctorPatient::getPatientId, patient.getId())
              .eq(DoctorPatient::getStatus, "active");
        if (doctorPatientMapper.selectCount(existQ) > 0) {
            throw new BusinessException("该患者已绑定");
        }

        DoctorPatient dp = DoctorPatient.builder()
                .doctorId(doctorId)
                .patientId(patient.getId())
                .bindAt(LocalDateTime.now())
                .status("active")
                .build();
        doctorPatientMapper.insert(dp);

        if (getProfileByUserId(patient.getId()) == null) {
            PatientProfile profile = PatientProfile.builder()
                    .userId(patient.getId())
                    .doctorId(doctorId)
                    .riskLevel(0)
                    .communicationStyle("gentle")
                    .status("active")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            patientProfileMapper.insert(profile);
        }

        log.info("医生{}绑定患者{}成功", doctorId, patient.getId());
        return dp;
    }

    @Transactional(rollbackFor = Exception.class)
    public void unbindPatient(Long doctorId, Long patientId) {
        LambdaQueryWrapper<DoctorPatient> q = new LambdaQueryWrapper<>();
        q.eq(DoctorPatient::getDoctorId, doctorId)
         .eq(DoctorPatient::getPatientId, patientId)
         .eq(DoctorPatient::getStatus, "active");
        DoctorPatient dp = doctorPatientMapper.selectOne(q);
        if (dp == null) {
            throw new BusinessException("绑定关系不存在");
        }
        dp.setStatus("removed");
        doctorPatientMapper.updateById(dp);
    }

    // ============== 患者列表 ==============

    public Page<Map<String, Object>> getMyPatients(Long doctorId, String keyword, Integer riskLevel, int page, int size) {
        LambdaQueryWrapper<DoctorPatient> dpQ = new LambdaQueryWrapper<>();
        dpQ.eq(DoctorPatient::getDoctorId, doctorId)
           .eq(DoctorPatient::getStatus, "active");
        List<DoctorPatient> bindings = doctorPatientMapper.selectList(dpQ);

        if (bindings.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> patientIds = bindings.stream().map(DoctorPatient::getPatientId).toList();
        Map<Long, LocalDateTime> bindAtMap = bindings.stream()
                .collect(Collectors.toMap(DoctorPatient::getPatientId, DoctorPatient::getBindAt, (a, b) -> a));

        LambdaQueryWrapper<User> userQ = new LambdaQueryWrapper<>();
        userQ.in(User::getId, patientIds);
        if (StringUtils.hasText(keyword)) {
            userQ.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }

        Map<Long, PatientProfile> profileMap = new HashMap<>();
        LambdaQueryWrapper<PatientProfile> profileQ = new LambdaQueryWrapper<>();
        profileQ.in(PatientProfile::getUserId, patientIds);
        patientProfileMapper.selectList(profileQ).forEach(p -> profileMap.put(p.getUserId(), p));

        if (riskLevel != null) {
            Set<Long> matchIds = profileMap.entrySet().stream()
                    .filter(e -> riskLevel.equals(e.getValue().getRiskLevel()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            if (matchIds.isEmpty()) {
                return new Page<>(page, size, 0);
            }
            userQ.in(User::getId, matchIds);
        }

        userQ.orderByDesc(User::getCreatedAt);
        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), userQ);

        List<Map<String, Object>> records = userPage.getRecords().stream().map(u -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", u.getId());
            item.put("patientId", u.getId());
            item.put("username", u.getUsername());
            item.put("nickname", u.getNickname());
            item.put("phone", u.getPhone());
            item.put("gender", u.getGender());
            item.put("avatar", u.getAvatar());
            item.put("bindAt", bindAtMap.get(u.getId()));

            PatientProfile profile = profileMap.get(u.getId());
            item.put("riskLevel", profile != null ? profile.getRiskLevel() : 0);
            item.put("diagnosisSummary", profile != null ? profile.getDiagnosisSummary() : null);
            item.put("communicationStyle", profile != null ? profile.getCommunicationStyle() : "gentle");
            item.put("profileStatus", profile != null ? profile.getStatus() : null);
            return item;
        }).toList();

        Page<Map<String, Object>> result = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        result.setRecords(records);
        return result;
    }

    // ============== 患者档案 ==============

    public PatientProfile getProfileByUserId(Long userId) {
        LambdaQueryWrapper<PatientProfile> q = new LambdaQueryWrapper<>();
        q.eq(PatientProfile::getUserId, userId).last("LIMIT 1");
        return patientProfileMapper.selectOne(q);
    }

    @Transactional(rollbackFor = Exception.class)
    public PatientProfile updateProfile(Long doctorId, Long patientId, PatientProfile dto) {
        assertDoctorPatientBound(doctorId, patientId);

        PatientProfile existing = getProfileByUserId(patientId);
        if (existing == null) {
            dto.setUserId(patientId);
            dto.setDoctorId(doctorId);
            dto.setCreatedAt(LocalDateTime.now());
            dto.setUpdatedAt(LocalDateTime.now());
            if (dto.getStatus() == null) dto.setStatus("active");
            patientProfileMapper.insert(dto);
            return dto;
        }

        existing.setChiefComplaint(dto.getChiefComplaint());
        existing.setDiagnosisSummary(dto.getDiagnosisSummary());
        existing.setRiskLevel(dto.getRiskLevel());
        existing.setTreatmentPlan(dto.getTreatmentPlan());
        existing.setMedicationNotes(dto.getMedicationNotes());
        existing.setPersonalityTraits(dto.getPersonalityTraits());
        existing.setCommunicationStyle(dto.getCommunicationStyle());
        existing.setTags(dto.getTags());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        patientProfileMapper.updateById(existing);
        return existing;
    }

    // ============== 患者概览 ==============

    public Map<String, Object> getPatientOverview(Long doctorId, Long patientId) {
        assertDoctorPatientBound(doctorId, patientId);

        Map<String, Object> overview = new LinkedHashMap<>();
        User patient = userMapper.selectById(patientId);
        overview.put("userId", patientId);
        overview.put("username", patient != null ? patient.getUsername() : null);
        overview.put("nickname", patient != null ? patient.getNickname() : null);

        PatientProfile profile = getProfileByUserId(patientId);
        overview.put("profile", profile);

        LambdaQueryWrapper<EmotionDiary> diaryQ = new LambdaQueryWrapper<>();
        diaryQ.eq(EmotionDiary::getUserId, patientId).orderByDesc(EmotionDiary::getCreatedAt).last("LIMIT 3");
        overview.put("recentDiaries", emotionDiaryMapper.selectList(diaryQ));

        LambdaQueryWrapper<ConsultationSession> sessionQ = new LambdaQueryWrapper<>();
        sessionQ.eq(ConsultationSession::getUserId, patientId).orderByDesc(ConsultationSession::getStartedAt).last("LIMIT 3");
        overview.put("recentSessions", consultationSessionMapper.selectList(sessionQ));

        LambdaQueryWrapper<PsychologicalTestReport> reportQ = new LambdaQueryWrapper<>();
        reportQ.eq(PsychologicalTestReport::getUserId, patientId).orderByDesc(PsychologicalTestReport::getCreatedAt).last("LIMIT 3");
        overview.put("recentReports", psychologicalTestReportMapper.selectList(reportQ));

        LambdaQueryWrapper<ClinicalNote> noteQ = new LambdaQueryWrapper<>();
        noteQ.eq(ClinicalNote::getPatientId, patientId).orderByDesc(ClinicalNote::getCreatedAt).last("LIMIT 5");
        overview.put("recentNotes", clinicalNoteMapper.selectList(noteQ));

        return overview;
    }

    // ============== 患者数据访问 ==============

    public Page<EmotionDiary> getPatientDiaries(Long doctorId, Long patientId, int page, int size) {
        assertDoctorPatientBound(doctorId, patientId);
        LambdaQueryWrapper<EmotionDiary> q = new LambdaQueryWrapper<>();
        q.eq(EmotionDiary::getUserId, patientId).orderByDesc(EmotionDiary::getCreatedAt);
        return emotionDiaryMapper.selectPage(new Page<>(page, size), q);
    }

    public Page<ConsultationSession> getPatientSessions(Long doctorId, Long patientId, int page, int size) {
        assertDoctorPatientBound(doctorId, patientId);
        LambdaQueryWrapper<ConsultationSession> q = new LambdaQueryWrapper<>();
        q.eq(ConsultationSession::getUserId, patientId).orderByDesc(ConsultationSession::getStartedAt);
        return consultationSessionMapper.selectPage(new Page<>(page, size), q);
    }

    public Page<PsychologicalTestReport> getPatientReports(Long doctorId, Long patientId, int page, int size) {
        assertDoctorPatientBound(doctorId, patientId);
        LambdaQueryWrapper<PsychologicalTestReport> q = new LambdaQueryWrapper<>();
        q.eq(PsychologicalTestReport::getUserId, patientId).orderByDesc(PsychologicalTestReport::getCreatedAt);
        return psychologicalTestReportMapper.selectPage(new Page<>(page, size), q);
    }

    // ============== 临床备注 ==============

    @Transactional(rollbackFor = Exception.class)
    public ClinicalNote addNote(Long doctorId, Long patientId, ClinicalNote note) {
        assertDoctorPatientBound(doctorId, patientId);
        note.setDoctorId(doctorId);
        note.setPatientId(patientId);
        note.setCreatedAt(LocalDateTime.now());
        clinicalNoteMapper.insert(note);
        return note;
    }

    public Page<ClinicalNote> getNotes(Long doctorId, Long patientId, int page, int size) {
        assertDoctorPatientBound(doctorId, patientId);
        LambdaQueryWrapper<ClinicalNote> q = new LambdaQueryWrapper<>();
        q.eq(ClinicalNote::getPatientId, patientId)
         .eq(ClinicalNote::getDoctorId, doctorId)
         .orderByDesc(ClinicalNote::getCreatedAt);
        return clinicalNoteMapper.selectPage(new Page<>(page, size), q);
    }

    // ============== 统计 ==============

    public Map<String, Object> getDashboardStats(Long doctorId) {
        Map<String, Object> stats = new LinkedHashMap<>();

        LambdaQueryWrapper<DoctorPatient> dpQ = new LambdaQueryWrapper<>();
        dpQ.eq(DoctorPatient::getDoctorId, doctorId).eq(DoctorPatient::getStatus, "active");
        List<DoctorPatient> bindings = doctorPatientMapper.selectList(dpQ);
        List<Long> patientIds = bindings.stream().map(DoctorPatient::getPatientId).toList();

        stats.put("totalPatients", patientIds.size());

        if (!patientIds.isEmpty()) {
            LambdaQueryWrapper<PatientProfile> riskQ = new LambdaQueryWrapper<>();
            riskQ.in(PatientProfile::getUserId, patientIds).ge(PatientProfile::getRiskLevel, 2);
            stats.put("highRiskCount", patientProfileMapper.selectCount(riskQ));

            Map<Integer, Long> riskDist = new HashMap<>();
            LambdaQueryWrapper<PatientProfile> allProfileQ = new LambdaQueryWrapper<>();
            allProfileQ.in(PatientProfile::getUserId, patientIds);
            patientProfileMapper.selectList(allProfileQ).forEach(p -> {
                int rl = p.getRiskLevel() != null ? p.getRiskLevel() : 0;
                riskDist.merge(rl, 1L, Long::sum);
            });
            stats.put("riskDistribution", riskDist);

            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            LambdaQueryWrapper<EmotionDiary> activeDiaryQ = new LambdaQueryWrapper<>();
            activeDiaryQ.in(EmotionDiary::getUserId, patientIds).ge(EmotionDiary::getCreatedAt, weekAgo);
            stats.put("activeDiariesThisWeek", emotionDiaryMapper.selectCount(activeDiaryQ));

            LambdaQueryWrapper<ConsultationSession> activeSessionQ = new LambdaQueryWrapper<>();
            activeSessionQ.in(ConsultationSession::getUserId, patientIds).ge(ConsultationSession::getStartedAt, weekAgo);
            stats.put("activeSessionsThisWeek", consultationSessionMapper.selectCount(activeSessionQ));
        } else {
            stats.put("highRiskCount", 0);
            stats.put("riskDistribution", Collections.emptyMap());
            stats.put("activeDiariesThisWeek", 0);
            stats.put("activeSessionsThisWeek", 0);
        }

        return stats;
    }

    // ============== 工具方法 ==============

    private void assertDoctorPatientBound(Long doctorId, Long patientId) {
        // 管理员拥有全量查看权限
        User operator = userMapper.selectById(doctorId);
        if (operator != null && UserType.ADMIN.getCode().equals(operator.getUserType())) {
            return;
        }

        LambdaQueryWrapper<DoctorPatient> q = new LambdaQueryWrapper<>();
        q.eq(DoctorPatient::getDoctorId, doctorId)
         .eq(DoctorPatient::getPatientId, patientId)
         .eq(DoctorPatient::getStatus, "active");
        if (doctorPatientMapper.selectCount(q) == 0) {
            throw new BusinessException("无权访问该患者数据");
        }
    }
}
