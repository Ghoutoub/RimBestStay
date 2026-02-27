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
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `actif` bit(1) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `mot_de_passe` varchar(100) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_type` varchar(20) DEFAULT 'CLIENT',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,_binary '','ghoutoubghassem47@gmail.com','$2a$10$DWV4H.AaeFjZ63IJb17fLOYtSJGpsc5Wix3beVwcEPKTyU2DZgP3C','ghoutoub',NULL,NULL,'CLIENT'),(2,_binary '','admin@rimbeststay.com','$2a$12$8VMtAD8xE9HyDb8M40LEb.LxNpJHDuSOQYtKV2F8FeHX2xBYAUa8O','admin',NULL,NULL,'ADMIN'),(3,_binary '','alioune@gmail.com','$2a$10$Dd/cDYyJM2ARDX5hxW.diOaZRGmUNqkjwzRl83ccI2YKaV0DEo0pi','alioune',NULL,NULL,'CLIENT'),(5,_binary '','taleb@gmail.com','$2a$10$mB5V2N5uhqAULBBDQHOnxODIabZl88q4Edvm3PHB8Z8i7azoJO1tG','taleb',NULL,NULL,'CLIENT'),(6,_binary '','taleb1@gmail.com','$2a$10$WwMy.va0K7JkK4NlNv/l3.vxw1F5bw/fhFEAsK8lOf7pZ1Fss06oW','taleb1',NULL,NULL,'CLIENT'),(7,_binary '\0','taleb2@gmail.com','$2a$10$ba8bm3BB3tHjgaUppOHteeAePrbZpDp5vCPGLyQvsUsN1Qdmgaw96','taleb2',NULL,'2026-01-26 02:15:34.911000','CLIENT'),(8,_binary '','hamoud@gmail.com','$2a$10$Zpmkk0z2yTQgcigulDRXlucZBWjWFoldQzvaMLAmbZsowjtLN362C','hamoud',NULL,NULL,'CLIENT'),(9,_binary '','bouha@gmail.com','$2a$10$ibEZTgE2CS7AU.n3qHbAWu0iEOr2oCnHesA1jCHf7Ib8ys1c3ySlW','bouha','2026-01-25 02:53:51.559000','2026-01-25 02:53:51.559000','CLIENT'),(10,_binary '','rim@gmail.com','$2a$10$J3//kyfb1m149O0OYIIDE.FW9kozqFbSpE5AmstR9KriEDupgLy9q','rim','2026-01-25 16:17:13.668000','2026-01-26 02:15:06.429000','CLIENT'),(11,_binary '','admin1@rimbeststay.com','$2a$10$Djgvl7GZSVOvuoHbxnfxx.dUIQ4SJE.Ku0tiVrBHJCNJP1F/eRqgy','admin1','2026-01-25 16:19:06.282000','2026-01-25 16:19:06.282000','CLIENT'),(13,_binary '','admin2@rimbeststay.com','$2a$10$v6qH2uJjD7w7ZQ1Xp8rBZ.7K9j8L5mN3B2vC1x8Z9l0P4qR5tS6uV7w','Administrateur Principal','2026-01-25 16:45:08.000000','2026-01-25 16:45:08.000000','ADMIN'),(14,_binary '','baba@gmail.com','$2a$10$ZqvsO7SWB8.wcLtKfsnI1uOY6zo53Vf1QyiDv1O9Kw8NxFw4IwIk2','ba','2026-01-25 18:33:57.343000','2026-01-25 18:33:57.343000','CLIENT'),(15,_binary '','admin3@rimbeststay.com','$2a$10$v6qH2uJjD7w7ZQ1Xp8rBZ.7K9j8L5mN3B2vC1x8Z9l0P4qR5tS6uV7w','Administrateur Principal','2026-01-25 18:38:23.000000','2026-01-25 18:38:23.000000','ADMIN'),(16,_binary '','med@gmail.com','$2a$10$EXx6UawjX6NPcGbMaeaX7uq3Y5KWf2LduR1m/8FvOs2FC4V2.PEom','med ','2026-01-25 21:24:13.399000','2026-01-25 21:24:13.399000','CLIENT'),(17,_binary '','dayson@gmail.com','$2a$10$mqXvXeZMyPNsvssnE8RhiubQZ3Joj5VKD6I7eMrFM.ciXfPUYrRMW','Dayson','2026-01-26 02:23:59.806000','2026-01-26 02:23:59.806000','CLIENT'),(18,_binary '','messi@gmail.com','$2a$10$OQIFJ2bbuV0YM/XyEXMcv..PworFn/WqwQkjVim4p9l8w4mWCEgqC','messi','2026-01-26 20:04:51.107000','2026-01-26 20:04:51.107000','CLIENT'),(19,_binary '','sidi@gmail.com','$2a$10$uhQ8ZB9FXQTGcPAo/fbVAeDKWlU3ZhZ4oXrmp85x213nDc/cV4qom','sidi','2026-01-26 20:58:16.740000','2026-01-26 20:58:16.740000','CLIENT');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-27  4:32:38
