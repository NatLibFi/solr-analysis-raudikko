/*
 * Copyright (C) 2021  Evident Solutions Oy
 * Copyright (C) 2022  University of Helsinki (The National Library of Finland)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package fi.nationallibrary.solr.raudikko.analysis;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import fi.evident.raudikko.Morphology;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaudikkoTokenFilterFactory extends TokenFilterFactory {

    private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AnalysisCache analysisCache;

    private final RaudikkoTokenFilterConfiguration cfg = new RaudikkoTokenFilterConfiguration();

    private final Morphology morphology;

    public RaudikkoTokenFilterFactory(Map<String, String> args) {
        super(args);

        cfg.analyzeAll = getBoolean(args, "analyzeAll", cfg.analyzeAll);
        cfg.expandCompounds = getBoolean(args, "expandCompounds", cfg.expandCompounds);
        cfg.minimumWordSize = getInt(args, "minimumWordSize", cfg.minimumWordSize);
        cfg.maximumWordSize = getInt(args, "maximumWordSize", cfg.maximumWordSize);
        cfg.cacheSize = getInt(args, "analysisCacheSize", cfg.cacheSize);

        analysisCache = new AnalysisCache(cfg.cacheSize);
        log.info("initialized with cache for " + cfg.cacheSize + " entries");

        // TODO support configurable morphology
        this.morphology = MorphologyFactory.getInstance();
    }

    public TokenStream create(TokenStream input) {
        return new RaudikkoTokenFilter(input, morphology.newAnalyzer(), analysisCache, cfg);
    }
}
