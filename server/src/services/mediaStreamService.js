/**
 * Stream Resolver for Movies, Hollywood Series, and Korean Dramas
 * Multi-server configuration:
 * - HD-1: VidLink (Fast, reliable, multi-language subtitles)
 * - HD-2: VidSrc (High-speed multi-quality embed)
 * - HD-3: SuperEmbed (Automatic multi-source fallback)
 */

export const getMediaStreamingSources = ({ type, id, season = 1, episode = 1 }) => {
  const isMovie = type === 'movie';
  const tmdbId = id;

  const servers = [
    {
      name: 'HD-1 (VidLink)',
      server: 'vidlink',
      type: 'sub',
      url: isMovie
        ? `https://vidlink.pro/movie/${tmdbId}`
        : `https://vidlink.pro/tv/${tmdbId}/${season}/${episode}`,
    },
    {
      name: 'HD-2 (VidSrc)',
      server: 'vidsrc',
      type: 'sub',
      url: isMovie
        ? `https://vidsrc.to/embed/movie/${tmdbId}`
        : `https://vidsrc.to/embed/tv/${tmdbId}/${season}/${episode}`,
    },
    {
      name: 'HD-3 (SuperEmbed)',
      server: 'superembed',
      type: 'sub',
      url: isMovie
        ? `https://multiembed.mov/?video_id=${tmdbId}&tmdb=1`
        : `https://multiembed.mov/?video_id=${tmdbId}&tmdb=1&s=${season}&e=${episode}`,
    },
    {
      name: 'VidSrc V2',
      server: 'vidsrc-cc',
      type: 'sub',
      url: isMovie
        ? `https://vidsrc.cc/v2/embed/movie/${tmdbId}`
        : `https://vidsrc.cc/v2/embed/tv/${tmdbId}/${season}/${episode}`,
    },
  ];

  return {
    isEmbed: true,
    embedUrl: servers[0].url,
    servers,
    currentServer: servers[0],
    skipData: null,
  };
};

export default {
  getMediaStreamingSources,
};
