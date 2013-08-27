CREATE DATABASE weathervane;
CREATE USER 'weathervane_user'@'localhost' IDENTIFIED BY '$56aB86?1#';
GRANT SELECT, INSERT, UPDATE, DELETE ON weathervane.* TO 'weathervane_user'@'localhost';
CREATE TABLE weathervane.version (version varchar(50));
INSERT INTO weathervane.version (version) VALUES ('1.0.0.0');
CREATE TABLE weathervane.simulation (id INT NOT NULL AUTO_INCREMENT, run ENUM('03', '09', '15', '21') NOT NULL, model ENUM('em', 'nmb', 'emm', 'nmm') NOT NULL, resolution ENUM('132') NOT NULL, perturbation ENUM('ctl', 'n1', 'n2', 'n3', 'p1', 'p2', 'p3') NOT NULL, rundate DATE, forcast_hour VARCHAR(2), priority BIT(1) DEFAULT 0, `repeat` BIT(1) NOT NULL, PRIMARY KEY (id));
