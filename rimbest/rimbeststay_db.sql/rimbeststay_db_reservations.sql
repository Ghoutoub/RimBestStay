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
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reference` varchar(255) NOT NULL,
  `client_id` bigint NOT NULL,
  `chambre_id` bigint NOT NULL,
  `date_arrivee` date NOT NULL,
  `date_depart` date NOT NULL,
  `nombre_personnes` int DEFAULT '1',
  `statut` enum('EN_ATTENTE','CONFIRMEE','ANNULEE','TERMINEE') DEFAULT 'EN_ATTENTE',
  `prix_total` decimal(38,2) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `reference` (`reference`),
  KEY `idx_reservations_client` (`client_id`),
  KEY `idx_reservations_chambre` (`chambre_id`),
  KEY `idx_reservations_dates` (`date_arrivee`,`date_depart`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`client_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`chambre_id`) REFERENCES `chambres` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reservations_chk_1` CHECK ((`date_depart` > `date_arrivee`))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES (1,'RES-1769445506476-65',16,8,'2026-02-02','2026-02-04',1,'EN_ATTENTE',1620.00,'2026-01-26 16:38:26','2026-01-26 16:38:26'),(2,'RES-1769458062723-504',18,1,'2026-02-02','2026-02-05',2,'EN_ATTENTE',1970.00,'2026-01-26 20:07:42','2026-01-26 20:07:42'),(3,'RES-1769461185291-718',19,3,'2026-02-02','2026-02-04',1,'EN_ATTENTE',1520.00,'2026-01-26 20:59:45','2026-01-26 20:59:45'),(4,'RES-1769468981130-817',19,6,'2026-02-03','2026-02-05',1,'EN_ATTENTE',380.00,'2026-01-26 23:09:41','2026-01-26 23:09:41'),(5,'RES-1769472269513-840',18,4,'2026-02-02','2026-02-05',2,'EN_ATTENTE',1220.00,'2026-01-27 00:04:29','2026-01-27 00:04:29'),(6,'RES-1769474702296-99',16,7,'2026-02-04','2026-02-06',1,'EN_ATTENTE',520.00,'2026-01-27 00:45:02','2026-01-27 00:45:02');
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-27  4:32:37
