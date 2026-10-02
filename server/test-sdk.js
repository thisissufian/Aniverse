import { HttpClient, FetchTransport, GogoanimeProvider, MegaPlayProvider, AnikotoProvider, AnimeParadiseProvider, AllmangaProvider } from 'anime-sdk';

async function withTimeout(promise, ms, label) {
  const timer = new Promise((_, rej) => setTimeout(() => rej(new Error(`${label} timed out after ${ms}ms`)), ms));
  return Promise.race([promise, timer]);
}

async function test() {
  const http = new HttpClient({ transport: new FetchTransport() });
  
  const providers = [
    ['GogoanimeProvider', new GogoanimeProvider(http)],
    ['MegaPlayProvider', new MegaPlayProvider(http)],
    ['AnikotoProvider', new AnikotoProvider(http)],
    ['AnimeParadiseProvider', new AnimeParadiseProvider(http)],
    ['AllmangaProvider', new AllmangaProvider(http)],
  ];

  for (const [name, prov] of providers) {
    console.log(`\n=== ${name} ===`);
    try {
      const results = await withTimeout(prov.search({ query: 'Naruto', kind: 'ANIME' }), 15000, `${name}.search`);
      console.log('Search type:', typeof results, Array.isArray(results) ? `array[${results.length}]` : '');
      
      const list = Array.isArray(results) ? results : results?.results || [];
      if (list.length === 0) { console.log('No results'); continue; }
      
      const first = list[0];
      console.log('First:', JSON.stringify(first, null, 2)?.substring(0, 300));
      
      // Get episodes
      const units = await withTimeout(prov.fetchContentUnits({ mediaId: first.id }), 15000, `${name}.eps`);
      const eps = Array.isArray(units) ? units : units?.units || [];
      console.log('Episodes:', eps.length);
      
      if (eps.length === 0) continue;
      console.log('Ep1:', JSON.stringify(eps[0], null, 2)?.substring(0, 200));
      
      // Resolve stream
      const stream = await withTimeout(prov.resolveStream({ unitId: eps[0].id, mediaId: first.id }), 20000, `${name}.stream`);
      console.log('STREAM:', JSON.stringify(stream, null, 2)?.substring(0, 1000));
      
    } catch (err) {
      console.error(`${name} error:`, err.message);
    }
  }
}

test().then(() => process.exit(0)).catch(e => { console.error(e); process.exit(1); });
