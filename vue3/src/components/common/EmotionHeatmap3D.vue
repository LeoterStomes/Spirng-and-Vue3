<template>
  <div class="emotion-heatmap-container">
    <div class="heatmap-header">
      <h3 class="heatmap-title">
        <i class="fas fa-heart"></i>
        情绪时光热力图
      </h3>
      <div class="heatmap-subtitle">探索你的情绪节律，发现内心的美好时光</div>
      <div class="heatmap-info">
        <div class="info-item">
          <i class="fas fa-calendar-alt"></i>
          <span>{{ dateRange }}</span>
        </div>
        <div class="info-item">
          <i class="fas fa-star"></i>
          <span>最活跃时段: {{ peakEmotionTime }}</span>
        </div>
      </div>
    </div>
    
    <div class="heatmap-content">
      <div ref="heatmapContainer" class="heatmap-3d"></div>
    </div>

    <div class="emotion-distribution">
      <h4 class="distribution-title">
        <i class="fas fa-rainbow"></i>
        情绪花园
      </h4>
      <div class="emotion-garden">
        <div 
          v-for="(count, emotion) in emotionDistribution" 
          :key="emotion"
          class="emotion-flower"
          :class="{ 
            'hidden': !emotionVisibility[emotion],
            'highlighted': hoveredObject && hoveredObject.userData?.dominantEmotion === emotion
          }"
          :style="{ 
            '--flower-size': Math.min(count / maxEmotionCount * 1.5 + 0.8, 2),
            '--flower-glow': Math.min(count / maxEmotionCount * 0.8, 0.6),
            '--emotion-color': getEmotionColorString(emotion)
          }"
          @click="toggleEmotionVisibility(emotion)"
          @mouseenter="highlightEmotion(emotion)"
          @mouseleave="clearEmotionHighlight()"
        >
          <div class="flower-petals">
            <div class="petal" v-for="i in 6" :key="i"></div>
          </div>
          <div class="flower-center">
            <span class="emotion-name">{{ emotion }}</span>
            <span class="emotion-count">{{ count }}</span>
          </div>
          <div class="visibility-indicator">
            <i :class="emotionVisibility[emotion] ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as THREE from 'three'

const props = defineProps({
  heatmapData: {
    type: Object,
    default: () => ({
      gridData: [],
      emotionDistribution: {},
      peakEmotionTime: '00:00',
      dateRange: ''
    })
  }
})

const heatmapContainer = ref(null)
let scene, camera, renderer, animationId, raycaster, mouse

// 情绪颜色映射系统 - 使用随机生成的颜色
const emotionColorMap = ref({})

// 情绪可见性状态管理
const emotionVisibility = ref({})
const hoveredObject = ref(null)

// 计算属性
const dateRange = computed(() => props.heatmapData.dateRange || '暂无数据')
const peakEmotionTime = computed(() => props.heatmapData.peakEmotionTime || '00:00')
const emotionDistribution = computed(() => props.heatmapData.emotionDistribution || {})
const maxEmotionCount = computed(() => {
  const counts = Object.values(emotionDistribution.value)
  return counts.length > 0 ? Math.max(...counts) : 1
})

// 生成随机颜色
const generateRandomColor = () => {
  const hue = Math.random() // 0-1
  const saturation = 0.7 + Math.random() * 0.3 // 0.7-1.0，保持饱和度较高
  const lightness = 0.5 + Math.random() * 0.3 // 0.5-0.8，保持亮度适中
  return { h: hue, s: saturation, l: lightness }
}

// 初始化情绪颜色映射
const initEmotionColorMap = () => {
  const colorMap = {}
  Object.keys(emotionDistribution.value).forEach(emotion => {
    colorMap[emotion] = generateRandomColor()
  })
  emotionColorMap.value = colorMap
}

// 初始化情绪可见性状态
const initEmotionVisibility = () => {
  const visibility = {}
  Object.keys(emotionDistribution.value).forEach(emotion => {
    visibility[emotion] = true // 默认全部可见
  })
  emotionVisibility.value = visibility
}

// 切换情绪可见性
const toggleEmotionVisibility = (emotion) => {
  emotionVisibility.value[emotion] = !emotionVisibility.value[emotion]
  // 重新渲染热力图
  updateHeatmapVisibility()
}

// 根据情绪获取颜色
const getEmotionColor = (emotion, intensity = 1) => {
  const colorConfig = emotionColorMap.value[emotion] || { h: 0.5, s: 0.5, l: 0.5 }
  const color = new THREE.Color()
  // 根据强度调整亮度
  const adjustedL = Math.max(0.3, Math.min(0.9, colorConfig.l * intensity))
  color.setHSL(colorConfig.h, colorConfig.s, adjustedL)
  return color
}

// 更新热力图可见性
const updateHeatmapVisibility = () => {
  if (!scene) return
  
  scene.traverse((child) => {
    if (child.isMesh && child.geometry.type === 'CylinderGeometry' && child.userData.isRadialBar) {
      const emotion = child.userData.dominantEmotion
      const isVisible = emotionVisibility.value[emotion]
      child.visible = isVisible
      
      // 如果可见，恢复正常透明度；如果不可见，设为完全透明
      if (child.material) {
        child.material.opacity = isVisible ? 0.85 : 0
      }
    }
  })
}

// 获取情绪颜色字符串（用于CSS）
const getEmotionColorString = (emotion) => {
  const colorConfig = emotionColorMap.value[emotion] || { h: 0.5, s: 0.5, l: 0.5 }
  return `hsl(${colorConfig.h * 360}, ${colorConfig.s * 100}%, ${colorConfig.l * 100}%)`
}

// 高亮情绪
const highlightEmotion = (emotion) => {
  if (!scene) return
  
  scene.traverse((child) => {
    if (child.isMesh && child.geometry.type === 'CylinderGeometry' && child.userData.isRadialBar) {
      if (child.userData.dominantEmotion === emotion) {
        // 高亮选中的情绪柱状图
        child.material.emissive = getEmotionColor(emotion, 0.4)
        child.scale.set(1.15, 1.15, 1.15)
      } else {
        // 淡化其他柱状图
        child.material.opacity = 0.2
      }
    }
  })
}

// 清除情绪高亮
const clearEmotionHighlight = () => {
  if (!scene) return
  
  scene.traverse((child) => {
    if (child.isMesh && child.geometry.type === 'CylinderGeometry' && child.userData.isRadialBar) {
      const emotion = child.userData.dominantEmotion
      const isVisible = emotionVisibility.value[emotion]
      
      // 恢复原始状态
      child.material.emissive = getEmotionColor(emotion, 0.2)
      child.scale.set(1, 1, 1)
      child.material.opacity = isVisible ? 0.85 : 0
    }
  })
}

// 初始化3D热力图
const init3DHeatmap = () => {
  if (!heatmapContainer.value) return

  // 清理之前的场景
  cleanup()

  const container = heatmapContainer.value
  const width = container.clientWidth
  const height = container.clientHeight

  // 创建场景
  scene = new THREE.Scene()
  // 黑色背景
  scene.background = new THREE.Color(0x000000)

  // 创建相机
  camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000)
  camera.position.set(18, 12, 18)
  camera.lookAt(0, 0, 0)

  // 创建渲染器
  renderer = new THREE.WebGLRenderer({ 
    antialias: true,
    alpha: true
  })
  renderer.setSize(width, height)
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2
  container.appendChild(renderer.domElement)

  // 初始化射线投射器和鼠标向量
  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()

  // 适应黑色背景的光照系统
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.4)
  scene.add(ambientLight)

  const mainLight = new THREE.DirectionalLight(0xffffff, 1.5)
  mainLight.position.set(15, 20, 10)
  mainLight.castShadow = true
  mainLight.shadow.mapSize.width = 4096
  mainLight.shadow.mapSize.height = 4096
  mainLight.shadow.camera.near = 0.5
  mainLight.shadow.camera.far = 500
  scene.add(mainLight)

  // 补充光源
  const fillLight = new THREE.DirectionalLight(0xffffff, 0.8)
  fillLight.position.set(-10, 10, -10)
  scene.add(fillLight)

  // 创建热力图数据
  createHeatmapBars()

  // 添加环境装饰
  createEnvironment()

  // 添加控制器
  addControls()

  // 开始渲染循环
  animate()
}

// 创建球状+柱状图热力图
const createHeatmapBars = () => {
  if (!props.heatmapData.gridData || props.heatmapData.gridData.length === 0) {
    return
  }

  const gridData = props.heatmapData.gridData
  const maxValue = Math.max(...gridData.flat().map(point => point.value))
  
  // 创建中心大球
  const centralSphereRadius = 5
  const centralSphereGeometry = new THREE.SphereGeometry(centralSphereRadius, 32, 24)
  const centralSphereMaterial = new THREE.MeshPhongMaterial({
    color: 0x1e3a8a,  // 深蓝色
    transparent: true,
    opacity: 0.7,
    shininess: 120,
    emissive: new THREE.Color(0x0f172a)  // 深蓝发光
  })
  const centralSphere = new THREE.Mesh(centralSphereGeometry, centralSphereMaterial)
  centralSphere.position.set(0, 0, 0)
  centralSphere.castShadow = true
  centralSphere.receiveShadow = true
  scene.add(centralSphere)

  // 在球面上分布柱状图
  gridData.forEach((dayData, dayIndex) => {
    dayData.forEach((point, hourIndex) => {
      if (point.value > 0 && point.dominantEmotion) {
        const barLength = (point.value / Math.max(maxValue, 1)) * 4 + 1.5 // 柱状图长度
        
        // 使用螺旋分布算法，让柱状图在球面上更均匀分布
        const totalPoints = 24 * 7 // 总点数
        const currentIndex = dayIndex * 24 + hourIndex // 当前点的索引
        
        // 黄金角度螺旋分布 - 这是在球面上分布点的最优算法
        const goldenAngle = Math.PI * (3 - Math.sqrt(5)) // 黄金角度
        
        // 计算纬度 (从-1到1，然后转换为角度)
        const y = 1 - (currentIndex / (totalPoints - 1)) * 2 // y从1到-1
        const radius = Math.sqrt(1 - y * y) // 在该纬度的圆半径
        
        // 计算经度
        const theta = goldenAngle * currentIndex // 螺旋角度
        
        // 转换为球面坐标
        const longitude = theta
        const latitude = Math.asin(y) // 从y值计算纬度
        
        // 球面坐标转笛卡尔坐标（球面上的起始点）
        const sphereX = centralSphereRadius * radius * Math.cos(longitude)
        const sphereY = centralSphereRadius * y
        const sphereZ = centralSphereRadius * radius * Math.sin(longitude)
        
        // 计算从球心指向球面点的方向向量
        const direction = new THREE.Vector3(sphereX, sphereY, sphereZ).normalize()
        
        // 创建柱状几何体
        const geometry = new THREE.CylinderGeometry(0.25, 0.35, barLength, 8)
        
        // 根据主导情绪获取颜色
        const emotion = point.dominantEmotion
        const intensity = point.value / Math.max(maxValue, 1)
        const color = getEmotionColor(emotion, intensity)

        // 创建材质
        const material = new THREE.MeshPhongMaterial({ 
          color,
          shininess: 50,
          transparent: true,
          opacity: 0.85,
          emissive: color.clone().multiplyScalar(0.2)
        })
        
        const cylinder = new THREE.Mesh(geometry, material)
        
        // 设置柱状图位置：从球面开始，沿方向向外延伸
        const startDistance = centralSphereRadius + 0.1 // 略微离开球面
        const cylinderPosition = direction.clone().multiplyScalar(startDistance + barLength / 2)
        cylinder.position.copy(cylinderPosition)
        
        // 设置柱状图旋转，使其指向球心外侧
        const up = new THREE.Vector3(0, 1, 0)
        const quaternion = new THREE.Quaternion()
        quaternion.setFromUnitVectors(up, direction)
        cylinder.quaternion.copy(quaternion)
        
        // 添加微妙的随机旋转
        cylinder.rotateOnAxis(direction, Math.random() * 0.3 - 0.15)
        
        cylinder.castShadow = true
        cylinder.receiveShadow = true
        
        // 添加用户数据以便交互
        cylinder.userData = {
          hour: hourIndex,
          day: dayIndex,
          value: point.value,
          avgMoodScore: point.avgMoodScore,
          dominantEmotion: point.dominantEmotion,
          isRadialBar: true // 标识这是径向柱状图
        }
        
        // 根据情绪可见性设置初始状态
        const isVisible = emotionVisibility.value[emotion]
        cylinder.visible = isVisible
        if (!isVisible) {
          material.opacity = 0
        }
        
        scene.add(cylinder)
      }
    })
  })
}

// 创建环境装饰
const createEnvironment = () => {
  // 创建深色底面
  const floorGeometry = new THREE.PlaneGeometry(35, 25)
  const floorMaterial = new THREE.MeshPhongMaterial({ 
    color: 0x111111,
    transparent: true,
    opacity: 0.8
  })
  const floor = new THREE.Mesh(floorGeometry, floorMaterial)
  floor.rotation.x = -Math.PI / 2
  floor.position.y = -0.1
  floor.receiveShadow = true
  scene.add(floor)

  // 创建明亮的网格线
  const createSoftGrid = () => {
    const gridMaterial = new THREE.LineBasicMaterial({ 
      color: 0x444444,
      transparent: true,
      opacity: 0.5
    })

    // 时间轴网格线 (X方向)
    for (let i = 0; i <= 24; i += 4) {
      const geometry = new THREE.BufferGeometry().setFromPoints([
        new THREE.Vector3((i - 12) * 1.2, 0, -6),
        new THREE.Vector3((i - 12) * 1.2, 0, 6)
      ])
      const line = new THREE.Line(geometry, gridMaterial)
      scene.add(line)
    }

    // 星期轴网格线 (Z方向)
    for (let i = 0; i <= 6; i++) {
      const geometry = new THREE.BufferGeometry().setFromPoints([
        new THREE.Vector3(-15, 0, (i - 3) * 1.5),
        new THREE.Vector3(15, 0, (i - 3) * 1.5)
      ])
      const line = new THREE.Line(geometry, gridMaterial)
      scene.add(line)
    }
  }

  createSoftGrid()

  // 添加装饰性的粒子效果
  const createParticles = () => {
    const particleCount = 50
    const positions = new Float32Array(particleCount * 3)
    
    for (let i = 0; i < particleCount; i++) {
      positions[i * 3] = (Math.random() - 0.5) * 40     // x
      positions[i * 3 + 1] = Math.random() * 20 + 10    // y
      positions[i * 3 + 2] = (Math.random() - 0.5) * 30 // z
    }

    const particleGeometry = new THREE.BufferGeometry()
    particleGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))

    const particleMaterial = new THREE.PointsMaterial({
      color: 0xffffff,
      size: 0.3,
      transparent: true,
      opacity: 0.4,
      sizeAttenuation: true
    })

    const particles = new THREE.Points(particleGeometry, particleMaterial)
    scene.add(particles)
  }

  createParticles()
}

// 添加鼠标控制
const addControls = () => {
  let mouseX = 0, mouseY = 0
  let isMouseDown = false

  const onMouseMove = (event) => {
    if (!heatmapContainer.value || !renderer) return
    
    const canvas = renderer.domElement
    const rect = canvas.getBoundingClientRect()
    
    // 更新鼠标位置（归一化设备坐标）
    mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1
    
    // 检测悬浮对象
    if (!isMouseDown) {
      checkHover()
    }
    
    if (!isMouseDown) return
    
    const deltaX = event.clientX - mouseX
    const deltaY = event.clientY - mouseY
    
    // 旋转相机
    const spherical = new THREE.Spherical()
    spherical.setFromVector3(camera.position)
    spherical.theta -= deltaX * 0.01
    spherical.phi += deltaY * 0.01
    spherical.phi = Math.max(0.1, Math.min(Math.PI - 0.1, spherical.phi))
    
    camera.position.setFromSpherical(spherical)
    camera.lookAt(0, 0, 0)
    
    mouseX = event.clientX
    mouseY = event.clientY
  }

  const onMouseDown = (event) => {
    isMouseDown = true
    mouseX = event.clientX
    mouseY = event.clientY
  }

  const onMouseUp = () => {
    isMouseDown = false
  }

  const onWheel = (event) => {
    // 缩放控制
    const scale = event.deltaY > 0 ? 1.1 : 0.9
    camera.position.multiplyScalar(scale)
    
    // 限制缩放范围
    const distance = camera.position.length()
    if (distance < 12) {
      camera.position.setLength(12)
    } else if (distance > 60) {
      camera.position.setLength(60)
    }
  }

  if (heatmapContainer.value) {
    const canvas = renderer.domElement
    canvas.addEventListener('mousemove', onMouseMove)
    canvas.addEventListener('mousedown', onMouseDown)
    canvas.addEventListener('mouseup', onMouseUp)
    canvas.addEventListener('wheel', onWheel)
  }
}

// 检测悬浮对象
const checkHover = () => {
  if (!raycaster || !mouse || !camera || !scene) return
  
  // 更新射线投射器
  raycaster.setFromCamera(mouse, camera)
  
  // 获取所有可交互的对象（径向柱状图）
  const cylinders = []
  scene.traverse((child) => {
    if (child.isMesh && child.geometry.type === 'CylinderGeometry' && child.userData.isRadialBar && child.visible) {
      cylinders.push(child)
    }
  })
  
  // 检测相交对象
  const intersects = raycaster.intersectObjects(cylinders)
  
  if (intersects.length > 0) {
    const newHoveredObject = intersects[0].object
    
    // 如果悬浮对象发生变化
    if (hoveredObject.value !== newHoveredObject) {
      // 恢复之前悬浮对象的状态
      if (hoveredObject.value) {
        resetObjectState(hoveredObject.value)
      }
      
      // 设置新的悬浮对象
      hoveredObject.value = newHoveredObject
      highlightObject(newHoveredObject)
    }
  } else {
    // 没有悬浮对象
    if (hoveredObject.value) {
      resetObjectState(hoveredObject.value)
      hoveredObject.value = null
    }
  }
}

// 高亮对象
const highlightObject = (object) => {
  if (!object || !object.material) return
  
  const emotion = object.userData.dominantEmotion
  
  // 增强发光效果
  object.material.emissive = getEmotionColor(emotion, 0.4)
  
  // 轻微放大
  object.scale.set(1.15, 1.15, 1.15)
  
  // 增加透明度
  object.material.opacity = 1.0
}

// 重置对象状态
const resetObjectState = (object) => {
  if (!object || !object.material) return
  
  const emotion = object.userData.dominantEmotion
  
  // 恢复原始发光
  object.material.emissive = getEmotionColor(emotion, 0.2)
  
  // 恢复原始大小
  object.scale.set(1, 1, 1)
  
  // 恢复原始透明度
  const isVisible = emotionVisibility.value[emotion]
  object.material.opacity = isVisible ? 0.85 : 0
}

// 动画循环
const animate = () => {
  animationId = requestAnimationFrame(animate)
  
  // 温柔的呼吸动画
  const time = Date.now() * 0.001
  
  if (scene) {
    // 轻微的场景摆动
    scene.rotation.y = Math.sin(time * 0.1) * 0.02
    
    // 让径向柱状图有轻微的呼吸效果
    scene.traverse((child) => {
      if (child.isMesh && child.geometry.type === 'CylinderGeometry' && child.userData.isRadialBar) {
        // 非常轻微的呼吸效果，只在Y轴方向微调长度
        const baseScale = 1.0
        const breathingOffset = Math.sin(time * 0.5 + child.position.x * 0.1 + child.position.z * 0.1) * 0.02
        child.scale.set(baseScale, baseScale + breathingOffset, baseScale)
      }
    })
    
    // 粒子漂浮动画
    scene.traverse((child) => {
      if (child.isPoints) {
        child.rotation.y = time * 0.05
        const positions = child.geometry.attributes.position.array
        for (let i = 1; i < positions.length; i += 3) {
          positions[i] += Math.sin(time + positions[i * 3]) * 0.01
        }
        child.geometry.attributes.position.needsUpdate = true
      }
    })
  }
  
  if (renderer && scene && camera) {
    renderer.render(scene, camera)
  }
}

// 清理资源
const cleanup = () => {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  
  if (renderer) {
    if (heatmapContainer.value && renderer.domElement.parentNode === heatmapContainer.value) {
      heatmapContainer.value.removeChild(renderer.domElement)
    }
    renderer.dispose()
    renderer = null
  }
  
  if (scene) {
    // 清理场景中的所有对象
    while (scene.children.length > 0) {
      const object = scene.children[0]
      if (object.geometry) object.geometry.dispose()
      if (object.material) {
        if (Array.isArray(object.material)) {
          object.material.forEach(material => material.dispose())
        } else {
          object.material.dispose()
        }
      }
      scene.remove(object)
    }
    scene = null
  }
  
  camera = null
}

// 响应式处理
const handleResize = () => {
  if (!heatmapContainer.value || !renderer || !camera) return
  
  const width = heatmapContainer.value.clientWidth
  const height = heatmapContainer.value.clientHeight
  
  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
}

// 生命周期
onMounted(() => {
  // 初始化情绪颜色映射和可见性状态
  initEmotionColorMap()
  initEmotionVisibility()
  init3DHeatmap()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  cleanup()
  window.removeEventListener('resize', handleResize)
})

// 监听数据变化
watch(() => props.heatmapData, () => {
  initEmotionColorMap()
  initEmotionVisibility()
  init3DHeatmap()
}, { deep: true })
</script>

<style lang="scss" scoped>
.emotion-heatmap-container {
  background: linear-gradient(145deg, #1a1a1a 0%, #2d2d2d 100%);
  border-radius: 20px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 2px 8px rgba(255, 255, 255, 0.05);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.heatmap-header {
  padding: 30px;
  background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 30%, #fd79a8 100%);
  color: #2d3436;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="%23ffffff" opacity="0.1"/><circle cx="75" cy="75" r="1" fill="%23ffffff" opacity="0.1"/><circle cx="50" cy="10" r="0.5" fill="%23ffffff" opacity="0.15"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
    opacity: 0.3;
    pointer-events: none;
  }
  
  .heatmap-title {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 700;
    display: flex;
    align-items: center;
    gap: 12px;
    position: relative;
    z-index: 1;
    
    i {
      font-size: 28px;
      color: #e84393;
      animation: heartbeat 2s ease-in-out infinite;
    }
  }
  
  .heatmap-subtitle {
    font-size: 14px;
    opacity: 0.8;
    margin-bottom: 20px;
    font-style: italic;
    position: relative;
    z-index: 1;
  }
  
  .heatmap-info {
    display: flex;
    gap: 24px;
    position: relative;
    z-index: 1;
    
    .info-item {
      display: flex;
      align-items: center;
      gap: 8px;
      background: rgba(255, 255, 255, 0.25);
      padding: 8px 16px;
      border-radius: 25px;
      font-size: 13px;
      font-weight: 500;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.3);
      
      i {
        color: #e84393;
      }
    }
  }
}

.heatmap-content {
  padding: 30px;
  background: rgba(0, 0, 0, 0.2);
}

.heatmap-3d {
  width: 100%;
  height: 500px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 
    inset 0 2px 10px rgba(0, 0, 0, 0.3),
    0 4px 20px rgba(0, 0, 0, 0.5);
  background: linear-gradient(145deg, #000000, #111111);
}


.emotion-distribution {
  padding: 0 30px 30px;
  background: rgba(0, 0, 0, 0.1);
  
  .distribution-title {
    margin: 0 0 25px 0;
    color: #ffffff;
    font-size: 18px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 10px;
    
    i {
      color: #e84393;
      font-size: 20px;
    }
  }
  
  .emotion-garden {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    justify-content: center;
  }
  
  .emotion-flower {
    position: relative;
    width: calc(60px * var(--flower-size));
    height: calc(60px * var(--flower-size));
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: scale(1.1);
      filter: brightness(1.1);
    }
    
    &.hidden {
      opacity: 0.3;
      filter: grayscale(70%);
      
      .flower-petals .petal {
        background: linear-gradient(135deg, #ddd, #bbb) !important;
      }
      
      .flower-center {
        background: radial-gradient(circle, #f5f5f5, #ddd) !important;
      }
    }
    
    &.highlighted {
      transform: scale(1.2);
      filter: brightness(1.3) drop-shadow(0 0 20px var(--emotion-color));
      z-index: 10;
      
      .flower-petals .petal {
        background: linear-gradient(135deg, var(--emotion-color), color-mix(in srgb, var(--emotion-color) 80%, #fff)) !important;
        filter: drop-shadow(0 0 15px var(--emotion-color));
      }
    }
    
    .visibility-indicator {
      position: absolute;
      top: -8px;
      right: -8px;
      background: rgba(255, 255, 255, 0.9);
      border-radius: 50%;
      width: 20px;
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 10px;
      color: #666;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transition: all 0.3s ease;
      
      i {
        transition: color 0.3s ease;
      }
    }
    
    &:hover .visibility-indicator {
      background: rgba(255, 255, 255, 1);
      transform: scale(1.1);
      
      i {
        color: #333;
      }
    }
    
    &.hidden .visibility-indicator i {
      color: #999;
    }
    
    .flower-petals {
      position: absolute;
      width: 100%;
      height: 100%;
      animation: gentleRotate 10s linear infinite;
      
      .petal {
        position: absolute;
        width: 30%;
        height: 30%;
        background: linear-gradient(135deg, var(--emotion-color), color-mix(in srgb, var(--emotion-color) 70%, #fff));
        border-radius: 50% 50% 50% 0;
        transform-origin: 85% 85%;
        box-shadow: 
          0 2px 8px rgba(244, 162, 97, 0.3),
          inset 0 1px 3px rgba(255, 255, 255, 0.5);
        filter: drop-shadow(0 0 calc(10px * var(--flower-glow)) var(--emotion-color));
        transition: all 0.3s ease;
        
        @for $i from 1 through 6 {
          &:nth-child(#{$i}) {
            transform: rotate(#{($i - 1) * 60deg});
          }
        }
      }
    }
    
    .flower-center {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: radial-gradient(circle, #fff, #ffeaa7);
      border-radius: 50%;
      width: 50%;
      height: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      box-shadow: 
        0 2px 8px rgba(0, 0, 0, 0.1),
        inset 0 1px 3px rgba(255, 255, 255, 0.8);
      
      .emotion-name {
        font-size: 10px;
        font-weight: 600;
        color: #2d3436;
        text-align: center;
        line-height: 1;
      }
      
      .emotion-count {
        font-size: 8px;
        color: #666666;
        margin-top: 2px;
      }
    }
  }
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

@keyframes gentleRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 响应式设计
@media (max-width: 768px) {
  .heatmap-header {
    padding: 20px;
    
    .heatmap-info {
      flex-direction: column;
      gap: 12px;
    }
  }
  
  .heatmap-content {
    padding: 20px;
  }
  
  .heatmap-3d {
    height: 350px;
  }
  
  .emotion-garden {
    gap: 15px;
  }
}
</style>
