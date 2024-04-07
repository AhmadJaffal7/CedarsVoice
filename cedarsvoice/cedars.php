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

    // Get the national ID from the request
    $nid = $_GET['id'];

    // Prepare and execute the SQL query
    $sql = "SELECT * FROM voter WHERE national_id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$nid]);

    // Check if the query returned a result
    $exists = $stmt->rowCount() > 0;

    // Send the response as a JSON object
    $response = new StdClass();
    $response->exists = $exists;
    echo json_encode($response);
} catch (PDOException $e) {
    $error = new StdClass();
    $error->message = "Database error: " . $e->getMessage();
    echo json_encode($error);
}

?>
