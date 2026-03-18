<template>
  <div class="animated-login">
    <section class="left-panel">
      <div class="panel-grid" />

      <header class="brand-row">
        <div class="brand-icon">✦</div>
        <div>
          <h1>校园二手交易平台管理端</h1>
          <p>Campus Trading Console</p>
        </div>
      </header>

      <div class="characters-stage">
        <div class="characters-scene">
          <div ref="purpleRef" class="char purple-char" :style="purpleStyle">
            <div class="eyes-row purple-eyes" :style="purpleEyesStyle">
              <div class="eye-ball small" :class="{ blinking: isPurpleBlinking }">
                <div
                  v-if="!isPurpleBlinking"
                  class="pupil"
                  :style="moveStyle(purplePupilShift, 7)"
                />
              </div>
              <div class="eye-ball small" :class="{ blinking: isPurpleBlinking }">
                <div
                  v-if="!isPurpleBlinking"
                  class="pupil"
                  :style="moveStyle(purplePupilShift, 7)"
                />
              </div>
            </div>
          </div>

          <div ref="blackRef" class="char black-char" :style="blackStyle">
            <div class="eyes-row black-eyes" :style="blackEyesStyle">
              <div class="eye-ball mini" :class="{ blinking: isBlackBlinking }">
                <div v-if="!isBlackBlinking" class="pupil" :style="moveStyle(blackPupilShift, 6)" />
              </div>
              <div class="eye-ball mini" :class="{ blinking: isBlackBlinking }">
                <div v-if="!isBlackBlinking" class="pupil" :style="moveStyle(blackPupilShift, 6)" />
              </div>
            </div>
          </div>

          <div ref="orangeRef" class="char orange-char" :style="orangeStyle">
            <div class="pupil-row orange-pupils" :style="orangeEyesStyle">
              <div class="pupil-only" :style="moveStyle(orangePupilShift, 12)" />
              <div class="pupil-only" :style="moveStyle(orangePupilShift, 12)" />
            </div>
          </div>

          <div ref="yellowRef" class="char yellow-char" :style="yellowStyle">
            <div class="pupil-row yellow-pupils" :style="yellowEyesStyle">
              <div class="pupil-only" :style="moveStyle(yellowPupilShift, 12)" />
              <div class="pupil-only" :style="moveStyle(yellowPupilShift, 12)" />
            </div>
            <div class="yellow-mouth" :style="yellowMouthStyle" />
          </div>
        </div>
      </div>

      <footer class="panel-links">
        <span>商品</span>
        <span>订单</span>
        <span>审核</span>
        <span>用户</span>
      </footer>
    </section>

    <section class="right-panel">
      <div class="mobile-brand">
        <div class="brand-icon">✦</div>
        <span>校园二手交易平台管理端</span>
      </div>

      <article class="login-card fade-in-up">
        <header class="login-head">
          <h2>Welcome back!</h2>
          <p>Please enter your details</p>
        </header>

        <form class="login-form" @submit.prevent="handleLogin">
          <label>
            <span>账号</span>
            <input
              v-model="form.username"
              class="app-input"
              type="text"
              placeholder="请输入管理员账号"
              autocomplete="username"
              @focus="handleFieldFocus"
              @blur="handleFieldBlur"
            />
          </label>

          <label>
            <span>密码</span>
            <div class="password-wrap">
              <input
                v-model="form.password"
                class="app-input"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                autocomplete="current-password"
                @focus="handleFieldFocus"
                @blur="handleFieldBlur"
              />
              <button class="password-toggle" type="button" @click="showPassword = !showPassword">
                <el-icon>
                  <Hide v-if="showPassword" />
                  <View v-else />
                </el-icon>
              </button>
            </div>
          </label>

          <p v-if="loginError" class="error-line">{{ loginError }}</p>

          <button class="login-btn" type="submit" :disabled="loading">
            {{ loading ? 'Signing in...' : 'Log in' }}
          </button>
        </form>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Hide, View } from '@element-plus/icons-vue'
import { adminLogin } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const loading = ref(false)
const loginError = ref('')
const showPassword = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const mouseX = ref(0)
const mouseY = ref(0)
const focusedCount = ref(0)

const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isTyping = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)

const purpleRef = ref(null)
const blackRef = ref(null)
const yellowRef = ref(null)
const orangeRef = ref(null)

const purpleBlinkTimer = ref(null)
const purpleBlinkRecoverTimer = ref(null)
const blackBlinkTimer = ref(null)
const blackBlinkRecoverTimer = ref(null)
const lookTimer = ref(null)
const peekTimer = ref(null)
const peekRecoverTimer = ref(null)

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value))
}

function randomBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function clearTimer(timerRef) {
  if (!timerRef.value) {
    return
  }
  clearTimeout(timerRef.value)
  timerRef.value = null
}

function normalize(value) {
  return typeof value === 'string' ? value.trim() : ''
}

function handleMouseMove(event) {
  mouseX.value = event.clientX
  mouseY.value = event.clientY
}

function calculateMotion(elementRef) {
  const element = elementRef.value

  if (!element) {
    return { faceX: 0, faceY: 0, bodySkew: 0 }
  }

  const rect = element.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3

  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY

  return {
    faceX: clamp(deltaX / 20, -15, 15),
    faceY: clamp(deltaY / 30, -10, 10),
    bodySkew: clamp(-deltaX / 120, -6, 6),
  }
}

const purplePos = computed(() => calculateMotion(purpleRef))
const blackPos = computed(() => calculateMotion(blackRef))
const yellowPos = computed(() => calculateMotion(yellowRef))
const orangePos = computed(() => calculateMotion(orangeRef))

const hasPassword = computed(() => form.password.length > 0)
const isPasswordVisible = computed(() => hasPassword.value && showPassword.value)
const isPasswordHidden = computed(() => hasPassword.value && !showPassword.value)

const purpleStyle = computed(() => {
  const baseSkew = purplePos.value.bodySkew

  if (isPasswordVisible.value) {
    return {
      transform: 'skewX(0deg)',
      height: '440px',
    }
  }

  if (isTyping.value || isPasswordHidden.value) {
    return {
      transform: `skewX(${baseSkew - 12}deg) translateX(40px)`,
      height: '440px',
    }
  }

  return {
    transform: `skewX(${baseSkew}deg)`,
    height: '400px',
  }
})

const blackStyle = computed(() => {
  const baseSkew = blackPos.value.bodySkew

  if (isPasswordVisible.value) {
    return {
      transform: 'skewX(0deg)',
    }
  }

  if (isLookingAtEachOther.value) {
    return {
      transform: `skewX(${baseSkew * 1.5 + 10}deg) translateX(20px)`,
    }
  }

  if (isTyping.value || isPasswordHidden.value) {
    return {
      transform: `skewX(${baseSkew * 1.5}deg)`,
    }
  }

  return {
    transform: `skewX(${baseSkew}deg)`,
  }
})

const orangeStyle = computed(() => ({
  transform: isPasswordVisible.value ? 'skewX(0deg)' : `skewX(${orangePos.value.bodySkew}deg)`,
}))

const yellowStyle = computed(() => ({
  transform: isPasswordVisible.value ? 'skewX(0deg)' : `skewX(${yellowPos.value.bodySkew}deg)`,
}))

const purpleEyesStyle = computed(() => {
  if (isPasswordVisible.value) {
    return {
      left: '20px',
      top: isPurplePeeking.value ? '42px' : '35px',
    }
  }

  if (isLookingAtEachOther.value) {
    return {
      left: '55px',
      top: '65px',
    }
  }

  return {
    left: `${45 + purplePos.value.faceX}px`,
    top: `${40 + purplePos.value.faceY}px`,
  }
})

const blackEyesStyle = computed(() => {
  if (isPasswordVisible.value) {
    return {
      left: '10px',
      top: '28px',
    }
  }

  if (isLookingAtEachOther.value) {
    return {
      left: '32px',
      top: '12px',
    }
  }

  return {
    left: `${26 + blackPos.value.faceX}px`,
    top: `${32 + blackPos.value.faceY}px`,
  }
})

const orangeEyesStyle = computed(() => ({
  left: isPasswordVisible.value ? '50px' : `${82 + orangePos.value.faceX}px`,
  top: isPasswordVisible.value ? '85px' : `${90 + orangePos.value.faceY}px`,
}))

const yellowEyesStyle = computed(() => ({
  left: isPasswordVisible.value ? '20px' : `${52 + yellowPos.value.faceX}px`,
  top: isPasswordVisible.value ? '35px' : `${40 + yellowPos.value.faceY}px`,
}))

const yellowMouthStyle = computed(() => ({
  left: isPasswordVisible.value ? '10px' : `${40 + yellowPos.value.faceX}px`,
  top: isPasswordVisible.value ? '88px' : `${88 + yellowPos.value.faceY}px`,
}))

const purplePupilShift = computed(() => {
  if (isPasswordVisible.value) {
    return {
      x: isPurplePeeking.value ? 4 : -4,
      y: isPurplePeeking.value ? 5 : -4,
    }
  }

  if (isLookingAtEachOther.value) {
    return { x: 3, y: 4 }
  }

  return {
    x: clamp(purplePos.value.faceX * 0.35, -5, 5),
    y: clamp(purplePos.value.faceY * 0.4, -5, 5),
  }
})

const blackPupilShift = computed(() => {
  if (isPasswordVisible.value) {
    return { x: -4, y: -4 }
  }

  if (isLookingAtEachOther.value) {
    return { x: 0, y: -4 }
  }

  return {
    x: clamp(blackPos.value.faceX * 0.4, -4, 4),
    y: clamp(blackPos.value.faceY * 0.4, -4, 4),
  }
})

const orangePupilShift = computed(() => {
  if (isPasswordVisible.value) {
    return { x: -5, y: -4 }
  }

  return {
    x: clamp(orangePos.value.faceX * 0.35, -5, 5),
    y: clamp(orangePos.value.faceY * 0.35, -5, 5),
  }
})

const yellowPupilShift = computed(() => {
  if (isPasswordVisible.value) {
    return { x: -5, y: -4 }
  }

  return {
    x: clamp(yellowPos.value.faceX * 0.35, -5, 5),
    y: clamp(yellowPos.value.faceY * 0.35, -5, 5),
  }
})

function moveStyle(offset, size) {
  return {
    width: `${size}px`,
    height: `${size}px`,
    transform: `translate(${offset.x}px, ${offset.y}px)`,
  }
}

function startPurpleBlinkLoop() {
  clearTimer(purpleBlinkTimer)
  purpleBlinkTimer.value = setTimeout(
    () => {
      isPurpleBlinking.value = true

      clearTimer(purpleBlinkRecoverTimer)
      purpleBlinkRecoverTimer.value = setTimeout(() => {
        isPurpleBlinking.value = false
        startPurpleBlinkLoop()
      }, 150)
    },
    randomBetween(3000, 7000),
  )
}

function startBlackBlinkLoop() {
  clearTimer(blackBlinkTimer)
  blackBlinkTimer.value = setTimeout(
    () => {
      isBlackBlinking.value = true

      clearTimer(blackBlinkRecoverTimer)
      blackBlinkRecoverTimer.value = setTimeout(() => {
        isBlackBlinking.value = false
        startBlackBlinkLoop()
      }, 150)
    },
    randomBetween(3000, 7000),
  )
}

function schedulePurplePeek() {
  clearTimer(peekTimer)

  if (!isPasswordVisible.value) {
    return
  }

  peekTimer.value = setTimeout(
    () => {
      isPurplePeeking.value = true

      clearTimer(peekRecoverTimer)
      peekRecoverTimer.value = setTimeout(() => {
        isPurplePeeking.value = false
        schedulePurplePeek()
      }, 800)
    },
    randomBetween(2000, 5000),
  )
}

function handleFieldFocus() {
  focusedCount.value += 1
  isTyping.value = true
}

function handleFieldBlur() {
  focusedCount.value = Math.max(0, focusedCount.value - 1)
  isTyping.value = focusedCount.value > 0
}

async function handleLogin() {
  const username = normalize(form.username)
  const password = normalize(form.password)

  loginError.value = ''

  if (!username || !password) {
    loginError.value = '账号和密码不能为空'
    ElMessage.warning(loginError.value)
    return
  }

  loading.value = true

  try {
    const loginResult = await adminLogin({ username, password })

    if (!loginResult?.token) {
      throw new Error('登录成功但未返回 token，请检查后端响应')
    }

    authStore.setLoginInfo(loginResult)
    ElMessage.success('登录成功')

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    router.replace(redirect)
  } catch (error) {
    const message = error.message || '登录失败，请重试'
    loginError.value = message
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

watch(isTyping, (typing) => {
  clearTimer(lookTimer)

  if (!typing) {
    isLookingAtEachOther.value = false
    return
  }

  isLookingAtEachOther.value = true
  lookTimer.value = setTimeout(() => {
    isLookingAtEachOther.value = false
  }, 800)
})

watch(isPasswordVisible, (visible) => {
  clearTimer(peekTimer)
  clearTimer(peekRecoverTimer)
  isPurplePeeking.value = false

  if (visible) {
    schedulePurplePeek()
  }
})

onMounted(() => {
  mouseX.value = window.innerWidth / 2
  mouseY.value = window.innerHeight / 2

  window.addEventListener('mousemove', handleMouseMove)
  startPurpleBlinkLoop()
  startBlackBlinkLoop()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)

  clearTimer(purpleBlinkTimer)
  clearTimer(purpleBlinkRecoverTimer)
  clearTimer(blackBlinkTimer)
  clearTimer(blackBlinkRecoverTimer)
  clearTimer(lookTimer)
  clearTimer(peekTimer)
  clearTimer(peekRecoverTimer)
})
</script>

<style scoped>
.animated-login {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(560px, 1fr) minmax(520px, 0.92fr);
  background: #ffffff;
}

.left-panel {
  position: relative;
  overflow: hidden;
  padding: 40px 42px 34px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: #ffea00;
  color: #3d2d0f;
}

.panel-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(138, 105, 27, 0.16) 1px, transparent 1px),
    linear-gradient(90deg, rgba(138, 105, 27, 0.16) 1px, transparent 1px);
  background-size: 20px 20px;
  opacity: 0.12;
  pointer-events: none;
}

.brand-row {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #67481a;
  font-size: 21px;
  font-weight: 700;
  background: rgba(255, 254, 239, 0.74);
  border: 1px solid rgba(255, 245, 205, 0.92);
  box-shadow: 0 10px 22px rgba(124, 87, 30, 0.2);
}

.brand-row h1 {
  margin: 0;
  font-size: 29px;
  line-height: 1.2;
  font-weight: 800;
  letter-spacing: 0.01em;
  color: #3b2a0d;
}

.brand-row p {
  margin: 2px 0 0;
  opacity: 0.82;
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6d531f;
}

.characters-stage {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 510px;
}

.characters-scene {
  position: relative;
  width: min(560px, 100%);
  height: 430px;
}

.char {
  position: absolute;
  bottom: 0;
  transform-origin: bottom center;
  transition:
    transform 0.7s ease,
    height 0.7s ease;
}

.purple-char {
  left: 70px;
  width: 180px;
  height: 400px;
  border-radius: 10px 10px 0 0;
  background: #f4c744;
  z-index: 1;
}

.black-char {
  left: 240px;
  width: 120px;
  height: 310px;
  border-radius: 8px 8px 0 0;
  background: #7a6024;
  z-index: 2;
}

.orange-char {
  left: 0;
  width: 240px;
  height: 200px;
  border-radius: 120px 120px 0 0;
  background: #f8b357;
  z-index: 3;
}

.yellow-char {
  left: 310px;
  width: 140px;
  height: 230px;
  border-radius: 70px 70px 0 0;
  background: #f5e17f;
  z-index: 4;
}

.eyes-row,
.pupil-row {
  position: absolute;
  display: flex;
  transition:
    left 0.2s ease,
    top 0.2s ease;
}

.purple-eyes {
  gap: 8px;
}

.black-eyes {
  gap: 6px;
}

.orange-pupils {
  gap: 8px;
}

.yellow-pupils {
  gap: 6px;
}

.eye-ball {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  overflow: hidden;
  transition: height 0.15s ease;
}

.eye-ball.small {
  width: 18px;
  height: 18px;
}

.eye-ball.mini {
  width: 16px;
  height: 16px;
}

.eye-ball.blinking {
  height: 2px !important;
}

.pupil {
  border-radius: 50%;
  background: #2d2d2d;
  transition: transform 0.1s ease-out;
}

.pupil-only {
  border-radius: 50%;
  background: #2d2d2d;
  transition: transform 0.1s ease-out;
}

.yellow-mouth {
  position: absolute;
  width: 80px;
  height: 4px;
  border-radius: 999px;
  background: #2d2d2d;
  transition:
    left 0.2s ease,
    top 0.2s ease;
}

.panel-links {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 20px;
  font-size: 14px;
  color: #5a4318;
  opacity: 0.92;
}

.right-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px;
  background: #ffffff;
}

.mobile-brand {
  display: none;
}

.login-card {
  width: min(430px, 100%);
  border-radius: 24px;
  padding: 34px 30px;
  border: 1px solid #e5e9f1;
  background: #ffffff;
  box-shadow: 0 18px 32px rgba(24, 38, 63, 0.1);
}

.login-head {
  text-align: center;
  margin-bottom: 26px;
}

.login-head h2 {
  margin: 0;
  font-size: 36px;
  line-height: 1.1;
  color: #2b3850;
}

.login-head p {
  margin: 8px 0 0;
  color: #7a8599;
  font-size: 14px;
}

.login-form {
  display: grid;
  gap: 14px;
}

.login-form label {
  display: grid;
  gap: 8px;
}

.login-form span {
  color: #4c5f82;
  font-size: 13px;
  font-weight: 600;
}

.animated-login .app-input {
  border-color: #d9e1ef;
  background: #ffffff;
}

.animated-login .app-input:focus {
  border-color: #f0cd3f;
  box-shadow: 0 0 0 3px rgba(249, 231, 77, 0.28);
}

.password-wrap {
  position: relative;
}

.password-wrap .app-input {
  padding-right: 44px;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 10px;
  width: 28px;
  height: 28px;
  transform: translateY(-50%);
  border: 0;
  background: transparent;
  color: #6c7a93;
  border-radius: 8px;
  cursor: pointer;
  display: grid;
  place-items: center;
}

.password-toggle:hover {
  background: #f4f7fc;
}

.error-line {
  margin: 0;
  color: #d94f5a;
  background: #fff0f2;
  border: 1px solid #ffd4d9;
  border-radius: 10px;
  padding: 8px 10px;
  font-size: 13px;
}

.login-btn {
  border: 0;
  border-radius: 12px;
  height: 46px;
  font-size: 15px;
  font-weight: 700;
  color: #493611;
  background: #ffea00;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.login-btn:hover {
  transform: translateY(-1px);
  background: #f3df36;
  box-shadow: 0 10px 18px rgba(193, 144, 41, 0.24);
}

.login-btn:disabled {
  opacity: 0.65;
  transform: none;
  cursor: not-allowed;
  box-shadow: none;
}

@media (max-width: 1180px) {
  .animated-login {
    grid-template-columns: 1fr;
  }

  .left-panel {
    display: none;
  }

  .mobile-brand {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;
    color: #62471a;
    font-weight: 700;
  }
}

@media (max-width: 640px) {
  .right-panel {
    padding: 18px;
  }

  .login-card {
    padding: 26px 18px;
    border-radius: 18px;
  }

  .login-head h2 {
    font-size: 30px;
  }
}
</style>
