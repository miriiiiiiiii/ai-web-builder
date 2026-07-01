<script setup lang="ts">
import { useLoginUserStore } from './stores/loginUser'

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

</script>

<template>
  <router-view />
</template>

<style>

</style>
