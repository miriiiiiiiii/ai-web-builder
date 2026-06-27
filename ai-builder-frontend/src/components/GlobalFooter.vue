<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      多智能体项目 by
      <a href="https://www.codefather.cn" target="_blank" rel="noopener noreferrer">
        程序员小马
      </a>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts"></script>

<style scoped>
.footer {
  background: #f0f2f5;
  text-align: center;
  padding: 24px 50px;
  position: relative;
  bottom: 0;
  width: 100%;
  margin-top: auto;
}

.footer-content {
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.3s;
}

.footer-content a:hover {
  color: #40a9ff;
  text-decoration: underline;
}

@media (max-width: 576px) {
  .footer {
    padding: 16px 24px;
  }

  .footer-content {
    font-size: 12px;
  }
}
</style>
