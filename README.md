# Raudikko Analysis for Solr

The Raudikko Analysis plugin provides Finnish language analysis for Solr using [Raudikko](https://github.com/EvidentSolutions/raudikko).

Based on the [Raudikko Analysis for Elasticsearch](https://github.com/EvidentSolutions/elasticsearch-analysis-raudikko) plugin.

## Supported versions

| Plugin version | Raudikko version | Elasticsearch versions |
| -------------- | ---------------- | ---------------------- |
| 0.1.1          | 0.1.1            | 8.x.x                  |

The plugin *should* support all 8.x.x versions, but has not been tested with all versions.

## Building

Run `./gradlew test distZip` to run tests and create the zip file in build/distributions.

## Installing

Copy both jar files from the distribution zip to an appropriate lib directory in Solr and configure the token filter in the schema (see below).

## Configuring

Include `raudikko` filter in the analysis chain in Solr schema.xml, for example:

```xml
  <fieldType name="text" class="solr.TextField" positionIncrementGap="100" uninvertible="false">
    <analyzer type="index">
      <tokenizer class="solr.ICUTokenizerFactory"/>
      <charFilter class="solr.MappingCharFilterFactory" mapping="mapping-special_fi.txt"/>
      <filter class="solr.ICUFoldingFilterFactory" filter="[^åäöÅÄÖ]"/>
      <filter class="solr.WordDelimiterGraphFilterFactory" generateWordParts="1" generateNumberParts="1" catenateWords="1" catenateNumbers="1" catenateAll="0" splitOnCaseChange="0" protected="delim_protected.txt"/>
      <filter class="solr.FlattenGraphFilterFactory"/>
      <filter class="fi.nationallibrary.solr.raudikko.analysis.RaudikkoTokenFilterFactory" analyzeAll="true" expandCompounds="true" analysisCacheSize="10000" />
      <filter class="solr.RemoveDuplicatesTokenFilterFactory"/>
    </analyzer>
    <analyzer type="query">
      <tokenizer class="solr.ICUTokenizerFactory"/>
      <charFilter class="solr.MappingCharFilterFactory" mapping="mapping-special_fi.txt"/>
      <filter class="solr.ICUFoldingFilterFactory" filter="[^åäöÅÄÖ]"/>
      <filter class="solr.WordDelimiterGraphFilterFactory" generateWordParts="1" generateNumberParts="1" catenateWords="0" catenateNumbers="0" catenateAll="0" splitOnCaseChange="0" protected="delim_protected.txt"/>
      <filter class="solr.SynonymGraphFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="true"/>
      <filter class="fi.nationallibrary.solr.raudikko.analysis.RaudikkoTokenFilterFactory" analyzeAll="true" expandCompounds="true"/>
      <filter class="solr.RemoveDuplicatesTokenFilterFactory"/>
    </analyzer>
  </fieldType>
```

You can use the following filter options to customize the behaviour of the filter:

| Parameter         | Default value    | Description                                                 |
|-------------------|------------------|-------------------------------------------------------------|
| analyzeAll        | true             | whether to use all analysis possibilities or just the first |
| expandCompounds   | false            | whether to expand compound words                            |
| minimumWordSize   | 3                | minimum length of words to analyze                          |
| maximumWordSize   | 100              | maximum length of words to analyze                          |
| analysisCacheSize | 1024             | number of analysis results to cache                         |

## Compatibility with SolrVoikko

This plugin supersedes [SolrVoikko2](https://github.com/NatLibFi/SolrPlugins/tree/master/Voikko). However, it is not fully compatible with SolrVoikko2 since some settings have been renamed and e.g. the dictionary to use is not currently configurable.

It should be possible to upgrade with minimal changes, and in most cases the analysis results are compatible so that an existing Solr collection can be migrated without downtime. Since the dictionary is unlikely to be the same version, reindexing of all records is recommended in any case.

## License and copyright

Copyright (C) 2021-2022  Evident Solutions Oy
Copyright (C) 2022  University of Helsinki (The National Library of Finland)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
