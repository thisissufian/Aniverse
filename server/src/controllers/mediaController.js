import tmdbService from '../services/tmdbService.js';
import mediaStreamService from '../services/mediaStreamService.js';

export const getHome = async (req, res, next) => {
  try {
    const { mode = 'hollywood' } = req.query;
    let data;
    if (mode === 'kdrama') {
      data = await tmdbService.getKDramaHome();
    } else {
      data = await tmdbService.getHollywoodHome();
    }
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const search = async (req, res, next) => {
  try {
    const { mode = 'hollywood', query = '', type = 'all', genre, year, page = 1 } = req.query;
    const data = await tmdbService.searchMedia({
      query,
      mode,
      type,
      genre,
      year: year ? parseInt(year, 10) : null,
      page: parseInt(page, 10) || 1,
    });
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const getDetail = async (req, res, next) => {
  try {
    const { type, id } = req.params;
    if (!['movie', 'tv'].includes(type)) {
      return res.status(400).json({ success: false, message: 'Invalid media type. Must be movie or tv' });
    }
    const data = await tmdbService.getMediaDetail(type, id);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const getEpisodes = async (req, res, next) => {
  try {
    const { id } = req.params;
    const { season = 1 } = req.query;
    const episodes = await tmdbService.getTvSeasonEpisodes(id, parseInt(season, 10) || 1);
    res.json({ success: true, data: episodes });
  } catch (error) {
    next(error);
  }
};

export const getStream = async (req, res, next) => {
  try {
    const { type, id } = req.params;
    const { season = 1, episode = 1 } = req.query;
    const data = mediaStreamService.getMediaStreamingSources({
      type,
      id,
      season: parseInt(season, 10) || 1,
      episode: parseInt(episode, 10) || 1,
    });
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export default {
  getHome,
  search,
  getDetail,
  getEpisodes,
  getStream,
};
