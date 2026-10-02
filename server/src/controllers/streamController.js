import * as consumetService from '../services/consumetService.js';

export const getStreamingSources = async (req, res, next) => {
  try {
    const { episodeId } = req.params; // Using episodeId as dataIds
    const { server } = req.query;
    const data = await consumetService.getStreamingSources(episodeId, server);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const getAvailableServers = async (req, res, next) => {
  try {
    const { episodeId } = req.params;
    const data = await consumetService.getAvailableServers(episodeId);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};

export const getServerEmbed = async (req, res, next) => {
  try {
    const { linkId } = req.params;
    const data = await consumetService.getServerEmbed(linkId);
    res.json({ success: true, data });
  } catch (error) {
    next(error);
  }
};
