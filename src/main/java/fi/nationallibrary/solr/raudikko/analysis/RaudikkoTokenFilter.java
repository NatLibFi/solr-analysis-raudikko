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

import fi.evident.raudikko.Analyzer;
import fi.evident.raudikko.Analysis;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

final class RaudikkoTokenFilter extends TokenFilter {

    private State current;
    private final Analyzer raudikkoAnalyzer;
    private final RaudikkoTokenFilterConfiguration cfg;

    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);

    private final Deque<CompoundToken> alternatives = new ArrayDeque<>();
    private final AnalysisCache analysisCache;

    private static final Pattern VALID_WORD_PATTERN = Pattern.compile("[a-zA-ZåäöÅÄÖ-]+");

    RaudikkoTokenFilter(TokenStream input,
                        Analyzer analyzer,
                        AnalysisCache analysisCache,
                        RaudikkoTokenFilterConfiguration cfg) {
        super(input);
        this.raudikkoAnalyzer = analyzer;
        this.analysisCache = analysisCache;
        this.cfg = cfg;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!alternatives.isEmpty()) {
            outputAlternative(alternatives.removeFirst());
            return true;
        }

        if (input.incrementToken()) {
            analyzeToken();
            return true;
        }

        return false;
    }

    private void analyzeToken() {
        if (!isCandidateForAnalysis(charTermAttribute))
            return;

        String word = charTermAttribute.toString();
        List<CompoundToken> result = analysisCache.get(word);
        if (null == result) {
            result = analyzeUncached(word);
            analysisCache.put(word, result);
        }
        if (result.isEmpty())
            return;

        charTermAttribute.setEmpty().append(result.get(0).txt);

        if (result.size() > 1) {
            current = captureState();

            alternatives.addAll(result.subList(1, result.size()));
        }
    }

    private List<CompoundToken> analyzeUncached(String word) {
        List<Analysis> analysis = raudikkoAnalyzer.analyze(word);
        if (analysis.size() == 0) {
            return Collections.emptyList();
        }
        LinkedList<CompoundToken> words = new LinkedList<CompoundToken>();

        for (Integer i = 0; i < analysis.size(); i++) {
            if (!cfg.analyzeAll && i > 0) {
                break;
            }
            final Analysis a = analysis.get(i);
            String baseForm = a.getBaseForm();
            if (!baseForm.isEmpty()) {
                final CompoundToken token = new CompoundToken(baseForm, 0);
                if (!words.contains(token)) {
                    words.add(token);
                }
            }

            if (cfg.expandCompounds) {
                final String fstOutput = a.getFstOutput();
                final String parts[] = fstOutput.split("\\[Xp\\]");
                if (parts.length <= 2) {
                    continue;
                }
                for (Integer p = 1; p < parts.length; p++) {
                    final Integer endOffset = parts[p].indexOf("[X]");
                    if (endOffset <= 0) {
                        continue;
                    }
                    // Replace equals sign e.g. with "hyvin=vointi"
                    words.add(
                        new CompoundToken(
                            parts[p].substring(0, endOffset).replaceAll("=", ""),
                            p - 1
                        )
                    );
                }
            }
        }
        return words;
    }

    private void outputAlternative(CompoundToken token) {
        restoreState(current);

        positionIncrementAttribute.setPositionIncrement(token.position);
        charTermAttribute.setEmpty().append(token.txt);
    }

    private boolean isCandidateForAnalysis(CharSequence word) {
        return word.length() >= cfg.minimumWordSize && word.length() <= cfg.maximumWordSize && VALID_WORD_PATTERN.matcher(word).matches();
    }
}
