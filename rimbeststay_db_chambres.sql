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
-- Table structure for table `chambres`
--

DROP TABLE IF EXISTS `chambres`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chambres` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hotel_id` bigint NOT NULL,
  `numero` varchar(20) NOT NULL,
  `type_chambre` varchar(50) NOT NULL,
  `capacite` int NOT NULL DEFAULT '1',
  `prix_nuit` double NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `disponible` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `depot_garantie` double DEFAULT NULL,
  `equipements` varchar(255) DEFAULT NULL,
  `prix_weekend` double DEFAULT NULL,
  `statut_nettoyage` varchar(255) DEFAULT NULL,
  `superficie` double DEFAULT NULL,
  `taxe_sejour` double DEFAULT NULL,
  `climatisation` bit(1) DEFAULT NULL,
  `coffre_fort` bit(1) DEFAULT NULL,
  `equipements_chambre` varchar(500) DEFAULT NULL,
  `images_chambre` varchar(1000) DEFAULT NULL,
  `minibar` bit(1) DEFAULT NULL,
  `nombre_lits` int DEFAULT NULL,
  `salle_bain_privee` bit(1) DEFAULT NULL,
  `television` bit(1) DEFAULT NULL,
  `type_lits` varchar(255) DEFAULT NULL,
  `vue_type` varchar(255) DEFAULT NULL,
  `wifi` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_chambre_hotel` (`hotel_id`,`numero`),
  KEY `idx_chambres_hotel` (`hotel_id`),
  CONSTRAINT `chambres_ibfk_1` FOREIGN KEY (`hotel_id`) REFERENCES `hotels` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chambres`
--

LOCK TABLES `chambres` WRITE;
/*!40000 ALTER TABLE `chambres` DISABLE KEYS */;
INSERT INTO `chambres` VALUES (1,1,'101','SUITE',2,650,'Suite Deluxe avec vue sur jardin',1,'2026-01-24 03:35:14','2026-01-24 03:35:14',1000,NULL,750,'PROPRE',45,20,_binary '',_binary '','TV,AC,MINIBAR,SAFE,TERRACE,BATHTUB','/images/suite1.jpg',_binary '',1,_binary '',_binary '','1 grand lit','JARDIN',_binary ''),(2,1,'102','DOUBLE',2,450,'Chambre double standard',1,'2026-01-24 03:35:14','2026-01-24 03:35:14',500,NULL,500,'PROPRE',30,20,_binary '',_binary '','TV,AC,SAFE','/images/double1.jpg',_binary '\0',2,_binary '',_binary '','2 lits simples','VILLE',_binary ''),(3,1,'103','SUITE',3,750,'Suite Présidentielle',1,'2026-01-24 03:35:14','2026-01-24 03:35:14',1500,NULL,850,'PROPRE',60,20,_binary '',_binary '','TV,AC,MINIBAR,SAFE,TERRACE,JACUZZI','/images/suite2.jpg',_binary '',2,_binary '',_binary '','1 grand lit + 1 lit simple','JARDIN',_binary ''),(4,2,'201','FAMILIALE',4,400,'Chambre familiale spacieuse',1,'2026-01-24 03:35:14','2026-01-24 03:35:14',800,NULL,450,'PROPRE',50,20,_binary '',_binary '','TV,AC,MINIBAR,SAFE,TERRACE','/images/family1.jpg',_binary '',3,_binary '',_binary '','1 grand lit + 2 lits simples','PISCINE',_binary ''),(5,2,'202','DOUBLE',2,320,'Chambre double vue jardin',0,'2026-01-24 03:35:14','2026-01-27 20:27:20',500,NULL,350,'PROPRE',35,20,_binary '',_binary '','TV,AC,SAFE','/images/double3.jpg',_binary '\0',1,_binary '',_binary '','1 grand lit','JARDIN',_binary ''),(6,3,'301','SIMPLE',1,180,'Chambre simple économique',0,'2026-01-24 03:35:14','2026-01-27 16:42:14',300,NULL,200,'PROPRE',20,20,_binary '',_binary '\0','TV,AC','/images/simple1.jpg',_binary '\0',1,_binary '',_binary '','1 lit simple','VILLE',_binary ''),(7,3,'302','DOUBLE',2,250,'Chambre double avec balcon',1,'2026-01-24 03:35:14','2026-01-24 03:35:14',400,NULL,280,'PROPRE',30,20,_binary '',_binary '','TV,AC,SAFE,TERRACE','/images/double2.jpg',_binary '\0',1,_binary '',_binary '','1 grand lit','VILLE',_binary ''),(8,6,'79','SIMPLE',1,800,'',0,'2026-01-26 15:54:16','2026-02-07 09:26:04',500,NULL,1000,'PROPRE',20,20,_binary '',_binary '\0',NULL,'/uploads/chambres/plaza/79/1bfadf16-b133-466e-928b-e3d97bdbcf89.jpg',_binary '\0',1,_binary '',_binary '',NULL,NULL,_binary ''),(9,7,'103','DOUBLE',2,1000.08,'',0,'2026-01-27 13:50:15','2026-01-27 18:42:47',500,NULL,1500,'PROPRE',20,20,_binary '',_binary '\0',NULL,NULL,_binary '\0',1,_binary '',_binary '',NULL,NULL,_binary ''),(10,6,'80','SIMPLE',1,1000,NULL,0,'2026-01-27 17:15:25','2026-01-27 17:18:58',500,NULL,1500,'PROPRE',15,20,_binary '',_binary '\0',NULL,NULL,_binary '\0',1,_binary '',_binary '',NULL,NULL,_binary ''),(11,8,'1','DOUBLE',2,1000,NULL,1,'2026-02-05 00:01:16','2026-02-05 00:01:16',500,NULL,1500,'PROPRE',15,20,_binary '',_binary '\0',NULL,'/uploads/chambres/grand_plaza/1/a19ef16d-05f1-4891-914e-f3a3753a39ca.jpg',_binary '\0',1,_binary '',_binary '',NULL,NULL,_binary '');
/*!40000 ALTER TABLE `chambres` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-15  5:51:55
