<template>
    <nav class="navbar navbar-expand-sm navbar-dark bg-dark">
      <div class="container-fluid">
        <router-link to="/" class="navbar-brand font-weight-bold">BANK</router-link>
        <ul class="nav navbar-nav mr-auto">
          <li class="nav-item">
            <router-link to="/loans" class="nav-link">Loans</router-link>
          </li>
        </ul>
      </div>
    </nav>

    <div class="container">
      <router-view />
    </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "App",
});
</script>
