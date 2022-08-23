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

import org.apache.commons.lang.LocaleUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

final class RaudikkoTokenFilter extends TokenFilter {

    private State current;
    private int currentPosition = 1;
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
            outputAlternative();
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
        currentPosition = 1;
    }

    private List<CompoundToken> analyzeUncached(String word) {
        List<Analysis> analysis = raudikkoAnalyzer.analyze(word);
        if (analysis.size() == 0) {
            return Collections.emptyList();
        }
        LinkedList<CompoundToken> words = new LinkedList<CompoundToken>();
        final Locale locale = new Locale("fi", "FI");

        for (Integer i = 0; i < analysis.size(); i++) {
            if (!cfg.analyzeAll && i > 0) {
                break;
            }
            final Analysis a = analysis.get(i);
            String baseForm = a.getBaseForm();
            if (cfg.lowercase) {
                baseForm = baseForm.toLowerCase(locale);
            }
            if (!baseForm.isEmpty()) {
                final CompoundToken token = new CompoundToken(baseForm, 1);
                if (!words.contains(token)) {
                    words.add(token);
                }
            }

            if (cfg.expandCompounds) {
                final String parts[] = a.getFstOutput().split("\\[Xp\\]");
                if (parts.length <= 2) {
                    continue;
                }
                for (Integer p = 1; p < parts.length; p++) {
                    final Integer endOffset = parts[p].indexOf("[X]");
                    if (endOffset <= 0) {
                        continue;
                    }
                    // Replace equals sign e.g. with "hyvin=vointi"
                    String part = parts[p].substring(0, endOffset).replaceAll("=", "");
                    if (cfg.lowercase) {
                        part = part.toLowerCase(locale);
                    }
                    final CompoundToken token = new CompoundToken(part, p);
                    if (!words.contains(token)) {
                        words.add(token);
                    }

                }
            }
        }
        return words;
    }

    private void outputAlternative() {
        restoreState(current);

        CompoundToken token = null;
        int prevPosition = currentPosition;
        // Find next token for this or next position
        for (; currentPosition <= prevPosition + 1; currentPosition++) {
            Iterator<CompoundToken> it = alternatives.iterator();
            while (it.hasNext()) {
                CompoundToken t = it.next();
                if (t.position == currentPosition) {
                    token = t;
                    it.remove();
                    break;
                }
            }
            if (token != null) {
                break;
            }
        }
        if (token == null) {
            alternatives.clear();
            return;
        }

        positionIncrementAttribute.setPositionIncrement(currentPosition > prevPosition ? 1 : 0);
        charTermAttribute.setEmpty().append(token.txt);
    }

    private boolean isCandidateForAnalysis(CharSequence word) {
        return word.length() >= cfg.minimumWordSize && word.length() <= cfg.maximumWordSize && VALID_WORD_PATTERN.matcher(word).matches();
    }
}
