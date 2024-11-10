<template>
  <nav class="navbar navbar-expand-sm navbar-dark bg-dark">
    <div class="container-fluid">
      <RouterLink to="/" class="navbar-brand font-weight-bold">BANK </RouterLink>
      <ul class="nav navbar-nav">
        <li class="nav-item">
          <RouterLink to="/loans" class="nav-link">Loans</RouterLink>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script setup lang="ts">
</script>

<style scoped>
div.container-fluid {
  padding-left: 5%;
  justify-content: left;
}
</style>
