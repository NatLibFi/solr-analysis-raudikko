/*
 * Copyright (C) 2021  Evident Solutions Oy
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

import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

/**
 * Factory that creates {@link FinnishTokenizer}.
 */
public class FinnishTokenizerFactory extends TokenizerFactory {

    public FinnishTokenizerFactory(Map<String,String> args) {
        super(args);
    }

    @Override
    public final Tokenizer create(AttributeFactory factory)
    {
        return new FinnishTokenizer();
    }
}
