# Encodeurs phonétiques

### Description

Lorsqu’on crée un filtre phonétique, il faut choisir un encodeur.

| Encodeur | Description | Exemple | 
| -------- | ---------- | -------- | 
| soundex | conserve la première lettre et les consonnes <br/> donne des valeurs numériques aux lettres | perreault : P643 <br/> perrot : P630 | 
| refined_soundex | comme soundex, donne des valeurs numériques à certaines lettres | perreault : P109076 <br/> perrot : P10906 |
| metaphone | comme soundex, mais avec plus de règles <br/> adapté à l’anglais | perreault : PRLT <br/> perrot : PRT | 
| double_metaphone | permet plus qu’une sortie pour une entrée | perreault : PRLT <br/> perrot : PRT | 
| beider_morse | retourne plusieurs sorties pour une entrée <br/> peut “deviner” la langue <br/> ici, utilisé avec l’option "languageset":"french" <br/> remplace le terme d’origine | perreault : piro, pirolt <br/> perrot : piro, pirot |


### Tester un encodeur
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
			"encoder": "soundex",
			"replace": "false"
		}
   	],
  	"text" : ["this is a test", "the second text"]
}
```

### Références

https://spinscale.de/posts/2021-06-30-implementing-phonetic-search-with-elasticsearch.html 

https://www.elastic.co/guide/en/elasticsearch/plugins/current/analysis-phonetic-token-filter.html

