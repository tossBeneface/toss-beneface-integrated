-- MySQL dump 10.13  Distrib 8.0.46, for Linux (aarch64)
--
-- Host: db-aivle-bigproject.c16qqgc0i5qn.ap-northeast-2.rds.amazonaws.com    Database: toss_beneface
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `analysis_history`
--

DROP TABLE IF EXISTS `analysis_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `analysis_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `store_id` int DEFAULT NULL,
  `analysis_script` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `created_at` datetime DEFAULT (now()),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=586 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `attachment`
--

DROP TABLE IF EXISTS `attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attachment` (
  `attachment_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `file_path` varchar(500) DEFAULT NULL,
  `file_status` enum('ACTIVATE','DEACTIVATE') NOT NULL,
  `file_type` varchar(50) NOT NULL,
  `url` varchar(500) NOT NULL,
  `qna_board_id` bigint NOT NULL,
  PRIMARY KEY (`attachment_id`),
  KEY `FKc13aiw1n2xtdp2tpgtdes3obs` (`qna_board_id`),
  CONSTRAINT `FKc13aiw1n2xtdp2tpgtdes3obs` FOREIGN KEY (`qna_board_id`) REFERENCES `qna_board` (`qna_board_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `basic_menu`
--

DROP TABLE IF EXISTS `basic_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cafe` varchar(255) NOT NULL,
  `img` varchar(255) DEFAULT NULL,
  `menu` varchar(255) NOT NULL,
  `price` double NOT NULL,
  `stock` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `card`
--

DROP TABLE IF EXISTS `card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `card` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `card_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `card_company` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `card_image` varchar(255) DEFAULT NULL,
  `card_number` varchar(255) DEFAULT NULL,
  `card_type` varchar(255) DEFAULT NULL,
  `customer_key` varchar(255) DEFAULT NULL,
  `expiry_date` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `now_per` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `card_type` (`card_name`,`card_company`),
  KEY `FKbf204t9qecurpbyoqlmpcy5t4` (`member_id`),
  CONSTRAINT `FKbf204t9qecurpbyoqlmpcy5t4` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `card_benefit`
--

DROP TABLE IF EXISTS `card_benefit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `card_benefit` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `card_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT (now()),
  `updated_at` datetime(6) DEFAULT (now()),
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `benefit` int DEFAULT NULL,
  `card_name` varchar(255) DEFAULT NULL,
  `card_company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `limit_month` int DEFAULT NULL,
  `limit_once` int DEFAULT NULL,
  `min_pay` int DEFAULT NULL,
  `min_per` int DEFAULT NULL,
  `monthly` int DEFAULT NULL,
  `shop` varchar(255) DEFAULT NULL,
  `summary` varchar(255) DEFAULT NULL,
  `corcompany` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1myd5024jg8jtv3g8xjuxd00g` (`card_id`),
  CONSTRAINT `FK1myd5024jg8jtv3g8xjuxd00g` FOREIGN KEY (`card_id`) REFERENCES `card` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=539 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `card_bin`
--

DROP TABLE IF EXISTS `card_bin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `card_bin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `card_id` bigint NOT NULL,
  `card_bin` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqff7gagocx9v11yhib4fc55eg` (`card_id`),
  CONSTRAINT `FKqff7gagocx9v11yhib4fc55eg` FOREIGN KEY (`card_id`) REFERENCES `card` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `comment_content` text NOT NULL,
  `comment_status` enum('ACTIVATE','DEACTIVATE') NOT NULL,
  `member_id` bigint NOT NULL,
  `qna_board_id` bigint NOT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `FKmrrrpi513ssu63i2783jyiv9m` (`member_id`),
  KEY `FKgqjbapc8ju878kloi8yj9g6n4` (`qna_board_id`),
  CONSTRAINT `FKgqjbapc8ju878kloi8yj9g6n4` FOREIGN KEY (`qna_board_id`) REFERENCES `qna_board` (`qna_board_id`),
  CONSTRAINT `FKmrrrpi513ssu63i2783jyiv9m` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `district_flow`
--

DROP TABLE IF EXISTS `district_flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `district_flow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `district_age_script` varchar(255) DEFAULT NULL,
  `district_day_script` varchar(255) DEFAULT NULL,
  `district_gender_script` varchar(255) DEFAULT NULL,
  `district_time_script` varchar(255) DEFAULT NULL,
  `store_age_script` varchar(255) DEFAULT NULL,
  `store_day_script` varchar(255) DEFAULT NULL,
  `store_gender_script` varchar(255) DEFAULT NULL,
  `store_time_script` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `district_statistics`
--

DROP TABLE IF EXISTS `district_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `district_statistics` (
  `quarter_year_code` int NOT NULL DEFAULT (concat(year(curdate()),quarter(curdate()))),
  `district_type_code` char(1) DEFAULT NULL,
  `district_type_name` varchar(50) DEFAULT NULL,
  `district_code` int NOT NULL DEFAULT '3130099',
  `district_name` varchar(100) DEFAULT NULL,
  `municipal_code` int DEFAULT NULL,
  `municipal_name` varchar(50) DEFAULT NULL,
  `administrative_code` int DEFAULT NULL,
  `administrative_name` varchar(50) DEFAULT NULL,
  `x_coordinate` decimal(10,2) DEFAULT NULL,
  `y_coordinate` decimal(10,2) DEFAULT NULL,
  `area_size` decimal(10,2) DEFAULT NULL,
  `service_category_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CS100010',
  `service_category_name` varchar(50) DEFAULT NULL,
  `total_stores` int DEFAULT NULL,
  `similar_stores` int DEFAULT NULL,
  `opening_rate` decimal(10,2) DEFAULT NULL,
  `opened_stores` int DEFAULT NULL,
  `closure_rate` decimal(10,2) DEFAULT NULL,
  `closed_stores` int DEFAULT NULL,
  `franchise_stores` int DEFAULT NULL,
  `monthly_sales_amount` float DEFAULT NULL,
  `monthly_sales_count` int DEFAULT NULL,
  `weekday_sales_amount` float DEFAULT NULL,
  `weekend_sales_amount` float DEFAULT NULL,
  `monday_sales_amount` float DEFAULT NULL,
  `tuesday_sales_amount` float DEFAULT NULL,
  `wednesday_sales_amount` float DEFAULT NULL,
  `thursday_sales_amount` float DEFAULT NULL,
  `friday_sales_amount` float DEFAULT NULL,
  `saturday_sales_amount` float DEFAULT NULL,
  `sunday_sales_amount` float DEFAULT NULL,
  `sales_00_06_amount` float DEFAULT NULL,
  `sales_06_11_amount` float DEFAULT NULL,
  `sales_11_14_amount` float DEFAULT NULL,
  `sales_14_17_amount` float DEFAULT NULL,
  `sales_17_21_amount` float DEFAULT NULL,
  `sales_21_24_amount` float DEFAULT NULL,
  `male_sales_amount` float DEFAULT NULL,
  `female_sales_amount` float DEFAULT NULL,
  `age_10_sales_amount` float DEFAULT NULL,
  `age_20_sales_amount` float DEFAULT NULL,
  `age_30_sales_amount` float DEFAULT NULL,
  `age_40_sales_amount` float DEFAULT NULL,
  `age_50_sales_amount` float DEFAULT NULL,
  `age_60_plus_sales_amount` float DEFAULT NULL,
  `weekday_sales_count` int DEFAULT NULL,
  `weekend_sales_count` int DEFAULT NULL,
  `monday_sales_count` int DEFAULT NULL,
  `tuesday_sales_count` int DEFAULT NULL,
  `wednesday_sales_count` int DEFAULT NULL,
  `thursday_sales_count` int DEFAULT NULL,
  `friday_sales_count` int DEFAULT NULL,
  `saturday_sales_count` int DEFAULT NULL,
  `sunday_sales_count` int DEFAULT NULL,
  `sales_00_06_count` int DEFAULT NULL,
  `sales_06_11_count` int DEFAULT NULL,
  `sales_11_14_count` int DEFAULT NULL,
  `sales_14_17_count` int DEFAULT NULL,
  `sales_17_21_count` int DEFAULT NULL,
  `sales_21_24_count` int DEFAULT NULL,
  `male_sales_count` int DEFAULT NULL,
  `female_sales_count` int DEFAULT NULL,
  `age_10_sales_count` int DEFAULT NULL,
  `age_20_sales_count` int DEFAULT NULL,
  `age_30_sales_count` int DEFAULT NULL,
  `age_40_sales_count` int DEFAULT NULL,
  `age_50_sales_count` int DEFAULT NULL,
  `age_60_plus_sales_count` int DEFAULT NULL,
  `district_change_index` char(2) DEFAULT NULL,
  `district_change_index_name` varchar(50) DEFAULT NULL,
  `avg_operating_months` int DEFAULT NULL,
  `avg_closure_months` int DEFAULT NULL,
  `seoul_avg_operating_months` int DEFAULT NULL,
  `seoul_avg_closure_months` int DEFAULT NULL,
  `total_floating_population` int DEFAULT NULL,
  `male_floating_population` int DEFAULT NULL,
  `female_floating_population` int DEFAULT NULL,
  `age_10_floating_population` int DEFAULT NULL,
  `age_20_floating_population` int DEFAULT NULL,
  `age_30_floating_population` int DEFAULT NULL,
  `age_40_floating_population` int DEFAULT NULL,
  `age_50_floating_population` int DEFAULT NULL,
  `age_60_plus_floating_population` int DEFAULT NULL,
  `floating_population_00_06` int DEFAULT NULL,
  `floating_population_06_11` int DEFAULT NULL,
  `floating_population_11_14` int DEFAULT NULL,
  `floating_population_14_17` int DEFAULT NULL,
  `floating_population_17_21` int DEFAULT NULL,
  `floating_population_21_24` int DEFAULT NULL,
  `floating_population_monday` int DEFAULT NULL,
  `floating_population_tuesday` int DEFAULT NULL,
  `floating_population_wednesday` int DEFAULT NULL,
  `floating_population_thursday` int DEFAULT NULL,
  `floating_population_friday` int DEFAULT NULL,
  `floating_population_saturday` int DEFAULT NULL,
  `floating_population_sunday` int DEFAULT NULL,
  PRIMARY KEY (`district_code`,`service_category_code`,`quarter_year_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `district_statistics_predict`
--

DROP TABLE IF EXISTS `district_statistics_predict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `district_statistics_predict` (
  `quarter_year_code` int NOT NULL DEFAULT (concat(year(curdate()),quarter(curdate()))),
  `district_type_code` char(1) DEFAULT NULL,
  `district_type_name` varchar(50) DEFAULT NULL,
  `district_code` int NOT NULL DEFAULT '3130099',
  `district_name` varchar(100) DEFAULT NULL,
  `municipal_code` int DEFAULT NULL,
  `municipal_name` varchar(50) DEFAULT NULL,
  `administrative_code` int DEFAULT NULL,
  `administrative_name` varchar(50) DEFAULT NULL,
  `x_coordinate` decimal(10,2) DEFAULT NULL,
  `y_coordinate` decimal(10,2) DEFAULT NULL,
  `area_size` decimal(10,2) DEFAULT NULL,
  `service_category_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CS100010',
  `service_category_name` varchar(50) DEFAULT NULL,
  `total_stores` int DEFAULT NULL,
  `similar_stores` int DEFAULT NULL,
  `opening_rate` decimal(10,2) DEFAULT NULL,
  `opened_stores` int DEFAULT NULL,
  `closure_rate` decimal(10,2) DEFAULT NULL,
  `closed_stores` int DEFAULT NULL,
  `franchise_stores` int DEFAULT NULL,
  `monthly_sales_amount` float DEFAULT NULL,
  `monthly_sales_count` int DEFAULT NULL,
  `weekday_sales_amount` float DEFAULT NULL,
  `weekend_sales_amount` float DEFAULT NULL,
  `monday_sales_amount` float DEFAULT NULL,
  `tuesday_sales_amount` float DEFAULT NULL,
  `wednesday_sales_amount` float DEFAULT NULL,
  `thursday_sales_amount` float DEFAULT NULL,
  `friday_sales_amount` float DEFAULT NULL,
  `saturday_sales_amount` float DEFAULT NULL,
  `sunday_sales_amount` float DEFAULT NULL,
  `sales_00_06_amount` float DEFAULT NULL,
  `sales_06_11_amount` float DEFAULT NULL,
  `sales_11_14_amount` float DEFAULT NULL,
  `sales_14_17_amount` float DEFAULT NULL,
  `sales_17_21_amount` float DEFAULT NULL,
  `sales_21_24_amount` float DEFAULT NULL,
  `male_sales_amount` float DEFAULT NULL,
  `female_sales_amount` float DEFAULT NULL,
  `age_10_sales_amount` float DEFAULT NULL,
  `age_20_sales_amount` float DEFAULT NULL,
  `age_30_sales_amount` float DEFAULT NULL,
  `age_40_sales_amount` float DEFAULT NULL,
  `age_50_sales_amount` float DEFAULT NULL,
  `age_60_plus_sales_amount` float DEFAULT NULL,
  `weekday_sales_count` int DEFAULT NULL,
  `weekend_sales_count` int DEFAULT NULL,
  `monday_sales_count` int DEFAULT NULL,
  `tuesday_sales_count` int DEFAULT NULL,
  `wednesday_sales_count` int DEFAULT NULL,
  `thursday_sales_count` int DEFAULT NULL,
  `friday_sales_count` int DEFAULT NULL,
  `saturday_sales_count` int DEFAULT NULL,
  `sunday_sales_count` int DEFAULT NULL,
  `sales_00_06_count` int DEFAULT NULL,
  `sales_06_11_count` int DEFAULT NULL,
  `sales_11_14_count` int DEFAULT NULL,
  `sales_14_17_count` int DEFAULT NULL,
  `sales_17_21_count` int DEFAULT NULL,
  `sales_21_24_count` int DEFAULT NULL,
  `male_sales_count` int DEFAULT NULL,
  `female_sales_count` int DEFAULT NULL,
  `age_10_sales_count` int DEFAULT NULL,
  `age_20_sales_count` int DEFAULT NULL,
  `age_30_sales_count` int DEFAULT NULL,
  `age_40_sales_count` int DEFAULT NULL,
  `age_50_sales_count` int DEFAULT NULL,
  `age_60_plus_sales_count` int DEFAULT NULL,
  `district_change_index` char(2) DEFAULT NULL,
  `district_change_index_name` varchar(50) DEFAULT NULL,
  `avg_operating_months` int DEFAULT NULL,
  `avg_closure_months` int DEFAULT NULL,
  `seoul_avg_operating_months` int DEFAULT NULL,
  `seoul_avg_closure_months` int DEFAULT NULL,
  `total_floating_population` int DEFAULT NULL,
  `male_floating_population` int DEFAULT NULL,
  `female_floating_population` int DEFAULT NULL,
  `age_10_floating_population` int DEFAULT NULL,
  `age_20_floating_population` int DEFAULT NULL,
  `age_30_floating_population` int DEFAULT NULL,
  `age_40_floating_population` int DEFAULT NULL,
  `age_50_floating_population` int DEFAULT NULL,
  `age_60_plus_floating_population` int DEFAULT NULL,
  `floating_population_00_06` int DEFAULT NULL,
  `floating_population_06_11` int DEFAULT NULL,
  `floating_population_11_14` int DEFAULT NULL,
  `floating_population_14_17` int DEFAULT NULL,
  `floating_population_17_21` int DEFAULT NULL,
  `floating_population_21_24` int DEFAULT NULL,
  `floating_population_monday` int DEFAULT NULL,
  `floating_population_tuesday` int DEFAULT NULL,
  `floating_population_wednesday` int DEFAULT NULL,
  `floating_population_thursday` int DEFAULT NULL,
  `floating_population_friday` int DEFAULT NULL,
  `floating_population_saturday` int DEFAULT NULL,
  `floating_population_sunday` int DEFAULT NULL,
  PRIMARY KEY (`district_code`,`service_category_code`,`quarter_year_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `face_registration`
--

DROP TABLE IF EXISTS `face_registration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `face_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(255) NOT NULL,
  `member_id` bigint NOT NULL,
  `user_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsewydsvanlwgv2xlly7ht23rq` (`member_id`),
  CONSTRAINT `FKsewydsvanlwgv2xlly7ht23rq` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `budget` varchar(200) DEFAULT NULL,
  `email` varchar(50) NOT NULL,
  `gender` enum('FEMALE','MALE') NOT NULL,
  `member_name` varchar(20) NOT NULL,
  `member_status` enum('ACTIVATE','DEACTIVATE') NOT NULL,
  `password` varchar(200) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `profile_img` varchar(200) DEFAULT NULL,
  `refresh_token` varchar(250) DEFAULT NULL,
  `role` enum('ADMIN','OWNER','USER') NOT NULL,
  `token_expiration_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `UK_mbmcqelty0fbrvxp1q58dn57t` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `count` int NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` int NOT NULL,
  `order_payment_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjup0mvvwij8in88s53862ey3o` (`order_payment_id`),
  CONSTRAINT `FKjup0mvvwij8in88s53862ey3o` FOREIGN KEY (`order_payment_id`) REFERENCES `order_payment` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_payment`
--

DROP TABLE IF EXISTS `order_payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `card_reason` varchar(255) DEFAULT NULL,
  `total_amount` int DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjre3dvig3u4ngd9dn4r9gn10o` (`member_id`),
  CONSTRAINT `FKjre3dvig3u4ngd9dn4r9gn10o` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `approved_at` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `order_id` varchar(255) DEFAULT NULL,
  `order_name` varchar(255) DEFAULT NULL,
  `payment_key` varchar(255) DEFAULT NULL,
  `receipt_url` varchar(255) DEFAULT NULL,
  `requested_at` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `total_amount` int NOT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4pswry4r5sx6j57cdeulh1hx8` (`member_id`),
  CONSTRAINT `FK4pswry4r5sx6j57cdeulh1hx8` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cafe` varchar(255) DEFAULT NULL,
  `img` varchar(255) DEFAULT NULL,
  `menu` varchar(255) DEFAULT NULL,
  `price` int NOT NULL,
  `stock` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6306 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qna_board`
--

DROP TABLE IF EXISTS `qna_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qna_board` (
  `qna_board_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `content` text NOT NULL,
  `content_status` enum('ACTIVATE','DEACTIVATE') NOT NULL,
  `title` varchar(255) NOT NULL,
  `member_id` bigint NOT NULL,
  PRIMARY KEY (`qna_board_id`),
  KEY `FK1767hj4kp94x7wlnco9h1x2xo` (`member_id`),
  CONSTRAINT `FK1767hj4kp94x7wlnco9h1x2xo` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `qr_auth`
--

DROP TABLE IF EXISTS `qr_auth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `qr_auth` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `authenticated` bit(1) NOT NULL,
  `nonce` varchar(255) NOT NULL,
  `member_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm0wuh2ehfvd9aw1hoqj1emhl` (`member_id`),
  CONSTRAINT `FKm0wuh2ehfvd9aw1hoqj1emhl` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=208 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sales_summary`
--

DROP TABLE IF EXISTS `sales_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_summary` (
  `sales_summary_id` int NOT NULL AUTO_INCREMENT,
  `store_id` int NOT NULL,
  `trade_area_id` int NOT NULL,
  `day_of_week` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `summary_date` date DEFAULT NULL,
  `total_sales` float DEFAULT NULL,
  `female_sales` float DEFAULT NULL,
  `male_sales` float DEFAULT NULL,
  `sales_10s` float DEFAULT NULL,
  `sales_20s` float DEFAULT NULL,
  `sales_30s` float DEFAULT NULL,
  `sales_40s` float DEFAULT NULL,
  `sales_50s` float DEFAULT NULL,
  `sales_over_60s` float DEFAULT NULL,
  `time_00_06` float DEFAULT NULL,
  `time_06_11` float DEFAULT NULL,
  `time_11_14` float DEFAULT NULL,
  `time_14_17` float DEFAULT NULL,
  `time_17_21` float DEFAULT NULL,
  `time_21_24` float DEFAULT NULL,
  PRIMARY KEY (`sales_summary_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=732 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `store`
--

DROP TABLE IF EXISTS `store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store` (
  `store_id` bigint NOT NULL AUTO_INCREMENT,
  `city_id` bigint DEFAULT NULL,
  `town_id` bigint DEFAULT NULL,
  `state_id` bigint DEFAULT NULL,
  `category_id` bigint NOT NULL,
  `store_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `longitude` float DEFAULT NULL,
  `latitude` float DEFAULT NULL,
  `address` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `old_address` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `contact` varchar(20) DEFAULT NULL,
  `store_status` enum('OPEN','CLOSED','UNDER_CONSTRUCTION') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'OPEN',
  `created_at` datetime NOT NULL DEFAULT (now()),
  `updated_at` datetime NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP,
  `represent_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `budget` int DEFAULT '0',
  `sales` int DEFAULT '0',
  PRIMARY KEY (`store_id`) USING BTREE,
  KEY `category_id` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `store_category`
--

DROP TABLE IF EXISTS `store_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_category` (
  `category_id` int NOT NULL,
  `category_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_data_test`
--

DROP TABLE IF EXISTS `user_data_test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_data_test` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `card_id` bigint NOT NULL,
  `card_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '카드명',
  `card_company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '법인',
  `last_per` int NOT NULL COMMENT '전월 실적',
  `now_per` int NOT NULL COMMENT '금월 실적',
  `monthly_split` int NOT NULL COMMENT '혜택 받은 횟수',
  `accrue_benefit` int NOT NULL COMMENT '혜택 받은 금액',
  `card_number` varchar(20) NOT NULL,
  `expiry_date` varchar(10) NOT NULL,
  `cvc` int NOT NULL COMMENT 'cvc번호',
  `pwd` int NOT NULL COMMENT '카드비밀번호',
  `created_at` datetime(6) DEFAULT (now()),
  `updated_at` datetime(6) DEFAULT (now()),
  `created_by` varchar(255) DEFAULT NULL,
  `modified_by` varchar(255) DEFAULT NULL,
  `date` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '오늘 날짜',
  `pay_amount` int DEFAULT NULL COMMENT '결제 금액',
  `card_limit` int NOT NULL,
  `card` varchar(255) NOT NULL,
  `corcompany` varchar(255) NOT NULL,
  `card_benefit_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `member_id` (`member_id`) USING BTREE,
  KEY `FK_card_id` (`card_id`),
  CONSTRAINT `FK8hude4m0wcrd7a5j8h3sh4a3g` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FK_card_id` FOREIGN KEY (`card_id`) REFERENCES `card` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'toss_beneface'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-04  1:04:59
