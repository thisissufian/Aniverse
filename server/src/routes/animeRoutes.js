import express from 'express';
import * as animeController from '../controllers/animeController.js';

const router = express.Router();

router.get('/home', animeController.getHomePageData);
router.get('/trending', animeController.getTrending);
router.get('/popular', animeController.getPopular);
router.get('/search', animeController.searchAnime);
router.get('/recent-episodes', animeController.getRecentEpisodes);
router.get('/:id', animeController.getAnimeDetail);
router.get('/:id/episodes', animeController.getEpisodes);

export default router;
