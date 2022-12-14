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

public class RaudikkoTokenFilterConfiguration {

    /** If true, use analysis candidates returned by Raudikko, otherwise use only the first result */
    boolean analyzeAll = true;

    /** If true, all parts of a compound word are returned */
    boolean expandCompounds = false;

    /** If true, all terms are lowercased */
    boolean lowercase = true;

    /** Words shorter than this threshold are ignored */
    int minimumWordSize = 3;

    /** Words longer than this threshold are ignored */
    int maximumWordSize = 100;

    /** Analysis cache size */
    int cacheSize = 1024;
}
