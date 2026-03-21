/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80013 (8.0.13)
 Source Host           : localhost:3307
 Source Schema         : mental_health_assistant

 Target Server Type    : MySQL
 Target Server Version : 80013 (8.0.13)
 File Encoding         : 65001

 Date: 21/03/2026 17:33:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_analysis_task
-- ----------------------------
DROP TABLE IF EXISTS `ai_analysis_task`;
CREATE TABLE `ai_analysis_task`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `diary_id` bigint(20) NOT NULL COMMENT '日记ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，FAILED-失败',
  `task_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型：AUTO-自动触发，MANUAL-手动触发，ADMIN-管理员触发，BATCH-批量触发',
  `priority` int(11) NOT NULL DEFAULT 2 COMMENT '优先级：1-低，2-正常，3-高，4-紧急',
  `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `max_retry_count` int(11) NOT NULL DEFAULT 3 COMMENT '最大重试次数',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `started_at` datetime NULL DEFAULT NULL COMMENT '处理开始时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '处理完成时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_diary_id`(`diary_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_task_type`(`task_type` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_status_priority`(`status` ASC, `priority` ASC) USING BTREE,
  INDEX `idx_status_created_at`(`status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_task_type_created_at`(`task_type` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_retry_status`(`status` ASC, `retry_count` ASC, `max_retry_count` ASC) USING BTREE,
  CONSTRAINT `fk_ai_task_diary` FOREIGN KEY (`diary_id`) REFERENCES `emotion_diary` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_ai_task_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI分析任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ai_analysis_task
-- ----------------------------
INSERT INTO `ai_analysis_task` VALUES (32, 8, 2, 'COMPLETED', 'AUTO', 2, 0, 3, NULL, '2025-09-14 09:42:34', '2025-09-14 09:42:44', '2025-09-14 09:42:34', '2025-09-14 09:42:44');
INSERT INTO `ai_analysis_task` VALUES (33, 8, 2, 'COMPLETED', 'AUTO', 2, 0, 3, NULL, '2025-09-14 14:04:32', '2025-09-14 14:04:37', '2025-09-14 14:04:32', '2025-09-14 14:04:37');
INSERT INTO `ai_analysis_task` VALUES (34, 5, 2, 'COMPLETED', 'ADMIN', 3, 0, 3, NULL, '2025-09-14 14:05:48', '2025-09-14 14:05:52', '2025-09-14 14:05:48', '2025-09-14 14:05:52');
INSERT INTO `ai_analysis_task` VALUES (35, 8, 2, 'COMPLETED', 'AUTO', 2, 0, 3, NULL, '2025-09-14 14:13:21', '2025-09-14 14:13:26', '2025-09-14 14:13:21', '2025-09-14 14:13:26');
INSERT INTO `ai_analysis_task` VALUES (36, 8, 2, 'COMPLETED', 'ADMIN', 3, 0, 3, NULL, '2025-09-14 14:14:21', '2025-09-14 14:14:45', '2025-09-14 14:14:21', '2025-09-14 14:14:45');
INSERT INTO `ai_analysis_task` VALUES (37, 27, 2, 'COMPLETED', 'AUTO', 2, 0, 3, NULL, '2026-03-21 12:57:12', '2026-03-21 12:57:16', '2026-03-21 12:57:12', '2026-03-21 12:57:16');

-- ----------------------------
-- Table structure for consultation_message
-- ----------------------------
DROP TABLE IF EXISTS `consultation_message`;
CREATE TABLE `consultation_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint(20) NOT NULL COMMENT '会话ID',
  `sender_type` tinyint(4) NOT NULL COMMENT '发送者类型 1:用户 2:AI助手',
  `message_type` tinyint(4) NULL DEFAULT 1 COMMENT '消息类型 1:文本',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `emotion_tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '情绪标签',
  `ai_model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '使用的AI模型',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_message`(`session_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `consultation_message_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `consultation_session` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 118 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '咨询消息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of consultation_message
-- ----------------------------
INSERT INTO `consultation_message` VALUES (98, 17, 1, 1, '你好，我叫小明', NULL, NULL, '2025-09-14 14:11:53');
INSERT INTO `consultation_message` VALUES (99, 17, 2, 1, '你好呀小明！😊 很高兴认识你～我是你的心理健康助手，可以叫我\"暖心\"或者\"小暖\"都可以。最近过得怎么样呀？作为大学生，是不是有很多想分享的事情呢？💬\n\n有什么想聊的都可以跟我说说，无论是学习压力、人际关系，还是对未来的一些迷茫和困惑...我都在这里认真听着呢✨', NULL, 'openai', '2025-09-14 14:11:58');
INSERT INTO `consultation_message` VALUES (100, 17, 1, 1, '我考上研究生了，但是很迷茫', NULL, NULL, '2025-09-14 14:12:09');
INSERT INTO `consultation_message` VALUES (101, 17, 2, 1, '恭喜你考上研究生呀小明！🎉 这真是个值得骄傲的好消息呢～不过听到你说感到迷茫，这种心情我完全能理解😊\n\n读研确实和本科阶段很不一样呢。可以跟我多说说你的迷茫具体来自哪里吗？✨比如是：\n1️⃣ 担心学术压力太大？\n2️⃣ 对研究方向不确定？\n3️⃣ 还是对未来的职业规划有些困惑？\n\n你知道吗？这种\"上岸后的迷茫\"其实很常见哦～就像站在新的起跑线上，眼前有太多可能性时的正常反应🌟\n\n要不要我们一起来梳理看看？慢慢说，我都在听💛', NULL, 'openai', '2025-09-14 14:12:19');
INSERT INTO `consultation_message` VALUES (102, 17, 1, 1, '能给我一些读研建议吗', NULL, NULL, '2025-09-14 14:12:29');
INSERT INTO `consultation_message` VALUES (103, 17, 2, 1, '当然可以呀，小明！🎓 作为即将开启研究生生涯的你，这些建议或许能帮到你呢～✨\n\n🔍 **学术适应篇**：\n1️⃣ 主动联系导师：开学前可以给导师发封邮件，聊聊研究方向和学习计划哦\n2️⃣ 培养文献管理习惯：推荐试试Zotero/EndNote，从第一天就建立文献库\n3️⃣ 每周做学术笔记：记录灵感比事后回忆容易多啦\n\n💡 **心态调整贴士**：\n- 第一学期允许自己\"慢热\"⏳ 很多人需要3-6个月适应节奏\n- 把大目标拆解成季度/月度小目标，成就感UP↑\n- 实验室同门是最佳战友，多分享困惑和资源呀\n\n👩💻 **实用小工具**：\n✓ 番茄工作法APP（专注25分钟+5分钟休息）\n✓ 学术日历标记重要节点\n✓ 定期备份研究数据！（血泪教训预警）\n\n小明注意到你之前提到迷茫，特别想告诉你：研究生阶段迷茫真的太正常了~🤗 建议可以：\n- 9月开学后多参加实验室组会\n- 旁听1-2门相关领域的课\n- 和师兄师姐约咖啡聊聊经验\n\n记得哦，读研是场马拉松不是短跑🏃，找到自己的节奏最重要啦！你现在最想先从哪个方面开始准备呢？💭', NULL, 'openai', '2025-09-14 14:12:47');
INSERT INTO `consultation_message` VALUES (104, 18, 1, 1, 'a', NULL, NULL, '2026-03-20 13:31:22');
INSERT INTO `consultation_message` VALUES (105, 19, 1, 1, '你好', NULL, NULL, '2026-03-20 13:36:29');
INSERT INTO `consultation_message` VALUES (106, 19, 2, 1, '你好呀！很高兴见到你～ 😊\n\n作为大学生，你最近过得怎么样呢？无论是学习、生活还是人际关系，有任何想聊的话题都可以和我分享。我会在这里耐心倾听，陪你一起梳理心情。\n\n记得，无论遇到什么困扰，你都不是一个人在面对哦～', NULL, 'openai', '2026-03-20 13:36:32');
INSERT INTO `consultation_message` VALUES (107, 20, 1, 1, '我今天心情好', NULL, NULL, '2026-03-20 14:33:11');
INSERT INTO `consultation_message` VALUES (108, 20, 2, 1, '看到你连续说了三遍“心情好”，我也忍不住跟着开心起来了呢！😊 这种积极的状态真的很棒，像是阳光洒在心里的感觉～  \n\n是发生了什么特别的事情吗？还是就是突然觉得今天一切都特别美好？有时候好心情就像一份礼物，无论原因是什么，都值得好好珍惜和享受。  \n\n如果你愿意分享这份快乐的来源，我很乐意和你一起庆祝；如果只是想单纯地表达这份喜悦，那我也在这里为你开心！记得把这份好心情的能量储存起来，让它成为你内心的小太阳呀～ 🌞', NULL, 'openai', '2026-03-20 14:33:16');
INSERT INTO `consultation_message` VALUES (109, 21, 1, 1, '我今天心情不好', NULL, NULL, '2026-03-20 14:56:13');
INSERT INTO `consultation_message` VALUES (110, 21, 2, 1, '听到你这么说，我真的很关心你。心情不好是很正常的，每个人都会有这样的时刻。如果你愿意的话，可以和我聊聊发生了什么，或者只是单纯地倾诉一下。我在这里陪着你，不用着急。💛\n\n有时候，把情绪说出来会感觉轻松一些。你最近是不是遇到了什么压力，或者有什么事情让你感到困扰呢？', NULL, 'openai', '2026-03-20 14:56:17');
INSERT INTO `consultation_message` VALUES (111, 21, 1, 1, '我想听歌，你可以给点建议吗', NULL, NULL, '2026-03-20 14:56:42');
INSERT INTO `consultation_message` VALUES (112, 21, 2, 1, '当然可以！音乐确实是调节心情的好伙伴呢～ 🎵\n\n根据不同的情绪需求，我为你推荐几类歌曲，你可以根据自己的感觉来选择：\n\n**如果希望被温柔安抚：**\n- 《世间美好与你环环相扣》- 柏松\n- 《路过人间》- 郁可唯\n- 《慢慢喜欢你》- 莫文蔚\n- 《平凡的一天》- 毛不易\n\n**如果想释放情绪：**\n- 《山海》- 草东没有派对\n- 《我还年轻 我还年轻》- 老王乐队\n- 《没有理想的人不伤心》- 新裤子乐队\n\n**如果想获得力量感：**\n- 《追梦赤子心》- GALA\n- 《你的答案》- 阿冗\n- 《星辰大海》- 黄霄雲\n\n**纯音乐/轻音乐推荐：**\n- 《River Flows in You》- Yiruma\n- 《A Thousand Miles》钢琴版\n- 久石让的许多作品（比如《Summer》）\n\n你也可以试试：\n1. 创建专属的“心情歌单”，把不同情绪对应的歌分类存放\n2. 边听歌边写写简单的感受或随笔\n3. 戴上耳机去校园里散散步，让音乐和微风一起陪伴你\n\n你最近比较常听哪种类型的歌呢？或者有没有特别想调节的情绪方向？我可以再为你推荐更具体的歌曲～ 🌼\n\n当然可以！音乐确实是调节心情的好伙伴呢～ 🎵\n\n根据不同的情绪需求，我为你推荐几类歌曲，你可以根据自己的感觉来选择：\n\n**如果希望被温柔安抚：**\n- 《世间美好与你环环相扣》- 柏松\n- 《路过人间》- 郁可唯\n- 《慢慢喜欢你》- 莫文蔚\n- 《平凡的一天》- 毛不易\n\n**如果想释放情绪：**\n- 《山海》- 草东没有派对\n- 《我还年轻 我还年轻》- 老王乐队\n- 《没有理想的人不伤心》- 新裤子乐队\n\n**如果想获得力量感：**\n- 《追梦赤子心》- GALA\n- 《你的答案》- 阿冗\n- 《星辰大海》- 黄霄雲\n\n**纯音乐/轻音乐推荐：**\n- 《River Flows in You》- Yiruma\n- 《A Thousand Miles》钢琴版\n- 久石让的许多作品（比如《Summer》）\n\n你也可以试试：\n1. 创建专属的“心情歌单”，把不同情绪对应的歌分类存放\n2. 边听歌边写写简单的感受或随笔\n3. 戴上耳机去校园里散散步，让音乐和微风一起陪伴你\n\n你最近比较常听哪种类型的歌呢？或者有没有特别想调节的情绪方向？我可以再为你推荐更具体的歌曲～ 🌼', NULL, 'openai', '2026-03-20 14:56:56');
INSERT INTO `consultation_message` VALUES (113, 22, 1, 1, '小暖你能看到我刚刚做的心理测试吗', NULL, NULL, '2026-03-21 00:01:56');
INSERT INTO `consultation_message` VALUES (114, 22, 2, 1, '我理解你可能有些担心测试结果，或者希望有人能和你一起聊聊这些发现。不过作为AI助手，我无法直接查看你做的具体测试内容哦😊\n\n但如果你愿意的话，可以和我分享一下：\n✨ 你做了什么样的心理测试？\n✨ 测试结果让你有什么感受或想法？\n✨ 有没有什么特别想讨论的部分？\n\n我们可以一起聊聊测试带给你的启发，或者你对某些结果的困惑。记住，任何测试都只是参考工具，真正重要的是你对自己的感受和理解。\n\n你愿意和我多说一些吗？我在这里认真听着呢~', NULL, 'openai', '2026-03-21 00:02:01');
INSERT INTO `consultation_message` VALUES (115, 23, 1, 1, '我完成了一份心理测试，请你基于我的报告帮助我理解情况，并给出具体的心理疏导建议与可执行的下一步行动/可能的治疗或求助方向。\n\n（风险等级：1，总分：7）\n\n心理测试报告摘要：\n您好，感谢您完成了这份抑郁倾向测试。根据您的回答，您的总分为7分（满分18分）。这个分数表明您目前可能正经历一些情绪上的低落或困扰，但尚未达到需要紧急干预的程度。测试结果显示，您在情绪感受和日常兴趣方面可能受到了一些影响，比如感到情绪有些低落，或者对某些活动的兴趣有所减少。这些感受是真实且值得被关注的。请理解，这只是一份初步的自我评估，不能替代专业的临床诊断。重要的是，您已经迈出了关注自己心理健康的第一步。接下来的报告将为您提供更详细的分析和一些温和的建议，帮助您更好地理解当前的状态并采取积极的应对措施。\n\n心理测试报告正文（节选）：\n根据您提供的答案，我们进行了分析。本次测试共6个条目，每个条目得分范围为0-3分，总分范围为0-18分。您的具体得分情况如下：D1（情绪低落）0分，D2（兴趣减退）1分，D3（精力/疲劳）2分，D4（自我评价）2分，D5（注意力）1分，D6（悲观想法）1分，总分为7分。\n\n**得分解读**\n总分7分处于轻度关注区间。这表明您近期可能体验到了一些抑郁相关的情绪或思维模式，但整体严重程度较轻。具体来看，您在“精力/疲劳”和“自我评价”两个维度上得分相对较高（均为2分），这可能意味着您最近感到比平时更容易疲劳或精力不足，并且在看待自己时可能夹杂着一些消极或不满意的想法。在“兴趣减退”、“注意力”和“悲观想法”维度上得分均为1分，提示这些方面有轻微的影响，例如可能对某些事情的兴趣不如从前集中，偶尔会感到注意力难以集中，或者对未来产生过一丝担忧。值得欣慰的是，在核心的“情绪低落”项目上您得分为0分，这是一个积极的信号。\n\n**整体评估**\n综合来看，您目前的状态可能更接近于一种“亚临床”的情绪困扰，或者是对近期生活压力的一种反应。这种状态很常见，许多人都会在人生的某个阶段经历。它提醒我们需要给予自己的内心世界更多的关怀和照料。虽然当前没有显示需要立即危机干预的迹象，但持续的低落感、疲劳感和自我批评如果得不到缓解，可能会影响您的生活质量和心理韧性。\n\n**重要说明**\n这份报告基于您提供的信息生成，仅作为自我了解和心理健康的参考工具，绝不能用于替代精神科医生或心理咨询师的专业评估与诊断。每个人的情况都是独特的，测试分数只是反映某个时间点的状态。如果您对自己的感受感到持续困扰，寻求专业帮助始终是最佳选择。\n\n如果你判断存在较高心理风险，请优先给出紧急应对建议，并鼓励我尽快联系专业机构/学校心理中心。', NULL, NULL, '2026-03-21 13:26:26');
INSERT INTO `consultation_message` VALUES (116, 23, 1, 1, '我刚完成了心理测试，请你结合我的测试报告解释我的情况，并给出具体的心理疏导建议与可执行的下一步/可能的求助方向。', NULL, NULL, '2026-03-21 13:26:26');
INSERT INTO `consultation_message` VALUES (117, 23, 2, 1, '看到你主动完成测试并愿意深入了解自己的状态，这真的非常棒！这说明你对自己的心理健康有着很好的觉察力和关怀心。❤️\n\n首先，我想请你放心。根据你提供的报告，**风险等级为1，总分7分**，这确实表明你目前处于**轻度情绪困扰**的区间，但**没有紧急风险**。报告特别指出，你在“情绪低落”这一核心项目上得分为0分，这是一个非常积极的信号，说明你基本的情绪基调是稳定的。\n\n让我帮你梳理一下报告反映的情况：\n\n**你的状态可能更像是：**\n- **一种“心理疲劳”或“压力反应”**：你在“精力/疲劳”（2分）和“自我评价”（2分）上得分相对突出，这很可能意味着你最近可能比较累，身体和大脑在提醒你需要休息；同时，你对自己可能有些苛责或不够满意的想法。\n- **兴趣和注意力有轻微波动**：对事情的热忱偶尔下降，专注力不如平时，这常常和疲劳、压力相伴而生。\n- **对未来有隐隐的担忧**：偶尔有一些悲观的想法，这是人在压力下的常见反应。\n\n**总的来说**，这不像是一种疾病，而更像你的内心在对你发出一个温和的提醒：“嘿，我最近有些耗竭，需要被温柔地照顾一下了。” 很多人（尤其是处于学业、人生转折阶段的朋友）都会经历类似的状态。\n\n---\n\n### **具体的心理疏导建议与可执行步骤**\n\n你可以把这些看作一个“自我关怀工具包”，不必全部做完，选择其中1-2个你觉得最自然、最容易开始的就好。\n\n**1. 应对“精力疲劳”与提升能量：**\n- **微行动策略**：与其想着“我要锻炼一小时”，不如从“每天散步10分钟”或“跟着视频做5分钟拉伸”开始。微小、可持续的行动最能积累能量。\n- **睡眠卫生**：关注睡眠质量。尝试睡前半小时放下手机，听听舒缓的音乐或播客，用温水泡脚。小小的仪式感能大大改善休息。\n- **饮食关照**：留意一下是否因为忙碌而忽略了规律进食。尝试在三餐中加入一些你喜欢的、健康的食物，让吃饭变成一件愉悦的事。\n\n**2. 改善“自我评价”与对待自己：**\n- **书写自我关怀日记**：每天睡前，写下1-2件“今天我做得不错的小事”或“我感谢自己的一个瞬间”。例如：“今天我虽然很累，但还是完成了报告，谢谢自己的坚持。” 这能直接对抗消极的自我评价。\n- **练习“像对待朋友一样对待自己”**：当你又开始批评自己时，停下来问：“如果是我最好的朋友处于这种情况，我会对他说什么？” 然后把这句话说给自己听。\n- **肯定自己的努力**：你完成测试并寻求帮助，这本身就是非常了不起的、关爱自己的努力！请先肯定这一步。\n\n**3. 管理注意力与恢复兴趣：**\n- **“五分钟启动法”**：对于需要专注但提不起劲的事，告诉自己“我只做五分钟”。往往开始后，就能继续下去。\n- **重新连接让你愉悦的小事**：想想过去让你感到放松或快乐的一件小事（比如听某类音乐、拼图、整理房间、接触大自然），安排一个很短的时间（15分钟）去纯粹地体验它，不带任何目的。\n\n---\n\n### **可能的求助与前进方向**\n\n**现阶段，你可以：**\n- **将自我关怀作为首要任务**：优先执行上述建议，给自己2-4周的时间，像进行一个温柔的“自我实验”，观察感受的变化。\n- **利用身边的支持系统**：可以和一位你信任的、能给你带来积极能量的朋友或家人，聊聊你最近的感受。倾诉本身就有疗愈作用。\n- **了解学校的心理资源**：你可以登录学校官网，查一下心理咨询中心的介绍、预约方式和开放时间。**仅仅只是了解信息，并不意味着你必须立刻去**。这能让你在需要时，减少一步障碍。\n\n**何时考虑寻求专业帮助：**\n- 如果你尝试自我调节后，这种疲劳感、自我批评或兴趣减退的状态**持续超过两周，且明显影响了你的学业、社交或日常生活**。\n- 或者你只是单纯地觉得，和一个人聊聊这些想法，会让你更舒服。\n\n如果选择求助，学校心理咨询中心是一个**安全、保密且免费（通常）** 的起点。心理咨询师会和你一起梳理情况，提供专业的支持，他们的工作就是帮助你更好地应对这些挑战。\n\n**最后想对你说：**\n你目前的状态更像是一次心灵的“感冒”，很常见，也完全有办法恢复。你敏锐地察觉到了它的存在，这已经是康复路上最重要的一步。请相信，这种疲惫和困扰的感受是会变化的。你值得被自己温柔以待。🌱\n\n如果还有任何想聊的，或者实践中有新的感受，我都在这里。', NULL, 'openai', '2026-03-21 13:26:56');

-- ----------------------------
-- Table structure for consultation_session
-- ----------------------------
DROP TABLE IF EXISTS `consultation_session`;
CREATE TABLE `consultation_session`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `session_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话标题',
  `started_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `last_emotion_analysis` json NULL COMMENT '最后一次情绪分析结果(JSON格式)',
  `last_emotion_updated_at` datetime NULL DEFAULT NULL COMMENT '最后一次情绪分析更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_session`(`user_id` ASC, `started_at` ASC) USING BTREE,
  INDEX `idx_last_emotion_updated_at`(`last_emotion_updated_at` ASC) USING BTREE,
  CONSTRAINT `consultation_session_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '咨询会话表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of consultation_session
-- ----------------------------
INSERT INTO `consultation_session` VALUES (17, 2, '小暖助手 - 9/14/2025, 2:11:52 PM', '2025-09-14 14:11:53', '{\"icon\": \"🤔\", \"label\": \"求知\", \"keywords\": [\"建议\", \"求知\", \"准备\", \"规划\", \"咨询\"], \"riskLevel\": 0, \"timestamp\": 1719216000000, \"isNegative\": false, \"suggestion\": \"很高兴能为你提供读研建议\", \"emotionScore\": 45, \"primaryEmotion\": \"思考\", \"riskDescription\": \"情绪稳定\", \"improvementSuggestions\": [\"列出具体问题清单\", \"咨询学长学姐经验\", \"规划研究方向\", \"制定学习计划\"]}', '2025-09-14 14:12:35');
INSERT INTO `consultation_session` VALUES (18, 2, '小暖助手 - 2026/3/20 13:31:22', '2026-03-20 13:31:22', '{\"icon\": \"😐\", \"label\": \"平静\", \"keywords\": [], \"riskLevel\": 0, \"timestamp\": 1773984682628, \"isNegative\": false, \"suggestion\": \"情绪状态平稳，慢慢来就好\", \"emotionScore\": 50, \"primaryEmotion\": \"中性\", \"riskDescription\": \"当前情绪状态稳定，无需特别关注\", \"improvementSuggestions\": [\"保持规律作息\", \"适当运动\", \"与朋友交流\"]}', '2026-03-20 13:31:23');
INSERT INTO `consultation_session` VALUES (19, 2, '小暖助手 - 2026/3/20 13:36:28', '2026-03-20 13:36:29', '{\"icon\": \"🙂\", \"label\": \"平静\", \"keywords\": [\"问候\", \"中性\", \"平静\"], \"riskLevel\": 0, \"timestamp\": 1727683200000, \"isNegative\": false, \"suggestion\": \"保持这份平静，享受当下的安宁。\", \"emotionScore\": 50, \"primaryEmotion\": \"平静\", \"riskDescription\": \"情绪稳定\", \"improvementSuggestions\": [\"深呼吸放松一下\", \"听首喜欢的音乐\", \"规划一件小事\"]}', '2026-03-20 13:36:33');
INSERT INTO `consultation_session` VALUES (20, 2, '小暖助手 - 2026/3/20 14:33:10', '2026-03-20 14:33:11', '{\"icon\": \"😊\", \"label\": \"开心\", \"keywords\": [\"心情好\", \"开心\", \"愉悦\"], \"riskLevel\": 0, \"timestamp\": 1727683200000, \"isNegative\": false, \"suggestion\": \"保持这份好心情，享受当下的美好时光吧！\", \"emotionScore\": 85, \"primaryEmotion\": \"开心\", \"riskDescription\": \"情绪稳定积极\", \"improvementSuggestions\": [\"记录下这份快乐\", \"和朋友分享喜悦\", \"做些喜欢的事\"]}', '2026-03-20 14:33:15');
INSERT INTO `consultation_session` VALUES (21, 2, '小暖助手 - 2026/3/20 14:56:13', '2026-03-20 14:56:13', '{\"icon\": \"😐\", \"label\": \"平静\", \"keywords\": [], \"riskLevel\": 0, \"timestamp\": 1773989816968, \"isNegative\": false, \"suggestion\": \"情绪状态平稳，慢慢来就好\", \"emotionScore\": 50, \"primaryEmotion\": \"中性\", \"riskDescription\": \"当前情绪状态稳定，无需特别关注\", \"improvementSuggestions\": [\"保持规律作息\", \"适当运动\", \"与朋友交流\"]}', '2026-03-20 14:56:57');
INSERT INTO `consultation_session` VALUES (22, 2, '小暖助手 - 2026/3/21 00:01:55', '2026-03-21 00:01:56', '{\"icon\": \"🤔\", \"label\": \"困惑\", \"keywords\": [\"困惑\", \"询问\", \"分享\", \"测试\"], \"riskLevel\": 0, \"timestamp\": 1739792160000, \"isNegative\": false, \"suggestion\": \"慢慢来，我在这里倾听你的分享。\", \"emotionScore\": 45, \"primaryEmotion\": \"困惑\", \"riskDescription\": \"情绪稳定\", \"improvementSuggestions\": [\"和朋友聊聊你的测试结果\", \"写下此刻的心情\", \"深呼吸放松一下\", \"做些喜欢的事情\"]}', '2026-03-21 00:02:01');
INSERT INTO `consultation_session` VALUES (23, 2, '心理测试报告咨询 - 今天还好', '2026-03-21 13:26:25', '{\"icon\": \"🤔\", \"label\": \"困惑\", \"keywords\": [\"困惑\", \"寻求解释\", \"期待建议\", \"主动求助\"], \"riskLevel\": 0, \"timestamp\": 1739797357000, \"isNegative\": false, \"suggestion\": \"你主动寻求帮助，这本身就是很棒的第一步。\", \"emotionScore\": 45, \"primaryEmotion\": \"困惑\", \"riskDescription\": \"情绪稳定，主动寻求帮助\", \"improvementSuggestions\": [\"深呼吸，给自己一点时间\", \"写下此刻的感受和疑问\", \"预约一次专业的心理咨询\", \"和朋友或家人聊聊你的想法\"]}', '2026-03-21 13:26:31');

-- ----------------------------
-- Table structure for emotion_diary
-- ----------------------------
DROP TABLE IF EXISTS `emotion_diary`;
CREATE TABLE `emotion_diary`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日记ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `diary_date` date NOT NULL COMMENT '日记日期',
  `mood_score` tinyint(4) NOT NULL COMMENT '情绪评分(1-10)',
  `dominant_emotion` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主要情绪',
  `emotion_triggers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '情绪触发因素',
  `diary_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '日记内容',
  `sleep_quality` tinyint(4) NULL DEFAULT NULL COMMENT '睡眠质量(1-5)',
  `stress_level` tinyint(4) NULL DEFAULT NULL COMMENT '压力水平(1-5)',
  `ai_emotion_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT 'AI情绪分析结果(JSON格式)',
  `ai_analysis_updated_at` datetime NULL DEFAULT NULL COMMENT 'AI分析更新时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_date_unique`(`user_id` ASC, `diary_date` ASC) USING BTREE,
  INDEX `idx_user_diary`(`user_id` ASC, `diary_date` ASC) USING BTREE,
  INDEX `idx_ai_analysis_time`(`ai_analysis_updated_at` ASC) USING BTREE,
  CONSTRAINT `emotion_diary_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '情绪日记表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of emotion_diary
-- ----------------------------
INSERT INTO `emotion_diary` VALUES (2, 2, '2025-09-08', 7, '愉快', '完成了一个重要项目', '今天成功完成了一个重要的项目，感觉很有成就感。团队合作也很顺利，大家都很配合。', 4, 2, NULL, NULL, '2025-09-08 09:41:02', '2025-09-08 09:41:02');
INSERT INTO `emotion_diary` VALUES (3, 2, '2025-09-09', 6, '平静', '日常学习', '今天是比较平常的一天，按计划完成了学习任务。没有特别的事情发生，心情比较平静。', 4, 2, NULL, NULL, '2025-09-09 09:41:02', '2025-09-09 09:41:02');
INSERT INTO `emotion_diary` VALUES (4, 2, '2025-09-10', 8, '兴奋', '收到好消息', '收到了期待已久的好消息！心情特别好，忍不住想要分享给身边的朋友。', 5, 1, NULL, NULL, '2025-09-10 09:41:02', '2025-09-10 09:41:02');
INSERT INTO `emotion_diary` VALUES (5, 2, '2025-09-11', 4, '焦虑', '考试压力', '明天有一场重要的考试，复习得不够充分，感到很焦虑。担心会考不好。', 3, 4, '{\"primaryEmotion\":\"焦虑\",\"emotionScore\":40,\"isNegative\":true,\"riskLevel\":1,\"keywords\":[\"考试\",\"压力\",\"焦虑\",\"复习\"],\"suggestion\":\"适当的压力可以转化为动力\",\"icon\":\"😰\",\"label\":\"焦虑\",\"riskDescription\":\"需要关注\",\"improvementSuggestions\":[\"深呼吸放松\",\"做最后一次重点复习\",\"保证充足睡眠\"],\"timestamp\":1716192000000}', '2025-09-14 14:05:52', '2025-09-11 09:41:02', '2025-09-14 14:05:51');
INSERT INTO `emotion_diary` VALUES (6, 2, '2025-09-12', 6, '轻松', '考试结束', '考试终于结束了，虽然不知道结果如何，但至少压力释放了。可以好好休息一下。', 4, 2, NULL, NULL, '2025-09-12 09:41:02', '2025-09-12 09:41:02');
INSERT INTO `emotion_diary` VALUES (7, 2, '2025-09-13', 5, '疲惫', '工作繁忙', '今天工作特别忙，一直在处理各种事务。感觉身心都有些疲惫，需要好好休息。', 3, 3, NULL, NULL, '2025-09-13 09:41:02', '2025-09-13 09:41:02');
INSERT INTO `emotion_diary` VALUES (8, 2, '2025-09-14', 3, '焦虑', '和朋友闹矛盾', '和朋友闹矛盾，很难过', 2, 4, '{\"primaryEmotion\":\"焦虑\",\"emotionScore\":75,\"isNegative\":true,\"riskLevel\":2,\"keywords\":[\"朋友\",\"矛盾\",\"焦虑\",\"压力\",\"睡眠\"],\"suggestion\":\"和朋友之间的矛盾让你焦虑，试着冷静下来沟通\",\"icon\":\"😰\",\"label\":\"焦虑\",\"riskDescription\":\"需要心理疏导\",\"improvementSuggestions\":[\"深呼吸放松心情\",\"和朋友坦诚沟通\",\"改善睡眠环境\",\"适当运动缓解压力\"],\"timestamp\":1719216000000}', '2025-09-14 14:14:45', '2025-09-14 09:41:02', '2025-09-14 14:14:44');
INSERT INTO `emotion_diary` VALUES (14, 4, '2025-09-08', 5, '紧张', '新环境适应', '刚到新环境，对一切都感到陌生。需要时间去适应新的生活节奏。', 3, 3, NULL, NULL, '2025-09-08 09:41:02', '2025-09-08 09:41:02');
INSERT INTO `emotion_diary` VALUES (15, 4, '2025-09-09', 6, '好奇', '探索新事物', '开始探索周围的环境，发现了很多有趣的地方。对未来充满好奇。', 4, 2, NULL, NULL, '2025-09-09 09:41:02', '2025-09-09 09:41:02');
INSERT INTO `emotion_diary` VALUES (16, 4, '2025-09-10', 8, '兴奋', '结识新朋友', '今天认识了几个很有趣的新朋友，大家聊得很投机。感觉生活开始变得丰富多彩。', 5, 1, NULL, NULL, '2025-09-10 09:41:02', '2025-09-10 09:41:02');
INSERT INTO `emotion_diary` VALUES (17, 4, '2025-09-11', 4, '孤独', '思念家乡', '突然很想念家乡和家人，感到有些孤独。虽然新环境很好，但还是会想家。', 3, 3, NULL, NULL, '2025-09-11 09:41:02', '2025-09-11 09:41:02');
INSERT INTO `emotion_diary` VALUES (18, 4, '2025-09-12', 7, '充实', '忙碌的一天', '今天过得很充实，完成了很多事情。感觉自己在慢慢适应新的生活。', 4, 2, NULL, NULL, '2025-09-12 09:41:02', '2025-09-12 09:41:02');
INSERT INTO `emotion_diary` VALUES (19, 4, '2025-09-13', 3, '挫折', '遇到困难', '遇到了一些困难，感到有些挫败。但我相信通过努力一定能够克服。', 2, 4, NULL, NULL, '2025-09-13 09:41:02', '2025-09-13 09:41:02');
INSERT INTO `emotion_diary` VALUES (20, 4, '2025-09-14', 6, '坚定', '制定计划', '为自己制定了详细的计划，感觉目标更加清晰了。有了方向就有了动力。', 4, 2, NULL, NULL, '2025-09-14 09:41:02', '2025-09-14 09:41:02');
INSERT INTO `emotion_diary` VALUES (26, 2, '2026-03-20', 10, '焦虑', '今天起晚了', '', NULL, NULL, NULL, NULL, '2026-03-20 14:35:11', '2026-03-20 23:58:19');
INSERT INTO `emotion_diary` VALUES (27, 2, '2026-03-21', 6, '困惑', '起床有点晚', '今天还好', 1, 2, '{\"primaryEmotion\":\"困惑\",\"emotionScore\":60,\"isNegative\":false,\"riskLevel\":0,\"keywords\":[\"困惑\",\"睡眠不佳\",\"平静\",\"轻微压力\",\"起床晚\"],\"suggestion\":\"今天还好，先从调整作息开始吧。\",\"icon\":\"🤔\",\"label\":\"困惑\",\"riskDescription\":\"情绪基本稳定，无明显风险\",\"improvementSuggestions\":[\"今晚早点休息\",\"喝杯温水放松一下\",\"规划明天的起床时间\"],\"timestamp\":1739791792000}', '2026-03-21 12:57:16', '2026-03-21 12:57:08', '2026-03-21 12:57:16');

-- ----------------------------
-- Table structure for knowledge_article
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_article`;
CREATE TABLE `knowledge_article`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章ID(UUID)',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章标题',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '文章摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章内容',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签',
  `author_id` bigint(20) NULL DEFAULT NULL COMMENT '作者ID',
  `read_count` int(11) NULL DEFAULT 0 COMMENT '阅读次数',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态 1:已发布',
  `published_at` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_category_article`(`category_id` ASC, `published_at` ASC) USING BTREE,
  INDEX `idx_title`(`title` ASC) USING BTREE,
  CONSTRAINT `knowledge_article_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `knowledge_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `knowledge_article_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '知识文章表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_article
-- ----------------------------
INSERT INTO `knowledge_article` VALUES ('550e8400-e29b-41d4-a716-446655440001', 1, '如何识别和管理焦虑情绪', '焦虑是现代生活中常见的情绪反应，学会识别和管理焦虑对心理健康至关重要。', '<p>焦虑是一种正常的情绪反应，但过度的焦虑可能会影响我们的日常生活。本文将介绍如何识别焦虑的症状，以及一些有效的管理方法。</p><h3>识别焦虑的症状</h3><ul><li>心跳加速、出汗</li><li>呼吸急促</li><li>思维混乱</li><li>难以集中注意力</li></ul><h3>管理焦虑的方法</h3><ol><li>深呼吸练习</li><li>正念冥想</li><li>适度运动</li><li>寻求专业帮助</li></ol>', '', '焦虑,情绪管理,心理健康', 1, 15, 1, '2025-09-01 10:00:00', '2025-09-01 10:00:00', '2025-09-01 10:00:00');
INSERT INTO `knowledge_article` VALUES ('550e8400-e29b-41d4-a716-446655440002', 2, '情绪调节的五个有效策略', '掌握情绪调节技巧，让生活更加平衡和谐。', '<p>情绪调节是心理健康的重要组成部分。以下是五个经过科学验证的情绪调节策略：</p><h3>1. 认知重构</h3><p>学会从不同角度看待问题，挑战负面思维模式。</p><h3>2. 情绪标记</h3><p>准确识别和命名自己的情绪状态。</p><h3>3. 渐进式肌肉放松</h3><p>通过身体放松来缓解情绪紧张。</p><h3>4. 表达性写作</h3><p>通过写作来处理复杂的情绪体验。</p><h3>5. 社交支持</h3><p>寻求朋友和家人的理解与支持。</p>', '', '情绪调节,心理技巧,自我管理', 1, 23, 1, '2025-09-02 14:30:00', '2025-09-02 14:30:00', '2025-09-02 14:30:00');
INSERT INTO `knowledge_article` VALUES ('550e8400-e29b-41d4-a716-446655440003', 3, '职场压力管理指南', '在快节奏的工作环境中，学会有效管理压力是职业成功的关键。', '<p>职场压力是现代工作生活中不可避免的挑战。本指南将帮助您建立有效的压力管理体系。</p><h3>识别压力源</h3><ul><li>工作量过大</li><li>人际关系紧张</li><li>职业发展焦虑</li><li>工作与生活平衡困难</li></ul><h3>压力管理策略</h3><ol><li>时间管理技巧</li><li>设定合理目标</li><li>建立支持网络</li><li>培养兴趣爱好</li><li>定期休息和放松</li></ol><h3>长期压力预防</h3><p>建立健康的工作习惯和生活方式，预防压力累积。</p>', '', '职场压力,时间管理,工作生活平衡', 1, 31, 1, '2025-09-03 09:15:00', '2025-09-03 09:15:00', '2025-09-03 09:15:00');
INSERT INTO `knowledge_article` VALUES ('550e8400-e29b-41d4-a716-446655440004', 4, '建立健康的人际关系', '良好的人际关系是心理健康和生活幸福的基石。', '<p>人际关系质量直接影响我们的心理健康和生活满意度。本文将探讨如何建立和维护健康的人际关系。</p><h3>健康关系的特征</h3><ul><li>相互尊重和信任</li><li>有效沟通</li><li>情感支持</li><li>个人边界清晰</li></ul><h3>沟通技巧</h3><ol><li>积极倾听</li><li>表达真实感受</li><li>非暴力沟通</li><li>冲突解决</li></ol><h3>维护关系的方法</h3><p>定期联系、表达感激、共同成长、处理分歧。</p>', '', '人际关系,沟通技巧,社交能力', 1, 18, 1, '2025-09-04 16:20:00', '2025-09-04 16:20:00', '2025-09-04 16:20:00');
INSERT INTO `knowledge_article` VALUES ('550e8400-e29b-41d4-a716-446655440005', 1, '睡眠质量与心理健康', '优质睡眠是维护心理健康的重要基础。', '<p>睡眠质量与心理健康密切相关。充足的睡眠不仅能恢复身体机能，还对情绪调节和认知功能有重要影响。</p><h3>睡眠对心理健康的影响</h3><ul><li>情绪稳定性</li><li>压力应对能力</li><li>记忆和学习能力</li><li>注意力集中</li></ul><h3>改善睡眠质量的方法</h3><ol><li>建立规律作息</li><li>创造良好睡眠环境</li><li>睡前放松活动</li><li>避免刺激性物质</li><li>适度运动</li></ol>', '', '睡眠健康,心理健康,生活习惯', 1, 27, 1, '2025-09-05 11:45:00', '2025-09-05 11:45:00', '2025-09-05 11:45:00');
INSERT INTO `knowledge_article` VALUES ('550e8400-e29b-41d4-a716-446655440006', 2, '正念练习入门指南', '正念练习是现代心理学推荐的有效减压和情绪调节方法。', '<p>正念是一种专注于当下体验的练习方法，能够帮助我们更好地管理情绪和压力。</p><h3>什么是正念</h3><p>正念是有意识地、不带评判地关注当下时刻的能力。</p><h3>正念的益处</h3><ul><li>减少焦虑和抑郁</li><li>提高专注力</li><li>改善情绪调节</li><li>增强自我觉察</li></ul><h3>基础正念练习</h3><ol><li>呼吸观察</li><li>身体扫描</li><li>行走冥想</li><li>日常正念</li></ol><h3>练习建议</h3><p>从每天5-10分钟开始，逐渐增加练习时间。</p>', '', '正念,冥想,减压,专注力', 1, 42, 1, '2025-09-06 13:10:00', '2025-09-06 13:10:00', '2025-09-06 13:10:00');
INSERT INTO `knowledge_article` VALUES ('550e8400-e29b-41d4-a716-446655440007', 3, '学生心理压力应对策略', '帮助学生识别和应对学习生活中的各种心理压力。', '<p>学生时期面临着学业、人际、未来规划等多重压力。掌握有效的应对策略对学生心理健康至关重要。</p><h3>常见压力源</h3><ul><li>学业成绩压力</li><li>同伴关系</li><li>家庭期望</li><li>未来规划焦虑</li></ul><h3>应对策略</h3><ol><li>合理规划学习时间</li><li>培养兴趣爱好</li><li>建立支持网络</li><li>学会放松技巧</li><li>寻求专业帮助</li></ol><h3>预防措施</h3><p>建立健康的学习习惯和生活方式。</p>', '', '学生心理,学业压力,青少年心理', 1, 35, 1, '2025-09-07 08:30:00', '2025-09-07 08:30:00', '2025-09-07 08:30:00');
INSERT INTO `knowledge_article` VALUES ('550e8400-e29b-41d4-a716-446655440008', 1, '抑郁情绪的识别与应对', '了解抑郁情绪的特征，学会早期识别和有效应对。', '<p>抑郁情绪是常见的心理困扰，及早识别和干预对预防抑郁症发展具有重要意义。</p><h3>抑郁情绪的表现</h3><ul><li>持续的悲伤情绪</li><li>兴趣和愉悦感丧失</li><li>疲劳和精力不足</li><li>睡眠和食欲改变</li><li>注意力难以集中</li></ul><h3>应对方法</h3><ol><li>规律的作息时间</li><li>适度的体育锻炼</li><li>社交活动参与</li><li>专业心理咨询</li><li>药物治疗（如需要）</li></ol><h3>寻求帮助</h3><p>当症状持续超过两周时，应及时寻求专业帮助。</p>', '/files/bussiness/article/1757687894684.jpeg', '抑郁,情绪障碍,心理治疗', 1, 29, 1, '2025-09-08 15:00:00', '2025-09-08 15:00:00', '2025-09-12 22:38:16');

-- ----------------------------
-- Table structure for knowledge_category
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_category`;
CREATE TABLE `knowledge_category`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父分类ID',
  `category_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `category_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类代码',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '分类描述',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `category_code`(`category_code` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '知识文章分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_category
-- ----------------------------
INSERT INTO `knowledge_category` VALUES (1, 0, '心理健康基础', NULL, '心理健康基础知识和概念.', 30, 1, '2025-09-04 12:50:03', '2025-09-13 11:48:16');
INSERT INTO `knowledge_category` VALUES (2, 0, '情绪管理', NULL, '情绪识别、调节和管理技巧', 40, 1, '2025-09-04 12:50:03', '2025-09-13 11:48:16');
INSERT INTO `knowledge_category` VALUES (3, 0, '压力缓解', NULL, '压力来源分析和缓解方法', 20, 1, '2025-09-04 12:50:03', '2025-09-13 11:48:16');
INSERT INTO `knowledge_category` VALUES (4, 0, '人际关系', NULL, '人际交往和关系处理', 10, 1, '2025-09-04 12:50:03', '2025-09-13 11:48:16');

-- ----------------------------
-- Table structure for psychological_test_report
-- ----------------------------
DROP TABLE IF EXISTS `psychological_test_report`;
CREATE TABLE `psychological_test_report`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `test_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `answers_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `total_score` int(11) NULL DEFAULT NULL,
  `risk_level` int(11) NULL DEFAULT NULL,
  `report_summary` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `report_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `report_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_psych_test_report_user_created`(`user_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of psychological_test_report
-- ----------------------------
INSERT INTO `psychological_test_report` VALUES (1, 2, 'DEPRESSION', '心理测试报告 - 2026-03-20T23:57:38.124392300', '{\"scores\":{\"D1\":0,\"D2\":0,\"D3\":0,\"D4\":0,\"D5\":0,\"D6\":0},\"filledAt\":1774022258046,\"version\":1}', NULL, NULL, NULL, NULL, NULL, 'PROCESSING', NULL, '2026-03-20 23:57:38', '2026-03-20 23:57:38');
INSERT INTO `psychological_test_report` VALUES (2, 2, 'DEPRESSION', '心理测试报告 - 2026-03-20T23:58:56.475356', '{\"scores\":{\"D1\":0,\"D2\":0,\"D3\":0,\"D4\":1,\"D5\":2,\"D6\":2},\"filledAt\":1774022336462,\"version\":1}', NULL, NULL, NULL, NULL, NULL, 'PROCESSING', NULL, '2026-03-20 23:58:56', '2026-03-20 23:58:56');
INSERT INTO `psychological_test_report` VALUES (3, 2, 'PERSONALITY', '心理测试报告 - 2026-03-21T00:01:20.585094600', '{\"scores\":{\"P5\":1,\"P4\":1,\"P3\":1,\"P2\":1,\"P1\":1},\"filledAt\":1774022480572,\"version\":1}', NULL, NULL, NULL, NULL, NULL, 'PROCESSING', NULL, '2026-03-21 00:01:21', '2026-03-21 00:01:21');
INSERT INTO `psychological_test_report` VALUES (4, 2, 'DEPRESSION', '心理测试报告 - 2026-03-21T12:56:15.469307600', '{\"scores\":{\"D1\":0,\"D2\":0,\"D3\":1,\"D4\":1,\"D5\":1,\"D6\":1},\"filledAt\":1774068975388,\"version\":1}', NULL, NULL, NULL, NULL, NULL, 'PROCESSING', NULL, '2026-03-21 12:56:15', '2026-03-21 12:56:15');
INSERT INTO `psychological_test_report` VALUES (5, 2, 'DEPRESSION', '今天还好', '{\"scores\":{\"D1\":0,\"D2\":1,\"D3\":2,\"D4\":2,\"D5\":1,\"D6\":1},\"filledAt\":1774070544187,\"version\":1}', 7, 1, '根据您提供的抑郁倾向测试结果，您的总分为7分（满分18分）。这个分数表明您目前可能正经历一些情绪上的困扰，存在轻度的抑郁倾向。具体来看，您在“情绪低落”和“兴趣减退”两个核心维度上得分相对较高，反映出您可能感到持续的悲伤或对以往喜爱的活动失去了部分热情。同时，您在“精力下降”和“自我评价低”方面也表现出一些迹象，这可能会影响您的日常动力和自信心。总体而言，您的情况值得关注，但尚未达到需要紧急干预的程度。重要的是，您已经迈出了自我觉察的第一步，这是非常积极的行为。接下来的报告将为您提供更详细的分析和具体的、可操作的建议，帮助您更好地理解和应对当前的情绪状态。请记住，这份报告不能替代专业的医疗诊断，但它可以作为一个有用的参考起点。', '感谢您完成这份抑郁倾向测试。以下是根据您的答案生成的分析报告，旨在帮助您更好地理解当前的情绪状态，并提供一些支持性的建议。\n\n**一、 总体得分与风险水平**\n您的测试总分为**7分**（基于6个条目，每个条目0-3分，满分18分）。根据通用的评估标准，这个分数落在**轻度关注**区间。我们设定的风险等级为**1级**。这意味着您可能正在经历一些与抑郁情绪相关的困扰，这些困扰对您的日常生活产生了一定影响，但尚未构成严重的功能障碍或危机。识别这些早期信号是进行自我关怀和寻求适当支持的重要一步。\n\n**二、 各维度详细分析**\n您的答案分布如下：\n- **D1（情绪低落）**：得分为0分，表明在过去一段时间里，您可能没有感到特别显著或持续的情绪低落。这是一个积极的信号。\n- **D2（兴趣减退）**：得分为1分，提示您对某些事情或活动的兴趣可能比平时略有减少，但并未完全丧失。您可能发现一些曾经喜欢做的事情，现在做起来感觉没那么有乐趣了。\n- **D3（精力下降/疲劳感）**：得分为2分，这是您得分较高的项目之一。这表明您可能经常感到比平时更容易疲劳，精力不足，即使休息后也难以完全恢复。这种持续的疲惫感是抑郁常见的躯体症状之一。\n- **D4（自我评价低/无价值感）**：得分为2分，同样是得分较高的项目。您可能对自己有较多的负面看法，容易感到自责，或觉得自己不如他人。这种低自我价值感会显著影响情绪和动力。\n- **D5（注意力集中困难）**：得分为1分，提示您在集中注意力、做决定方面可能偶尔会遇到一些困难，比如容易走神或觉得思考变慢。\n- **D6（睡眠问题）**：得分为1分，表明您的睡眠可能受到了一些轻微干扰，例如入睡稍困难、睡眠较浅或早醒，但问题尚不严重。\n\n综合来看，您的困扰主要集中在**精力疲乏**和**自我价值感受挫**两个方面，同时伴随着轻度的兴趣减退和注意力问题。这种模式在压力期或情绪低谷期是比较常见的。\n\n**三、 理解您当前的状态**\n感到精力不济和自我怀疑，确实会让人感到挣扎和无力。这些感受是真实的，也值得被认真对待。它们可能是身体和心理在提醒您，当前的生活节奏、压力水平或某些未处理的情感需要得到更多的关注。请理解，出现这些感受并不意味着您“不够好”或“脆弱”，而是人类情绪光谱的一部分。许多人在人生的某个阶段都会经历类似的挑战。\n\n**四、 重要提醒**\n本报告是基于有限条目的自评工具生成，其结果**不能作为临床诊断依据**。抑郁情绪与抑郁症之间存在区别，后者需要由精神科医生或临床心理学家通过面谈和更全面的评估来确诊。如果您发现这些症状持续超过两周，且严重影响到您的工作、学习、社交或日常生活，强烈建议您寻求专业的评估。\n\n**五、 支持与展望**\n尽管面临挑战，但您主动进行测试的行为本身就展现了积极的自我关怀意识和解决问题的意愿，这是非常宝贵的。针对轻度关注的状态，通过一些积极的自我调节和生活方式调整，通常可以有效地改善情绪和精力水平。在接下来的部分，我们将提供一些具体的、可操作的步骤供您参考。请以温和、耐心的态度对待自己，改变需要时间。', '{}', 'COMPLETED', NULL, '2026-03-21 13:22:24', '2026-03-21 13:23:00');
INSERT INTO `psychological_test_report` VALUES (6, 2, 'DEPRESSION', '今天还好', '{\"scores\":{\"D1\":0,\"D2\":1,\"D3\":2,\"D4\":2,\"D5\":1,\"D6\":1},\"filledAt\":1774070565588,\"version\":1}', 7, 1, '您好，感谢您完成了这份抑郁倾向测试。根据您的回答，您的总分为7分（满分18分）。这个分数表明您目前可能正经历一些情绪上的低落或困扰，但尚未达到需要紧急干预的程度。测试结果显示，您在情绪感受和日常兴趣方面可能受到了一些影响，比如感到情绪有些低落，或者对某些活动的兴趣有所减少。这些感受是真实且值得被关注的。请理解，这只是一份初步的自我评估，不能替代专业的临床诊断。重要的是，您已经迈出了关注自己心理健康的第一步。接下来的报告将为您提供更详细的分析和一些温和的建议，帮助您更好地理解当前的状态并采取积极的应对措施。', '根据您提供的答案，我们进行了分析。本次测试共6个条目，每个条目得分范围为0-3分，总分范围为0-18分。您的具体得分情况如下：D1（情绪低落）0分，D2（兴趣减退）1分，D3（精力/疲劳）2分，D4（自我评价）2分，D5（注意力）1分，D6（悲观想法）1分，总分为7分。\n\n**得分解读**\n总分7分处于轻度关注区间。这表明您近期可能体验到了一些抑郁相关的情绪或思维模式，但整体严重程度较轻。具体来看，您在“精力/疲劳”和“自我评价”两个维度上得分相对较高（均为2分），这可能意味着您最近感到比平时更容易疲劳或精力不足，并且在看待自己时可能夹杂着一些消极或不满意的想法。在“兴趣减退”、“注意力”和“悲观想法”维度上得分均为1分，提示这些方面有轻微的影响，例如可能对某些事情的兴趣不如从前集中，偶尔会感到注意力难以集中，或者对未来产生过一丝担忧。值得欣慰的是，在核心的“情绪低落”项目上您得分为0分，这是一个积极的信号。\n\n**整体评估**\n综合来看，您目前的状态可能更接近于一种“亚临床”的情绪困扰，或者是对近期生活压力的一种反应。这种状态很常见，许多人都会在人生的某个阶段经历。它提醒我们需要给予自己的内心世界更多的关怀和照料。虽然当前没有显示需要立即危机干预的迹象，但持续的低落感、疲劳感和自我批评如果得不到缓解，可能会影响您的生活质量和心理韧性。\n\n**重要说明**\n这份报告基于您提供的信息生成，仅作为自我了解和心理健康的参考工具，绝不能用于替代精神科医生或心理咨询师的专业评估与诊断。每个人的情况都是独特的，测试分数只是反映某个时间点的状态。如果您对自己的感受感到持续困扰，寻求专业帮助始终是最佳选择。', '{}', 'COMPLETED', NULL, '2026-03-21 13:22:46', '2026-03-21 13:23:08');
INSERT INTO `psychological_test_report` VALUES (7, 2, 'DEPRESSION', '抑郁倾向测试 - 2026-03-21T13:57:18.038884', '{\"scores\":{\"E31\":1,\"D1\":1,\"D2\":1,\"D3\":2,\"D4\":2,\"D5\":1,\"D6\":1},\"filledAt\":1774072638025,\"version\":1}', NULL, NULL, NULL, NULL, NULL, 'FAILED', 'I/O error on POST request for \"https://api.deepseek.com/v1/chat/completions\": Connection reset', '2026-03-21 13:57:18', '2026-03-21 13:57:37');
INSERT INTO `psychological_test_report` VALUES (8, 2, 'DEPRESSION', '抑郁倾向测试 - 2026-03-21T13:58:13.061760400', '{\"scores\":{\"E31\":1,\"D1\":1,\"D2\":1,\"D3\":2,\"D4\":2,\"D5\":1,\"D6\":1},\"filledAt\":1774072693051,\"version\":1}', 9, 1, '根据您提供的抑郁倾向测试结果，您的总分为9分。这个分数表明您目前可能正经历一些情绪上的低落或困扰，处于轻度关注的范围内。测试结果显示您在情绪、兴趣和精力方面可能有一些轻微的变化，例如感到有些沮丧、对事物的兴趣略有减退，或者精力不如从前。这些感受在压力时期或生活变化中是常见的反应。重要的是，您已经迈出了关注自身心理健康的第一步，这非常值得肯定。接下来的报告将详细解释您的得分情况，并提供一些初步的应对思路。请记住，这份报告是基于有限信息的初步评估，不能替代专业的临床诊断。', '您好，感谢您完成这份抑郁倾向测试并信任我进行分析。我将根据您提供的答案，为您生成一份详细的报告。\n\n**得分分析**\n您的测试总分为9分（满分范围取决于具体量表，通常在此类筛查中，分数越高表示抑郁症状的频率或程度可能越高）。根据常见的解读框架，这个分数落在“轻度”或“需要关注”的区间。这意味着您可能在过去一段时间里，体验到了一些与抑郁情绪相关的症状，但这些症状的严重程度目前看来是相对较轻的。\n\n具体来看您的答题情况：您在“感到情绪低落、沮丧或绝望”（对应D3、D4项得分稍高）以及“做事时提不起劲或没有兴趣”（对应D1、D2项）等维度上反映出了些许困扰。这些感受可能表现为偶尔的心情不佳、对以往喜欢的事情兴趣下降，或者感觉完成日常任务需要比平时更多的努力。同时，您在关于睡眠、食欲等生理维度（如E31）以及关于自我价值感（D5、D6）的题目上得分较低，这是一个积极的信号，说明这些方面目前受的影响可能较小。\n\n**综合解读与关怀**\n得分反映的是一种状态，而非对您个人的定义。生活中许多因素，如工作学习压力、人际关系变化、季节更替或暂时的挫折，都可能引发类似的情绪波动。您能主动进行测试，说明您对自己的内心世界保持着觉察，这是自我关怀和迈向改善非常重要的一步。\n\n目前没有从您的答案中检测到立即性的危机信号（如强烈的自杀或自伤想法）。然而，即使是轻度的情绪困扰，如果持续存在，也会影响我们的生活质量和幸福感。因此，给予适当的关注和采取一些积极的应对措施是非常有益的。\n\n**重要提醒**\n本报告仅为基于您所提供答案的自动化分析和心理教育支持，绝不能作为正式的医学或心理学诊断。如果您感到这些困扰持续存在、加重，或已经明显干扰到您的日常工作、学习和社交，强烈建议您寻求合格的心理健康专业人士（如心理咨询师、心理治疗师或精神科医生）进行面对面的评估和咨询。', '{}', 'COMPLETED', NULL, '2026-03-21 13:58:13', '2026-03-21 13:58:37');

-- ----------------------------
-- Table structure for sys_file_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_info`;
CREATE TABLE `sys_file_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID，主键自增',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名（用户上传时的文件名）',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件访问路径（服务器存储路径）',
  `file_size` bigint(20) NOT NULL DEFAULT 0 COMMENT '文件大小，单位：字节',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型（IMG/PDF/TXT/DOC/XLS等）',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务类型（用于区分文件用途，如：avatar/document/attachment）',
  `business_id` char(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务对象ID（关联的业务数据主键）',
  `business_field` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务字段名（对应业务表中的字段名）',
  `upload_user_id` bigint(20) NULL DEFAULT NULL COMMENT '上传用户ID（记录谁上传的文件）',
  `is_temp` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否临时文件（0:否 1:是）',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态（0:删除 1:正常）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间（仅对临时文件有效）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE,
  INDEX `idx_upload_user`(`upload_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_temp_expire`(`is_temp` ASC, `expire_time` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统文件信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_info
-- ----------------------------
INSERT INTO `sys_file_info` VALUES (46, '7487e74da9ec1ee07b5064348942b2ca.png', '/files/bussiness/article/1756963989972.png', 178205, 'IMG', 'ARTICLE', 'b51ecba7-db3c-4a30-84ab-3a19e1e8264b', 'cover', 1, 0, 1, '2025-09-04 13:33:10', NULL);
INSERT INTO `sys_file_info` VALUES (47, '7487e74da9ec1ee07b5064348942b2ca.png', '/files/bussiness/article/1756964062385.png', 178205, 'IMG', 'ARTICLE', 'null', 'cover', 1, 0, 1, '2025-09-04 13:34:22', NULL);
INSERT INTO `sys_file_info` VALUES (48, '7487e74da9ec1ee07b5064348942b2ca.png', '/files/bussiness/article/1756964101109.png', 178205, 'IMG', 'ARTICLE', 'e26ad80e-87b2-490f-87c3-2d5f981ba7bf', 'cover', 1, 0, 1, '2025-09-04 13:35:01', NULL);
INSERT INTO `sys_file_info` VALUES (49, '7487e74da9ec1ee07b5064348942b2ca.png', '/files/bussiness/article/1756964116665.png', 178205, 'IMG', 'ARTICLE', '8d79085d-4686-4af7-813d-4f97c936ff3e', 'cover', 1, 0, 1, '2025-09-04 13:35:17', NULL);
INSERT INTO `sys_file_info` VALUES (50, 'image323232s.jpg', '/files/temp/1757670438125.jpg', 7727, 'IMG', 'USER_AVATAR', '1', 'avatar', 1, 0, 0, '2025-09-12 17:47:18', '2025-09-13 17:47:18');
INSERT INTO `sys_file_info` VALUES (51, 'Untitled.jpg', '/files/temp/1757670832161.jpg', 7393, 'IMG', 'USER_AVATAR', '1', 'avatar', 1, 0, 0, '2025-09-12 17:53:52', '2025-09-13 17:53:52');
INSERT INTO `sys_file_info` VALUES (52, 'ima9897ges.jpg', '/files/temp/1757670856780.jpg', 4977, 'IMG', 'USER_AVATAR', '1', 'avatar', 1, 0, 0, '2025-09-12 17:54:17', '2025-09-13 17:54:17');
INSERT INTO `sys_file_info` VALUES (53, 'image323232s.jpg', '/files/bussiness/user_avatar/1757687320361.jpg', 7727, 'IMG', 'USER_AVATAR', '1', 'avatar', 1, 0, 1, '2025-09-12 22:28:40', NULL);
INSERT INTO `sys_file_info` VALUES (54, '0a157ddd8a7070c5240fb3ad23a5fc38.jpeg', '/files/bussiness/article/1757687894684.jpeg', 135321, 'IMG', 'ARTICLE', '550e8400-e29b-41d4-a716-446655440008', 'cover', 1, 0, 1, '2025-09-12 22:38:15', NULL);
INSERT INTO `sys_file_info` VALUES (55, 'Unti555tled.jpg', '/files/bussiness/user_avatar/1757816752928.jpg', 6389, 'IMG', 'USER_AVATAR', '2', 'avatar', 2, 0, 1, '2025-09-14 10:25:53', NULL);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(4) NULL DEFAULT NULL COMMENT '性别 0:未知 1:男 2:女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `user_type` tinyint(4) NULL DEFAULT 1 COMMENT '用户类型 1:普通用户 2:管理员',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_type`(`user_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', 'admin@example.com', '13123456789', '$2a$10$qUjCfQLqf9qVt1w1LDoaj.f5TUzBYcXh3FNNA0BhoQ54Vv2cSUt7K', '系统管理员', '/files/bussiness/user_avatar/1757687320361.jpg', 1, '2019-09-02', 2, 1, '2025-08-30 12:00:01', '2025-09-12 22:28:42');
INSERT INTO `user` VALUES (2, 'test', '15165@qq.com', '13132584165', '$2a$10$qUjCfQLqf9qVt1w1LDoaj.f5TUzBYcXh3FNNA0BhoQ54Vv2cSUt7K', 'tyest', '/files/bussiness/user_avatar/1757816752928.jpg', 1, '2002-08-01', 1, 1, '2025-08-30 17:22:24', '2026-03-20 14:34:05');
INSERT INTO `user` VALUES (4, 'ces', '111111111111@qq.com', '13123456654', '$2a$10$Sy22CXQJe5OAS.Xvmbb60ul9O4a5qi5FtnOjFC2v/Ruk2binJ7vEm', '1561', NULL, 1, '2025-09-10', 1, 0, '2025-08-30 18:53:07', '2025-09-12 14:12:35');

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `article_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_article_unique`(`user_id` ASC, `article_id` ASC) USING BTREE,
  INDEX `article_id`(`article_id` ASC) USING BTREE,
  CONSTRAINT `user_favorite_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `user_favorite_ibfk_2` FOREIGN KEY (`article_id`) REFERENCES `knowledge_article` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_favorite
-- ----------------------------
INSERT INTO `user_favorite` VALUES (7, 2, '550e8400-e29b-41d4-a716-446655440008', '2025-09-14 14:13:37');
INSERT INTO `user_favorite` VALUES (8, 2, '550e8400-e29b-41d4-a716-446655440007', '2026-03-21 13:03:42');

SET FOREIGN_KEY_CHECKS = 1;
