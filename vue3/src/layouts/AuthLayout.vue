<template>
  <div class="auth-layout">
    <div class="bg-slides">
      <div
        v-for="(img, idx) in bgImages"
        :key="img"
        class="bg-slide"
        :class="{ active: idx === currentSlide }"
        :style="{ backgroundImage: `url(${img})` }"
      ></div>
    </div>
    <div class="bg-overlay"></div>
    <div class="snow-layer" aria-hidden="true">
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

    <div class="auth-content">
      <div class="auth-container">
        <div class="right-section">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import cataImg from '@/assets/cata.jpg'
import catbImg from '@/assets/catb.jpg'
import catcImg from '@/assets/catc.jpg'
import catdImg from '@/assets/catd.jpg'

const bgImages = [cataImg, catbImg, catcImg, catdImg]
const currentSlide = ref(0)
const snowflakes = Array.from({ length: 18 }, (_, index) => ({
  id: index + 1,
  left: `${Math.random() * 100}%`,
  duration: `${10 + Math.random() * 10}s`,
  delay: `${Math.random() * 6}s`,
  opacity: `${0.2 + Math.random() * 0.45}`,
  size: `${10 + Math.random() * 12}px`
}))

let slideTimer = null

onMounted(() => {
  slideTimer = window.setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % bgImages.length
  }, 6000)
})

onUnmounted(() => {
  if (slideTimer) {
    window.clearInterval(slideTimer)
    slideTimer = null
  }
})
</script>

<style scoped>
.auth-layout {
  background-color: #0f172a;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.bg-slides {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-slide {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transform: scale(1.03);
  transition: opacity 1.2s ease, transform 6s linear;
}

.bg-slide.active {
  opacity: 1;
  transform: scale(1);
}

.bg-overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  background: rgba(15, 23, 42, 0.42);
}

.snow-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 2;
  overflow: hidden;
}

.snowflake {
  position: absolute;
  top: -8vh;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.28);
  animation-name: snowfall;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

.auth-content {
  height: 100vh;
  position: relative;
  z-index: 3;
}

.auth-container {
  height: 100%;
  display: grid;
  place-items: center;
  padding: 1rem;
}

.right-section {
  width: 100%;
  max-width: 520px;
  background: rgba(255, 255, 255, 0.26);
  border: 1px solid rgba(255, 255, 255, 0.36);
  border-radius: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.28);
  padding: 1.8rem 1.4rem;
}

@media (min-width: 640px) {
  .right-section {
    padding: 2.2rem 1.8rem;
  }
}

@media (min-width: 1024px) {
  .right-section {
    max-width: 560px;
  }
}

@keyframes snowfall {
  0% {
    transform: translate3d(0, -10vh, 0) rotate(0deg);
  }
  100% {
    transform: translate3d(30px, 110vh, 0) rotate(360deg);
  }
}
</style>
