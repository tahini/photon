# Analyse phonétique

## Installation du plugin analysis-phonetic

Il faut installer le plugin ```analysis-phonetic``` dans Elasticsearch pour effectuer une analyse phonétique.
Ce plugin est maintenant installé et fonctionnel.
Cette section décrit les étapes effectuées pour l'installation ainsi que ce qui a été essayé, mais qui n'a pas fonctionné.


### 1. Ajouter les dépendances

On ajoute les dépendances nécessaires dans le fichier [pom.xml](./pom.xml).
On a besoin de ```commons-codec``` et ```lucene-analyzers-phonetic``` 
([ref](https://stackoverflow.com/a/38600668)).

```xml
<dependency><!-- dependency for analysis-phonetic plugin-->
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.15</version>
</dependency>
<dependency><!-- dependency for analysis-phonetic plugin-->
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-analyzers-phonetic</artifactId>
    <version>8.11.1</version>
</dependency>
```

### 2. Créer la classe du plugin

On récupère les classes du plugin à partir du [repository](https://github.com/elastic/elasticsearch/tree/master/plugins/analysis-phonetic/src/main/java/org/elasticsearch/plugin/analysis/phonetic) de Elasticsearch.

La classe [AnalysisPhoneticPlugin](./src/main/java/de/komoot/photon/elasticsearch/plugins/phonetic/AnalysisPhoneticPlugin.java) a été créée tel qu'indiqué dans la [référence](https://javadoc.io/static/org.elasticsearch/elasticsearch/8.2.0/org/elasticsearch/plugins/AnalysisPlugin.html) de l'interface Java ```AnalysisPlugin``` de Elasticsearch.

Quelques modifications ont été effectuées dans les fichier [PhoneticTokenFilterFactory.java](./src/main/java/de/komoot/photon/elasticsearch/plugins/phonetic/PhoneticTokenFilterFactory.java) pour corriger des erreurs à la compilation.
- La fonction ```getAsList``` de la classe [Settings](https://www.javadoc.io/doc/org.elasticsearch/elasticsearch/6.0.1/org/elasticsearch/common/settings/Settings.html) n'existe pas. La fonction ```getAsArray``` a été utilisée.
- Le mot-clé ```@Overide``` de la fonction ```getSynonymFilter``` a été enlevé parce qu'il n'existe pas une classe parent avec cette fonction.


### 3. Ajouter le plugin dans Elasticsearch

Dans la fonction ```start()``` du fichier [Server.java](./src/main/java/de/komoot/photon/elasticsearch/Server.java), le plugin ```analysis-phonetic``` a été importé à l'aide de la classe ```AnalysisPhoneticPlugin``` créée précédemment.
Tel qu'il est suggéré dans ce fil de [discussion](https://discuss.elastic.co/t/add-plugins-from-classpath-in-embedded-elasticsearch-client-node/36269/2) sur le forum de Elasticsearch, le plugin a été ajouté à la liste de plugins qui sont installés sur chaque node.



### Autres startégies


#### Maven

Pour installer le plugin, il faut que la version du plugin soit la même que la version de Elasticsearch, soit 5.6.16.
Je ne pouvais pas installer le plugin directement comme dépendance avec Maven parce que la version 5.6.16 n'est pas disponible dans [Maven Repository](https://mvnrepository.com/artifact/org.elasticsearch.plugin/analysis-phonetic).

Dans ce fil de [discussion](https://discuss.elastic.co/t/phonetic-plugin-in-maven-repository-is-outdated/70698) sur le forum de Elasticsearch, on indique que les artefacts dans Maven ne sont plus maintenus. En effet, Elasticsearch
 suggère de ne plus utiliser un serveur embedded. 


#### Télécharger le plugin

Il est possible de télécharger le fichier ```.jar``` correspondant à la bonne version du plugin à partir du [site](https://artifacts.elastic.co/downloads/elasticsearch-plugins/analysis-phonetic/analysis-phonetic-5.6.16.zip) de Elasticsearch.
Dans cette [discussion](https://discuss.elastic.co/t/how-to-install-plugins-for-embedded-elasticsearch/7216) sur le forum de Elasticsearch, on indique qu'il est possible de simplement ajouter le fichier ```.jar``` dans le classpath à l'aide Maven.

Par contre, dans cette autre [discussion](https://discuss.elastic.co/t/add-plugins-from-classpath-in-embedded-elasticsearch-client-node/36269)
on indique que cette option n'est plus disponible. On suggère de créer une classe avec le plugin
et l'intégrer dans le code de la création de chaque node.


#### Ligne de commande

Il serait peut-être possible d'installer le plugin en ligne de commande avec Maven avec une commande ```mvn install```.
Je voulais que le plugin soit installé automatiquement plutôt que de l'installer avant chaque build, donc je n'ai pas essayé cette stratégie ([ref](https://github.com/komoot/photon/pull/563)). 



## Création d'un analyseur

Il faut indiquer à Elasticsearch comment analyser un champ de texte. On peut lui indiquer, par exemple, que chaque mot est séparé par un espace. Cette opération s'effectue à l'aide d'un [analyseur](https://www.elastic.co/guide/en/elasticsearch/reference/current/analyzer-anatomy.html).  
D'abord, Elasticsearch a besoin d'un analyseur pour le texte reçu à partir d'une requête. Cet analyseur lui indique comment traiter le texte reçu pour une recherche.  
Ensuite, il faut un analyseur pour les données dans la base de données de Elasticsearch. Cet analyseur indique comment les données importées à partir de Nominatim sont indexées dans Elasticsearch ([ref](https://jessitron.com/2012/04/18/configuring-soundex-in-elasticsearch/)).  
Lors d'une recherche, Elasticsearch va comparer la requête ainsi que le contenu de la base de données et retourner les résultats qui correspondent. Il est donc important que les 2 champs utilisent le même analyseur pour pouvoir effectuer la comparaison.

Un analyseur phonétique a été créé. Cette section indique comment paramétrer et tester un analyseur dans Elasticsearch.


### Filtre phonétique

Un analyseur phonétique a besoin d'un filtre de type [phonetic](https://www.elastic.co/guide/en/elasticsearch/plugins/current/analysis-phonetic-token-filter.html). L'utilisation de ce type de filtre est possible grâce au plugin ```analysis-phonetic``` installé précédemment.

Le filtre phonétique ```soundex_filter``` a été construit comme suit :
```json
"soundex_filter": {
    "type": "phonetic",
    "encoder": "beider_morse",
    "languageset":[
        "french",
        "english"
    ]
}
```

Un filtre de type phonetic peut utiliser différents encodeurs pour l'analyse phonétique. Un encodeur prend un champ texte et le converti en un autre champ texte qui représente son encodage phonétique.
Quelques encodeurs sont décrits dans le fichier [encodeurs.md](./encodeurs.md).

Pour plus de détails sur les filtres phonétiques, cette [référence](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-custom-analyzer.html) de Elasticsearch indique comment les paramétrer. Plusieurs encodeurs sont décrits dans ce [blog](https://spinscale.de/posts/2021-06-30-implementing-phonetic-search-with-elasticsearch.html).



### Analyseur phonétique

Cette [référence](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-custom-analyzer.html) de Elasticsearch indique comment construire un analyseur.

Dans Photon, les analyseurs se trouvent dans le fichier [index_settings.json](./es/index_settings.json). 
Pour respecter la structure existante, 2 analyseurs identiques ont été créés : 
- ```index_soundex``` : indique comment analyser les données lorsqu'elles sont indexées
- ```search_soundex``` : indique comment la requête sera analysée

Les analyseurs sont construits comme suit :
```json
"search_soundex": {
    "tokenizer": "standard",
    "filter" : [
        "standard", 
        "lowercase", 
        "asciifolding", 
        "soundex_filter"
    ],
    "type": "custom"
}
```

Cet analyseur indique à Elasticsearch de séparer le champ texte en mots, de mettre chaque mot en minuscules et d'enlever les accents. Les mots sont ensuite envoyés au filtre phonétique ```soundex_filter```.

Pour plus de détails sur les analyseurs, il y a la référence pour les [tokenizers](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-tokenizers.html) et pour les [filtres](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-tokenfilters.html).


### Tester un analyseur 

On peut effectuer une requête HTTP à l'API [_analyse](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-analyze.html) de Elasticsearch en lui passant un analyseur ainsi qu'un champ texte correspondant à une requête.
Elasticsearch va retourner une liste de ```tokens``` qui représentent l'encodage de chaque terme de la requête.

```http
POST http://localhost:9200/_analyze?pretty
{
    "tokenizer": "standard",
    "filter" : [
        "standard", 
        "lowercase", 
        "asciifolding", 
        {
            "type": "phonetic",
            "encoder": "beider_morse",
            "languageset":["french","english"]
        }
    ],
  	"text" : ["this is a test", "the second text"]
}
```


## Mapper les données

Tel qu'indiqué précédemment, il faut dire à Elasticsearch quel analyseur utiliser pour indexer ou [mapper](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html) les données qui sont importées à partir de Nominatim. 

Les données contiennent plusieurs champs, par exemple le champ "name".
Il est possible d'utiliser plusieurs analyseurs sur un même champ. Pour ce faire, on doit créer plusieurs ```fields``` et indiquer l'analyseur qu'on veut utiliser pour chacun. 
Cette [référence](https://www.elastic.co/guide/en/elasticsearch/reference/current/multi-fields.html#_multi_fields_with_multiple_analyzers) décrit comment procéder.

Dans Photon, le fichier [mappings.json](./es/mappings.json) contient les instructions de mapping. 
Il y avait déjà les fields ```default``` et ```default.raw```. Pour l'analyse phonétique, un field ```phonetic``` a été ajouté chaque fois qu'il y avait un field ```raw```. Cela permet donc d'ajouter un analyseur phonétique.
```json
"collector": {
    "properties": {
        "default": {
            "type": "text",
            "analyzer": "index_ngram",
            "fields": {
                "raw": {
                    "type": "text",
                    "analyzer": "index_raw"
                },
                "phonetic": {
                    "type": "text",
                    "analyzer": "index_soundex"
                }
            }
        }
    }
}
```

On utilise donc 3 analyseurs pour indexer un champ texte :
- le champ ```default``` utilise l'analyseur ```index_ngram```
- le champ ```default.raw``` utilise l'analyseur ```index_raw```
- le champ ```default.phonetic``` utilise l'analyseur ```index_soundex```


### Consulter le fichier de mapping

On peut consulter le fichier de mapping utilisé par Photon avec la requête HTTP suivante (lorsque Photon est lancé).

```http
GET http://localhost:9200/_mapping
```

### Ré-importer les données

Si on effectue une modification sur le fichier ```mappings.json```, il faut ré-importer les données dans Elasticsearch à partir de Nominatim. Cela permettra de ré-indexer les données selon les nouvelles instructions. Pour importer les données, on peut utiliser la commande suivante :

```bash
java -jar ./target/photon*.jar -nominatim-import -host localhost -port 5432 -database nominatim -user <user> -password <password> -languages fr,en
```



## Paramétrages pour la recherche

Les paramètres de la requête sont contenus dans la fonction ```PhotonQueryBuilder()``` 
du fichier [PhotonQueryBuilder.java](./src/main/java/de/komoot/photon/elasticsearch/PhotonQueryBuilder.java).

Cette section décrit comment une recherche est construite dans Photon et les différents paramètres testés pour l'analyse phonétique.


### Options de requête

#### if (lenient)
Il y a une possibilité d'effectuer une requête à Photon en ajoutant le paramètre "[lenient](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-top-level-params)=true". Ce paramètre permet d'ignorer les erreurs de format dans une requête (ex. on s'attend à un int et on reçoit du texte).
Je n'ai pas effectué une recherche phonétique dans ce cas.

#### if (fuzziness == 0)
On peut effectuer une requête à Photon en ajoutant le paramètre "[fuzziness](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html#query-string-fuzziness)=0". Ce paramètre permet de chercher des éléments qui sont à une certaine distance d'édition du texte dans la requête. La valeur donnée à fuzziness permet d'indiquer combien d'opérations de substitution, d'insertion ou de délétions sont permises. Si la valeur est 0, cela veut dire qu'on cherche une correspondance exacte. Je n'ai donc pas effectué une recherche phonétique dans ce cas.

#### else
On regroupe ici toutes les autres requêtes, c'est à cet endroit que j'ai ajouté la recherche phonétique.



### Description de la requête

Photon utilise une requête de type 
[Boolean Query](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html) 
qui effectue une recherche en fonction d'une combinaison de sous-requêtes booléennes.
Pour plus de détails, voir la [javadoc](https://www.javadoc.io/doc/org.elasticsearch/elasticsearch/2.4.0/org/elasticsearch/index/query/BoolQueryBuilder.html). 


#### Paramètres de requête

Une requête booléenne peut être construite avec les clauses ```must``` et / ou ```should``` [ref](https://www.elastic.co/guide/en/elasticsearch/guide/current/combining-filters.html#bool-filter).
- La clause ```must``` fonctionne comme un opérateur ```ET``` et indique que le résultat doit correspondre à toutes les sous-requêtes contenues dans cette clause.
- La clause ```should``` fonctionne comme un opérateur ```OU``` et indique que le résultat doit correspondre à au moins une des sous-requêtes contenues dans cette clause.

Le paramètre [minimum_should_match](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-bool-query.html#bool-min-should-match) dans une requête booléenne indique combien de sous-requêtes dans la clause ```should``` doivent correspondre au résultat.


#### Paramètres de sous-requête

Chaque sous-requête peut avoir ses propres [paramètres](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html).

Dans une sous-requête, le paramètre ```minimum_should_match``` indique le nombre minimum de clauses qui doivent correspondre pour qu'un résultat soit retourné.

Le paramètre ```operator``` est utilisé sur les mots contenus dans une requête. Avec un opérateur ```AND``` on indique qu'on veut tous les termes contenus dans la requête.


### Score

Un [score](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-filter-context.html#relevance-scores) est calculé pour chaque élément qui correspond à une requête. Ce score détermine la pertinence d'un résultat.

Dans une requête booléenne, chaque sous-requête retourne un résultat et un score. Chaque sous-requête contribue au score total qui sera accordé à un résultat. Donc, pour un élément, on additionne le score retourné par ```must``` et par ```should``` pour obtenir le score total correspondant à cet élément.

Dans Photon, après la construction de la requête booléenne dans la fonction ```PhotonQueryBuilder```, on effectue une autre requête pour trouver une correspondance entre le champ ```collector.default.raw``` et le terme recherché. S'il y a une correspondance, ce résultat reçoit un boost.

Un boost est également accordé s'il y a une correspondance entre la requête et le champ ```housenumber```.

Pour plus d'informations sur la manière dont le score est calculé, voir cette [référence](https://www.compose.com/articles/how-scoring-works-in-elasticsearch/) ou cette [référence](https://compose.com/articles/elasticsearch-query-time-strategies-and-techniques-for-relevance-part-i/) pour des stratégies pour obtenir des résultats plus pertinents.


#### Voir les détails du calcul

On peut voir le détail du calcul du score dans Photon en ajoutant le paramètre "debug=true" à la requête HTTP.
```http
http://localhost:2322/api?q=<query>&lon=-73.61593&lat=45.548107&osm_tag=!highway&fuzziness=1&debug=true

```

On peut obtenir des informations concernant la manière dont le score a été calculé pour un résultat en particulier en utilisant l'option [_explain](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-explain.html) lorsqu'on effectue une recherche dans Elasticsearch.



### Ce que j'ai essayé

#### Paramètres de la requête

J'ai utilisé la clause ```must``` pour matcher avec le champ indexé phonétiquement ```default.phonetic```. La clause ```should``` est utilisée pour rechercher une correspondance soit avec le champ ```default.raw``` ou bien ```<language>.ngrams``` (certains champs sont indexés par langue).

J'ai modifié le paramètre ```minimum_should_match``` dans la requête booléenne.
Je l'ai modifié pour essayer de diminuer le poids des requêtes ```should```. 
Ce paramètre n'a pas pour effet de modifier le nombre de résultats retournés, mais il joue sur l'ordre dans lequel les résultats sont affichés.
- 50% : Au départ, la valeur de ce paramètre était à 50%. Avec cette valeur, les résultats phonétiques ne se trouvent pas très haut dans les résultats et parfois, on ne les trouve pas du tout.
- 49% : Cette valeur permet d'obtenir des résultats phonétiques classés assez haut dans les premiers résultats.
- 0% : toute valeur sous 49% ne semble pas avoir un effet sur l'ordre des résultats. J'ai laissé à 0% pour indiquer qu'il n'est pas nécessaire qu'il y ait une correspondance pour une clause should.


#### Paramètres de la sous-requête

Dans la sous-requête phonétique, j'ai mis une valeur de 0 au paramètre ```fuzziness```.
En effet, cette [référence](https://www.elastic.co/guide/en/elasticsearch/plugins/master/analysis-phonetic-token-filter.html) indique que ce n'est pas une bonne idée d'utiliser ce paramètre dans une recherche phonétique.

J'ai essayé de modifier le paramètre ```minimum_should_match``` dans la sous-requête phonétique, mais ça ne semble pas avoir d'effet sur les résultats. On obtient le même nombre de résultats retournés avec ou sans ce paramètre et avec différentes valeurs pour ce paramètre. Je l'ai laissé à 50% comme dans les autres sous-requêtes should.

J'ai laissé l'opérateur ```AND```, comme dans les sous-requêtes should, pour effectuer une recherche sur tous les mots de la requête.

J'ai ajouté le paramètre ```boost``` dans la sous-requête phonétique pour donner plus de poids à un résultat retourné par cette sous-requête.
- sans boost : Les résultats phonétiques ne sont pas présents ou bien ils sont très bas dans la liste des résultats.
- 1 : Certaines requêtes ne retournent aucun résultat phonétique.
- 10 : Il y a des résultats phonétiques en haut de la liste, ils sont mélangés avec des résultats non-phonétiques.
- 100, 1000, ... : Ces valeurs permettent de retourner des résultats phonétiques assez haut dans les premiers résultats, mais semblent baisser les résultats non-phonétiques très bas dans la liste.
J'ai conservé un boost de 10 parce que cela semble offrir le meilleur compromis, c'est-à-dire qu'on obtient des résultats phonétiques mélangés avec des résultats non-phonétiques.


#### Nombre de résultats affichés

Par défaut, Photon retourne 15 résultats. Il permet une valeur entre 1 et 50, mais cela peut être modifié dans le code du fichier [PhotonRequest.java](./src/main/java/de/komoot/photon/query/PhotonRequest.java).

On peut indiquer le nombre de résultats désirés dans la requêtte HTTP qu'on envoie à Photon.
```http
http://localhost:2322/api?q=<query>&lon=-73.61593&lat=45.548107&osm_tag=!highway&fuzziness=1&limit=25
```

Pour obtenir certains résulta phonétiques, j'ai essayé de modifier le nombre de résultats obtenus.
Si on change le nombre de résultats, l'ordre dans lequel les résultats sont affichés n'est pas modifié.
Pour le moment, 25 résultats semblent être suffisants pour couvrir nos cas d'utilisation.


#### Utilisation de l'emplacement sur la carte

Dans Photon, on peut effectuer une requête en utilisant les paramètres [location_bias_scale](https://github.com/komoot/photon#search-with-location-bias) et "zoom" qui permettent de prioriser les résultats qui sont proches de l'endroit sur la carte où on se situe lorsqu'on lance la requête.
Agrandir le zoom de la carte permet de donner plus d'importance au résultat recherché. 


### Tester les paramètrages pour la requête

On peut effectuer une requête HTTP à l'API [_search](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html) de Elasticsearch en lui donnant des paramètres pour la requête. On peut également obtenir un plus grand nombre de résultats avec le paramètre "size".

```http
POST http://localhost:9200/_search?pretty=true
{
  "size":1000,
  "query": {
   "bool": {
      "must": {
        "multi_match": {
          "query": "perreault",
          "fields": "collector.default.phonetic",
          "fuzziness": "0",
          "prefix_length": "0",
          "analyzer": "search_soundex",
          "operator": "and",
          "minimum_should_match": "50%",
          "boost": 1000
        }
      },
      "should": {
        "multi_match": {
          "query": "perreault",
          "fields": "collector.default",
          "fuzziness": "1",
          "prefix_length": "0",
          "analyzer": "search_ngram",
          "operator": "and",
          "minimum_should_match": "50%"
        }
      }
    }
  }
}
```

Cette requête retourne un résultat qui ressemble à ça :

```http
"hits": {
"total": 43245,
"max_score": 11295.771,
"hits": [ 
.....
```

