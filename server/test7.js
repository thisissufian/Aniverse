import axios from 'axios';
axios.get('https://api.anify.tv/info/180136').then(res => {
    console.log(Object.keys(res.data));
}).catch(e => console.log(e.message));
