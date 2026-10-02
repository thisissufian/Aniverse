import * as cheerio from 'cheerio';

async function test() {
  const dataId = 769; // one-piece-episode-of-luffy-hand-island-adventure-br7lf
  const epRes = await fetch(`https://anikototv.to/ajax/episode/list/${dataId}`, {
     headers: { 'X-Requested-With': 'XMLHttpRequest', 'Referer': 'https://anikototv.to/' }
  });
  const epData = await epRes.json();
  const $ep = cheerio.load(epData.result);
  
  const eps = [];
  $ep('a.ep-item').each((i, el) => {
    eps.push($ep(el).attr('data-id'));
  });
  
  console.log('a.ep-item count:', eps.length);
  
  const allLinks = [];
  $ep('a').each((i, el) => {
    allLinks.push($ep(el).attr('class'));
  });
  console.log('all a tags classes:', allLinks);
}
test();
