-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: rimbeststay_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `hotels`
--

DROP TABLE IF EXISTS `hotels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hotels` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nom` varchar(100) NOT NULL,
  `ville` varchar(100) NOT NULL,
  `pays` varchar(100) NOT NULL,
  `etoiles` int DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `telephone` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `partenaire_id` bigint DEFAULT NULL,
  `actif` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `equipements_hotel` varchar(500) DEFAULT NULL,
  `images_urls` varchar(1000) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `nombre_avis` int DEFAULT NULL,
  `note_moyenne` double DEFAULT NULL,
  `prix_minimum_indication` double DEFAULT NULL,
  `quartier` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_hotels_ville` (`ville`),
  KEY `idx_hotels_pays` (`pays`),
  KEY `idx_hotels_partenaire` (`partenaire_id`),
  CONSTRAINT `hotels_ibfk_1` FOREIGN KEY (`partenaire_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `hotels_chk_1` CHECK (((`etoiles` >= 1) and (`etoiles` <= 5)))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hotels`
--

LOCK TABLES `hotels` WRITE;
/*!40000 ALTER TABLE `hotels` DISABLE KEYS */;
INSERT INTO `hotels` VALUES (1,'Hotel La Mamounia','Marrakech','Maroc',5,'Un palace historique au cœur de Marrakech','Avenue Bab Jdid, Marrakech 40040','+212 5243-88600','contact@mamounia.com',NULL,1,'2026-01-24 03:35:14','2026-01-24 03:35:14','WIFI,POOL,SPA,PARKING,RESTAURANT,BAR,GYM','/images/hotel1.jpg,/images/hotel2.jpg,/images/hotel3.jpg',31.628,-7.9882,1287,9.2,450,'Médina'),(2,'Sofitel Rabat','Rabat','Maroc',5,'Hôtel de luxe avec jardins magnifiques','BP 450 Souissi, Rabat 10000','+212 5376-75600','reservation@sofitel-rabat.com',NULL,1,'2026-01-24 03:35:14','2026-01-24 03:35:14','WIFI,POOL,SPA,PARKING,RESTAURANT,BAR,GYM,TENNIS','/images/sofitel1.jpg,/images/sofitel2.jpg',33.9692,-6.8704,894,8.8,350,'Agdal'),(3,'Hotel Farah Casablanca','Casablanca','Maroc',4,'Hôtel moderne en centre-ville','Avenue des FAR, Casablanca 20000','+212 5224-31122','info@farah-casablanca.com',NULL,1,'2026-01-24 03:35:14','2026-01-24 03:35:14','WIFI,PARKING,RESTAURANT,BAR,GYM','/images/farah1.jpg',33.5731,-7.5898,456,8.1,220,'Centre Ville'),(4,'Atlas Essaouira & Spa','Essaouira','Maroc',4,'Hôtel face à la mer avec spa','Boulevard Mohamed V, Essaouira 44000','+212 5247-84600','contact@atlas-essaouira.com',NULL,1,'2026-01-24 03:35:14','2026-01-24 03:35:14','WIFI,POOL,SPA,PARKING,RESTAURANT,BAR','/images/atlas1.jpg,/images/atlas2.jpg',31.5135,-9.7699,321,8.5,280,'Plage'),(5,'Hotel Ibis Moussafir','Marrakech','Maroc',3,'Hôtel économique bien situé','Avenue Hassan II, Marrakech 40000','+212 5244-33211','ibis.marrakech@accor.com',NULL,1,'2026-01-24 03:35:14','2026-01-24 03:35:14','WIFI,PARKING,RESTAURANT','/images/ibis1.jpg',31.6345,-8.0089,215,7.8,120,'Gueliz'),(6,'plaza','nktt','Mauritania',4,'excellent et magnifique','tevra9-zeine','+22246515873','vasq1@gmail.com',17,1,'2026-01-26 02:26:35','2026-01-26 15:54:58',NULL,NULL,NULL,NULL,0,0,NULL,NULL);
/*!40000 ALTER TABLE `hotels` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-27  4:32:35
