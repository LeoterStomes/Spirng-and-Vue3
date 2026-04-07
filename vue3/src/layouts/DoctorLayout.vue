<template>
  <div class="backend-layout">
    <DoctorSidebar />
    <div class="main-content">
      <Navbar @logout="handleLogout" />
      <div class="content-container">
        <router-view v-slot="{ Component }">
          <transition
            name="fade"
            mode="out-in"
          >
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import DoctorSidebar from '@/components/doctor/DoctorSidebar.vue'
import Navbar from '@/components/backend/Navbar.vue'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  userStore.clearUserInfo()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.backend-layout {
  display: flex;
  height: 100vh;
  min-height: 100vh;
  background:
    radial-gradient(circle at 0% 0%, rgba(59, 130, 246, 0.12) 0%, transparent 35%),
    radial-gradient(circle at 100% 100%, rgba(16, 185, 129, 0.1) 0%, transparent 36%),
    #eff6ff;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.content-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  position: relative;
  background: rgba(255, 255, 255, 0.36);
  backdrop-filter: blur(6px);

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 3px;
  }
  &::-webkit-scrollbar-track {
    background-color: transparent;
  }
}
</style>
