-- Script de test pour vérifier la connexion à la base de données
-- Exécutez ce script pour tester votre configuration

-- 1. Vérifier la connexion
SELECT 'Connexion réussie!' as status;

-- 2. Vérifier les tables existantes
SHOW TABLES;

-- 3. Vérifier les utilisateurs
SELECT User, Host FROM mysql.user WHERE User LIKE '%mtg%';

-- 4. Vérifier les privilèges
SHOW GRANTS FOR 'mtg_app'@'%';

-- 5. Test de lecture
SELECT COUNT(*) as nombre_cartes FROM card LIMIT 1;

-- 6. Test d'écriture (optionnel)
-- INSERT INTO test_table (name) VALUES ('test_connection');
-- DELETE FROM test_table WHERE name = 'test_connection'; 