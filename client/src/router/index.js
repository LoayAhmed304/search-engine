import { createRouter, createWebHistory } from "vue-router";

import HomeView from "@/views/HomeView.vue";
import ResultsView from "@/views/ResultsView.vue";
import NotFoundView from "@/views/NotFoundView.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "/",
            name: "home",
            component: HomeView
        },
        {
            path: "/search",
            name: "search",
            component: ResultsView
        },
        {
            path: "/:catchAll(.*)",
            name: "not-found",
            component: NotFoundView
        },
    ],
});

export default router;
