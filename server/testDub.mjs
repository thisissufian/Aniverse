import * as cheerio from 'cheerio';

async function test() {
  const res = await fetch('https://anikototv.to/ajax/server/list?servers=WXlrUVJpZ2E3aGJBMzRxUFFlN29BSzZiTVBHUkxPa043VDk3WTVaUm9TYnI4MHB3UjA2Q3NLblA0T3hldWFJdmJRT21XS3VER0ZEU0FZZjBEMVJCczYxUmdFbHJwaXJIK2Z1WnFpTWh4bG0ySTY5S3hON2YrMWhCQ2pzWTNReWp3RFQrUGNGMXVqaitMT0xFYWp1aGV3PT0', {
    headers: { 'X-Requested-With': 'XMLHttpRequest' }
  });
  const data = await res.json();
  const $ = cheerio.load(data.result);
  
  const dubId = $('li[data-link-id]').filter((i, el) => $(el).closest('div[data-type]').attr('data-type') === 'dub').first().attr('data-link-id');
  const subId = $('li[data-link-id]').filter((i, el) => $(el).closest('div[data-type]').attr('data-type') === 'sub').first().attr('data-link-id');
  
  console.log('Dub ID:', dubId);
  console.log('Sub ID:', subId);
  
  const dubRes = await fetch('https://anikototv.to/ajax/server?get=' + dubId, {
    headers: { 'X-Requested-With': 'XMLHttpRequest' }
  });
  console.log('Dub Embed URL:', (await dubRes.json()).result?.url);
  
  const subRes = await fetch('https://anikototv.to/ajax/server?get=' + subId, {
    headers: { 'X-Requested-With': 'XMLHttpRequest' }
  });
  console.log('Sub Embed URL:', (await subRes.json()).result?.url);
}
test();
