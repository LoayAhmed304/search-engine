import './assets/styles/main.css'
import router from './router'

import { createApp } from 'vue'
import App from './App.vue'

import TheNavBar from '@/components/layout/TheNavBar.vue'
import TheFooter from '@/components/layout/TheFooter.vue'

const app = createApp(App);

// global components
app.component("TheNavBar", TheNavBar);
app.component("TheFooter", TheFooter);

app.use(router);
app.mount('#app');
