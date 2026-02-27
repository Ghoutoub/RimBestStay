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
-- Table structure for table `offres_promotionnelles`
--

DROP TABLE IF EXISTS `offres_promotionnelles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `offres_promotionnelles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chambre_id` bigint NOT NULL,
  `nom_offre` varchar(100) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `prix_promo` decimal(38,2) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_offres_chambre` (`chambre_id`),
  CONSTRAINT `offres_promotionnelles_ibfk_1` FOREIGN KEY (`chambre_id`) REFERENCES `chambres` (`id`) ON DELETE CASCADE,
  CONSTRAINT `offres_promotionnelles_chk_1` CHECK ((`date_fin` >= `date_debut`))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `offres_promotionnelles`
--

LOCK TABLES `offres_promotionnelles` WRITE;
/*!40000 ALTER TABLE `offres_promotionnelles` DISABLE KEYS */;
INSERT INTO `offres_promotionnelles` VALUES (1,1,'Promo Été 2024','2024-06-01','2024-08-31',380.00,'Réduction spéciale pour la saison estivale','2025-12-29 19:28:24');
/*!40000 ALTER TABLE `offres_promotionnelles` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-15  5:51:56
