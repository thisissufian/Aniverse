import axios from 'axios';
axios.get('https://anitaku.pe/search.html?keyword=My%20Hero%20Academia').then(res => console.log(res.status)).catch(e => console.log(e.message));
