-- Script de migration vers la base de données de production
-- Exécutez ce script pour migrer vos données locales vers la production

-- 1. Créer la base de données (si elle n'existe pas)
-- CREATE DATABASE IF NOT EXISTS mtg_db;
-- USE mtg_db;

-- 2. Vérifier que les tables existent
SHOW TABLES;

-- 3. Vérifier la structure des tables principales
DESCRIBE card;
DESCRIBE deck;
DESCRIBE deck_creator;
DESCRIBE color;
DESCRIBE format;

-- 4. Vérifier le nombre d'enregistrements
SELECT 'Cards' as table_name, COUNT(*) as count FROM card
UNION ALL
SELECT 'Decks', COUNT(*) FROM deck
UNION ALL
SELECT 'Users', COUNT(*) FROM deck_creator
UNION ALL
SELECT 'Colors', COUNT(*) FROM color
UNION ALL
SELECT 'Formats', COUNT(*) FROM format;

-- 5. Vérifier les contraintes de clés étrangères
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_SCHEMA = DATABASE()
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 6. Vérifier les index
SHOW INDEX FROM card;
SHOW INDEX FROM deck;
SHOW INDEX FROM deck_creator;

-- 7. Test de performance (optionnel)
-- EXPLAIN SELECT * FROM card WHERE name LIKE '%dragon%' LIMIT 10;

-- 8. Vérifier les privilèges de l'utilisateur
SHOW GRANTS FOR CURRENT_USER(); 