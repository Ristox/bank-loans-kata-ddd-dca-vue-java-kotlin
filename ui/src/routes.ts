import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';

import Loans from './components/Loans.vue';

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    alias: "/loans",
    name: "loans",
    component: Loans,
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.VITE_BASE_URL),
  routes
});

export default router;
