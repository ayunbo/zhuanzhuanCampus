-- Review feature patch
-- Apply this file for existing databases that already have table `review`

ALTER TABLE `review`
    ADD COLUMN IF NOT EXISTS `images` varchar(2048) NULL DEFAULT NULL COMMENT 'review image urls' AFTER `content`;
