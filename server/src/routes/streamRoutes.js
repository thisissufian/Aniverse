import express from 'express';
import * as streamController from '../controllers/streamController.js';
import corsProxy from '../middleware/corsProxy.js';

const router = express.Router();

router.get('/:episodeId/sources', streamController.getStreamingSources);
router.get('/:episodeId/servers', streamController.getAvailableServers);
router.get('/embed/:linkId', streamController.getServerEmbed);
router.get('/proxy/m3u8', corsProxy);

export default router;
