<template>
  <a-layout class="basic-layout">
    <!--顶部导航栏-->
    <GlobalHeader />
    <!--主要内容区域-->
    <a-layout-content class="content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </a-layout-content>
    <!--底部导航栏-->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.content {
  margin-top: 64px;
  flex: 1;
  background: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.content-wrapper {
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
  flex: 1;
}

@media (max-width: 768px) {
  .content-wrapper {
    padding: 16px;
  }
}

@media (max-width: 576px) {
  .content-wrapper {
    padding: 12px;
  }
}
</style>
