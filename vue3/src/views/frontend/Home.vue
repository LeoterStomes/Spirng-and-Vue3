<template>
  <div class="home">
    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-text">
            <h2 class="hero-title">
              一次温暖的对话<br>
              <span class="highlight-text">化孤独为慰藉</span>
            </h2>
            <p class="hero-description">
              每个深夜，每个焦虑的时刻，我们都在这里。不必独自承受，让心与心的连接温暖您的每一天
            </p>
            <div class="hero-actions">
              <router-link to="/consultation" class="primary-btn" v-if="isLoggedIn">
                <i class="fas fa-heart"></i>开始倾诉，获得陪伴
              </router-link>
              <router-link to="/auth/login" class="primary-btn" v-else>
                <i class="fas fa-heart"></i>开始倾诉，获得陪伴
              </router-link>
              <router-link to="/emotion-diary" class="secondary-btn" v-if="isLoggedIn">
                <i class="fas fa-pen-fancy"></i>记录心情，释放情感
              </router-link>
              <router-link to="/auth/login" class="secondary-btn" v-else>
                <i class="fas fa-pen-fancy"></i>记录心情，释放情感
              </router-link>
            </div>
          </div>
          <div class="hero-image">
            <div class="icon-wrapper">
              <div class="hero-icon">
                <i class="fas fa-robot"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 核心功能区域 -->
    <section class="features-section">
      <div class="features-container">
        <div class="section-header">
          <h3 class="section-title">我们的服务</h3>
          <p class="section-description">用心倾听每一个故事，用爱温暖每一颗心灵</p>
        </div>
        
        <div class="features-grid">
          <!-- AI咨询 -->
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon ai-icon">
                <i class="fas fa-comments"></i>
              </div>
              <h4 class="feature-title">心灵倾诉</h4>
              <p class="feature-description">无论何时何地，都有一颗温暖的心在倾听。分享您的烦恼，收获理解与支持</p>
              <router-link :to="isLoggedIn ? '/consultation' : '/auth/login'" class="feature-link">
                开始倾诉 <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>

          <!-- 情绪管理 -->
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon emotion-icon">
                <i class="fas fa-heart"></i>
              </div>
              <h4 class="feature-title">情感记录</h4>
              <p class="feature-description">每一种情绪都值得被记录和珍视。让我们陪您一起观察内心，拥抱真实的自己</p>
              <router-link :to="isLoggedIn ? '/emotion-diary' : '/auth/login'" class="feature-link">
                记录心情 <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>

          <!-- 知识学习 -->
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon knowledge-icon">
                <i class="fas fa-seedling"></i>
              </div>
              <h4 class="feature-title">心灵成长</h4>
              <p class="feature-description">在知识的花园中漫步，发现内心的力量。每一篇文章都是通往更好自己的路径</p>
              <router-link to="/knowledge" class="feature-link">
                开始探索 <i class="fas fa-arrow-right"></i>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </section>


  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)

</script>

<style scoped>
/* 导入Font Awesome图标库样式 */
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.home {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 英雄区域样式 */
.hero-section {
  background: linear-gradient(135deg, #4A90E2 0%, #7ED321 100%);
  color: white;
  padding: 5rem 0;
  min-height: 70vh;
  display: flex;
  align-items: center;
}

.hero-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 1rem;
}

.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  align-items: center;
}

.hero-title {
  font-size: 3rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 1.5rem;
}

.highlight-text {
  color: #ffd700;
}

.hero-description {
  font-size: 1.25rem;
  line-height: 1.6;
  margin-bottom: 2rem;
  color: rgba(255, 255, 255, 0.9);
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.primary-btn {
  background: white;
  color: #4A90E2;
  padding: 0.75rem 2rem;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 255, 255, 0.2);
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.primary-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 255, 255, 0.3);
}

.secondary-btn {
  border: 2px solid white;
  color: white;
  padding: 0.75rem 2rem;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.secondary-btn:hover {
  background: white;
  color: #4A90E2;
  transform: translateY(-2px);
}

.hero-image {
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.icon-wrapper {
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

.hero-icon {
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2) 0%, rgba(255, 255, 255, 0.1) 100%);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  position: relative;
  overflow: hidden;
}

.hero-icon::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transform: rotate(45deg);
  animation: shine 3s ease-in-out infinite;
}

.hero-icon i {
  font-size: 8rem;
  color: rgba(255, 255, 255, 0.9);
  position: relative;
  z-index: 1;
  text-shadow: 0 0 30px rgba(255, 255, 255, 0.5);
}

@keyframes shine {
  0% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
  50% { transform: translateX(100%) translateY(100%) rotate(45deg); }
  100% { transform: translateX(-100%) translateY(-100%) rotate(45deg); }
}

@keyframes breathing {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

/* 核心功能区域样式 */
.features-section {
  padding: 5rem 0;
  background: white;
}

.features-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.section-header {
  text-align: center;
  margin-bottom: 4rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 1rem;
}

.section-description {
  font-size: 1.25rem;
  color: #6b7280;
  max-width: 600px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.4s ease;
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(74, 144, 226, 0.15);
}

.feature-icon {
  width: 4rem;
  height: 4rem;
  margin: 0 auto 1rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
}

.ai-icon {
  background: linear-gradient(135deg, #4A90E2, #9013FE);
}

.emotion-icon {
  background: linear-gradient(135deg, #7ED321, #F5A623);
}

.knowledge-icon {
  background: linear-gradient(135deg, #F5A623, #9013FE);
}


.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.75rem;
}

.feature-description {
  color: #6b7280;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.feature-link {
  color: #4A90E2;
  text-decoration: none;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: color 0.3s ease;
}

.feature-link:hover {
  color: #3b7bc8;
}



/* 响应式设计 */
@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    gap: 2rem;
    text-align: center;
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-description {
    font-size: 1rem;
  }
  
  .hero-actions {
    justify-content: center;
  }
  
  .hero-icon {
    width: 200px;
    height: 200px;
  }
  
  .hero-icon i {
    font-size: 5rem;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .hero-section {
    padding: 3rem 0;
  }
  
  .hero-title {
    font-size: 1.75rem;
  }
  
  .primary-btn,
  .secondary-btn {
    padding: 0.5rem 1.5rem;
    font-size: 0.875rem;
  }
  
  .hero-icon {
    width: 150px;
    height: 150px;
  }
  
  .hero-icon i {
    font-size: 3.5rem;
  }
  
  .features-section {
    padding: 3rem 0;
  }
  
  .section-title {
    font-size: 1.75rem;
  }
  
  .feature-card {
    padding: 1.5rem;
  }
}
</style>
