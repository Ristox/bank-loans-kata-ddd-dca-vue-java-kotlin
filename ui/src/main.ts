import {createApp} from 'vue'
import App from './App.vue'
import routes from './routes';
import './scss/styles.scss'

createApp(App)
  .use(routes)
  .mount('#app')
