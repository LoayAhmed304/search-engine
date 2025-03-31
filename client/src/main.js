import './assets/styles/main.css'
import router from './router'

import { createApp } from 'vue'
import App from './App.vue'

import TheNavBar from '@/components/layout/TheNavBar.vue'
import TheFooter from '@/components/layout/TheFooter.vue'


import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { faMagnifyingGlass } from '@fortawesome/free-solid-svg-icons'
import { faPalette } from '@fortawesome/free-solid-svg-icons'

library.add(faMagnifyingGlass, faPalette);

const app = createApp(App);

// global components
app.component("TheNavBar", TheNavBar);
app.component("TheFooter", TheFooter);

app.use(router);
app.component('font-awesome-icon', FontAwesomeIcon);

app.mount('#app');
