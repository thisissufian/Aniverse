import * as cheerio from 'cheerio';

async function test() {
  const res = await fetch('https://anikototv.to/ajax/server/list?servers=WXlrUVJpZ2E3aGJBMzRxUFFlN29BSzZiTVBHUkxPa043VDk3WTVaUm9TYnI4MHB3UjA2Q3NLblA0T3hldWFJdmJRT21XS3VER0ZEU0FZZjBEMVJCczYxUmdFbHJwaXJIK2Z1WnFpTWh4bG0ySTY5S3hON2YrMWhCQ2pzWTNReWp3RFQrUGNGMXVqaitMT0xFYWp1aGV3PT0', {
    headers: { 'X-Requested-With': 'XMLHttpRequest' }
  });
  const data = await res.json();
  const $ = cheerio.load(data.result);
  
  let dubId = null;
  $('li[data-link-id]').each((i, el) => {
    const $el = $(el);
    const divType = $el.closest('div[data-type]').attr('data-type');
    if (divType === 'dub' && !dubId) {
       dubId = $el.attr('data-link-id');
    }
  });
  
  console.log('Dub ID:', dubId);
  const embedRes = await fetch('https://anikototv.to/ajax/server?get=' + dubId, {
    headers: { 'X-Requested-With': 'XMLHttpRequest' }
  });
  const embedData = await embedRes.json();
  console.log('Embed URL:', embedData.result.url);
}
test();
