import axios from 'axios';
import * as cheerio from 'cheerio';
import NodeCache from 'node-cache';
import { getAnimeDetail } from './anilistService.js';

const cache = new NodeCache({ stdTTL: 3600 }); // 1 hour cache
const BASE_URL = 'https://anikototv.to';
const HEADERS = {
  'X-Requested-With': 'XMLHttpRequest',
  'Referer': `${BASE_URL}/`,
  'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)'
};

const axiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: HEADERS
});

export const getEpisodes = async (anilistId) => {
  try {
    // 1. Get title from AniList
    const anime = await getAnimeDetail(anilistId);
    if (!anime) throw new Error('Anime not found on AniList');
    
    let title = anime.title.romaji || anime.title.english;
    if (!title) throw new Error('Anime title not found');
    
    // Check cache for slug
    let slug = cache.get(`anikoto_slug_${anilistId}`);
    
    if (!slug) {
      // Map AniList format to Anikoto term_type
      const formatMap = {
        'TV': 'TV',
        'TV_SHORT': 'TV',
        'MOVIE': 'Movie',
        'SPECIAL': 'Special',
        'OVA': 'OVA',
        'ONA': 'ONA'
      };
      const termType = formatMap[anime.format] || 'TV';

      // Step 1: Search anime on Anikoto
      const searchRes = await axiosInstance.get(`/filter`, {
        params: { 
          keyword: title,
          'term_type[]': termType
        }
      });
      const $ = cheerio.load(searchRes.data);
      
      const getFirstSlug = ($html) => {
        let foundSlug = null;
        $html('a[href*="/watch/"]').each((i, el) => {
          const href = $html(el).attr('href');
          const text = $html(el).text().trim();
          if (href && text.length > 3) {
            const match = href.match(/\/watch\/([^\/]+)/);
            if (match && !foundSlug) foundSlug = match[1];
          }
        });
        return foundSlug;
      };
      
      slug = getFirstSlug($);
      
      if (!slug) {
        // Fallback to english title if romaji fails
        if (title !== anime.title.english && anime.title.english) {
            const fallbackRes = await axiosInstance.get(`/filter`, {
                params: { 
                  keyword: anime.title.english,
                  'term_type[]': termType 
                }
            });
            const $fallback = cheerio.load(fallbackRes.data);
            slug = getFirstSlug($fallback);
        }
      }
      
      if (!slug) throw new Error('Anime not found on Anikoto');
      cache.set(`anikoto_slug_${anilistId}`, slug);
    }
    
    // Step 2: Get anime internal data-id
    const watchRes = await axiosInstance.get(`/watch/${slug}/ep-1`);
    const $watch = cheerio.load(watchRes.data);
    const dataId = $watch('[data-id][data-fetch]').attr('data-id');
    
    if (!dataId) throw new Error('Internal data-id not found');
    
    // Step 3: Get episode list
    const epListRes = await axiosInstance.get(`/ajax/episode/list/${dataId}`);
    if (epListRes.data.status !== 200) throw new Error('Failed to fetch episode list');
    
    const $ep = cheerio.load(epListRes.data.result);
    const episodes = [];
    
    $ep('a[data-id]').each((i, el) => {
      const $el = $ep(el);
      episodes.push({
        id: $el.attr('data-id'),
        number: parseInt($el.attr('data-num')),
        title: $el.attr('title') || `Episode ${$el.attr('data-num')}`,
        hasData: {
          sub: $el.attr('data-sub') === '1',
          dub: $el.attr('data-dub') === '1'
        },
        dataIds: $el.attr('data-ids')
      });
    });
    
    return episodes;
  } catch (error) {
    console.error('Error fetching episodes:', error);
    throw error;
  }
};

export const getAvailableServers = async (dataIds) => {
  try {
    // Step 4: Get server list
    const serverRes = await axiosInstance.get(`/ajax/server/list`, {
      params: { servers: dataIds }
    });
    
    if (serverRes.data.status !== 200) throw new Error('Failed to fetch server list');
    
    const $ = cheerio.load(serverRes.data.result);
    const servers = [];
    
    $('li[data-link-id]').each((i, el) => {
      const $el = $(el);
      const type = $el.closest('div[data-type]').attr('data-type') || 'sub';
      servers.push({
        linkId: $el.attr('data-link-id'),
        serverId: $el.attr('data-sv-id'),
        type: type,
        name: $el.text().trim()
      });
    });
    
    return servers;
  } catch (error) {
    console.error('Error fetching servers:', error);
    throw error;
  }
};

export const getServerEmbed = async (linkId) => {
  try {
    // Step 5: Get embed URL
    const embedRes = await axiosInstance.get(`/ajax/server`, {
      params: { get: linkId }
    });
    
    if (embedRes.data.status !== 200) throw new Error('Failed to fetch embed URL');
    
    return embedRes.data.result; // { url, skip_data }
  } catch (error) {
    console.error('Error fetching server embed:', error);
    throw error;
  }
};

export const getStreamingSources = async (dataIds, preferredServerId) => {
  try {
    const servers = await getAvailableServers(dataIds);
    if (!servers.length) throw new Error('No servers available');
    
    let targetServer;
    
    if (preferredServerId) {
        targetServer = servers.find(s => s.serverId === preferredServerId || s.name === preferredServerId) || servers[0];
    } else {
        targetServer = servers.find(s => s.name.toLowerCase() === 'hd-1' && s.type === 'sub') 
                    || servers.find(s => s.type === 'sub')
                    || servers[0];
    }
    
    const embedData = await getServerEmbed(targetServer.linkId);
    
    return {
      embedUrl: embedData.url,
      servers: servers,
      skipData: embedData.skip_data
    };
  } catch (error) {
    console.error('Error in getStreamingSources:', error);
    throw error;
  }
};
