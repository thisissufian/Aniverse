import express from 'express';
import { getHome, search, getDetail, getEpisodes, getStream } from '../controllers/mediaController.js';

const router = express.Router();

router.get('/home', getHome);
router.get('/search', search);
router.get('/detail/:type/:id', getDetail);
router.get('/tv/:id/episodes', getEpisodes);
router.get('/stream/:type/:id', getStream);

export default router;
