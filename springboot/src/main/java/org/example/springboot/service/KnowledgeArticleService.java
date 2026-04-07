package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.springboot.entity.KnowledgeArticle;
import org.example.springboot.entity.KnowledgeCategory;
import org.example.springboot.entity.User;
import org.example.springboot.entity.UserFavorite;
import org.example.springboot.mapper.KnowledgeArticleMapper;
import org.example.springboot.mapper.KnowledgeCategoryMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.UserFavoriteMapper;
import org.example.springboot.DTO.command.ArticleCreateDTO;
import org.example.springboot.DTO.command.KnowledgeImportTextDTO;
import org.example.springboot.DTO.command.ArticleUpdateDTO;
import org.example.springboot.DTO.query.ArticleListQueryDTO;
import org.example.springboot.DTO.response.ArticleResponseDTO;
import org.example.springboot.DTO.response.ArticleSimpleResponseDTO;
import org.example.springboot.DTO.response.ArticleStatisticsResponseDTO;
import org.example.springboot.enumClass.ArticleStatus;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.service.convert.ArticleConvert;

/**
 * 知识文章业务逻辑层
 * @author system
 */
@Slf4j
@Service
public class KnowledgeArticleService {

    @Resource
    private KnowledgeArticleMapper articleMapper;

    @Resource
    private KnowledgeCategoryMapper categoryMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserFavoriteMapper favoriteMapper;

    @Resource
    private UserFavoriteService userFavoriteService;

    /**
     * 创建文章
     * @param createDTO 创建命令
     * @param authorId 作者ID
     * @return 文章信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponseDTO createArticle(ArticleCreateDTO createDTO, Long authorId) {
        try {
            // 添加详细日志
            log.info("📝 创建文章请求: authorId={}, id={}, title={}, contentLength={}, summary={}", 
                authorId, createDTO.getId(), createDTO.getTitle(), 
                createDTO.getContent() != null ? createDTO.getContent().length() : 0,
                createDTO.getSummary());
            
            if (createDTO.getContent() == null || createDTO.getContent().trim().isEmpty()) {
                log.warn("⚠️ 文章内容为空: {}", createDTO);
                throw new BusinessException("文章内容不能为空");
            }
            
            // 验证分类是否存在且启用
            KnowledgeCategory category = categoryMapper.selectById(createDTO.getCategoryId());
            if (category == null) {
                throw new BusinessException("文章分类不存在");
            }
            if (!category.isEnabled()) {
                throw new BusinessException("文章分类已禁用");
            }

            // 验证作者是否存在
            User author = userMapper.selectById(authorId);
            if (author == null) {
                throw new BusinessException("作者用户不存在");
            }

            // 创建文章
            KnowledgeArticle article = ArticleConvert.createCommandToEntity(createDTO, authorId);
            if(createDTO.getStatus().equals(ArticleStatus.PUBLISHED.getCode())){
                article.setPublishedAt(LocalDateTime.now());
            }
            // 再次检查转换后的内容
            log.info("🔄 转换后的文章实体: title={}, contentLength={}", 
                article.getTitle(), 
                article.getContent() != null ? article.getContent().length() : 0);
            
            articleMapper.insert(article);
            
            // 插入后再次查询验证
            KnowledgeArticle savedArticle = articleMapper.selectById(article.getId());
            log.info("📋 数据库保存结果: id={}, title={}, contentLength={}",
                savedArticle.getId(), savedArticle.getTitle(),
                savedArticle.getContent() != null ? savedArticle.getContent().length() : 0);

            log.info("✅ 创建知识文章成功: {}", article.getTitle());
            return buildArticleResponse(article, category.getCategoryName(), author.getDisplayName(), false);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建知识文章失败", e);
            throw new ServiceException("创建文章失败，请稍后重试");
        }
    }

    /**
     * 导入纯文本到本地知识库（直接发布）
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponseDTO importPlainTextKnowledge(KnowledgeImportTextDTO dto, Long authorId) {
        if (!StringUtils.hasText(dto.getTitle()) || !StringUtils.hasText(dto.getContent())) {
            throw new BusinessException("标题和内容不能为空");
        }

        Long targetCategoryId = dto.getCategoryId();
        if (targetCategoryId == null) {
            LambdaQueryWrapper<KnowledgeCategory> categoryQuery = new LambdaQueryWrapper<>();
            categoryQuery.eq(KnowledgeCategory::getStatus, 1)
                    .orderByAsc(KnowledgeCategory::getSortOrder)
                    .orderByAsc(KnowledgeCategory::getId)
                    .last("LIMIT 1");
            KnowledgeCategory firstEnabledCategory = categoryMapper.selectOne(categoryQuery);
            if (firstEnabledCategory == null) {
                throw new BusinessException("请先创建并启用至少一个知识分类");
            }
            targetCategoryId = firstEnabledCategory.getId();
        }

        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setCategoryId(targetCategoryId);
        createDTO.setTitle(dto.getTitle().trim());
        createDTO.setSummary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : null);
        createDTO.setContent(dto.getContent().trim());
        createDTO.setTags(StringUtils.hasText(dto.getTags()) ? dto.getTags().trim() : "本地知识导入");
        createDTO.setStatus(ArticleStatus.PUBLISHED.getCode());

        return createArticle(createDTO, authorId);
    }

    /**
     * 基于关键词检索本地知识片段（优先用于AI回答）
     */
    public String buildKnowledgeContextForAi(String keyword, int maxCount) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        int limit = Math.max(1, Math.min(maxCount, 6));

        LambdaQueryWrapper<KnowledgeArticle> query = new LambdaQueryWrapper<>();
        query.eq(KnowledgeArticle::getStatus, ArticleStatus.PUBLISHED.getCode())
                .and(wrapper -> wrapper
                        .like(KnowledgeArticle::getTitle, keyword)
                        .or().like(KnowledgeArticle::getSummary, keyword)
                        .or().like(KnowledgeArticle::getTags, keyword)
                        .or().like(KnowledgeArticle::getContent, keyword)
                )
                .orderByDesc(KnowledgeArticle::getReadCount)
                .orderByDesc(KnowledgeArticle::getPublishedAt)
                .last("LIMIT " + limit);

        List<KnowledgeArticle> articles = articleMapper.selectList(query);
        if (articles == null || articles.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < articles.size(); i++) {
            KnowledgeArticle article = articles.get(i);
            String summary = StringUtils.hasText(article.getSummary()) ? article.getSummary() : article.getAutoSummary();
            String snippet = summary == null ? "" : summary.trim();
            if (snippet.length() > 220) {
                snippet = snippet.substring(0, 220) + "...";
            }
            context.append(i + 1)
                    .append(". 《")
                    .append(article.getTitle())
                    .append("》")
                    .append(StringUtils.hasText(snippet) ? " - " + snippet : "")
                    .append("\n");
        }
        return context.toString().trim();
    }

    /**
     * 更新文章
     * @param articleId 文章ID
     * @param updateDTO 更新命令
     * @param currentUserId 当前用户ID
     * @return 文章信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponseDTO updateArticle(String articleId, ArticleUpdateDTO updateDTO, Long currentUserId) {
        try {
            // 检查文章是否存在
            KnowledgeArticle existingArticle = articleMapper.selectById(articleId);
            if (existingArticle == null) {
                throw new BusinessException("文章不存在");
            }

            // 权限检查：只有作者或管理员可以编辑
            User currentUser = userMapper.selectById(currentUserId);
            if (!existingArticle.getAuthorId().equals(currentUserId) && !currentUser.isAdmin()) {
                throw new BusinessException("无权限编辑此文章");
            }

            // 验证分类
            if (updateDTO.getCategoryId() != null) {
                KnowledgeCategory category = categoryMapper.selectById(updateDTO.getCategoryId());
                if (category == null) {
                    throw new BusinessException("文章分类不存在");
                }
                if (!category.isEnabled()) {
                    throw new BusinessException("文章分类已禁用");
                }
            }

            // 更新文章
            KnowledgeArticle updateArticle = ArticleConvert.updateCommandToEntity(updateDTO);
            LambdaUpdateWrapper<KnowledgeArticle> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KnowledgeArticle::getId, articleId);

            if (updateArticle.getCategoryId() != null) {
                updateWrapper.set(KnowledgeArticle::getCategoryId, updateArticle.getCategoryId());
            }
            if (StringUtils.hasText(updateArticle.getTitle())) {
                updateWrapper.set(KnowledgeArticle::getTitle, updateArticle.getTitle());
            }
            if (StringUtils.hasText(updateArticle.getSummary())) {
                updateWrapper.set(KnowledgeArticle::getSummary, updateArticle.getSummary());
            }
            if (StringUtils.hasText(updateArticle.getContent())) {
                updateWrapper.set(KnowledgeArticle::getContent, updateArticle.getContent());
            }
            if (StringUtils.hasText(updateArticle.getCoverImage())) {
                updateWrapper.set(KnowledgeArticle::getCoverImage, updateArticle.getCoverImage());
            }
            if (StringUtils.hasText(updateArticle.getTags())) {
                updateWrapper.set(KnowledgeArticle::getTags, updateArticle.getTags());
            }
            if (updateArticle.getStatus() != null) {
                updateWrapper.set(KnowledgeArticle::getStatus, updateArticle.getStatus());
                // 如果状态改为已发布，设置发布时间
                if (updateArticle.getStatus().equals(ArticleStatus.PUBLISHED.getCode()) && 
                    existingArticle.getPublishedAt() == null) {
                    updateWrapper.set(KnowledgeArticle::getPublishedAt, LocalDateTime.now());
                }
            }
            updateWrapper.set(KnowledgeArticle::getUpdatedAt, LocalDateTime.now());

            articleMapper.update(null, updateWrapper);

            // 获取更新后的文章
            KnowledgeArticle updatedArticle = articleMapper.selectById(articleId);
            log.info("更新知识文章成功: {}", updatedArticle.getTitle());

            return getArticleById(articleId, currentUserId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新知识文章失败", e);
            throw new ServiceException("更新文章失败，请稍后重试");
        }
    }

    /**
     * 删除文章
     * @param articleId 文章ID
     * @param currentUserId 当前用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(String articleId, Long currentUserId) {
        try {
            // 检查文章是否存在
            KnowledgeArticle article = articleMapper.selectById(articleId);
            if (article == null) {
                throw new BusinessException("文章不存在");
            }

            // 权限检查：只有作者或管理员可以删除
            User currentUser = userMapper.selectById(currentUserId);
            if (!article.getAuthorId().equals(currentUserId) && !currentUser.isAdmin()) {
                throw new BusinessException("无权限删除此文章");
            }

            // 删除相关收藏记录
            deleteRelatedFavorites(articleId);

            // 删除文章
            articleMapper.deleteById(articleId);
            log.info("删除知识文章成功: {}", article.getTitle());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除知识文章失败", e);
            throw new ServiceException("删除文章失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取文章详情
     * @param articleId 文章ID
     * @param currentUserId 当前用户ID（可为null）
     * @return 文章详情
     */
    public ArticleResponseDTO getArticleById(String articleId, Long currentUserId) {
        KnowledgeArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 只有已发布的文章才能被普通用户查看
        if (currentUserId != null) {
            User currentUser = userMapper.selectById(currentUserId);
            if (!article.isPublished() && 
                !article.getAuthorId().equals(currentUserId) && 
                !currentUser.isAdmin()) {
                throw new BusinessException("文章不存在");
            }
        } else if (!article.isPublished()) {
            throw new BusinessException("文章不存在");
        }

        // 获取分类和作者信息
        KnowledgeCategory category = categoryMapper.selectById(article.getCategoryId());
        User author = userMapper.selectById(article.getAuthorId());

        // 检查是否收藏
        boolean isFavorited = false;
        if (currentUserId != null) {
            isFavorited = checkUserFavorite(currentUserId, articleId);
        }

        return buildArticleResponse(article, 
                                  category != null ? category.getCategoryName() : "未知分类",
                                  author != null ? author.getDisplayName() : "未知作者",
                                  isFavorited);
    }

    /**
     * 阅读文章（增加阅读次数）
     * @param articleId 文章ID
     * @param currentUserId 当前用户ID（可为null）
     * @return 文章详情
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponseDTO readArticle(String articleId, Long currentUserId) {
        try {
            // 获取文章详情
            ArticleResponseDTO articleResponse = getArticleById(articleId, currentUserId);

            // 增加阅读次数
            LambdaUpdateWrapper<KnowledgeArticle> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KnowledgeArticle::getId, articleId)
                        .setSql("read_count = read_count + 1");
            articleMapper.update(null, updateWrapper);

            // 更新响应中的阅读次数
            articleResponse.setReadCount(articleResponse.getReadCount() + 1);

            return articleResponse;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("阅读文章失败", e);
            throw new ServiceException("阅读文章失败，请稍后重试");
        }
    }

    /**
     * 分页查询文章列表
     * @param queryDTO 查询条件
     * @param currentUserId 当前用户ID（可为null）
     * @return 文章分页列表
     */
    public Page<ArticleSimpleResponseDTO> getArticlePage(ArticleListQueryDTO queryDTO, Long currentUserId) {
        try {
            Page<KnowledgeArticle> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());

            // 构建查询条件
            LambdaQueryWrapper<KnowledgeArticle> queryWrapper = new LambdaQueryWrapper<>();

            // 非管理员只能查看已发布的文章
            if (currentUserId == null) {
                queryWrapper.eq(KnowledgeArticle::getStatus, ArticleStatus.PUBLISHED.getCode());
            } else {
                User currentUser = userMapper.selectById(currentUserId);
                if (!currentUser.isAdmin()) {
                    queryWrapper.and(wrapper -> wrapper
                        .eq(KnowledgeArticle::getStatus, ArticleStatus.PUBLISHED.getCode())
                        .or()
                        .eq(KnowledgeArticle::getAuthorId, currentUserId)
                    );
                }
            }

            // 其他查询条件
            if (queryDTO.getCategoryId() != null) {
                queryWrapper.eq(KnowledgeArticle::getCategoryId, queryDTO.getCategoryId());
            }
            
            // 关键词搜索：同时搜索标题、内容和标签
            if (StringUtils.hasText(queryDTO.getKeyword())) {
                queryWrapper.and(wrapper -> wrapper
                    .like(KnowledgeArticle::getTitle, queryDTO.getKeyword())
                    .or().like(KnowledgeArticle::getContent, queryDTO.getKeyword())
                    .or().like(KnowledgeArticle::getTags, queryDTO.getKeyword())
                );
            }
            
            // 单独的标题和标签搜索（用于高级搜索）
            if (StringUtils.hasText(queryDTO.getTitle())) {
                queryWrapper.like(KnowledgeArticle::getTitle, queryDTO.getTitle());
            }
            if (StringUtils.hasText(queryDTO.getTags())) {
                queryWrapper.like(KnowledgeArticle::getTags, queryDTO.getTags());
            }
            if (queryDTO.getAuthorId() != null) {
                queryWrapper.eq(KnowledgeArticle::getAuthorId, queryDTO.getAuthorId());
            }
            if (queryDTO.getStatus() != null) {
                queryWrapper.eq(KnowledgeArticle::getStatus, queryDTO.getStatus());
            }

            // 日期范围查询
            if (StringUtils.hasText(queryDTO.getStartDate()) || StringUtils.hasText(queryDTO.getEndDate())) {
                if (StringUtils.hasText(queryDTO.getStartDate())) {
                    LocalDateTime startDateTime = LocalDateTime.parse(queryDTO.getStartDate() + " 00:00:00", 
                                                                     DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    queryWrapper.ge(KnowledgeArticle::getPublishedAt, startDateTime);
                }
                if (StringUtils.hasText(queryDTO.getEndDate())) {
                    LocalDateTime endDateTime = LocalDateTime.parse(queryDTO.getEndDate() + " 23:59:59", 
                                                                   DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    queryWrapper.le(KnowledgeArticle::getPublishedAt, endDateTime);
                }
            }

            // 排序
            String sortField = queryDTO.getSortField();
            String sortDirection = queryDTO.getSortDirection();
            if ("readCount".equals(sortField)) {
                if ("asc".equals(sortDirection)) {
                    queryWrapper.orderByAsc(KnowledgeArticle::getReadCount);
                } else {
                    queryWrapper.orderByDesc(KnowledgeArticle::getReadCount);
                }
            } else if ("createdAt".equals(sortField)) {
                if ("asc".equals(sortDirection)) {
                    queryWrapper.orderByAsc(KnowledgeArticle::getCreatedAt);
                } else {
                    queryWrapper.orderByDesc(KnowledgeArticle::getCreatedAt);
                }
            } else {
                // 默认按发布时间降序
                if ("asc".equals(sortDirection)) {
                    queryWrapper.orderByAsc(KnowledgeArticle::getPublishedAt);
                } else {
                    queryWrapper.orderByDesc(KnowledgeArticle::getPublishedAt);
                }
            }

            Page<KnowledgeArticle> articlePage = articleMapper.selectPage(page, queryWrapper);

            // 转换为响应DTO
            Page<ArticleSimpleResponseDTO> responsePage = new Page<>();
            responsePage.setCurrent(articlePage.getCurrent());
            responsePage.setSize(articlePage.getSize());
            responsePage.setTotal(articlePage.getTotal());
            responsePage.setPages(articlePage.getPages());

            List<ArticleSimpleResponseDTO> responseList = buildArticleSimpleResponseList(
                    articlePage.getRecords(), currentUserId);

            responsePage.setRecords(responseList);
            return responsePage;

        } catch (Exception e) {
            log.error("查询知识文章列表失败", e);
            throw new ServiceException("查询文章列表失败，请稍后重试");
        }
    }

    /**
     * 发布文章
     * @param articleId 文章ID
     * @param currentUserId 当前用户ID
     * @return 文章信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponseDTO publishArticle(String articleId, Long currentUserId) {
        try {
            KnowledgeArticle article = articleMapper.selectById(articleId);
            if (article == null) {
                throw new BusinessException("文章不存在");
            }

            // 权限检查
            User currentUser = userMapper.selectById(currentUserId);
            if (!article.getAuthorId().equals(currentUserId) && !currentUser.isAdmin()) {
                throw new BusinessException("无权限发布此文章");
            }

            // 状态检查
            if (!ArticleStatus.fromCode(article.getStatus()).canPublish()) {
                throw new BusinessException("当前状态不允许发布");
            }

            // 发布文章
            KnowledgeArticle publishedArticle = ArticleConvert.publishArticle(article);
            LambdaUpdateWrapper<KnowledgeArticle> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KnowledgeArticle::getId, articleId)
                        .set(KnowledgeArticle::getStatus, publishedArticle.getStatus())
                        .set(KnowledgeArticle::getPublishedAt, publishedArticle.getPublishedAt())
                        .set(KnowledgeArticle::getUpdatedAt, publishedArticle.getUpdatedAt());

            articleMapper.update(null, updateWrapper);
            log.info("发布知识文章成功: {}", article.getTitle());

            return getArticleById(articleId, currentUserId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发布知识文章失败", e);
            throw new ServiceException("发布文章失败，请稍后重试");
        }
    }

    /**
     * 下线文章
     * @param articleId 文章ID
     * @param currentUserId 当前用户ID
     * @return 文章信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponseDTO offlineArticle(String articleId, Long currentUserId) {
        try {
            KnowledgeArticle article = articleMapper.selectById(articleId);
            if (article == null) {
                throw new BusinessException("文章不存在");
            }

            // 权限检查
            User currentUser = userMapper.selectById(currentUserId);
            if (!article.getAuthorId().equals(currentUserId) && !currentUser.isAdmin()) {
                throw new BusinessException("无权限下线此文章");
            }

            // 状态检查
            if (!ArticleStatus.fromCode(article.getStatus()).canOffline()) {
                throw new BusinessException("当前状态不允许下线");
            }

            // 下线文章
            KnowledgeArticle offlineArticle = ArticleConvert.offlineArticle(article);
            LambdaUpdateWrapper<KnowledgeArticle> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KnowledgeArticle::getId, articleId)
                        .set(KnowledgeArticle::getStatus, offlineArticle.getStatus())
                        .set(KnowledgeArticle::getUpdatedAt, offlineArticle.getUpdatedAt());

            articleMapper.update(null, updateWrapper);
            log.info("下线知识文章成功: {}", article.getTitle());

            return getArticleById(articleId, currentUserId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("下线知识文章失败", e);
            throw new ServiceException("下线文章失败，请稍后重试");
        }
    }

    /**
     * 构建文章响应DTO
     */
    private ArticleResponseDTO buildArticleResponse(KnowledgeArticle article, 
                                                   String categoryName, 
                                                   String authorName, 
                                                   Boolean isFavorited) {
        return ArticleConvert.entityToResponse(article, categoryName, authorName, isFavorited);
    }

    /**
     * 构建文章简化响应DTO列表
     */
    private List<ArticleSimpleResponseDTO> buildArticleSimpleResponseList(List<KnowledgeArticle> articles, 
                                                                          Long currentUserId) {
        if (articles.isEmpty()) {
            return List.of();
        }

        // 获取分类信息
        List<Long> categoryIds = articles.stream()
                .map(KnowledgeArticle::getCategoryId)
                .distinct()
                .toList();
        
        Map<Long, String> categoryMap = categoryMapper.selectBatchIds(categoryIds)
                .stream()
                .collect(Collectors.toMap(KnowledgeCategory::getId, KnowledgeCategory::getCategoryName));

        // 获取作者信息
        List<Long> authorIds = articles.stream()
                .map(KnowledgeArticle::getAuthorId)
                .distinct()
                .toList();
        
        Map<Long, String> authorMap = userMapper.selectBatchIds(authorIds)
                .stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName));

        // 获取收藏信息
        Map<String, Boolean> favoriteMap = Map.of();
        if (currentUserId != null) {
            List<String> articleIds = articles.stream().map(KnowledgeArticle::getId).toList();
            favoriteMap = getUserFavoriteMap(currentUserId, articleIds);
        }

        // 获取收藏数量信息
        List<String> articleIds = articles.stream().map(KnowledgeArticle::getId).toList();
        Map<String, Integer> favoriteCountMap = userFavoriteService.getArticleFavoriteCountMap(articleIds);

        final Map<String, Boolean> finalFavoriteMap = favoriteMap;

        return articles.stream()
                .map(article -> ArticleConvert.entityToSimpleResponseWithFavoriteCount(
                        article,
                        categoryMap.getOrDefault(article.getCategoryId(), "未知分类"),
                        authorMap.getOrDefault(article.getAuthorId(), "未知作者"),
                        finalFavoriteMap.getOrDefault(article.getId(), false),
                        favoriteCountMap.getOrDefault(article.getId(), 0)
                ))
                .toList();
    }

    /**
     * 检查用户是否收藏了文章
     */
    private boolean checkUserFavorite(Long userId, String articleId) {
        LambdaQueryWrapper<UserFavorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(UserFavorite::getUserId, userId)
                    .eq(UserFavorite::getArticleId, articleId);
        return favoriteMapper.selectCount(favoriteQuery) > 0;
    }

    /**
     * 获取用户收藏映射
     */
    private Map<String, Boolean> getUserFavoriteMap(Long userId, List<String> articleIds) {
        LambdaQueryWrapper<UserFavorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(UserFavorite::getUserId, userId)
                    .in(UserFavorite::getArticleId, articleIds);
        
        List<UserFavorite> favorites = favoriteMapper.selectList(favoriteQuery);
        return favorites.stream()
                .collect(Collectors.toMap(UserFavorite::getArticleId, favorite -> true));
    }

    /**
     * 删除相关收藏记录
     */
    private void deleteRelatedFavorites(String articleId) {
        LambdaQueryWrapper<UserFavorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(UserFavorite::getArticleId, articleId);
        favoriteMapper.delete(favoriteQuery);
    }

    /**
     * 更新文章状态
     * @param articleId 文章ID
     * @param status 新状态
     * @param currentUserId 当前用户ID
     * @return 文章信息
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleResponseDTO updateArticleStatus(String articleId, Integer status, Long currentUserId) {
        try {
            // 检查文章是否存在
            KnowledgeArticle article = articleMapper.selectById(articleId);
            if (article == null) {
                throw new BusinessException("文章不存在");
            }

            // 权限检查：只有作者或管理员可以更新状态
            User currentUser = userMapper.selectById(currentUserId);
            if (!article.getAuthorId().equals(currentUserId) && !currentUser.isAdmin()) {
                throw new BusinessException("无权限更新此文章状态");
            }

            // 状态验证
            ArticleStatus targetStatus = ArticleStatus.fromCode(status);
            if (targetStatus == null) {
                throw new BusinessException("无效的文章状态");
            }

            // 更新文章状态
            LambdaUpdateWrapper<KnowledgeArticle> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(KnowledgeArticle::getId, articleId)
                        .set(KnowledgeArticle::getStatus, status)
                        .set(KnowledgeArticle::getUpdatedAt, LocalDateTime.now());

            // 如果状态改为已发布，设置发布时间
            if (status.equals(ArticleStatus.PUBLISHED.getCode()) && article.getPublishedAt() == null) {
                updateWrapper.set(KnowledgeArticle::getPublishedAt, LocalDateTime.now());
            }

            articleMapper.update(null, updateWrapper);
            log.info("更新文章状态成功: articleId={}, status={}", articleId, status);

            return getArticleById(articleId, currentUserId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新文章状态失败", e);
            throw new ServiceException("更新文章状态失败，请稍后重试");
        }
    }

    /**
     * 批量删除文章
     * @param articleIds 文章ID列表
     * @param currentUserId 当前用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteArticles(List<String> articleIds, Long currentUserId) {
        try {
            if (articleIds == null || articleIds.isEmpty()) {
                throw new BusinessException("删除的文章ID列表不能为空");
            }

            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser == null) {
                throw new BusinessException("用户不存在");
            }

            int deletedCount = 0;
            for (String articleId : articleIds) {
                try {
                    // 检查文章是否存在
                    KnowledgeArticle article = articleMapper.selectById(articleId);
                    if (article == null) {
                        log.warn("文章不存在，跳过删除: articleId={}", articleId);
                        continue;
                    }

                    // 权限检查：只有作者或管理员可以删除
                    if (!article.getAuthorId().equals(currentUserId) && !currentUser.isAdmin()) {
                        log.warn("无权限删除文章，跳过: articleId={}, userId={}", articleId, currentUserId);
                        continue;
                    }

                    // 删除相关收藏记录
                    deleteRelatedFavorites(articleId);

                    // 删除文章
                    articleMapper.deleteById(articleId);
                    deletedCount++;
                    log.info("删除文章成功: articleId={}", articleId);

                } catch (Exception e) {
                    log.error("删除文章失败: articleId={}", articleId, e);
                }
            }

            if (deletedCount == 0) {
                throw new BusinessException("没有成功删除任何文章");
            }

            log.info("批量删除文章完成: 总数={}, 成功删除={}", articleIds.size(), deletedCount);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除文章失败", e);
            throw new ServiceException("批量删除文章失败，请稍后重试");
        }
    }

    /**
     * 获取文章统计信息
     * @param currentUserId 当前用户ID
     * @return 统计信息
     */
    public ArticleStatisticsResponseDTO getArticleStatistics(Long currentUserId) {
        try {
            // 权限检查：需要管理员权限
            User currentUser = userMapper.selectById(currentUserId);
            if (currentUser == null || !currentUser.isAdmin()) {
                throw new BusinessException("无权限查看统计信息");
            }

            // 查询文章总数
            LambdaQueryWrapper<KnowledgeArticle> totalQuery = new LambdaQueryWrapper<>();
            Long totalArticles = articleMapper.selectCount(totalQuery);

            // 查询已发布文章数
            LambdaQueryWrapper<KnowledgeArticle> publishedQuery = new LambdaQueryWrapper<>();
            publishedQuery.eq(KnowledgeArticle::getStatus, ArticleStatus.PUBLISHED.getCode());
            Long publishedArticles = articleMapper.selectCount(publishedQuery);

            // 查询草稿文章数
            LambdaQueryWrapper<KnowledgeArticle> draftQuery = new LambdaQueryWrapper<>();
            draftQuery.eq(KnowledgeArticle::getStatus, ArticleStatus.DRAFT.getCode());
            Long draftArticles = articleMapper.selectCount(draftQuery);

            // 查询已下线文章数
            LambdaQueryWrapper<KnowledgeArticle> offlineQuery = new LambdaQueryWrapper<>();
            offlineQuery.eq(KnowledgeArticle::getStatus, ArticleStatus.OFFLINE.getCode());
            Long offlineArticles = articleMapper.selectCount(offlineQuery);

            // 查询总阅读量
            List<KnowledgeArticle> allArticles = articleMapper.selectList(null);
            Long totalViews = allArticles.stream()
                    .mapToLong(article -> article.getReadCount() != null ? article.getReadCount() : 0L)
                    .sum();

            // 查询总收藏数
            LambdaQueryWrapper<UserFavorite> favoriteQuery = new LambdaQueryWrapper<>();
            favoriteQuery.isNotNull(UserFavorite::getArticleId);
            Long totalFavorites = favoriteMapper.selectCount(favoriteQuery);

            return ArticleStatisticsResponseDTO.builder()
                    .totalArticles(totalArticles)
                    .publishedArticles(publishedArticles)
                    .draftArticles(draftArticles)
                    .offlineArticles(offlineArticles)
                    .totalViews(totalViews)
                    .totalFavorites(totalFavorites)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取文章统计信息失败", e);
            throw new ServiceException("获取统计信息失败，请稍后重试");
        }
    }
}