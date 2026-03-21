<template>
  <div class="auth-layout">
    <!-- 主要内容区域 -->
    <div class="auth-content">
      <div class="auth-container">
        <!-- 左侧背景区域 -->
        <div class="left-section">
          <div class="left-overlay"></div>
          <div class="left-content">
            <!-- 文字内容区域 -->
            <div class="welcome-content">
              <div class="breathing-animation">
              
                <h2 class="welcome-title">{{ welcomeText.title }}</h2>
                <p class="welcome-text">
                  {{ welcomeText.description }}
                </p>
              </div>
            </div>
            <!-- 机器人图标区域 -->
            <div class="robot-section">
              <div class="robot-wrapper">
                <div class="robot-icon">
                  <i class="fas fa-robot"></i>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧表单区域 -->
        <div class="right-section">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// 根据路由动态设置欢迎文本
const welcomeText = computed(() => {
  switch (route.name) {
    case 'Login':
      return {
        title: '心理AI助手',
        description: '每个深夜，每个焦虑的时刻，我们都在这里。不必独自承受，让心与心的连接温暖您的每一天'
      }
    case 'Register':
      return {
        title: '加入我们',
        description: '一次温暖的对话，化孤独为慰藉。让我们陪伴您每一步成长，用爱温暖每一颗心灵'
      }
    default:
      return {
        title: '心理健康助手',
        description: '用心倾听每一个故事，用爱温暖每一颗心灵。让心与心的连接温暖您的每一天'
      }
  }
})
</script>

<style scoped>
/* 基础样式 */
.auth-layout {
  background-color: #f9fafb;
  min-height: 100vh;
}


/* 主内容区域 */
.auth-content {
  height: 100vh;
}

.auth-container {
  height: 100%;
  display: flex;
}

/* 左侧背景区域 */
.left-section {
  display: none;
  width: 50%;

  background: linear-gradient(135deg, #4A90E2 0%, #7ED321 100%);
  position: relative;
  overflow: hidden;
}

@media (min-width: 1024px) {
  .left-section {
    display: flex;
  }
}

.left-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.2);
}

.left-content {
  position: relative;
  z-index: 10;
  display: flex;
  width: 100%;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: white;

  gap: 2rem;
}

/* 文字内容区域 */
.welcome-content {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.breathing-animation {
  max-width: 28rem;
}



.welcome-title {
  font-size: 2.25rem;
  font-weight: bold;
  margin-bottom: 1rem;
  margin-top: 0;
  line-height: 1.2;
}

.welcome-text {
  font-size: 1.125rem;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
  margin: 0;
}

/* 机器人图标区域 */
.robot-section {
  display: flex;
  justify-content: center;
  align-items: center;
}

.robot-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}

.robot-icon {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.2);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 
    0 15px 35px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease;
}

.robot-icon:hover {
  transform: translateY(-5px);
}

.robot-icon::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.08), transparent);
  transform: rotate(45deg);
  animation: shine 4s ease-in-out infinite;
}

.robot-icon i {
  font-size: 4rem;
  color: rgba(255, 255, 255, 0.95);
  position: relative;
  z-index: 1;
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
}

@keyframes shine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  50% { transform: translateX(100%) translateY(100%) rotate(45deg); }
  100% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
}

/* 呼吸动画 */
.breathing-animation {
  animation: breathing 4s ease-in-out infinite;
}

@keyframes breathing {
  0%, 100% { 
    transform: scale(1); 
  }
  50% { 
    transform: scale(1.02); 
  }
}

/* 右侧表单区域 */
.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 3rem 1rem;
}

@media (min-width: 640px) {
  .right-section {
    padding: 3rem 1.5rem;
  }
}

@media (min-width: 1024px) {
  .right-section {
    padding: 3rem 5rem;
  }
}

@media (min-width: 1280px) {
  .right-section {
    padding: 3rem 6rem;
  }
}

/* 左侧区域响应式设计 */
@media (max-width: 1023px) {
  .left-content {
    padding: 2rem;
  }
  
  .welcome-title {
    font-size: 2rem;
  }
  
  .welcome-text {
    font-size: 1rem;
  }
  

  
  .robot-icon {
    width: 120px;
    height: 120px;
  }
  
  .robot-icon i {
    font-size: 3rem;
  }
}

@media (max-width: 768px) {
  .robot-section {
    padding-top: 1rem;
  }
  
  .robot-icon {
    width: 100px;
    height: 100px;
  }
  
  .robot-icon i {
    font-size: 2.5rem;
  }
  
  .welcome-title {
    font-size: 1.75rem;
  }
  

}

/* 全局链接效果 */
a {
  transition: color 0.3s ease;
}
</style>
