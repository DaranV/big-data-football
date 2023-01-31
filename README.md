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

On éxecute le .jar en lançant: <br>
`spark-submit --class project.football.FootballStats --master yarn --deploy-mode cluster --driver-memory 4g --executor-memory 2g --executor-cores 1 bigdataproject-1.0-SNAPSHOT.jar input/goalscorers.csv output`

Les résultats seront dans le dossier output sous formes de fichiers csv.

### Résultats

#### Top 10 des équipes qui marquent le plus
![top_10_team](https://user-images.githubusercontent.com/55244579/215908807-c357abf8-a39b-4cc4-99d6-2416a729e3cf.png)

#### Top 10 des meilleurs buteurs internationaux
![top_10_player](https://user-images.githubusercontent.com/55244579/215908806-da908633-8a3f-4fc9-be4d-901fd252b137.png)

#### Moyenne de buts marqués pour chaque équipe
![avg_goal_per_match](https://user-images.githubusercontent.com/55244579/215908805-0bc2f621-52dc-4bbb-815a-f2cf9ae7e65e.png)

#### Nombre de buts marqués en première/deuxième mi-temps et prolongation
![avg_goal_per_match](https://user-images.githubusercontent.com/55244579/215908804-c11d39c8-f099-4e98-8279-b764aa9d05b2.png)

#### Pourcentage de but marqué par Cristiano Ronaldo à l'extérieur et à domicile
![ronaldo_goals](https://user-images.githubusercontent.com/55244579/215908802-5839a0ec-9877-48dc-a5e7-3fe5a2f9eb1c.png)

#### Pourcentage de gagner à domicile, à l'extérieur ou bien de faire un nul
![win_loose_draw](https://user-images.githubusercontent.com/55244579/215908809-018bab92-2b22-40df-a9d1-dfb4fe526591.png)

#### Totaux de buts marqués chaque année
![goals_each_year](https://user-images.githubusercontent.com/55244579/215908808-5cba83e8-314c-4be3-87ec-5436ce76afc8.png)


### Points d'amélioration
On voulait à la base lier ce dataset avec un autre relatant des conflits qui ont eu lieu durant le 20-21ème siècle afin de voir s'il y a une corrélation entre la forme d'une équipe nationale et la situation dans le pays. Mais malgré une contrainte de temps nous n'avons pas eu le temps de faire cela.
Nous aurions également voulu utiliser Grafana ou bien Tableau pour la visualisation, même si les graphiques Excel sont suffisants pour les requêtes qu'on a fait.
