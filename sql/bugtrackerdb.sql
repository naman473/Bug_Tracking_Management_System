-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Apr 28, 2026 at 11:48 PM
-- Server version: 10.4.28-MariaDB
-- PHP Version: 8.2.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `bugtrackerdb`
--

-- --------------------------------------------------------

--
-- Table structure for table `bugs`
--

CREATE TABLE `bugs` (
  `bug_id` varchar(100) NOT NULL,
  `bug_title` varchar(100) NOT NULL,
  `bug_desc` varchar(500) DEFAULT NULL,
  `project_name` varchar(200) DEFAULT NULL,
  `created_by` varchar(100) DEFAULT NULL,
  `open_date` date DEFAULT NULL,
  `assigned_to` varchar(100) DEFAULT NULL,
  `marked_for_closing` tinyint(1) DEFAULT NULL,
  `closed_by` varchar(100) DEFAULT NULL,
  `bug_status` varchar(30) NOT NULL DEFAULT 'Open',
  `severity_level` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `bugs`
--

INSERT INTO `bugs` (`bug_id`, `bug_title`, `bug_desc`, `project_name`, `created_by`, `open_date`, `assigned_to`, `marked_for_closing`, `closed_by`, `bug_status`, `severity_level`) VALUES
('BUG-810A3BE3', 'login ', 'issue on login', 'Alphashop', 'Aditya', '2026-04-29', 'Divyanshu', 1, 'Divyanshu', 'Closed', 'major');

-- --------------------------------------------------------

--
-- Table structure for table `bug_activity`
--

CREATE TABLE `bug_activity` (
  `id` bigint(20) NOT NULL,
  `bug_id` varchar(100) NOT NULL,
  `actor_name` varchar(100) NOT NULL,
  `action_type` varchar(50) NOT NULL,
  `detail` varchar(300) DEFAULT NULL,
  `occurred_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `bug_activity`
--

INSERT INTO `bug_activity` (`id`, `bug_id`, `actor_name`, `action_type`, `detail`, `occurred_at`) VALUES
(20, 'BUG-810A3BE3', 'Aditya', 'REPORTED', 'Bug reported by Aditya', '2026-04-28 16:07:47'),
(21, 'BUG-810A3BE3', 'Pranjal', 'ASSIGNED', 'Assigned to Divyanshu', '2026-04-28 16:10:00'),
(22, 'BUG-810A3BE3', 'Pranjal', 'MARKED_FOR_CLOSE', 'Marked for closing by Pranjal', '2026-04-28 16:14:50'),
(23, 'BUG-810A3BE3', 'Divyanshu', 'CLOSED', 'Closed by Divyanshu', '2026-04-28 16:14:58');

-- --------------------------------------------------------

--
-- Table structure for table `bug_comments`
--

CREATE TABLE `bug_comments` (
  `id` bigint(20) NOT NULL,
  `bug_id` varchar(100) NOT NULL,
  `author_name` varchar(100) NOT NULL,
  `author_role` varchar(50) NOT NULL,
  `body` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `bug_comments`
--

INSERT INTO `bug_comments` (`id`, `bug_id`, `author_name`, `author_role`, `body`, `created_at`) VALUES
(2, 'BUG-810A3BE3', 'Aditya', 'tester', 'check for alert', '2026-04-28 21:38:55');

-- --------------------------------------------------------

--
-- Table structure for table `imported_users`
--

CREATE TABLE `imported_users` (
  `user_id` varchar(200) NOT NULL,
  `email_id` varchar(100) NOT NULL,
  `role` varchar(30) DEFAULT NULL,
  `user_name` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `imported_users`
--

INSERT INTO `imported_users` (`user_id`, `email_id`, `role`, `user_name`) VALUES
('dev1', 'dev1@example.com', 'Developer', 'Rudra'),
('dev2', 'dev2@example.com', 'Developer', 'Divyanshu'),
('manager1', 'manager1@example.com', 'ProjectManager', 'Pranjal'),
('manager2', 'manager2@example.com', 'ProjectManager', 'Naman'),
('tester1', 'tester1@example.com', 'Tester', 'Shashi'),
('tester2', 'tester2@example.com', 'Tester', 'Aditya');

-- --------------------------------------------------------

--
-- Table structure for table `project`
--

CREATE TABLE `project` (
  `project_id` varchar(36) NOT NULL,
  `project_name` varchar(200) NOT NULL,
  `project_desc` varchar(800) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `project_state` varchar(30) NOT NULL DEFAULT 'InProgress'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `project`
--

INSERT INTO `project` (`project_id`, `project_name`, `project_desc`, `start_date`, `project_state`) VALUES
('PRJ-1DAC97E15DC04F229DBBECFC8E98D8F8', 'Alphashop', 'Ecommerce web app', '2026-04-28', 'InProgress');

-- --------------------------------------------------------

--
-- Table structure for table `team`
--

CREATE TABLE `team` (
  `user_id` varchar(100) NOT NULL,
  `project_id` varchar(100) NOT NULL,
  `role` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `team`
--

INSERT INTO `team` (`user_id`, `project_id`, `role`) VALUES
('dev2', 'PRJ-1DAC97E15DC04F229DBBECFC8E98D8F8', 'Developer'),
('manager1', 'PRJ-1DAC97E15DC04F229DBBECFC8E98D8F8', 'ProjectManager'),
('tester2', 'PRJ-1DAC97E15DC04F229DBBECFC8E98D8F8', 'Tester');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` varchar(100) NOT NULL,
  `email_id` varchar(100) NOT NULL,
  `role` varchar(30) NOT NULL,
  `user_name` varchar(100) DEFAULT NULL,
  `password` varchar(200) NOT NULL,
  `last_login` timestamp NULL DEFAULT NULL,
  `project_counter` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `email_id`, `role`, `user_name`, `password`, `last_login`, `project_counter`) VALUES
('dev2', 'dev2@example.com', 'Developer', 'Divyanshu', '$2a$10$NAeuFEXIKtwlRKjQIYeSx.e5/B/58Ghgk5LpWLW9bdJKG2SO6X7by', '2026-04-28 16:13:00', 1),
('manager1', 'manager1@example.com', 'ProjectManager', 'Pranjal', '$2a$10$.D9ls7knPRPwEuRgf5AVlup8lU9646y8pR9LV7ZUM1LCwCjIHh1TC', '2026-04-28 16:02:03', 1),
('tester2', 'tester2@example.com', 'Tester', 'Aditya', '$2a$10$ygM9z/DYJ1m4dhWxqrQnMeZEvTd69M3aFNQ7vw3O6VRJtze0E7G7W', '2026-04-28 16:15:26', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bugs`
--
ALTER TABLE `bugs`
  ADD PRIMARY KEY (`bug_id`),
  ADD KEY `idx_bugs_status` (`bug_status`),
  ADD KEY `idx_bugs_created_by` (`created_by`),
  ADD KEY `idx_bugs_assigned_to` (`assigned_to`),
  ADD KEY `idx_bugs_project` (`project_name`);

--
-- Indexes for table `bug_activity`
--
ALTER TABLE `bug_activity`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_ba_bug_id` (`bug_id`);

--
-- Indexes for table `bug_comments`
--
ALTER TABLE `bug_comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_comment_bug` (`bug_id`);

--
-- Indexes for table `imported_users`
--
ALTER TABLE `imported_users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `uq_imported_email` (`email_id`);

--
-- Indexes for table `project`
--
ALTER TABLE `project`
  ADD PRIMARY KEY (`project_id`),
  ADD UNIQUE KEY `uq_project_name` (`project_name`);

--
-- Indexes for table `team`
--
ALTER TABLE `team`
  ADD PRIMARY KEY (`user_id`,`project_id`),
  ADD KEY `fk_team_project` (`project_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `uq_users_email` (`email_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bug_activity`
--
ALTER TABLE `bug_activity`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `bug_comments`
--
ALTER TABLE `bug_comments`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bugs`
--
ALTER TABLE `bugs`
  ADD CONSTRAINT `fk_bugs_project` FOREIGN KEY (`project_name`) REFERENCES `project` (`project_name`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `bug_activity`
--
ALTER TABLE `bug_activity`
  ADD CONSTRAINT `fk_ba_bug` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE;

--
-- Constraints for table `bug_comments`
--
ALTER TABLE `bug_comments`
  ADD CONSTRAINT `fk_comment_bug` FOREIGN KEY (`bug_id`) REFERENCES `bugs` (`bug_id`) ON DELETE CASCADE;

--
-- Constraints for table `team`
--
ALTER TABLE `team`
  ADD CONSTRAINT `fk_team_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_team_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
