-- SportLink Database Schema
-- Database: sportlink
-- Server: MariaDB 10.4.32

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- Ensure the database exists and select it
CREATE DATABASE IF NOT EXISTS `sportlink` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `sportlink`;

-- --------------------------------------------------------
-- Table: admin
-- --------------------------------------------------------
CREATE TABLE `admin` (
  `id_admin` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `statut` enum('actif','inactif') DEFAULT 'actif'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Admin account. Password set to a strong default below (hashed with SHA-256).
-- Plain password: Sp0rtL!nk2026$
INSERT INTO `admin` (`id_admin`, `nom`, `prenom`, `email`, `mot_de_passe`, `telephone`, `statut`) VALUES
(1, 'SportLink', 'Admin', 'admin@sportlink.com', SHA2('Sp0rtL!nk2026$', 256), '+21612345678', 'actif');

-- --------------------------------------------------------
-- Table: user
-- --------------------------------------------------------
CREATE TABLE `user` (
  `id_user` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `date_inscription` datetime DEFAULT current_timestamp(),
  `telephone` varchar(20) DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL,
  `date_naissance` date DEFAULT NULL,
  `photo` varchar(255) DEFAULT NULL,
  `type_user` enum('Coach','Membre') NOT NULL,
  `statut` enum('actif','inactif','en attente') DEFAULT 'en attente'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample users: Coach Karim (password: Coach2026!) and Membre Marie (password: Marie2026!)
INSERT INTO `user` (`id_user`, `nom`, `prenom`, `email`, `mot_de_passe`, `date_inscription`, `telephone`, `ville`, `date_naissance`, `photo`, `type_user`, `statut`) VALUES
(1, 'Ben Ali', 'Karim', 'karim.coach@sportlink.com', SHA2('Coach2026!', 256), '2026-02-08 17:44:32', '+21698765432', 'Tunis', NULL, 'karim_photo.jpg', 'Coach', 'actif'),
(2, 'Dupont', 'Marie', 'marie.membre@sportlink.com', SHA2('Marie2026!', 256), '2026-02-08 17:44:32', NULL, 'Tunis', '1995-03-15', 'marie_photo.jpg', 'Membre', 'actif');

-- --------------------------------------------------------
-- Table: club
-- --------------------------------------------------------
CREATE TABLE `club` (
  `id_club` int(11) NOT NULL,
  `nom` varchar(150) NOT NULL,
  `adresse` text DEFAULT NULL,
  `ville` varchar(100) DEFAULT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `date_creation` date DEFAULT NULL,
  `description` text DEFAULT NULL,
  `photo_club` varchar(255) DEFAULT NULL,
  `photo_salle` varchar(255) DEFAULT NULL,
  `id_admin_responsable` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `club` (`id_club`, `nom`, `adresse`, `ville`, `telephone`, `email`, `date_creation`, `description`, `photo_club`, `photo_salle`, `id_admin_responsable`) VALUES
(1, 'Fitness Center Tunis', '123 Avenue Habib Bourguiba', 'Tunis', '+21671234567', 'contact@fitnesscenter.tn', '2024-01-01', 'Centre de fitness moderne avec équipements haut de gamme', 'club_exterior.jpg', 'gym_interior.jpg', 1);

-- --------------------------------------------------------
-- Table: coach
-- --------------------------------------------------------
CREATE TABLE `coach` (
  `id_coach` int(11) NOT NULL,
  `specialite` varchar(100) DEFAULT NULL,
  `experience_annees` int(11) DEFAULT 0,
  `certification` varchar(200) DEFAULT NULL,
  `biographie` text DEFAULT NULL,
  `photo_certification` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `coach` (`id_coach`, `specialite`, `experience_annees`, `certification`, `biographie`, `photo_certification`) VALUES
(1, 'Crossfit & Musculation', 5, 'Certification Crossfit L1', NULL, 'crossfit_cert.jpg');

-- --------------------------------------------------------
-- Table: membre
-- --------------------------------------------------------
CREATE TABLE `membre` (
  `id_membre` int(11) NOT NULL,
  `taille_cm` int(11) DEFAULT NULL,
  `poids_kg` decimal(5,2) DEFAULT NULL,
  `objectif_sportif` text DEFAULT NULL,
  `photo_progression` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `membre` (`id_membre`, `taille_cm`, `poids_kg`, `objectif_sportif`, `photo_progression`) VALUES
(2, 165, 62.50, 'Perdre 5kg et améliorer endurance', 'progression_marie.jpg');

-- --------------------------------------------------------
-- Table: abonnement
-- --------------------------------------------------------
CREATE TABLE `abonnement` (
  `id_abonnement` int(11) NOT NULL,
  `type_abonnement` enum('Mensuel','Trimestriel','Annuel','VIP') NOT NULL,
  `prix` decimal(10,2) NOT NULL,
  `duree_jours` int(11) NOT NULL,
  `description` text DEFAULT NULL,
  `id_club` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `abonnement` (`id_abonnement`, `type_abonnement`, `prix`, `duree_jours`, `description`, `id_club`) VALUES
(1, 'Mensuel', 80.00, 30, 'Accès illimité à toutes les séances', 1),
(2, 'Trimestriel', 210.00, 90, 'Abonnement 3 mois avec réduction', 1);

-- --------------------------------------------------------
-- Table: club_coach
-- --------------------------------------------------------
CREATE TABLE `club_coach` (
  `id_club` int(11) NOT NULL,
  `id_coach` int(11) NOT NULL,
  `date_debut` date NOT NULL,
  `role_coach` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `club_coach` (`id_club`, `id_coach`, `date_debut`, `role_coach`) VALUES
(1, 1, '2024-01-01', 'Coach principal Crossfit');

-- --------------------------------------------------------
-- Table: club_membre
-- --------------------------------------------------------
CREATE TABLE `club_membre` (
  `id_club` int(11) NOT NULL,
  `id_membre` int(11) NOT NULL,
  `date_adhesion` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `club_membre` (`id_club`, `id_membre`, `date_adhesion`) VALUES
(1, 2, '2024-01-15');

-- --------------------------------------------------------
-- Table: membre_abonnement
-- --------------------------------------------------------
CREATE TABLE `membre_abonnement` (
  `id_membre` int(11) NOT NULL,
  `id_abonnement` int(11) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date DEFAULT NULL,
  `etat_paiement` enum('payé','en attente','expiré') DEFAULT 'en attente'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `membre_abonnement` (`id_membre`, `id_abonnement`, `date_debut`, `date_fin`, `etat_paiement`) VALUES
(2, 1, '2024-01-15', NULL, 'payé');

-- --------------------------------------------------------
-- Table: seance
-- --------------------------------------------------------
CREATE TABLE `seance` (
  `id_seance` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `id_club` int(11) NOT NULL,
  `id_coach` int(11) NOT NULL,
  `type_seance` varchar(50) DEFAULT NULL,
  `niveau` varchar(50) DEFAULT NULL,
  `capacite_max` int(11) DEFAULT 20,
  `statut` varchar(20) DEFAULT 'planifiée',
  `photo_seance` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `seance` (`id_seance`, `titre`, `description`, `date_debut`, `date_fin`, `id_club`, `id_coach`, `type_seance`, `niveau`, `capacite_max`, `statut`, `photo_seance`) VALUES
(1, 'Crossfit Intensif', 'Séance intensive de Crossfit', '2024-03-20 18:00:00', '2024-03-20 19:30:00', 1, 1, 'Crossfit', 'Tous niveaux', 15, 'planifiée', 'crossfit_class.jpg'),
(2, 'Yoga Détente', 'Yoga doux pour la relaxation', '2024-03-21 09:00:00', '2024-03-21 10:00:00', 1, 1, 'Yoga', 'Débutant', 20, 'planifiée', 'yoga_class.jpg');

-- --------------------------------------------------------
-- Table: participation
-- --------------------------------------------------------
CREATE TABLE `participation` (
  `id_membre` int(11) NOT NULL,
  `id_seance` int(11) NOT NULL,
  `date_inscription` datetime DEFAULT current_timestamp(),
  `present` tinyint(1) DEFAULT 0,
  `calories_brulees` int(11) DEFAULT NULL,
  `satisfaction` int(11) DEFAULT NULL,
  `commentaire_coach` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `participation` (`id_membre`, `id_seance`, `date_inscription`, `present`, `calories_brulees`, `satisfaction`, `commentaire_coach`) VALUES
(2, 1, '2026-02-08 17:44:32', 1, 450, 5, 'Très bonne participation'),
(2, 2, '2026-02-08 17:44:32', 0, NULL, NULL, NULL);

-- --------------------------------------------------------
-- Table: notifications
-- --------------------------------------------------------
CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `title` varchar(120) NOT NULL,
  `message` text NOT NULL,
  `recipient_id` int(11) DEFAULT NULL COMMENT 'NULL = broadcast par rôle',
  `recipient_role` enum('ADMIN','COACH','MEMBER') NOT NULL,
  `type` enum('INFO','ALERT','PAYMENT','SESSION','SYSTEM') NOT NULL DEFAULT 'INFO',
  `read_status` tinyint(1) DEFAULT 0 COMMENT '0=non lue, 1=lue',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `created_by` int(11) DEFAULT NULL COMMENT 'id admin ou system',
  `source` enum('MANUAL','AUTO') DEFAULT 'MANUAL'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `notifications` (`id`, `title`, `message`, `recipient_id`, `recipient_role`, `type`, `read_status`, `created_at`, `created_by`, `source`) VALUES
(6, 'uezhfuezh', 'zeufhze', NULL, 'COACH', 'INFO', 0, '2026-02-13 12:51:08', NULL, 'MANUAL'),
(8, 'test', 'testting', NULL, 'ADMIN', 'INFO', 0, '2026-02-21 09:24:59', NULL, 'MANUAL'),
(9, 'test', 'brb', NULL, 'MEMBER', 'SESSION', 0, '2026-02-21 09:29:08', NULL, 'MANUAL');

-- --------------------------------------------------------
-- Indexes
-- --------------------------------------------------------
ALTER TABLE `abonnement` ADD PRIMARY KEY (`id_abonnement`), ADD KEY `id_club` (`id_club`);
ALTER TABLE `admin` ADD PRIMARY KEY (`id_admin`), ADD UNIQUE KEY `email` (`email`);
ALTER TABLE `club` ADD PRIMARY KEY (`id_club`), ADD KEY `id_admin_responsable` (`id_admin_responsable`);
ALTER TABLE `club_coach` ADD PRIMARY KEY (`id_club`,`id_coach`,`date_debut`), ADD KEY `id_coach` (`id_coach`);
ALTER TABLE `club_membre` ADD PRIMARY KEY (`id_club`,`id_membre`), ADD KEY `id_membre` (`id_membre`);
ALTER TABLE `coach` ADD PRIMARY KEY (`id_coach`);
ALTER TABLE `membre` ADD PRIMARY KEY (`id_membre`);
ALTER TABLE `membre_abonnement` ADD PRIMARY KEY (`id_membre`,`id_abonnement`,`date_debut`), ADD KEY `id_abonnement` (`id_abonnement`);
ALTER TABLE `notifications` ADD PRIMARY KEY (`id`), ADD KEY `idx_recipient` (`recipient_id`,`recipient_role`), ADD KEY `idx_created_at` (`created_at`), ADD KEY `idx_read_status` (`read_status`), ADD KEY `idx_recipient_role` (`recipient_role`);
ALTER TABLE `participation` ADD PRIMARY KEY (`id_membre`,`id_seance`), ADD KEY `id_seance` (`id_seance`);
ALTER TABLE `seance` ADD PRIMARY KEY (`id_seance`), ADD KEY `id_club` (`id_club`), ADD KEY `id_coach` (`id_coach`);
ALTER TABLE `user` ADD PRIMARY KEY (`id_user`), ADD UNIQUE KEY `email` (`email`);

-- --------------------------------------------------------
-- AUTO_INCREMENT
-- --------------------------------------------------------
ALTER TABLE `abonnement` MODIFY `id_abonnement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
ALTER TABLE `admin` MODIFY `id_admin` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
ALTER TABLE `club` MODIFY `id_club` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
ALTER TABLE `notifications` MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;
ALTER TABLE `seance` MODIFY `id_seance` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
ALTER TABLE `user` MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

-- --------------------------------------------------------
-- Foreign Keys
-- --------------------------------------------------------
ALTER TABLE `abonnement` ADD CONSTRAINT `abonnement_ibfk_1` FOREIGN KEY (`id_club`) REFERENCES `club` (`id_club`) ON DELETE CASCADE;
ALTER TABLE `club` ADD CONSTRAINT `club_ibfk_1` FOREIGN KEY (`id_admin_responsable`) REFERENCES `admin` (`id_admin`) ON DELETE SET NULL;
ALTER TABLE `club_coach` ADD CONSTRAINT `club_coach_ibfk_1` FOREIGN KEY (`id_club`) REFERENCES `club` (`id_club`) ON DELETE CASCADE, ADD CONSTRAINT `club_coach_ibfk_2` FOREIGN KEY (`id_coach`) REFERENCES `coach` (`id_coach`) ON DELETE CASCADE;
ALTER TABLE `club_membre` ADD CONSTRAINT `club_membre_ibfk_1` FOREIGN KEY (`id_club`) REFERENCES `club` (`id_club`) ON DELETE CASCADE, ADD CONSTRAINT `club_membre_ibfk_2` FOREIGN KEY (`id_membre`) REFERENCES `membre` (`id_membre`) ON DELETE CASCADE;
ALTER TABLE `coach` ADD CONSTRAINT `coach_ibfk_1` FOREIGN KEY (`id_coach`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;
ALTER TABLE `membre` ADD CONSTRAINT `membre_ibfk_1` FOREIGN KEY (`id_membre`) REFERENCES `user` (`id_user`) ON DELETE CASCADE;
ALTER TABLE `membre_abonnement` ADD CONSTRAINT `membre_abonnement_ibfk_1` FOREIGN KEY (`id_membre`) REFERENCES `membre` (`id_membre`) ON DELETE CASCADE, ADD CONSTRAINT `membre_abonnement_ibfk_2` FOREIGN KEY (`id_abonnement`) REFERENCES `abonnement` (`id_abonnement`) ON DELETE CASCADE;
ALTER TABLE `participation` ADD CONSTRAINT `participation_ibfk_1` FOREIGN KEY (`id_membre`) REFERENCES `membre` (`id_membre`) ON DELETE CASCADE, ADD CONSTRAINT `participation_ibfk_2` FOREIGN KEY (`id_seance`) REFERENCES `seance` (`id_seance`) ON DELETE CASCADE;
ALTER TABLE `seance` ADD CONSTRAINT `seance_ibfk_1` FOREIGN KEY (`id_club`) REFERENCES `club` (`id_club`) ON DELETE CASCADE, ADD CONSTRAINT `seance_ibfk_2` FOREIGN KEY (`id_coach`) REFERENCES `coach` (`id_coach`) ON DELETE CASCADE;

COMMIT;
