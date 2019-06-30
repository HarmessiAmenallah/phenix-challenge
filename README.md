## cette solution est une reponse au carrefour-challenge


Énoncé:
--------

Nos magasins produisent tous les jours des fichiers de logs contenant les informations relatives à leur activité de vente journalière. De plus, chaque magasin possède son propre référentiel de prix journalier.


Le fichier des transactions journalières contient ces infos: `txId|datetime|magasin|produit|qte`
 
Et celui du référentiel produit: `produit|prix`

où:
 - txId : id de transaction (nombre)
 - datetime : date et heure au format ISO 8601
 - magasin : UUID identifiant le magasin
 - produit : id du produit (nombre)
 - qte : quantité (nombre)
 - prix : prix du produit en euros

Notre système collecte toutes les informations des transactions de tous les magasins en un seul fichier.
Par contre les fichiers de référentiels produits sont reçu par magasin.
Les règles de nommage de ces fichiers sont les suivantes:

  - les transactions : `transactions_YYYYMMDD.data`
  - les référentiels : `reference_prod-ID_MAGASIN_YYYYMMDD.data` où ID_MAGASIN est un UUID identifiant le magasin.

Vous trouverez dans le répertoire /data/ des fichiers exemples qui vous permettront d'avoir une idée concrète de leur contenu.

Nous avons besoin de déterminer, chaque jour, les 100 produits qui ont les meilleures ventes et ceux qui génèrent le plus gros Chiffre d'Affaire par magasin et en général.

De plus, on a besoin d'avoir ces mêmes indicateurs sur les 7 derniers jours.

Les résultats sont les fichiers:
	
1. `top_100_ventes_<ID_MAGASIN>_YYYYMMDD.data` 
2. `top_100_ventes_GLOBAL_YYYYMMDD.data`
3. `top_100_ca_<ID_MAGASIN>_YYYYMMDD.data`
4. `top_100_ca_GLOBAL_YYYYMMDD.data`
5. `top_100_ventes_<ID_MAGASIN>_YYYYMMDD-J7.data` 
6. `top_100_ventes_GLOBAL_YYYYMMDD-J7.data`
7. `top_100_ca_<ID_MAGASIN>_YYYYMMDD-J7.data`
8. `top_100_ca_GLOBAL_YYYYMMDD-J7.data`



# phenix-challenge

la génération des donnees est assurée par le package  dataGen qui assure la generation des fichiers de transactions et de reference produits dont nous souhaitons calculés les indicateurs

## Run


java -jar fichier.jar -i input -o output


## Environnement de travail


  UBUNTU 18
  
  INTELLIJ
  
  SCALA 2.12.8
  
  SBT  1.2.8


## Limitation de la RAM

.jvmopts









