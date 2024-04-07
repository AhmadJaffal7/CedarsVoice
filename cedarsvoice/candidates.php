<?php
define('DBHOST', 'localhost');
define('DBNAME', 'cedars_voice');
define('DBUSER', 'root');
define('DBPASS', '');
define('DBCONNSTRING', 'mysql:host='.DBHOST.';dbname='.DBNAME);

require_once('config.php');

// Create connection
try {
    $pdo = new PDO(DBCONNSTRING, DBUSER, DBPASS);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Prepare and execute the SQL query to get all candidate names
    $sql = "SELECT candidate_name FROM candidates"; // Updated table name
    $stmt = $pdo->query($sql);
    $candidates = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Send the response as a JSON object containing candidate names
    echo json_encode($candidates);
} catch (PDOException $e) {
    $error = new StdClass();
    $error->message = "Database error: " . $e->getMessage();
    echo json_encode($error);
}
?>
