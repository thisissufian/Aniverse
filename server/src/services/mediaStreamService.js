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
        ? `https://vidlink.pro/movie/${tmdbId}?primaryColor=8B5CF6&secondaryColor=13131a&iconColor=ffffff`
        : `https://vidlink.pro/tv/${tmdbId}/${season}/${episode}?primaryColor=8B5CF6&secondaryColor=13131a&iconColor=ffffff`,
    },
    {
      name: 'HD-2 (MultiEmbed)',
      server: 'multiembed',
      type: 'sub',
      url: isMovie
        ? `https://multiembed.mov/?video_id=${tmdbId}&tmdb=1`
        : `https://multiembed.mov/?video_id=${tmdbId}&tmdb=1&s=${season}&e=${episode}`,
    },
    {
      name: 'HD-3 (VidSrc)',
      server: 'vidsrc-me',
      type: 'sub',
      url: isMovie
        ? `https://vidsrc.me/embed/movie?tmdb=${tmdbId}`
        : `https://vidsrc.me/embed/tv?tmdb=${tmdbId}&season=${season}&episode=${episode}`,
    },
    {
      name: 'HD-4 (AutoEmbed)',
      server: 'autoembed',
      type: 'sub',
      url: isMovie
        ? `https://autoembed.co/movie/tmdb/${tmdbId}`
        : `https://autoembed.co/tv/tmdb/${tmdbId}-${season}-${episode}`,
    },
    {
      name: 'HD-5 (2Embed)',
      server: '2embed',
      type: 'sub',
      url: isMovie
        ? `https://www.2embed.cc/embed/${tmdbId}`
        : `https://www.2embed.cc/embedtv/${tmdbId}&s=${season}&e=${episode}`,
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
