import gogoanime from 'gogoanime-api-new';

gogoanime.search('My Hero Academia').then(res => {
    console.log(res);
}).catch(console.error);
