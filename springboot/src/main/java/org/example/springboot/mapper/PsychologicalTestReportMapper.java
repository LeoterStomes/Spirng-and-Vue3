package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.PsychologicalTestReport;

/**
 * 心理测试报告Mapper
 */
@Mapper
public interface PsychologicalTestReportMapper extends BaseMapper<PsychologicalTestReport> {
}

