<template>
  <div class="home">
    <div
      class="snow-layer"
      aria-hidden="true"
    >
      <span
        v-for="flake in snowflakes"
        :key="flake.id"
        class="snowflake"
        :style="{
          left: flake.left,
          animationDuration: flake.duration,
          animationDelay: flake.delay,
          opacity: flake.opacity,
          fontSize: flake.size
        }"
      >
        ❄
      </span>
    </div>

    <div class="home-background">
      <div
        v-for="(img, idx) in heroImages"
        :key="`home-${img}`"
        class="home-bg-slide"
        :class="{ active: idx === currentSlide }"
        :style="{ backgroundImage: `url(${img})` }"
      />
    </div>

    <section class="hero-section">
      <div class="hero-overlay" />

      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-text">
            <h2 class="hero-title">
              一次温暖的对话<br>
              <span class="highlight-text">化孤独为慰藉</span>
            </h2>
            <p class="hero-description">
              每个深夜，每个焦虑时刻，我们都在这里。不必独自承受，让心与心的连接温暖你的每一天。
            </p>
            <div class="hero-actions">
              <router-link
                v-if="isLoggedIn"
                to="/consultation"
                class="primary-btn"
              >
                <i class="fas fa-heart" />
                开始倾诉，获得陪伴
              </router-link>
              <router-link
                v-else
                to="/auth/login"
                class="primary-btn"
              >
                <i class="fas fa-heart" />
                开始倾诉，获得陪伴
              </router-link>

              <router-link
                v-if="isLoggedIn"
                to="/emotion-diary"
                class="secondary-btn"
              >
                <i class="fas fa-pen-fancy" />
                记录心情，释放情感
              </router-link>
              <router-link
                v-else
                to="/auth/login"
                class="secondary-btn"
              >
                <i class="fas fa-pen-fancy" />
                记录心情，释放情感
              </router-link>
            </div>
          </div>

          <div class="hero-image">
            <div class="hero-panel">
              <div class="panel-badge">
                24小时陪伴
              </div>
              <h4>让小暖陪你慢慢变好</h4>
              <transition
                name="slogan-fade"
                mode="out-in"
              >
                <p
                  :key="currentSloganIndex"
                  class="rotating-slogan"
                >
                  {{ heroSlogans[currentSloganIndex] }}
                </p>
              </transition>
              <div class="slide-indicators">
                <span
                  v-for="(_, idx) in heroImages"
                  :key="idx"
                  class="indicator"
                  :class="{ active: idx === currentSlide }"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="features-section">
      <div class="features-overlay" />

      <div class="features-container">
        <div class="section-header">
          <h3 class="section-title">
            我们的服务
          </h3>
          <p class="section-description">
            用心倾听每一个故事，用爱温暖每一颗心灵
          </p>
        </div>

        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon ai-icon">
                <i class="fas fa-comments" />
              </div>
              <h4 class="feature-title">
                心灵倾诉
              </h4>
              <p class="feature-description">
                无论何时何地，都有一颗温暖的心在倾听。分享烦恼，收获理解与支持。
              </p>
              <router-link
                :to="isLoggedIn ? '/consultation' : '/auth/login'"
                class="feature-link"
              >
                开始倾诉
                <i class="fas fa-arrow-right" />
              </router-link>
            </div>
          </div>

          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon emotion-icon">
                <i class="fas fa-heart" />
              </div>
              <h4 class="feature-title">
                情感记录
              </h4>
              <p class="feature-description">
                每一种情绪都值得被记录和珍视。陪你观察内心，拥抱真实的自己。
              </p>
              <router-link
                :to="isLoggedIn ? '/emotion-diary' : '/auth/login'"
                class="feature-link"
              >
                记录心情
                <i class="fas fa-arrow-right" />
              </router-link>
            </div>
          </div>

          <div class="feature-card">
            <div class="feature-content">
              <div class="feature-icon knowledge-icon">
                <i class="fas fa-seedling" />
              </div>
              <h4 class="feature-title">
                心灵成长
              </h4>
              <p class="feature-description">
                在知识花园中漫步，发现内心力量。每篇内容都通往更好的自己。
              </p>
              <router-link
                to="/knowledge"
                class="feature-link"
              >
                开始探索
                <i class="fas fa-arrow-right" />
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useUserStore } from '@/store/user'
import cataImg from '@/assets/cata.jpg'
import catbImg from '@/assets/catb.jpg'
import catcImg from '@/assets/catc.jpg'
import catdImg from '@/assets/catd.jpg'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)
const heroImages = [cataImg, catbImg, catcImg, catdImg]
const heroSlogans = [
  '倾诉心声、记录心情、探索内心、获得陪伴',
  '改变自己，从现在开始',
  '小暖，陪你一起成长',
  '今天你的心情怎么样^-^'
]
const snowflakes = Array.from({ length: 30 }, (_, index) => ({
  id: index + 1,
  left: `${Math.random() * 100}%`,
  duration: `${8 + Math.random() * 10}s`,
  delay: `${Math.random() * 8}s`,
  opacity: `${0.25 + Math.random() * 0.5}`,
  size: `${12 + Math.random() * 14}px`
}))

const currentSlide = ref(0)
const slideDirection = ref(1)
const currentSloganIndex = ref(0)
let slideTimer = null
let sloganTimer = null

const playHeroSlider = () => {
  const next = currentSlide.value + slideDirection.value
  if (next >= heroImages.length) {
    slideDirection.value = -1
    currentSlide.value = heroImages.length - 2
    return
  }
  if (next < 0) {
    slideDirection.value = 1
    currentSlide.value = 1
    return
  }
  currentSlide.value = next
}

onMounted(() => {
  slideTimer = window.setInterval(playHeroSlider, 7000)
  sloganTimer = window.setInterval(() => {
    currentSloganIndex.value = (currentSloganIndex.value + 1) % heroSlogans.length
  }, 3000)
})

onUnmounted(() => {
  if (slideTimer) {
    window.clearInterval(slideTimer)
    slideTimer = null
  }
  if (sloganTimer) {
    window.clearInterval(sloganTimer)
    sloganTimer = null
  }
})
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.home {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  position: relative;
}

.home-background {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.home-bg-slide {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transform: scale(1.035);
  transition: opacity 2.4s ease, transform 7s linear;
}

.home-bg-slide.active {
  opacity: 1;
  transform: scale(1);
}

.snow-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 6;
  overflow: hidden;
}

.snowflake {
  position: absolute;
  top: -5vh;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.35);
  animation-name: snowfall;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

.hero-section {
  position: relative;
  z-index: 1;
  color: #fff;
  padding: 5rem 0;
  min-height: 72vh;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(110deg, rgba(8, 20, 45, 0.78) 0%, rgba(8, 20, 45, 0.55) 45%, rgba(8, 20, 45, 0.45) 100%);
}

.hero-container {
  position: relative;
  z-index: 2;
  max-width: 1100px;
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
  color: #ffe08a;
}

.hero-description {
  font-size: 1.2rem;
  line-height: 1.7;
  margin-bottom: 2rem;
  color: rgba(255, 255, 255, 0.95);
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.primary-btn {
  background: rgba(255, 255, 255, 0.96);
  color: #1e3a8a;
  padding: 0.75rem 2rem;
  border-radius: 0.65rem;
  text-decoration: none;
  font-weight: 700;
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.primary-btn:hover {
  transform: translateY(-2px);
}

.secondary-btn {
  border: 2px solid rgba(255, 255, 255, 0.88);
  color: #fff;
  padding: 0.75rem 2rem;
  border-radius: 0.65rem;
  text-decoration: none;
  font-weight: 700;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  transform: translateY(-2px);
}

.hero-image {
  display: flex;
  justify-content: center;
}

.hero-panel {
  max-width: 360px;
  width: 100%;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.22);
  padding: 1.5rem;
  text-align: left;
}

.panel-badge {
  display: inline-block;
  padding: 0.35rem 0.8rem;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.35);
  font-size: 0.85rem;
  margin-bottom: 0.9rem;
}

.hero-panel h4 {
  margin: 0 0 0.7rem;
  font-size: 1.35rem;
  font-weight: 700;
}

.hero-panel p {
  margin: 0;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.92);
}

.rotating-slogan {
  min-height: 2.8em;
}

.slogan-fade-enter-active,
.slogan-fade-leave-active {
  transition: opacity 0.45s ease, transform 0.45s ease;
}

.slogan-fade-enter-from,
.slogan-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.slide-indicators {
  margin-top: 1rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.indicator {
  width: 20px;
  height: 4px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.35);
  transition: all 0.35s ease;
}

.indicator.active {
  width: 34px;
  background: #fff;
}

.features-section {
  position: relative;
  z-index: 1;
  padding: 5rem 0;
  overflow: hidden;
}

.features-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: linear-gradient(150deg, rgba(7, 19, 42, 0.8) 0%, rgba(12, 30, 60, 0.65) 45%, rgba(8, 20, 45, 0.72) 100%);
}

.features-container {
  position: relative;
  z-index: 2;
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
  color: #fff;
  margin-bottom: 1rem;
}

.section-description {
  font-size: 1.25rem;
  color: rgba(255, 255, 255, 0.86);
  max-width: 600px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: rgba(255, 255, 255, 0.14);
  border-radius: 1rem;
  padding: 2rem;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(6px);
  transition: all 0.35s ease;
  text-align: center;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(74, 144, 226, 0.2);
}

.feature-icon {
  width: 4rem;
  height: 4rem;
  margin: 0 auto 1rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.5rem;
}

.ai-icon {
  background: linear-gradient(135deg, #4a90e2, #9013fe);
}

.emotion-icon {
  background: linear-gradient(135deg, #7ed321, #f5a623);
}

.knowledge-icon {
  background: linear-gradient(135deg, #f5a623, #9013fe);
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #fff;
  margin-bottom: 0.75rem;
}

.feature-description {
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.feature-link {
  color: #bfdbfe;
  text-decoration: none;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  transition: color 0.3s ease;
}

.feature-link:hover {
  color: #dbeafe;
}

@keyframes snowfall {
  0% {
    transform: translate3d(0, -10vh, 0) rotate(0deg);
  }
  100% {
    transform: translate3d(30px, 110vh, 0) rotate(360deg);
  }
}

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

  .hero-panel {
    margin: 0 auto;
    text-align: center;
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
    padding: 0.5rem 1.2rem;
    font-size: 0.875rem;
  }

  .hero-panel h4 {
    font-size: 1.15rem;
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
