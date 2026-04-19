<template>
  <div class="dashboard-page">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-glow welcome-glow-left" />
      <div class="welcome-glow welcome-glow-right" />
      <el-row :gutter="24" align="middle" class="welcome-row">
        <el-col :xs="24" :lg="15">
          <div class="welcome-main">
            <el-tag type="primary" effect="dark" round class="welcome-tag">后台首页</el-tag>

            <div class="identity-row">
              <el-avatar :size="68" class="welcome-avatar">
                <el-icon size="34"><UserFilled /></el-icon>
              </el-avatar>

              <div class="identity-copy">
                <div class="welcome-title">{{ greetingText }}，{{ displayName }}</div>
                <div class="welcome-subtitle">欢迎使用校园二手交易平台管理系统</div>
                <div class="welcome-meta">
                  <el-icon><Clock /></el-icon>
                  <span>{{ currentTimeText }}</span>
                </div>
              </div>
            </div>

            <el-space wrap class="welcome-actions">
              <el-tag type="success" effect="plain" round>管理后台</el-tag>
              <el-tag type="info" effect="plain" round>实时值守</el-tag>
              <el-tag type="warning" effect="plain" round>校园交易平台</el-tag>
            </el-space>
          </div>
        </el-col>
        <el-col :xs="24" :lg="9">
          <div class="visual-panel">
            <div class="visual-grid" />
            <div class="visual-orb visual-orb-lg" />
            <div class="visual-orb visual-orb-sm" />
            <div class="visual-card visual-card-top">
              <el-icon size="22"><Monitor /></el-icon>
              <span>系统稳定运行</span>
            </div>
            <div class="visual-card visual-card-bottom">
              <el-icon size="22"><DataBoard /></el-icon>
              <span>模块持续建设中</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="placeholder-card">
      <el-empty description="数据统计功能开发中，敬请期待..." />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Clock, DataBoard, Monitor, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const currentTime = ref(new Date())

let timer = null

const displayName = computed(() => {
  const storeName = authStore.user.name || authStore.user.username
  const localName = window.localStorage.getItem('adminName') || ''
  return storeName || localName || '管理员'
})

const greetingText = computed(() => {
  const hour = currentTime.value.getHours()
  if (hour < 12) return '上午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentTimeText = computed(() =>
  currentTime.value.toLocaleString('zh-CN', {
    hour12: false,
  }),
)

onMounted(() => {
  timer = window.setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
})

onBeforeUnmount(() => {
  if (timer) {
    window.clearInterval(timer)
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-card {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.72);
  background:
    radial-gradient(circle at left top, rgba(82, 160, 255, 0.2), transparent 34%),
    radial-gradient(circle at right center, rgba(53, 125, 255, 0.16), transparent 32%),
    linear-gradient(135deg, #f6fbff 0%, #edf5ff 38%, #ffffff 100%);
  box-shadow: 0 18px 40px rgba(26, 84, 160, 0.08);
}

.welcome-row {
  position: relative;
  z-index: 1;
}

.welcome-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 8px 4px;
}

.welcome-tag {
  width: fit-content;
}

.identity-row {
  display: flex;
  align-items: center;
  gap: 18px;
}

.welcome-avatar {
  flex-shrink: 0;
  color: #ffffff;
  background: linear-gradient(135deg, #2f7df6 0%, #6ca8ff 100%);
  box-shadow: 0 14px 30px rgba(47, 125, 246, 0.28);
}

.identity-copy {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.welcome-title {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.3;
  color: #303133;
}

.welcome-subtitle {
  font-size: 16px;
  color: #909399;
}

.welcome-meta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #7f8694;
  font-size: 14px;
}

.welcome-actions {
  row-gap: 10px;
}

.visual-panel {
  position: relative;
  min-height: 280px;
  border-radius: 28px;
  overflow: hidden;
  background:
    linear-gradient(160deg, rgba(255, 255, 255, 0.88) 0%, rgba(233, 244, 255, 0.94) 100%);
  border: 1px solid rgba(115, 164, 255, 0.22);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.visual-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(96, 151, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(96, 151, 255, 0.1) 1px, transparent 1px);
  background-size: 24px 24px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.88), transparent 92%);
}

.visual-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(2px);
}

.visual-orb-lg {
  width: 168px;
  height: 168px;
  top: 26px;
  right: 34px;
  background: radial-gradient(circle at 30% 30%, #ffffff 0%, #90bbff 42%, rgba(144, 187, 255, 0.18) 74%, transparent 100%);
}

.visual-orb-sm {
  width: 82px;
  height: 82px;
  bottom: 34px;
  left: 40px;
  background: radial-gradient(circle at 30% 30%, #ffffff 0%, #bfd8ff 46%, rgba(191, 216, 255, 0.16) 76%, transparent 100%);
}

.visual-card {
  position: absolute;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  border-radius: 18px;
  color: #31527d;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(142, 183, 255, 0.2);
  box-shadow: 0 16px 24px rgba(71, 114, 176, 0.08);
  backdrop-filter: blur(10px);
}

.visual-card-top {
  top: 36px;
  left: 28px;
}

.visual-card-bottom {
  right: 24px;
  bottom: 34px;
}

.welcome-glow {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.welcome-glow-left {
  width: 220px;
  height: 220px;
  left: -90px;
  top: -100px;
  background: radial-gradient(circle, rgba(91, 157, 255, 0.18) 0%, rgba(91, 157, 255, 0) 72%);
}

.welcome-glow-right {
  width: 260px;
  height: 260px;
  right: -100px;
  bottom: -120px;
  background: radial-gradient(circle, rgba(170, 208, 255, 0.2) 0%, rgba(170, 208, 255, 0) 70%);
}

.placeholder-card {
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}

@media (max-width: 768px) {
  .welcome-title {
    font-size: 24px;
  }

  .identity-row {
    align-items: flex-start;
  }

  .visual-panel {
    min-height: 220px;
    margin-top: 6px;
  }

  .visual-card {
    padding: 12px 14px;
    font-size: 13px;
  }
}

@media (max-width: 560px) {
  .identity-row {
    flex-direction: column;
  }

  .welcome-main {
    gap: 18px;
  }

  .welcome-title {
    font-size: 22px;
  }
}
</style>
