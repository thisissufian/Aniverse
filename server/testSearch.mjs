import * as cheerio from 'cheerio';

async function testSearch(keyword) {
  const res = await fetch(`https://anikototv.to/filter?keyword=${encodeURIComponent(keyword)}`);
  const text = await res.text();
  const $ = cheerio.load(text);
  
  console.log(`\n=== Testing Selectors for "${keyword}" ===`);
  
  console.log('.film_list-wrap:', $('.film_list-wrap').length);
  console.log('.flw-item:', $('.flw-item').length);
  console.log('.film-name:', $('.film-name').length);
  console.log('a[href*="/watch/"]:', $('a[href*="/watch/"]').length);
  
  console.log('\n--- First 3 Watch Links ---');
  $('a[href*="/watch/"]').slice(0, 3).each((i, el) => {
     console.log('href:', $(el).attr('href'));
     console.log('text:', $(el).text().trim());
     console.log('parent class:', $(el).parent().attr('class'));
  });
}

testSearch('ONE PIECE');
