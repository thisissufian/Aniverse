import * as cheerio from 'cheerio';

const BASE = 'https://anikototv.to';
const HEADERS = { 'X-Requested-With': 'XMLHttpRequest', 'Referer': `${BASE}/watch/naruto-shippuden-c8gov/ep-1` };

// Get servers
const epRes = await fetch(`${BASE}/ajax/episode/list/1498`, { headers: HEADERS });
const epData = await epRes.json();
const $ep = cheerio.load(epData.result);
const ids = $ep('a[data-id]').first().attr('data-ids');
const srvRes = await fetch(`${BASE}/ajax/server/list?servers=${ids}`, { headers: HEADERS });
const srvData = await srvRes.json();
const $srv = cheerio.load(srvData.result);

// Get ALL servers with their data-link-id
const servers = [];
$srv('[data-link-id]').each((i, el) => {
  const type = $srv(el).closest('[data-type]').attr('data-type') || 'sub';
  servers.push({
    name: $srv(el).text().trim(),
    linkId: $srv(el).attr('data-link-id'),
    svId: $srv(el).attr('data-sv-id'),
    type,
  });
});

console.log(`Found ${servers.length} servers`);
servers.forEach(s => console.log(`  ${s.name} (${s.type}) svId=${s.svId} linkId=${s.linkId.substring(0, 30)}...`));

// Now try to get embed URL using data-link-id!
for (const server of servers.slice(0, 4)) {
  console.log(`\n=== Getting embed for ${server.name} (${server.type}) ===`);
  const embedRes = await fetch(`${BASE}/ajax/server?get=${server.linkId}`, { headers: HEADERS });
  const embedData = await embedRes.json();
  console.log('Response:', JSON.stringify(embedData).substring(0, 500));
}
