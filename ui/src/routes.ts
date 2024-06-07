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
  history: createWebHistory(),
  routes
});

export default router;
