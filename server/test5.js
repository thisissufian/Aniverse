import axios from 'axios';
import * as cheerio from 'cheerio';

axios.get('https://anitaku.pe/search.html?keyword=My%20Hero%20Academia').then(res => {
    console.log(res.data.substring(0, 1000));
}).catch(console.error);
