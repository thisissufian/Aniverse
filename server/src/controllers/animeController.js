import * as anilistService from '../services/anilistService.js';
import * as consumetService from '../services/consumetService.js';

export const getHomePageData = async (req, res, next) => {
  try {
    const data = await anilistService.getHomePageData();
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const getTrending = async (req, res, next) => {
  try {
    const { page, perPage } = req.query;
    const data = await anilistService.getTrending(page, perPage);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const getPopular = async (req, res, next) => {
  try {
    const { page, perPage } = req.query;
    const data = await anilistService.getPopular(page, perPage);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const searchAnime = async (req, res, next) => {
  try {
    const { q, search, genres, format, sort, status, season, year, page } = req.query;
    const queryStr = search || q;
    const filters = { genres, format, sort, status, season, year, page };
    const data = await anilistService.searchAnime(queryStr, filters);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const getAnimeDetail = async (req, res, next) => {
  try {
    const { id } = req.params;
    const data = await anilistService.getAnimeDetail(id);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const getEpisodes = async (req, res, next) => {
  try {
    const { id } = req.params;
    const data = await consumetService.getEpisodes(id);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const getRecentEpisodes = async (req, res, next) => {
  try {
    const { page = 1, perPage = 20, filter = 'ALL' } = req.query;
    const data = await anilistService.getRecentEpisodes(page, perPage, filter);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};
