<template>
  <div class="home-blog">
    <section class="blog-hero">
      <div class="hero-carousel">
        <button class="hero-nav prev" @click="prevHero">‹</button>
        <button class="hero-nav next" @click="nextHero">›</button>

        <transition name="hero-slide" mode="out-in">
          <div
            v-if="activeHeroArticle"
            :key="activeHeroArticle.id"
            class="hero-slide-card"
          >
            <div class="hero-slide-text">
              <p class="hero-kicker">MENTAL BLOG</p>
              <h1>{{ activeHeroArticle.title }}</h1>
              <p class="hero-desc">
                {{ activeHeroArticle.summary || getAutoSummary(activeHeroArticle.content) }}
              </p>
              <div class="hero-meta">
                <span>{{ activeHeroArticle.categoryName || '心理知识' }}</span>
                <span>{{ formatDate(activeHeroArticle.publishedAt) }}</span>
                <span>阅读 {{ activeHeroArticle.readCount || 0 }}</span>
              </div>
              <div class="hero-actions">
                <router-link :to="`/knowledge/article/${activeHeroArticle.id}`" class="btn primary">
                  阅读全文
                </router-link>
                <router-link :to="isLoggedIn ? '/consultation' : '/auth/login'" class="btn ghost">
                  AI 咨询
                </router-link>
              </div>
            </div>

            <router-link :to="`/knowledge/article/${activeHeroArticle.id}`" class="hero-slide-image">
              <img :src="activeHeroArticle.coverImage || fallbackCover" :alt="activeHeroArticle.title" @error="onImageError">
            </router-link>
          </div>
          <div v-else key="empty" class="hero-slide-card empty">
            <div class="hero-slide-text">
              <p class="hero-kicker">MENTAL BLOG</p>
              <h1>心理成长与陪伴空间</h1>
              <p class="hero-desc">在这里记录、阅读、倾诉，把复杂情绪整理成能被理解的文字与行动。</p>
            </div>
          </div>
        </transition>

        <div class="hero-dots">
          <button
            v-for="(item, idx) in heroArticles"
            :key="item.id"
            class="hero-dot"
            :class="{ active: idx === heroIndex }"
            @click="goHero(idx)"
          />
        </div>
      </div>

      <div class="hero-search">
        <input
          v-model.trim="searchKeyword"
          type="text"
          placeholder="输入关键词，快速搜索心理文章"
          @keyup.enter="goSearch"
        >
        <button @click="goSearch">搜索</button>
      </div>
    </section>

    <section class="home-main">
      <div class="main-grid">
        <div class="left-column">
          <div class="section-title-row">
            <h2>最新文章</h2>
            <router-link to="/knowledge" class="more-link">查看更多</router-link>
          </div>
          <div v-loading="latestLoading" class="article-list">
            <router-link
              v-for="article in latestArticles"
              :key="article.id"
              :to="`/knowledge/article/${article.id}`"
              class="article-item"
            >
              <img :src="article.coverImage || fallbackCover" :alt="article.title" @error="onImageError">
              <div class="article-info">
                <p class="article-meta">{{ article.categoryName || '心理知识' }} · {{ formatDate(article.publishedAt) }}</p>
                <h3>{{ article.title }}</h3>
                <p class="article-summary">{{ article.summary || getAutoSummary(article.content) }}</p>
                <p class="article-foot">阅读 {{ article.readCount || 0 }} · 收藏 {{ article.favoriteCount || 0 }}</p>
              </div>
            </router-link>
            <div v-if="!latestLoading && latestArticles.length === 0" class="empty-text">暂无文章内容</div>
          </div>
        </div>

        <aside class="right-column">
          <div class="widget">
            <h3>快捷入口</h3>
            <div class="quick-links">
              <router-link :to="isLoggedIn ? '/emotion-diary' : '/auth/login'">情绪日记</router-link>
              <router-link :to="isLoggedIn ? '/psychological-test' : '/auth/login'">心理测试</router-link>
              <router-link :to="isLoggedIn ? '/consultation' : '/auth/login'">AI 咨询</router-link>
              <router-link to="/knowledge">知识分类</router-link>
            </div>
          </div>

          <div class="widget">
            <h3>热门阅读</h3>
            <router-link
              v-for="item in popularArticles"
              :key="item.id"
              :to="`/knowledge/article/${item.id}`"
              class="hot-item"
            >
              <span class="hot-title">{{ item.title }}</span>
              <span class="hot-count">👀 {{ item.readCount || 0 }}</span>
            </router-link>
            <p v-if="!popularArticles.length" class="empty-text">暂无热门文章</p>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getArticlePage } from '@/api/knowledgeArticle'
import { formatDate } from '@/utils/dateUtils'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)

const searchKeyword = ref('')
const latestLoading = ref(false)
const latestArticles = ref([])
const popularArticles = ref([])
const heroIndex = ref(0)
const fallbackCover = 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=600&q=80'
let heroTimer = null

const heroArticles = computed(() => latestArticles.value.slice(0, 4))
const activeHeroArticle = computed(() => heroArticles.value[heroIndex.value] || null)

const goSearch = () => {
  router.push({
    path: '/knowledge',
    query: searchKeyword.value ? { keyword: searchKeyword.value } : undefined
  })
}

const onImageError = (event) => {
  event.target.src = fallbackCover
}

const getAutoSummary = (content) => {
  if (!content) return '暂无摘要'
  const plainText = content.replace(/<[^>]+>/g, '')
  return plainText.length > 90 ? `${plainText.substring(0, 90)}...` : plainText
}

const fetchLatestArticles = () => {
  latestLoading.value = true
  getArticlePage(
    { currentPage: 1, size: 6, sortField: 'publishedAt', sortDirection: 'desc' },
    {
      onSuccess: (res) => {
        latestArticles.value = res?.records || []
        latestLoading.value = false
      },
      onError: () => {
        latestArticles.value = []
        latestLoading.value = false
      }
    }
  )
}

const fetchPopularArticles = () => {
  getArticlePage(
    { currentPage: 1, size: 6, sortField: 'readCount', sortDirection: 'desc' },
    {
      onSuccess: (res) => {
        popularArticles.value = res?.records || []
      },
      onError: () => {
        popularArticles.value = []
      }
    }
  )
}

const nextHero = () => {
  if (!heroArticles.value.length) return
  heroIndex.value = (heroIndex.value + 1) % heroArticles.value.length
}

const prevHero = () => {
  if (!heroArticles.value.length) return
  heroIndex.value = (heroIndex.value - 1 + heroArticles.value.length) % heroArticles.value.length
}

const goHero = (index) => {
  heroIndex.value = index
}

const startHeroAutoPlay = () => {
  if (heroTimer) window.clearInterval(heroTimer)
  heroTimer = window.setInterval(() => {
    if (heroArticles.value.length > 1) nextHero()
  }, 5000)
}

watch(heroArticles, (val) => {
  if (!val.length) {
    heroIndex.value = 0
    return
  }
  if (heroIndex.value >= val.length) {
    heroIndex.value = 0
  }
})

onMounted(() => {
  fetchLatestArticles()
  fetchPopularArticles()
  startHeroAutoPlay()
})

onUnmounted(() => {
  if (heroTimer) {
    window.clearInterval(heroTimer)
    heroTimer = null
  }
})
</script>

<style scoped>
.home-blog {
  background: #f5f7fb;
  min-height: 100vh;
  color: #1f2937;
}

.blog-hero {
  max-width: 1160px;
  margin: 0 auto;
  padding: 3rem 1rem 2rem;
}

.hero-carousel {
  position: relative;
}

.hero-slide-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 1rem;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 1rem;
}

.hero-slide-card.empty {
  grid-template-columns: 1fr;
}

.hero-slide-text {
  padding: 0.4rem 0.35rem;
}

.hero-slide-image {
  display: block;
}

.hero-slide-image img {
  width: 100%;
  height: 260px;
  object-fit: cover;
  border-radius: 14px;
}

.hero-kicker {
  font-size: 0.8rem;
  letter-spacing: 0.12em;
  color: #6b7280;
  margin-bottom: 0.4rem;
}

.blog-hero h1 {
  font-size: 2.2rem;
  margin: 0 0 0.8rem;
}

.hero-desc {
  margin: 0;
  color: #4b5563;
  max-width: 720px;
}

.hero-actions {
  margin-top: 1.3rem;
  display: flex;
  gap: 0.75rem;
}

.hero-meta {
  margin-top: 0.8rem;
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  color: #6b7280;
  font-size: 0.85rem;
}

.btn {
  text-decoration: none;
  border-radius: 10px;
  padding: 0.6rem 1rem;
  font-weight: 600;
}

.btn.primary {
  background: #2563eb;
  color: #fff;
}

.btn.ghost {
  border: 1px solid #cbd5e1;
  color: #334155;
}

.hero-nav {
  position: absolute;
  top: 45%;
  z-index: 3;
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 50%;
  background: rgba(17, 24, 39, 0.7);
  color: #fff;
  cursor: pointer;
}

.hero-nav.prev {
  left: -10px;
}

.hero-nav.next {
  right: -10px;
}

.hero-dots {
  margin-top: 0.7rem;
  display: flex;
  gap: 0.35rem;
  justify-content: center;
}

.hero-dot {
  width: 16px;
  height: 4px;
  border: 0;
  border-radius: 999px;
  background: #cbd5e1;
  cursor: pointer;
}

.hero-dot.active {
  width: 28px;
  background: #2563eb;
}

.hero-search {
  margin-top: 1rem;
  display: flex;
  gap: 0.5rem;
  max-width: 560px;
}

.hero-search input {
  flex: 1;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 0.65rem 0.85rem;
}

.hero-search button {
  border: 0;
  border-radius: 10px;
  background: #111827;
  color: #fff;
  padding: 0 1rem;
}

.home-main {
  max-width: 1160px;
  margin: 0 auto;
  padding: 0 1rem 2.5rem;
}

.main-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 1rem;
}

.left-column,
.right-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title-row h2 {
  margin: 0;
}

.more-link {
  font-size: 0.9rem;
  color: #2563eb;
  text-decoration: none;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.article-item {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 0.9rem;
  text-decoration: none;
  color: inherit;
  background: #fff;
  border-radius: 14px;
  padding: 0.7rem;
  border: 1px solid #e5e7eb;
}

.article-item img {
  width: 100%;
  height: 140px;
  border-radius: 10px;
  object-fit: cover;
}

.article-meta {
  font-size: 0.8rem;
  color: #6b7280;
  margin: 0;
}

.article-info h3 {
  margin: 0.35rem 0 0.45rem;
  font-size: 1.05rem;
}

.article-summary {
  margin: 0;
  color: #4b5563;
  line-height: 1.6;
}

.article-foot {
  margin: 0.6rem 0 0;
  font-size: 0.85rem;
  color: #6b7280;
}

.widget {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  padding: 1rem;
}

.widget h3 {
  margin: 0 0 0.9rem;
}

.quick-links {
  display: grid;
  gap: 0.6rem;
}

.quick-links a {
  text-decoration: none;
  color: #1f2937;
  padding: 0.45rem 0.6rem;
  border-radius: 8px;
  background: #f8fafc;
}

.hot-item {
  display: flex;
  justify-content: space-between;
  gap: 0.5rem;
  text-decoration: none;
  color: #334155;
  padding: 0.45rem 0;
  border-bottom: 1px dashed #e5e7eb;
}

.hot-title {
  flex: 1;
}

.hot-count {
  color: #6b7280;
}

.empty-text {
  font-size: 0.9rem;
  color: #6b7280;
}

.hero-slide-enter-active,
.hero-slide-leave-active {
  transition: all 0.35s ease;
}

.hero-slide-enter-from,
.hero-slide-leave-to {
  opacity: 0;
  transform: translateX(12px);
}

@media (max-width: 992px) {
  .hero-slide-card {
    grid-template-columns: 1fr;
  }

  .hero-nav.prev {
    left: 6px;
  }

  .hero-nav.next {
    right: 6px;
  }

  .main-grid {
    grid-template-columns: 1fr;
  }

  .article-item {
    grid-template-columns: 1fr;
  }
}
</style>
