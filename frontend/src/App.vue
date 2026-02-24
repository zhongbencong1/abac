<template>
  <el-container class="layout">
    <el-aside width="200px" class="aside">
      <div class="logo">ABAC 鉴权</div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/">概览</el-menu-item>
        <el-menu-item index="/users">用户管理</el-menu-item>
        <el-menu-item index="/attributes">属性管理</el-menu-item>
        <el-menu-item index="/policies">策略配置</el-menu-item>
        <el-menu-item index="/authz">鉴权校验</el-menu-item>
        <el-menu-item index="/audit">审计日志</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app { height: 100%; }
.layout { height: 100%; }
.aside { background: #304156; }
.logo { height: 50px; line-height: 50px; text-align: center; color: #fff; font-weight: bold; }
.main { background: #f0f2f5; padding: 20px; overflow: auto; }
</style>
