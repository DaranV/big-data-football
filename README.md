## Rapport projet big-data-football

#### Technologies utilisé: Apache Hadoop, Apache Spark, Docker

#### Sujet
Nous avons analysé un dataset issue du site [Kaggle](https://www.kaggle.com/datasets/martj42/international-football-results-from-1872-to-2017 "titre de lien optionnel") au nom de goalscorers.csv (que vous pourrez trouver dans le dossier `src/main/ressources`) qui liste tous les buts marqués lors des match internationaux depuis 1872.

Nous avons réalisé plusieurs requêtes dont les résultats sont stockés dans des fichiers csv (dossier `src/main/ressources/out`):
- Top 10 des équipes qui marquent le plus
- Top 10 des meilleurs buteurs internationaux
- Moyenne de buts marqués pour chaque équipe
- Nombre de buts marqués en première/deuxième mi-temps et prolongation
- Pourcentage de but marqué par Cristiano Ronaldo à l'extérieur et à domicile
- Pourcentage de gagner à domicile, à l'extérieur ou bien de faire un nul
- Totaux de buts marqués chaque année


### Installation
Image docker : `docker pull daranesiea/bigdata-projet:version1`

`docker run -itd --net=hadoop -p 50070:50070 -p 8088:8088 -p 7077:7077 -p 16010:16010 --name hadoop-master --hostname hadoop-master daranesiea/bigdata-projet:version1` <br>
<br>
`docker exec -it hadoop-master bash`
<br>
<br>
`./start-hadoop.sh`

Vous trouverez le fichier .jar `bigdataproject-1.0-SNAPSHOT.jar`. Vous pourrez également consulter le code dans le depôt git.
Le dataset goalscorer.csv se trouvera dans le dossier `/input`


### Points d'amélioration
