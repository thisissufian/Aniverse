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
      name: 'HD-1 (VixSrc)',
      server: 'vixsrc',
      type: 'sub',
      url: isMovie
        ? `https://vixsrc.to/movie/${tmdbId}?primaryColor=8B5CF6&secondaryColor=13131a`
        : `https://vixsrc.to/tv/${tmdbId}/${season}/${episode}?primaryColor=8B5CF6&secondaryColor=13131a`,
    },
    {
      name: 'HD-2 (VsEmbed)',
      server: 'vsembed',
      type: 'sub',
      url: isMovie
        ? `https://vsembed.ru/embed/movie/${tmdbId}`
        : `https://vsembed.ru/embed/tv/${tmdbId}/${season}-${episode}`,
    },
    {
      name: 'HD-3 (VidLink)',
      server: 'vidlink',
      type: 'sub',
      url: isMovie
        ? `https://vidlink.pro/movie/${tmdbId}?primaryColor=8B5CF6&secondaryColor=13131a&iconColor=ffffff`
        : `https://vidlink.pro/tv/${tmdbId}/${season}/${episode}?primaryColor=8B5CF6&secondaryColor=13131a&iconColor=ffffff`,
    },
    {
      name: 'HD-4 (AnyEmbed)',
      server: 'anyembed',
      type: 'sub',
      url: isMovie
        ? `https://anyembed.xyz/embed/tmdb-movie-${tmdbId}`
        : `https://anyembed.xyz/embed/tmdb-tv-${tmdbId}-${season}-${episode}`,
    },
    {
      name: 'HD-5 (MultiEmbed)',
      server: 'multiembed',
      type: 'sub',
      url: isMovie
        ? `https://multiembed.mov/?video_id=${tmdbId}&tmdb=1`
        : `https://multiembed.mov/?video_id=${tmdbId}&tmdb=1&s=${season}&e=${episode}`,
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
