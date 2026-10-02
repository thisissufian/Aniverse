import axios from 'axios';

axios.get('https://ajax.gogocdn.net/site/loadAjaxSearch?keyword=My%20Hero%20Academia').then(res => {
    console.log(res.data.substring(0, 500));
}).catch(console.error);
